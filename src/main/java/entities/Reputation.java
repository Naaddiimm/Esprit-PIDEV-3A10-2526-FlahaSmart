package entities;

public class Reputation {

    private int    id_rep;
    private int    id_user;
    private int    points;
    private String badge;

    public Reputation() {}

    public Reputation(int id_user) {
        this.id_user = id_user;
        this.points  = 0;
        this.badge   = "🌱 Débutant";
    }

    public Reputation(int id_rep, int id_user, int points, String badge) {
        this.id_rep  = id_rep;
        this.id_user = id_user;
        this.points  = points;
        this.badge   = badge;
    }

    public int    getId_rep()              { return id_rep; }
    public void   setId_rep(int id_rep)    { this.id_rep = id_rep; }
    public int    getId_user()             { return id_user; }
    public void   setId_user(int id_user)  { this.id_user = id_user; }
    public int    getPoints()              { return points; }
    public void   setPoints(int points)    { this.points = points; }
    public String getBadge()               { return badge; }
    public void   setBadge(String badge)   { this.badge = badge; }
}