package entities.advancedfeatures;

/**
 * Modèle pour une recommandation de culture dans le cadre de la rotation
 */
public class RecommandationCulture {

    private String culture;
    private double scoreCompatibilite;  // 0-100
    private String famille;              // Légumineuses, Céréales, etc.
    private String raisonRecommandation;
    private String beneficesSol;
    private String periodeOptimale;
    private boolean recommandee;
    private String niveauRecommandation; // IDEAL, BON, ACCEPTABLE, DECONSEILLE

    public RecommandationCulture() {}

    public RecommandationCulture(String culture, double scoreCompatibilite) {
        this.culture = culture;
        this.scoreCompatibilite = scoreCompatibilite;
        this.recommandee = scoreCompatibilite >= 50;
        determinerNiveau();
    }

    private void determinerNiveau() {
        if (scoreCompatibilite >= 85) {
            niveauRecommandation = "IDEAL";
        } else if (scoreCompatibilite >= 70) {
            niveauRecommandation = "BON";
        } else if (scoreCompatibilite >= 50) {
            niveauRecommandation = "ACCEPTABLE";
        } else {
            niveauRecommandation = "DECONSEILLE";
        }
    }

    // Getters et Setters
    public String getCulture() { return culture; }
    public void setCulture(String culture) { this.culture = culture; }

    public double getScoreCompatibilite() { return scoreCompatibilite; }
    public void setScoreCompatibilite(double scoreCompatibilite) {
        this.scoreCompatibilite = scoreCompatibilite;
        determinerNiveau();
    }

    public String getFamille() { return famille; }
    public void setFamille(String famille) { this.famille = famille; }

    public String getRaisonRecommandation() { return raisonRecommandation; }
    public void setRaisonRecommandation(String raisonRecommandation) { this.raisonRecommandation = raisonRecommandation; }

    public String getBeneficesSol() { return beneficesSol; }
    public void setBeneficesSol(String beneficesSol) { this.beneficesSol = beneficesSol; }

    public String getPeriodeOptimale() { return periodeOptimale; }
    public void setPeriodeOptimale(String periodeOptimale) { this.periodeOptimale = periodeOptimale; }

    public boolean isRecommandee() { return recommandee; }
    public void setRecommandee(boolean recommandee) { this.recommandee = recommandee; }

    public String getNiveauRecommandation() { return niveauRecommandation; }
    public void setNiveauRecommandation(String niveauRecommandation) { this.niveauRecommandation = niveauRecommandation; }

    /**
     * Retourne l'icône du niveau de recommandation
     */
    public String getNiveauIcon() {
        if (niveauRecommandation == null) return "⚪";
        switch (niveauRecommandation) {
            case "IDEAL": return "⭐";
            case "BON": return "✅";
            case "ACCEPTABLE": return "🟡";
            case "DECONSEILLE": return "❌";
            default: return "⚪";
        }
    }

    /**
     * Retourne la couleur associée au niveau
     */
    public String getNiveauColor() {
        if (niveauRecommandation == null) return "#6b7280";
        switch (niveauRecommandation) {
            case "IDEAL": return "#059669";
            case "BON": return "#22c55e";
            case "ACCEPTABLE": return "#f59e0b";
            case "DECONSEILLE": return "#dc2626";
            default: return "#6b7280";
        }
    }

    /**
     * Retourne l'icône de la famille de culture
     */
    public String getFamilleIcon() {
        if (famille == null) return "🌱";
        switch (famille.toLowerCase()) {
            case "légumineuses": return "🫘";
            case "céréales": return "🌾";
            case "solanacées": return "🍅";
            case "crucifères": return "🥬";
            case "cucurbitacées": return "🥒";
            case "alliacées": return "🧅";
            case "ombellifères": return "🥕";
            case "oléagineuses": return "🌻";
            default: return "🌱";
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s (%.0f%%) - %s",
            getNiveauIcon(), culture, scoreCompatibilite, niveauRecommandation);
    }
}

