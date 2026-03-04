package servise;

import entities.Commentaire;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<Commentaire> {

    private final Connection          connection;
    private final ServiceReputation   serviceReputation   = new ServiceReputation();
    private final ServiceThreads      serviceThreads      = new ServiceThreads();
    private final ServiceNotification serviceNotification = new ServiceNotification();

    public ServiceCommentaire() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void ajouterAvecStatut(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaires (id_thread, id_user, contenu, date_creation, statut, sentiment) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, c.getId_thread());
        ps.setInt(2, c.getId_user());
        ps.setString(3, c.getContenu());
        ps.setTimestamp(4, Timestamp.valueOf(c.getDate_creation()));
        ps.setString(5, c.getStatut()    != null ? c.getStatut()    : "actif");
        ps.setString(6, c.getSentiment() != null ? c.getSentiment() : "neutre");
        ps.executeUpdate();

        if ("actif".equals(c.getStatut())) {
            int    idAuteur = serviceThreads.getIdUserParThread(c.getId_thread());
            String titreTh  = serviceThreads.getTitreParId(c.getId_thread());

            if (idAuteur != 0 && idAuteur != c.getId_user()) {
                serviceReputation.ajouterPoints(idAuteur, ServiceReputation.POINTS_COMMENTAIRE);
                // 🔔 Notification
                serviceNotification.creer(idAuteur,
                        "💬  User #" + c.getId_user() + " a commenté votre thread : \"" + titreTh + "\"",
                        "commentaire");
            }
        }
    }

    @Override
    public void ajouter(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaires (id_thread, id_user, contenu, date_creation, statut, sentiment) VALUES (?, ?, ?, ?, 'actif', 'neutre')";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, c.getId_thread());
        ps.setInt(2, c.getId_user());
        ps.setString(3, c.getContenu());
        ps.setTimestamp(4, Timestamp.valueOf(c.getDate_creation()));
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Commentaire c) throws SQLException {
        String sql = "DELETE FROM commentaires WHERE id_commentaire = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, c.getId_commentaire());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Commentaire c) throws SQLException {
        String sql = "UPDATE commentaires SET contenu = ? WHERE id_commentaire = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, c.getContenu());
        ps.setInt(2, c.getId_commentaire());
        ps.executeUpdate();
    }

    @Override
    public List<Commentaire> recuperer() throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM commentaires ORDER BY date_creation ASC");
        while (rs.next()) liste.add(mapper(rs));
        return liste;
    }

    public List<Commentaire> recupererParThread(int idThread) throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM commentaires WHERE id_thread = ? ORDER BY date_creation ASC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) liste.add(mapper(rs));
        return liste;
    }

    public int compter(int idThread) throws SQLException {
        String sql = "SELECT COUNT(*) FROM commentaires WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    private Commentaire mapper(ResultSet rs) throws SQLException {
        return new Commentaire(
                rs.getInt("id_commentaire"),
                rs.getInt("id_thread"),
                rs.getInt("id_user"),
                rs.getString("contenu"),
                rs.getTimestamp("date_creation").toLocalDateTime(),
                rs.getString("statut"),
                rs.getString("sentiment")
        );
    }
}