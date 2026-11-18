package model;

public class LiniaComanda {
    private int id;
    private int comandaId;
    private int producteId;
    private int quantitat;
    private double preuUnitari;
    private String nomProducte; // només per mostrar (s'omple amb un JOIN)

    public LiniaComanda() { }

    /** Constructor per crear una línia nova des del menú (el preu el posa el DAO). */
    public LiniaComanda(int producteId, int quantitat) {
        this.producteId = producteId;
        this.quantitat = quantitat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getComandaId() { return comandaId; }
    public void setComandaId(int comandaId) { this.comandaId = comandaId; }
    public int getProducteId() { return producteId; }
    public void setProducteId(int producteId) { this.producteId = producteId; }
    public int getQuantitat() { return quantitat; }
    public void setQuantitat(int quantitat) { this.quantitat = quantitat; }
    public double getPreuUnitari() { return preuUnitari; }
    public void setPreuUnitari(double preuUnitari) { this.preuUnitari = preuUnitari; }
    public String getNomProducte() { return nomProducte; }
    public void setNomProducte(String nomProducte) { this.nomProducte = nomProducte; }

    public double getImport() {
        return quantitat * preuUnitari;
    }

    @Override
    public String toString() {
        return String.format("   - %-15s x%-3d %8.2f €  = %8.2f €",
                nomProducte != null ? nomProducte : "Producte " + producteId,
                quantitat, preuUnitari, getImport());
    }
}
