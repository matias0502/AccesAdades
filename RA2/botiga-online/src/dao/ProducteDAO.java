package dao;

import model.Producte;
import util.Connexio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD de la taula Productes. Totes les consultes amb PreparedStatement. */
public class ProducteDAO {

    // CREATE
    public int inserir(Producte p) throws SQLException {
        String sql = "INSERT INTO Productes (nom, preu, estoc) VALUES (?, ?, ?)";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPreu());
            ps.setInt(3, p.getEstoc());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            return p.getId();
        }
    }

    // READ (tots)
    public List<Producte> llistar() throws SQLException {
        String sql = "SELECT id, nom, preu, estoc FROM Productes ORDER BY id";
        List<Producte> llista = new ArrayList<>();
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                llista.add(mapejar(rs));
            }
        }
        return llista;
    }

    // READ (un)
    public Producte cercarPerId(int id) throws SQLException {
        String sql = "SELECT id, nom, preu, estoc FROM Productes WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapejar(rs);
                }
            }
        }
        return null;
    }

    // UPDATE
    public boolean actualitzar(Producte p) throws SQLException {
        String sql = "UPDATE Productes SET nom = ?, preu = ?, estoc = ? WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPreu());
            ps.setInt(3, p.getEstoc());
            ps.setInt(4, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Productes WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Producte mapejar(ResultSet rs) throws SQLException {
        return new Producte(rs.getInt("id"), rs.getString("nom"), rs.getDouble("preu"), rs.getInt("estoc"));
    }
}
