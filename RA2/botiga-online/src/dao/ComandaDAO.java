package dao;

import model.Comanda;
import model.Descompte;
import model.LiniaComanda;
import util.Connexio;

import java.sql.*;
import java.util.List;

/**
 * Creació de comandes amb transacció (commit / rollback)
 * i aplicació de descomptes protegida amb un savepoint.
 *
 * Flux:
 *   BEGIN (autoCommit = false)
 *     1. Comprovar estoc            ─┐
 *     2. Decrementar estoc           │ si alguna cosa falla
 *     3. INSERT Comandes             │ → rollback() complet
 *     4. INSERT LiniesComanda        │
 *     5. Calcular i guardar total   ─┘
 *     SAVEPOINT DespresLinies
 *     6. Intentar aplicar descomptes
 *          ├─ OK    → total amb descompte
 *          └─ ERROR → rollback(savepoint): tiquet sense descompte
 *   COMMIT
 */
public class ComandaDAO {

    private final DescompteDAO descompteDAO = new DescompteDAO();

    public Comanda crearComanda(int clientId, List<LiniaComanda> linies) throws SQLException {
        if (linies == null || linies.isEmpty()) {
            throw new SQLException("La comanda ha de tenir com a mínim una línia.");
        }

        Connection conn = null;
        try {
            conn = Connexio.getConnection();
            conn.setAutoCommit(false); // Inici de la transacció

            // ---- 1. Comprovar estoc (i agafar el preu actual del producte) ----
            // FOR UPDATE bloqueja la fila fins al commit/rollback perquè cap altra
            // transacció pugui vendre el mateix estoc alhora.
            String sqlEstoc = "SELECT nom, preu, estoc FROM Productes WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(sqlEstoc)) {
                for (LiniaComanda l : linies) {
                    if (l.getQuantitat() <= 0) {
                        throw new SQLException("Quantitat no vàlida per al producte " + l.getProducteId());
                    }
                    ps.setInt(1, l.getProducteId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El producte " + l.getProducteId() + " no existeix.");
                        }
                        int estoc = rs.getInt("estoc");
                        if (estoc < l.getQuantitat()) {
                            throw new SQLException("Estoc insuficient de '" + rs.getString("nom")
                                    + "' (demanat " + l.getQuantitat() + ", disponible " + estoc + ").");
                        }
                        l.setPreuUnitari(rs.getDouble("preu"));
                        l.setNomProducte(rs.getString("nom"));
                    }
                }
            }

            // ---- 2. Decrementar estoc ----
            String sqlDecrementar = "UPDATE Productes SET estoc = estoc - ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDecrementar)) {
                for (LiniaComanda l : linies) {
                    ps.setInt(1, l.getQuantitat());
                    ps.setInt(2, l.getProducteId());
                    ps.executeUpdate();
                }
            }

            // ---- 3. Inserir la comanda ----
            int comandaId;
            String sqlComanda = "INSERT INTO Comandes (client_id, total) VALUES (?, 0)";
            try (PreparedStatement ps = conn.prepareStatement(sqlComanda, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, clientId);
                ps.executeUpdate(); // si el client no existeix, la FK llança excepció → rollback
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No s'ha pogut obtenir l'id de la comanda.");
                    }
                    comandaId = rs.getInt(1);
                }
            }

            // ---- 4. Inserir les línies (en batch) ----
            String sqlLinia = "INSERT INTO LiniesComanda (comanda_id, producte_id, quantitat, preuUnitari) "
                            + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlLinia)) {
                for (LiniaComanda l : linies) {
                    l.setComandaId(comandaId);
                    ps.setInt(1, comandaId);
                    ps.setInt(2, l.getProducteId());
                    ps.setInt(3, l.getQuantitat());
                    ps.setDouble(4, l.getPreuUnitari());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ---- 5. Calcular i guardar el total sense descompte ----
            double subtotal = 0;
            for (LiniaComanda l : linies) {
                subtotal += l.getImport();
            }
            subtotal = arrodonir(subtotal);
            actualitzarTotal(conn, comandaId, subtotal);

            // ---- SAVEPOINT: a partir d'aquí, un error només desfà els descomptes ----
            Savepoint sp = conn.setSavepoint("DespresLinies");
            double totalFinal = subtotal;
            boolean descompteAplicat = false;

            // ---- 6. Intentar aplicar descomptes ----
            try {
                double totalAmbDescompte = calcularTotalAmbDescomptes(conn, linies);
                actualitzarTotal(conn, comandaId, totalAmbDescompte);
                totalFinal = totalAmbDescompte;
                descompteAplicat = totalAmbDescompte < subtotal;
            } catch (SQLException e) {
                conn.rollback(sp); // desfà només el que s'ha fet després del savepoint
                System.out.println("⚠ No s'han pogut aplicar els descomptes: " + e.getMessage());
                System.out.println("  Es guarda el tiquet sense descompte.");
            }

            conn.commit(); // Tot correcte: es confirma la transacció

            Comanda c = new Comanda(comandaId, clientId, null, totalFinal);
            c.setSubtotal(subtotal);
            c.setLinies(linies);
            c.setDescompteAplicat(descompteAplicat);
            return c;

        } catch (SQLException e) {
            // Qualsevol error dels passos 1-5 → es desfà TOTA la transacció
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error fent rollback: " + ex.getMessage());
                }
            }
            throw e; // la App mostrarà el missatge
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) { }
            }
        }
    }

    /**
     * Recorre les línies i aplica els descomptes de cada producte.
     *  - '%' : percentatge sobre l'import de la línia (0-100)
     *  - '€' : import fix per unitat (no pot superar el preu unitari)
     * Si un descompte no és vàlid es llança una SQLException perquè
     * el mètode que crida faci rollback al savepoint.
     */
    private double calcularTotalAmbDescomptes(Connection conn, List<LiniaComanda> linies) throws SQLException {
        double total = 0;
        for (LiniaComanda l : linies) {
            double importLinia = l.getImport();
            List<Descompte> descomptes = descompteDAO.llistarPerProducte(conn, l.getProducteId());

            for (Descompte d : descomptes) {
                double rebaixa;
                switch (d.getTipus()) {
                    case "%":
                        if (d.getValor() < 0 || d.getValor() > 100) {
                            throw new SQLException("Descompte " + d.getId() + " invàlid: " + d.getValor() + "%");
                        }
                        rebaixa = l.getImport() * d.getValor() / 100.0;
                        break;
                    case "€":
                        if (d.getValor() < 0 || d.getValor() > l.getPreuUnitari()) {
                            throw new SQLException("Descompte " + d.getId() + " invàlid: " + d.getValor()
                                    + " € supera el preu de '" + l.getNomProducte() + "'");
                        }
                        rebaixa = d.getValor() * l.getQuantitat();
                        break;
                    default:
                        throw new SQLException("Tipus de descompte desconegut: " + d.getTipus());
                }
                importLinia -= rebaixa;
            }

            if (importLinia < 0) {
                throw new SQLException("Els descomptes deixen la línia de '" + l.getNomProducte() + "' en negatiu.");
            }
            total += importLinia;
        }
        return arrodonir(total);
    }

    private void actualitzarTotal(Connection conn, int comandaId, double total) throws SQLException {
        String sql = "UPDATE Comandes SET total = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ps.setInt(2, comandaId);
            ps.executeUpdate();
        }
    }

    private double arrodonir(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
