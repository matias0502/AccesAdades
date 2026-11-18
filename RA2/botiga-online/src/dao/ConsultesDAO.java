package dao;

import model.Comanda;
import model.LiniaComanda;
import util.Connexio;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultes amb JOIN per a informes i llistats. */
public class ConsultesDAO {

    /**
     * Totes les comandes d'un client amb les seves línies i el nom de cada producte.
     * JOIN Comandes ⨝ LiniesComanda ⨝ Productes ⨝ Clients.
     * Com que el JOIN retorna una fila per línia, s'agrupen per comanda amb un Map.
     */
    public List<Comanda> comandesDeClient(int clientId) throws SQLException {
        String sql =
            "SELECT c.id AS comanda_id, c.data, c.total, cl.nom AS client, " +
            "       l.id AS linia_id, l.producte_id, p.nom AS producte, l.quantitat, l.preuUnitari " +
            "FROM Comandes c " +
            "JOIN Clients cl       ON cl.id = c.client_id " +
            "JOIN LiniesComanda l  ON l.comanda_id = c.id " +
            "JOIN Productes p      ON p.id = l.producte_id " +
            "WHERE c.client_id = ? " +
            "ORDER BY c.id, l.id";

        Map<Integer, Comanda> comandes = new LinkedHashMap<>(); // manté l'ordre
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("comanda_id");
                    Comanda c = comandes.get(id);
                    if (c == null) {
                        c = new Comanda(id, clientId, rs.getTimestamp("data"), rs.getDouble("total"));
                        c.setNomClient(rs.getString("client"));
                        comandes.put(id, c);
                    }
                    LiniaComanda l = new LiniaComanda();
                    l.setId(rs.getInt("linia_id"));
                    l.setComandaId(id);
                    l.setProducteId(rs.getInt("producte_id"));
                    l.setNomProducte(rs.getString("producte"));
                    l.setQuantitat(rs.getInt("quantitat"));
                    l.setPreuUnitari(rs.getDouble("preuUnitari"));
                    c.getLinies().add(l);
                    c.setSubtotal(c.getSubtotal() + l.getImport());
                }
            }
        }
        return new ArrayList<>(comandes.values());
    }

    /**
     * Resum de totes les comandes: subtotal (suma de línies), descompte i total final.
     * JOIN + GROUP BY + SUM.
     */
    public List<Comanda> totalsComandes() throws SQLException {
        String sql =
            "SELECT c.id, c.client_id, cl.nom AS client, c.data, c.total, " +
            "       SUM(l.quantitat * l.preuUnitari) AS subtotal " +
            "FROM Comandes c " +
            "JOIN Clients cl      ON cl.id = c.client_id " +
            "JOIN LiniesComanda l ON l.comanda_id = c.id " +
            "GROUP BY c.id, c.client_id, cl.nom, c.data, c.total " +
            "ORDER BY c.id";

        List<Comanda> llista = new ArrayList<>();
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comanda c = new Comanda(rs.getInt("id"), rs.getInt("client_id"),
                        rs.getTimestamp("data"), rs.getDouble("total"));
                c.setNomClient(rs.getString("client"));
                c.setSubtotal(rs.getDouble("subtotal"));
                llista.add(c);
            }
        }
        return llista;
    }
}
