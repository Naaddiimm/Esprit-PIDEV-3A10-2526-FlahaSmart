package entities.commande;
import tools.myConnection;

public class Todo {
    private String nomTache;      // Consultant name/task owner
    private String tache;          // Task description
    private String statut;         // Status: TODO, DOING, DONE

    public Todo() {}

    public Todo(String nomTache, String tache, String statut) {
        this.nomTache = nomTache;
        this.tache = tache;
        this.statut = statut;
    }

    // Getters and Setters
    public String getNomTache() { return nomTache; }
    public void setNomTache(String nomTache) { this.nomTache = nomTache; }

    public String getTache() { return tache; }
    public void setTache(String tache) { this.tache = tache; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}