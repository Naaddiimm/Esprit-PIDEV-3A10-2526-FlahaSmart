package servise;

import entities.Reputation;
import utils.MyDataBase;

import java.sql.*;

public class ServiceReputation {

    private final Connection connection;

    // Points par action
    public static final int POINTS_THREAD     = 5;  // publier un thread
    public static final int POINTS_UPVOTE     = 3;  // recevoir un upvote
    public static final int POINTS_COMMENTAIRE = 2; // recevoir un commentaire
    public static final int POINTS_LIKE       = 1;  // recevoir un like

    public ServiceReputation() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // =========================================================
    //  AJOUTER / DÉDUIRE DES POINTS
    // =========================================================
    public void ajouterPoints(int idUser, int points) throws SQLException {
        // Créer la réputation si elle n'existe pas
        initialiserSiAbsent(idUser);

        String sql = "UPDATE reputation SET points = points + ? WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, points);
        ps.setInt(2, idUser);
        ps.executeUpdate();

        // Recalculer le badge
        mettreAJourBadge(idUser);

        System.out.println("✅ +" + points + " pts pour user #" + idUser
                + " → Total : " + getPoints(idUser) + " pts | Badge : " + getBadge(idUser));
    }

    public void deduirePoints(int idUser, int points) throws SQLException {
        initialiserSiAbsent(idUser);

        // Ne pas aller en dessous de 0
        String sql = "UPDATE reputation SET points = GREATEST(0, points - ?) WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, points);
        ps.setInt(2, idUser);
        ps.executeUpdate();

        mettreAJourBadge(idUser);
    }

    // =========================================================
    //  RÉCUPÉRER LA RÉPUTATION
    // =========================================================
    public Reputation getReputation(int idUser) throws SQLException {
        initialiserSiAbsent(idUser);

        String sql = "SELECT * FROM reputation WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Reputation(
                    rs.getInt("id_rep"),
                    rs.getInt("id_user"),
                    rs.getInt("points"),
                    rs.getString("badge")
            );
        }
        return new Reputation(idUser);
    }

    public int getPoints(int idUser) throws SQLException {
        return getReputation(idUser).getPoints();
    }

    public String getBadge(int idUser) throws SQLException {
        return getReputation(idUser).getBadge();
    }

    // =========================================================
    //  CALCUL DU BADGE
    // =========================================================
    private void mettreAJourBadge(int idUser) throws SQLException {
        int points = getPoints(idUser);
        String badge = calculerBadge(points);

        String sql = "UPDATE reputation SET badge = ? WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, badge);
        ps.setInt(2, idUser);
        ps.executeUpdate();
    }

    public static String calculerBadge(int points) {
        if (points >= 101) return "👑 Maître";
        if (points >= 51)  return "🌾 Expert";
        if (points >= 11)  return "🌿 Actif";
        return "🌱 Débutant";
    }

    // =========================================================
    //  INITIALISER SI ABSENT
    // =========================================================
    private void initialiserSiAbsent(int idUser) throws SQLException {
        String check = "SELECT id_rep FROM reputation WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(check);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            String insert = "INSERT INTO reputation (id_user, points, badge) VALUES (?, 0, '🌱 Débutant')";
            PreparedStatement ins = connection.prepareStatement(insert);
            ins.setInt(1, idUser);
            ins.executeUpdate();
        }
    }
}