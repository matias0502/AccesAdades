package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Comanda {
    private int id;
    private int clientId;
    private Timestamp data;
    private double total;
    private List<LiniaComanda> linies = new ArrayList<>();

    // Camps auxiliars (no són columnes de la taula Comandes)
    private String nomClient;          // s'omple amb JOIN a Clients
    private double subtotal;           // suma de les línies sense descompte
    private boolean descompteAplicat;  // resultat de l'intent amb savepoint

    public Comanda() { }

    public Comanda(int id, int clientId, Timestamp data, double total) {
        this.id = id;
        this.clientId = clientId;
        this.data = data;
        this.total = total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public Timestamp getData() { return data; }
    public void setData(Timestamp data) { this.data = data; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<LiniaComanda> getLinies() { return linies; }
    public void setLinies(List<LiniaComanda> linies) { this.linies = linies; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public boolean isDescompteAplicat() { return descompteAplicat; }
    public void setDescompteAplicat(boolean descompteAplicat) { this.descompteAplicat = descompteAplicat; }

    public double getDescompte() {
        return subtotal - total;
    }

    @Override
    public String toString() {
        return String.format("Comanda #%d  client %d  data %s  total %.2f €", id, clientId, data, total);
    }
}
