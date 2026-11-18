package dao;

import model.Descompte;
import util.Connexio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD de la taula Descomptes (cada descompte està associat a un producte). */
public class DescompteDAO {

    // CREATE
    public int inserir(Descompte d) throws SQLException {
        String sql = "INSERT INTO Descomptes (producte_id, tipus, valor) VALUES (?, ?, ?)";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getProducteId());
            ps.setString(2, d.getTipus());
            ps.setDouble(3, d.getValor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setId(rs.getInt(1));
                }
            }
            return d.getId();
        }
    }

    // READ (tots)
    public List<Descompte> llistar() throws SQLException {
        String sql = "SELECT id, producte_id, tipus, valor FROM Descomptes ORDER BY id";
        List<Descompte> llista = new ArrayList<>();
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                llista.add(mapejar(rs));
            }
        }
        return llista;
    }

    /**
     * Descomptes d'un producte fent servir una connexió JA OBERTA.
     * És important per a ComandaDAO: la consulta ha d'anar dins de la
     * mateixa transacció que la comanda.
     */
    public List<Descompte> llistarPerProducte(Connection conn, int producteId) throws SQLException {
        String sql = "SELECT id, producte_id, tipus, valor FROM Descomptes WHERE producte_id = ?";
        List<Descompte> llista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, producteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    llista.add(mapejar(rs));
                }
            }
        }
        return llista;
    }

    // UPDATE
    public boolean actualitzar(Descompte d) throws SQLException {
        String sql = "UPDATE Descomptes SET producte_id = ?, tipus = ?, valor = ? WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getProducteId());
            ps.setString(2, d.getTipus());
            ps.setDouble(3, d.getValor());
            ps.setInt(4, d.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Descomptes WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Descompte mapejar(ResultSet rs) throws SQLException {
        return new Descompte(rs.getInt("id"), rs.getInt("producte_id"),
                rs.getString("tipus"), rs.getDouble("valor"));
    }
}
