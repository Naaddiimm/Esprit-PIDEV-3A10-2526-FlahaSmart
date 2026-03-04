package entities;

import java.time.LocalDateTime;

public class Commentaire {

    private int           id_commentaire;
    private int           id_thread;
    private int           id_user;
    private String        contenu;
    private LocalDateTime date_creation;
    private String        statut;    // "actif" ou "banni"
    private String        sentiment; // "positif", "negatif", "neutre"

    public Commentaire() {}

    public Commentaire(int id_thread, int id_user, String contenu, LocalDateTime date_creation) {
        this.id_thread     = id_thread;
        this.id_user       = id_user;
        this.contenu       = contenu;
        this.date_creation = date_creation;
        this.statut        = "actif";
        this.sentiment     = "neutre";
    }

    public Commentaire(int id_commentaire, int id_thread, int id_user, String contenu,
                       LocalDateTime date_creation, String statut, String sentiment) {
        this.id_commentaire = id_commentaire;
        this.id_thread      = id_thread;
        this.id_user        = id_user;
        this.contenu        = contenu;
        this.date_creation  = date_creation;
        this.statut         = statut;
        this.sentiment      = sentiment;
    }

    public int           getId_commentaire()                           { return id_commentaire; }
    public void          setId_commentaire(int id_commentaire)         { this.id_commentaire = id_commentaire; }
    public int           getId_thread()                                { return id_thread; }
    public void          setId_thread(int id_thread)                   { this.id_thread = id_thread; }
    public int           getId_user()                                  { return id_user; }
    public void          setId_user(int id_user)                       { this.id_user = id_user; }
    public String        getContenu()                                  { return contenu; }
    public void          setContenu(String contenu)                    { this.contenu = contenu; }
    public LocalDateTime getDate_creation()                            { return date_creation; }
    public void          setDate_creation(LocalDateTime date_creation) { this.date_creation = date_creation; }
    public String        getStatut()                                   { return statut; }
    public void          setStatut(String statut)                      { this.statut = statut; }
    public String        getSentiment()                                { return sentiment; }
    public void          setSentiment(String sentiment)                { this.sentiment = sentiment; }
}