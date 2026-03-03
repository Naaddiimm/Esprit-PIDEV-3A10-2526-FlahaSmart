package entities.advancedfeatures;

import java.time.LocalDate;

/**
 * Modèle pour un plan d'irrigation intelligent
 */
public class IrrigationPlan {

    private int id;
    private String typeCulture;
    private double surfaceHectares;
    private double besoinEauLitres;
    private LocalDate dateIrrigation;
    private String heureOptimale;
    private int dureeMinutes;
    private String priorite; // URGENT, NORMAL, FAIBLE
    private String statut; // PLANIFIE, EN_COURS, TERMINE
    private String justification;

    // Données météo associées
    private int temperature;
    private int humiditeAir;
    private int humiditeSol; // Simulé
    private double precipitationsPrevues;
    private String conditionMeteo;

    // Nouveaux critères d'irrigation
    private LocalDate derniereDateIrrigation;
    private double quantiteEauPrecedente; // en litres

    public IrrigationPlan() {
        this.statut = "PLANIFIE";
    }

    public IrrigationPlan(String typeCulture, double surfaceHectares) {
        this.typeCulture = typeCulture;
        this.surfaceHectares = surfaceHectares;
        this.statut = "PLANIFIE";
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTypeCulture() { return typeCulture; }
    public void setTypeCulture(String typeCulture) { this.typeCulture = typeCulture; }

    public double getSurfaceHectares() { return surfaceHectares; }
    public void setSurfaceHectares(double surfaceHectares) { this.surfaceHectares = surfaceHectares; }

    public double getBesoinEauLitres() { return besoinEauLitres; }
    public void setBesoinEauLitres(double besoinEauLitres) { this.besoinEauLitres = besoinEauLitres; }

    public LocalDate getDateIrrigation() { return dateIrrigation; }
    public void setDateIrrigation(LocalDate dateIrrigation) { this.dateIrrigation = dateIrrigation; }

    public String getHeureOptimale() { return heureOptimale; }
    public void setHeureOptimale(String heureOptimale) { this.heureOptimale = heureOptimale; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }

    public int getHumiditeAir() { return humiditeAir; }
    public void setHumiditeAir(int humiditeAir) { this.humiditeAir = humiditeAir; }

    public int getHumiditeSol() { return humiditeSol; }
    public void setHumiditeSol(int humiditeSol) { this.humiditeSol = humiditeSol; }

    public double getPrecipitationsPrevues() { return precipitationsPrevues; }
    public void setPrecipitationsPrevues(double precipitationsPrevues) { this.precipitationsPrevues = precipitationsPrevues; }

    public String getConditionMeteo() { return conditionMeteo; }
    public void setConditionMeteo(String conditionMeteo) { this.conditionMeteo = conditionMeteo; }

    public LocalDate getDerniereDateIrrigation() { return derniereDateIrrigation; }
    public void setDerniereDateIrrigation(LocalDate derniereDateIrrigation) { this.derniereDateIrrigation = derniereDateIrrigation; }

    public double getQuantiteEauPrecedente() { return quantiteEauPrecedente; }
    public void setQuantiteEauPrecedente(double quantiteEauPrecedente) { this.quantiteEauPrecedente = quantiteEauPrecedente; }

    /**
     * Retourne le nombre de jours depuis la dernière irrigation
     */
    public int getJoursDepuisDerniereIrrigation() {
        if (derniereDateIrrigation == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(derniereDateIrrigation, LocalDate.now());
    }

    /**
     * Retourne la quantité d'eau précédente formatée
     */
    public String getQuantiteEauPrecedenteFormate() {
        if (quantiteEauPrecedente >= 1000) {
            return String.format("%.1f m³", quantiteEauPrecedente / 1000);
        }
        return String.format("%.0f L", quantiteEauPrecedente);
    }

    /**
     * Retourne l'icône de priorité
     */
    public String getPrioriteIcon() {
        switch (priorite) {
            case "URGENT": return "🔴";
            case "NORMAL": return "🟡";
            case "FAIBLE": return "🟢";
            default: return "⚪";
        }
    }

    /**
     * Retourne le besoin en eau formaté
     */
    public String getBesoinEauFormate() {
        if (besoinEauLitres >= 1000) {
            return String.format("%.1f m³", besoinEauLitres / 1000);
        }
        return String.format("%.0f L", besoinEauLitres);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s (%s)",
            typeCulture, dateIrrigation, heureOptimale, priorite);
    }
}

