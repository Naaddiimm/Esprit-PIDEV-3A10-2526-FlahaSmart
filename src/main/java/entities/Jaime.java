package entities;

import java.time.LocalDateTime;

public class Jaime {

    private int           id_jaime;
    private int           id_thread;
    private int           id_user;
    private LocalDateTime date_jaime;

    public Jaime() {}

    public Jaime(int id_thread, int id_user, LocalDateTime date_jaime) {
        this.id_thread  = id_thread;
        this.id_user    = id_user;
        this.date_jaime = date_jaime;
    }

    public Jaime(int id_jaime, int id_thread, int id_user, LocalDateTime date_jaime) {
        this.id_jaime   = id_jaime;
        this.id_thread  = id_thread;
        this.id_user    = id_user;
        this.date_jaime = date_jaime;
    }

    public int           getId_jaime()                           { return id_jaime; }
    public void          setId_jaime(int id_jaime)               { this.id_jaime = id_jaime; }
    public int           getId_thread()                          { return id_thread; }
    public void          setId_thread(int id_thread)             { this.id_thread = id_thread; }
    public int           getId_user()                            { return id_user; }
    public void          setId_user(int id_user)                 { this.id_user = id_user; }
    public LocalDateTime getDate_jaime()                         { return date_jaime; }
    public void          setDate_jaime(LocalDateTime date_jaime) { this.date_jaime = date_jaime; }

    @Override
    public String toString() {
        return "Jaime{id_jaime=" + id_jaime + ", id_thread=" + id_thread
                + ", id_user=" + id_user + ", date_jaime=" + date_jaime + "}";
    }
}