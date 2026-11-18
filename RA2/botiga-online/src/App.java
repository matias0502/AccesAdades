import dao.*;
import model.*;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menú principal per provar totes les funcionalitats de la botiga. */
public class App {

    private static final Scanner sc = new Scanner(System.in);
    private static final ProducteDAO producteDAO = new ProducteDAO();
    private static final ClientDAO clientDAO = new ClientDAO();
    private static final DescompteDAO descompteDAO = new DescompteDAO();
    private static final ComandaDAO comandaDAO = new ComandaDAO();
    private static final ConsultesDAO consultesDAO = new ConsultesDAO();

    public static void main(String[] args) {
        int opcio;
        do {
            System.out.println("\n===== BOTIGA ONLINE =====");
            System.out.println("1. Gestionar Productes");
            System.out.println("2. Gestionar Clients");
            System.out.println("3. Gestionar Descomptes");
            System.out.println("4. Crear Comanda");
            System.out.println("5. Llistar comandes d'un client (JOIN)");
            System.out.println("6. Totals de totes les comandes (JOIN)");
            System.out.println("0. Sortir");
            opcio = llegirInt("Opció: ");

            try {
                switch (opcio) {
                    case 1 -> menuProductes();
                    case 2 -> menuClients();
                    case 3 -> menuDescomptes();
                    case 4 -> crearComanda();
                    case 5 -> comandesClient();
                    case 6 -> totalsComandes();
                    case 0 -> System.out.println("Fins aviat!");
                    default -> System.out.println("Opció no vàlida.");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                // Clau duplicada (correu UNIQUE) o clau forana (registre en ús / inexistent)
                System.out.println("✖ Restricció de la BD: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("✖ Error: " + e.getMessage());
            }
        } while (opcio != 0);
        sc.close();
    }

    // ===================== PRODUCTES =====================
    private static void menuProductes() throws SQLException {
        System.out.println("\n--- PRODUCTES ---");
        System.out.println("1. Llistar  2. Afegir  3. Modificar  4. Eliminar  0. Tornar");
        switch (llegirInt("Opció: ")) {
            case 1 -> producteDAO.llistar().forEach(System.out::println);
            case 2 -> {
                String nom = llegirText("Nom: ");
                double preu = llegirDouble("Preu: ");
                int estoc = llegirInt("Estoc: ");
                int id = producteDAO.inserir(new Producte(nom, preu, estoc));
                System.out.println("✔ Producte creat amb id " + id);
            }
            case 3 -> {
                int id = llegirInt("Id del producte: ");
                Producte p = producteDAO.cercarPerId(id);
                if (p == null) { System.out.println("No existeix."); return; }
                System.out.println("Actual: " + p + "  (Enter per mantenir el valor)");
                String nom = llegirText("Nou nom: ");
                if (!nom.isBlank()) p.setNom(nom);
                String preu = llegirText("Nou preu: ");
                if (!preu.isBlank()) p.setPreu(Double.parseDouble(preu.replace(',', '.')));
                String estoc = llegirText("Nou estoc: ");
                if (!estoc.isBlank()) p.setEstoc(Integer.parseInt(estoc));
                System.out.println(producteDAO.actualitzar(p) ? "✔ Actualitzat" : "No s'ha actualitzat");
            }
            case 4 -> {
                int id = llegirInt("Id del producte: ");
                System.out.println(producteDAO.eliminar(id) ? "✔ Eliminat" : "No existeix.");
            }
            default -> { }
        }
    }

    // ===================== CLIENTS =====================
    private static void menuClients() throws SQLException {
        System.out.println("\n--- CLIENTS ---");
        System.out.println("1. Llistar  2. Afegir  3. Modificar  4. Eliminar  0. Tornar");
        switch (llegirInt("Opció: ")) {
            case 1 -> clientDAO.llistar().forEach(System.out::println);
            case 2 -> {
                String nom = llegirText("Nom: ");
                String correu = llegirText("Correu: ");
                int id = clientDAO.inserir(new Client(nom, correu));
                System.out.println("✔ Client creat amb id " + id);
            }
            case 3 -> {
                int id = llegirInt("Id del client: ");
                Client c = clientDAO.cercarPerId(id);
                if (c == null) { System.out.println("No existeix."); return; }
                System.out.println("Actual: " + c + "  (Enter per mantenir el valor)");
                String nom = llegirText("Nou nom: ");
                if (!nom.isBlank()) c.setNom(nom);
                String correu = llegirText("Nou correu: ");
                if (!correu.isBlank()) c.setCorreu(correu);
                System.out.println(clientDAO.actualitzar(c) ? "✔ Actualitzat" : "No s'ha actualitzat");
            }
            case 4 -> {
                int id = llegirInt("Id del client: ");
                System.out.println(clientDAO.eliminar(id) ? "✔ Eliminat" : "No existeix.");
            }
            default -> { }
        }
    }

    // ===================== DESCOMPTES =====================
    private static void menuDescomptes() throws SQLException {
        System.out.println("\n--- DESCOMPTES ---");
        System.out.println("1. Llistar  2. Afegir  3. Modificar  4. Eliminar  0. Tornar");
        switch (llegirInt("Opció: ")) {
            case 1 -> descompteDAO.llistar().forEach(System.out::println);
            case 2 -> {
                int producteId = llegirInt("Id del producte: ");
                String tipus = llegirTipus();
                double valor = llegirDouble("Valor: ");
                int id = descompteDAO.inserir(new Descompte(producteId, tipus, valor));
                System.out.println("✔ Descompte creat amb id " + id);
            }
            case 3 -> {
                int id = llegirInt("Id del descompte: ");
                int producteId = llegirInt("Id del producte: ");
                String tipus = llegirTipus();
                double valor = llegirDouble("Valor: ");
                boolean ok = descompteDAO.actualitzar(new Descompte(id, producteId, tipus, valor));
                System.out.println(ok ? "✔ Actualitzat" : "No existeix.");
            }
            case 4 -> {
                int id = llegirInt("Id del descompte: ");
                System.out.println(descompteDAO.eliminar(id) ? "✔ Eliminat" : "No existeix.");
            }
            default -> { }
        }
    }

    // ===================== COMANDES =====================
    private static void crearComanda() throws SQLException {
        clientDAO.llistar().forEach(System.out::println);
        int clientId = llegirInt("Id del client: ");

        List<LiniaComanda> linies = new ArrayList<>();
        producteDAO.llistar().forEach(System.out::println);
        while (true) {
            int producteId = llegirInt("Id del producte (0 per acabar): ");
            if (producteId == 0) break;
            int quantitat = llegirInt("Quantitat: ");
            linies.add(new LiniaComanda(producteId, quantitat));
        }

        Comanda c = comandaDAO.crearComanda(clientId, linies);

        System.out.println("\n✔ Comanda #" + c.getId() + " creada correctament");
        c.getLinies().forEach(System.out::println);
        System.out.printf("   Subtotal:   %8.2f €%n", c.getSubtotal());
        if (c.isDescompteAplicat()) {
            System.out.printf("   Descompte: -%8.2f €%n", c.getDescompte());
        }
        System.out.printf("   TOTAL:      %8.2f €%n", c.getTotal());
    }

    // ===================== CONSULTES =====================
    private static void comandesClient() throws SQLException {
        int clientId = llegirInt("Id del client: ");
        List<Comanda> comandes = consultesDAO.comandesDeClient(clientId);
        if (comandes.isEmpty()) {
            System.out.println("Aquest client no té comandes.");
            return;
        }
        System.out.println("\nComandes de " + comandes.get(0).getNomClient() + ":");
        for (Comanda c : comandes) {
            System.out.printf("%nComanda #%d  (%s)%n", c.getId(), c.getData());
            c.getLinies().forEach(System.out::println);
            System.out.printf("   Subtotal %.2f €  | Descompte %.2f €  | TOTAL %.2f €%n",
                    c.getSubtotal(), c.getDescompte(), c.getTotal());
        }
    }

    private static void totalsComandes() throws SQLException {
        List<Comanda> comandes = consultesDAO.totalsComandes();
        if (comandes.isEmpty()) {
            System.out.println("Encara no hi ha comandes.");
            return;
        }
        System.out.printf("%n%-6s %-12s %-20s %10s %10s %10s%n",
                "Id", "Client", "Data", "Subtotal", "Descompte", "Total");
        for (Comanda c : comandes) {
            System.out.printf("%-6d %-12s %-20s %10.2f %10.2f %10.2f%n",
                    c.getId(), c.getNomClient(), c.getData().toString().substring(0, 19),
                    c.getSubtotal(), c.getDescompte(), c.getTotal());
        }
    }

    // ===================== LECTURA DE DADES =====================
    // Es fa servir sempre nextLine() per evitar el problema del salt de línia
    // que deixa nextInt() al buffer.
    private static String llegirText(String missatge) {
        System.out.print(missatge);
        return sc.nextLine().trim();
    }

    private static int llegirInt(String missatge) {
        while (true) {
            try {
                return Integer.parseInt(llegirText(missatge));
            } catch (NumberFormatException e) {
                System.out.println("Introdueix un número enter.");
            }
        }
    }

    private static double llegirDouble(String missatge) {
        while (true) {
            try {
                return Double.parseDouble(llegirText(missatge).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Introdueix un número (ex: 19.99).");
            }
        }
    }

    private static String llegirTipus() {
        while (true) {
            String t = llegirText("Tipus (% o €): ");
            if (t.equals("%") || t.equals("€")) return t;
            System.out.println("Només '%' o '€'.");
        }
    }
}
