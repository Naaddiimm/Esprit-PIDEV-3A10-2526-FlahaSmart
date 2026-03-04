package entities;

public class Equipement {
    private int id_equipement;
    private String nom;
    private String type;
    private String etat;

    public Equipement(int id_equipement, String nom,String type, String etat) {
        this.id_equipement = id_equipement;
        this.nom = nom;
        this.type = type;
        this.etat = etat;
    }

    public Equipement(String nom,String type, String etat) {
        this.nom = nom;
        this.type = type;
        this.etat = etat;
    }

    public Equipement() {

    }

    public int getId_equipement() {
        return id_equipement;
    }
    public void setId_equipement(int id_equipement) {
        this.id_equipement = id_equipement;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getEtat() {
        return etat;
    }
    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return nom
                ;
    }
}
