package dao;

import model.Client;
import util.Connexio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD de la taula Clients. Totes les consultes amb PreparedStatement. */
public class ClientDAO {

    // CREATE
    public int inserir(Client c) throws SQLException {
        String sql = "INSERT INTO Clients (nom, correu) VALUES (?, ?)";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getCorreu());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
            return c.getId();
        }
    }

    // READ (tots)
    public List<Client> llistar() throws SQLException {
        String sql = "SELECT id, nom, correu FROM Clients ORDER BY id";
        List<Client> llista = new ArrayList<>();
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                llista.add(new Client(rs.getInt("id"), rs.getString("nom"), rs.getString("correu")));
            }
        }
        return llista;
    }

    // READ (un)
    public Client cercarPerId(int id) throws SQLException {
        String sql = "SELECT id, nom, correu FROM Clients WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(rs.getInt("id"), rs.getString("nom"), rs.getString("correu"));
                }
            }
        }
        return null;
    }

    // UPDATE
    public boolean actualitzar(Client c) throws SQLException {
        String sql = "UPDATE Clients SET nom = ?, correu = ? WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getCorreu());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Clients WHERE id = ?";
        try (Connection conn = Connexio.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
