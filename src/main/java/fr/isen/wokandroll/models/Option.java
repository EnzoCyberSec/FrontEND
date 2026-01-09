package fr.isen.wokandroll.models;

public class Option {
    private int idOption;
    private String libelle;
    private String type;
    private double prix;

    public Option(int idOption, String libelle, String type, double prix) {
        this.idOption = idOption;
        this.libelle = libelle;
        this.type = type;
        this.prix = prix;
    }

    public int getIdOption() { return idOption; }
    public void setIdOption(int idOption) { this.idOption = idOption; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    @Override
    public String toString() {
        return libelle;
    }
}