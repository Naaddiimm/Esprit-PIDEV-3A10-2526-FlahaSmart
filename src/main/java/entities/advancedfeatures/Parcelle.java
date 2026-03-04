package entities.advancedfeatures;

import java.time.LocalDate;

/**
 * Modèle pour représenter une parcelle de terrain
 */
public class Parcelle {

    private int id;
    private String nom;
    private double surfaceHectares;
    private String typeSol;           // Argileux, Sableux, Limoneux, Calcaire
    private String derniereCulture;
    private String avantDerniereCulture;
    private LocalDate dateDerniereRecolte;
    private int niveauAzote;          // 1-10
    private int niveauPhosphore;      // 1-10
    private int niveauPotassium;      // 1-10
    private double ph;                // 4.0 - 9.0
    private boolean enJachere;
    private int anneesDepuisJachere;

    public Parcelle() {
        this.enJachere = false;
        this.anneesDepuisJachere = 0;
    }

    public Parcelle(double surfaceHectares, String typeSol) {
        this.surfaceHectares = surfaceHectares;
        this.typeSol = typeSol;
        this.enJachere = false;
        this.anneesDepuisJachere = 0;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


    public double getSurfaceHectares() { return surfaceHectares; }
    public void setSurfaceHectares(double surfaceHectares) { this.surfaceHectares = surfaceHectares; }

    public String getTypeSol() { return typeSol; }
    public void setTypeSol(String typeSol) { this.typeSol = typeSol; }

    public String getDerniereCulture() { return derniereCulture; }
    public void setDerniereCulture(String derniereCulture) { this.derniereCulture = derniereCulture; }

    public String getAvantDerniereCulture() { return avantDerniereCulture; }
    public void setAvantDerniereCulture(String avantDerniereCulture) { this.avantDerniereCulture = avantDerniereCulture; }

    public LocalDate getDateDerniereRecolte() { return dateDerniereRecolte; }
    public void setDateDerniereRecolte(LocalDate dateDerniereRecolte) { this.dateDerniereRecolte = dateDerniereRecolte; }

    public int getNiveauAzote() { return niveauAzote; }
    public void setNiveauAzote(int niveauAzote) { this.niveauAzote = niveauAzote; }

    public int getNiveauPhosphore() { return niveauPhosphore; }
    public void setNiveauPhosphore(int niveauPhosphore) { this.niveauPhosphore = niveauPhosphore; }

    public int getNiveauPotassium() { return niveauPotassium; }
    public void setNiveauPotassium(int niveauPotassium) { this.niveauPotassium = niveauPotassium; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public boolean isEnJachere() { return enJachere; }
    public void setEnJachere(boolean enJachere) { this.enJachere = enJachere; }

    public int getAnneesDepuisJachere() { return anneesDepuisJachere; }
    public void setAnneesDepuisJachere(int anneesDepuisJachere) { this.anneesDepuisJachere = anneesDepuisJachere; }

    /**
     * Retourne le niveau de fertilité global (moyenne NPK)
     */
    public double getFertiliteGlobale() {
        return (niveauAzote + niveauPhosphore + niveauPotassium) / 3.0;
    }

    /**
     * Retourne l'icône du type de sol
     */
    public String getTypeSolIcon() {
        if (typeSol == null) return "🌍";
        switch (typeSol.toLowerCase()) {
            case "argileux": return "🟤";
            case "sableux": return "🟡";
            case "limoneux": return "🟢";
            case "calcaire": return "⚪";
            default: return "🌍";
        }
    }

    @Override
    public String toString() {
        return "Parcelle (" + surfaceHectares + " ha - " + typeSol + ")";
    }
}

