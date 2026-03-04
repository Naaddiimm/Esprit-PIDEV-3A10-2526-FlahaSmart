package entities;

import java.time.LocalDateTime;

public class thread {

    private int           id_thread;
    private String        titre;
    private String        contenu;
    private LocalDateTime date_creation;
    private LocalDateTime date_update;
    private int           id_user;
    private String        statut;
    private String        sentiment;
    private String        tags;

    public thread() {}

    public thread(String titre, String contenu, LocalDateTime date_creation,
                  LocalDateTime date_update, int id_user) {
        this.titre         = titre;
        this.contenu       = contenu;
        this.date_creation = date_creation;
        this.date_update   = date_update;
        this.id_user       = id_user;
        this.statut        = "actif";
        this.sentiment     = "neutre";
        this.tags          = "";
    }

    public thread(int id_thread, String titre, String contenu, LocalDateTime date_creation,
                  LocalDateTime date_update, int id_user, String statut, String sentiment) {
        this.id_thread     = id_thread;
        this.titre         = titre;
        this.contenu       = contenu;
        this.date_creation = date_creation;
        this.date_update   = date_update;
        this.id_user       = id_user;
        this.statut        = statut;
        this.sentiment     = sentiment;
        this.tags          = "";
    }

    public thread(int id_thread, String titre, String contenu, LocalDateTime date_creation,
                  LocalDateTime date_update, int id_user, String statut, String sentiment, String tags) {
        this.id_thread     = id_thread;
        this.titre         = titre;
        this.contenu       = contenu;
        this.date_creation = date_creation;
        this.date_update   = date_update;
        this.id_user       = id_user;
        this.statut        = statut;
        this.sentiment     = sentiment;
        this.tags          = tags != null ? tags : "";
    }

    public int           getId_thread()                    { return id_thread; }
    public void          setId_thread(int id_thread)       { this.id_thread = id_thread; }
    public String        getTitre()                        { return titre; }
    public void          setTitre(String titre)            { this.titre = titre; }
    public String        getContenu()                      { return contenu; }
    public void          setContenu(String contenu)        { this.contenu = contenu; }
    public LocalDateTime getDate_creation()                { return date_creation; }
    public void          setDate_creation(LocalDateTime d) { this.date_creation = d; }
    public LocalDateTime getDate_update()                  { return date_update; }
    public void          setDate_update(LocalDateTime d)   { this.date_update = d; }
    public int           getId_user()                      { return id_user; }
    public void          setId_user(int id_user)           { this.id_user = id_user; }
    public String        getStatut()                       { return statut; }
    public void          setStatut(String statut)          { this.statut = statut; }
    public String        getSentiment()                    { return sentiment; }
    public void          setSentiment(String sentiment)    { this.sentiment = sentiment; }
    public String        getTags()                         { return tags; }
    public void          setTags(String tags)              { this.tags = tags != null ? tags : ""; }
}