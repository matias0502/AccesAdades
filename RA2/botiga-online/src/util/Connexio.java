package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe d'utilitat que centralitza la connexió a MySQL.
 * Canvia USER i PASSWORD per les credencials del teu servidor.
 */
public class Connexio {

    private static final String URL =
            "jdbc:mysql://localhost:3306/botiga?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private Connexio() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
