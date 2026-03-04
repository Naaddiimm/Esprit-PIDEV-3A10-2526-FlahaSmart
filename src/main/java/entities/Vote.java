package entities;

import java.time.LocalDateTime;

public class Vote {

    private int           id_vote;
    private int           id_thread;
    private int           id_user;
    private String        type_vote; // "up" ou "down"
    private LocalDateTime date_vote;

    public Vote() {}

    public Vote(int id_thread, int id_user, String type_vote) {
        this.id_thread  = id_thread;
        this.id_user    = id_user;
        this.type_vote  = type_vote;
        this.date_vote  = LocalDateTime.now();
    }

    public Vote(int id_vote, int id_thread, int id_user, String type_vote, LocalDateTime date_vote) {
        this.id_vote    = id_vote;
        this.id_thread  = id_thread;
        this.id_user    = id_user;
        this.type_vote  = type_vote;
        this.date_vote  = date_vote;
    }

    public int           getId_vote()                        { return id_vote; }
    public void          setId_vote(int id_vote)             { this.id_vote = id_vote; }
    public int           getId_thread()                      { return id_thread; }
    public void          setId_thread(int id_thread)         { this.id_thread = id_thread; }
    public int           getId_user()                        { return id_user; }
    public void          setId_user(int id_user)             { this.id_user = id_user; }
    public String        getType_vote()                      { return type_vote; }
    public void          setType_vote(String type_vote)      { this.type_vote = type_vote; }
    public LocalDateTime getDate_vote()                      { return date_vote; }
    public void          setDate_vote(LocalDateTime d)       { this.date_vote = d; }
    public boolean       isUpvote()                          { return "up".equals(type_vote); }
    public boolean       isDownvote()                        { return "down".equals(type_vote); }
}