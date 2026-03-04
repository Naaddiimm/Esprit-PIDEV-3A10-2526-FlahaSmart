package servise;

import entities.thread;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceThreads implements IService<thread> {

    private Connection        connection;
    private ServiceReputation serviceReputation = new ServiceReputation();

    public ServiceThreads() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void ajouterAvecStatut(thread t) throws SQLException {
        validerThread(t);
        String sql = "INSERT INTO threads (titre, contenu, date_creation, date_update, id_user, statut, sentiment, tags) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, t.getTitre());
        ps.setString(2, t.getContenu());
        ps.setTimestamp(3, Timestamp.valueOf(t.getDate_creation()));
        ps.setTimestamp(4, Timestamp.valueOf(t.getDate_update()));
        ps.setInt(5, t.getId_user());
        ps.setString(6, t.getStatut()    != null ? t.getStatut()    : "actif");
        ps.setString(7, t.getSentiment() != null ? t.getSentiment() : "neutre");
        ps.setString(8, t.getTags()      != null ? t.getTags()      : "");
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) t.setId_thread(keys.getInt(1));

        if ("actif".equals(t.getStatut()))
            serviceReputation.ajouterPoints(t.getId_user(), ServiceReputation.POINTS_THREAD);
    }

    @Override
    public void ajouter(thread t) throws SQLException {
        validerThread(t);
        String sql = "INSERT INTO threads (titre, contenu, date_creation, date_update, id_user, statut, sentiment, tags) VALUES (?, ?, ?, ?, ?, 'actif', 'neutre', '')";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, t.getTitre());
        ps.setString(2, t.getContenu());
        ps.setTimestamp(3, Timestamp.valueOf(t.getDate_creation()));
        ps.setTimestamp(4, Timestamp.valueOf(t.getDate_update()));
        ps.setInt(5, t.getId_user());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(thread t) throws SQLException {
        String sql = "DELETE FROM threads WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, t.getId_thread());
        ps.executeUpdate();
    }

    @Override
    public void modifier(thread t) throws SQLException {
        validerThread(t);
        String sql = "UPDATE threads SET titre = ?, contenu = ?, date_update = ?, id_user = ? WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, t.getTitre());
        ps.setString(2, t.getContenu());
        ps.setObject(3, t.getDate_update());
        ps.setInt(4, t.getId_user());
        ps.setInt(5, t.getId_thread());
        ps.executeUpdate();
    }

    @Override
    public List<thread> recuperer() throws SQLException {
        List<thread> threads = new ArrayList<>();
        String sql = "SELECT * FROM threads ORDER BY date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) threads.add(mapper(rs));
        return threads;
    }

    public List<thread> recupererParScore() throws SQLException {
        List<thread> threads = new ArrayList<>();
        String sql = "SELECT t.*, " +
                "(SELECT COUNT(*) FROM votes v WHERE v.id_thread = t.id_thread AND v.type_vote = 'up') - " +
                "(SELECT COUNT(*) FROM votes v WHERE v.id_thread = t.id_thread AND v.type_vote = 'down') AS score " +
                "FROM threads t ORDER BY score DESC, t.date_creation DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) threads.add(mapper(rs));
        return threads;
    }

    public int getIdUserParThread(int idThread) throws SQLException {
        String sql = "SELECT id_user FROM threads WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id_user");
        return 0;
    }

    public String getTitreParId(int idThread) throws SQLException {
        String sql = "SELECT titre FROM threads WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("titre");
        return "Thread #" + idThread;
    }

    private thread mapper(ResultSet rs) throws SQLException {
        return new thread(
                rs.getInt("id_thread"),
                rs.getString("titre"),
                rs.getString("contenu"),
                rs.getTimestamp("date_creation").toLocalDateTime(),
                rs.getTimestamp("date_update").toLocalDateTime(),
                rs.getInt("id_user"),
                rs.getString("statut"),
                rs.getString("sentiment"),
                rs.getString("tags")
        );
    }

    private void validerThread(thread t) {
        if (t.getTitre() == null || t.getTitre().trim().isEmpty())
            throw new IllegalArgumentException("Le titre ne doit pas être vide !");
        if (t.getContenu() == null || t.getContenu().trim().isEmpty())
            throw new IllegalArgumentException("Le contenu ne doit pas être vide !");
        if (t.getId_user() <= 0)
            throw new IllegalArgumentException("id_user invalide !");
    }
    // =========================================================
//  AJOUTER CETTE MÉTHODE dans ServiceThreads.java
// =========================================================

    /**
     * Met à jour les tags du dernier thread inséré par un utilisateur.
     * Appelée après ApiClient.publierThread() car ModerationAPI ne gère pas les tags.
     */
    public void mettreAJourTagsDernierThread(int idUser, String tags) throws SQLException {
        String sql = "UPDATE threads SET tags = ? " +
                "WHERE id_user = ? " +
                "ORDER BY date_creation DESC LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, tags);
        ps.setInt(2, idUser);
        ps.executeUpdate();
    }
}