package entities;

import java.time.LocalDateTime;

public class Notification {

    private int           id_notif;
    private int           id_user;
    private String        message;
    private String        type;    // "like", "commentaire", "vote"
    private boolean       lu;
    private LocalDateTime date_notif;

    public Notification() {}

    public Notification(int id_user, String message, String type) {
        this.id_user    = id_user;
        this.message    = message;
        this.type       = type;
        this.lu         = false;
        this.date_notif = LocalDateTime.now();
    }

    public Notification(int id_notif, int id_user, String message, String type,
                        boolean lu, LocalDateTime date_notif) {
        this.id_notif   = id_notif;
        this.id_user    = id_user;
        this.message    = message;
        this.type       = type;
        this.lu         = lu;
        this.date_notif = date_notif;
    }

    public int           getId_notif()                       { return id_notif; }
    public void          setId_notif(int id_notif)           { this.id_notif = id_notif; }
    public int           getId_user()                        { return id_user; }
    public void          setId_user(int id_user)             { this.id_user = id_user; }
    public String        getMessage()                        { return message; }
    public void          setMessage(String message)          { this.message = message; }
    public String        getType()                           { return type; }
    public void          setType(String type)                { this.type = type; }
    public boolean       isLu()                              { return lu; }
    public void          setLu(boolean lu)                   { this.lu = lu; }
    public LocalDateTime getDate_notif()                     { return date_notif; }
    public void          setDate_notif(LocalDateTime d)      { this.date_notif = d; }
}