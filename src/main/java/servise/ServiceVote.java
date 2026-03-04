package servise;

import utils.MyDataBase;

import java.sql.*;

public class ServiceVote {

    private final Connection          connection;
    private final ServiceReputation   serviceReputation   = new ServiceReputation();
    private final ServiceThreads      serviceThreads      = new ServiceThreads();
    private final ServiceNotification serviceNotification = new ServiceNotification();

    public ServiceVote() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public String voter(int idThread, int idUser, String typeVote) throws SQLException {
        String voteActuel = getVoteActuel(idThread, idUser);
        int    idAuteur   = serviceThreads.getIdUserParThread(idThread);
        String titreTh    = serviceThreads.getTitreParId(idThread);

        if (voteActuel == null) {
            insererVote(idThread, idUser, typeVote);
            if (idAuteur != 0 && idAuteur != idUser) {
                if ("up".equals(typeVote)) {
                    serviceReputation.ajouterPoints(idAuteur, ServiceReputation.POINTS_UPVOTE);
                    // 🔔 Notification upvote
                    serviceNotification.creer(idAuteur,
                            "👍  User #" + idUser + " a voté pour votre thread : \"" + titreTh + "\"",
                            "vote");
                }
            }
            return typeVote;

        } else if (voteActuel.equals(typeVote)) {
            supprimerVote(idThread, idUser);
            if ("up".equals(typeVote) && idAuteur != 0 && idAuteur != idUser)
                serviceReputation.deduirePoints(idAuteur, ServiceReputation.POINTS_UPVOTE);
            return null;

        } else {
            changerVote(idThread, idUser, typeVote);
            if (idAuteur != 0 && idAuteur != idUser) {
                if ("up".equals(typeVote)) {
                    serviceReputation.ajouterPoints(idAuteur, ServiceReputation.POINTS_UPVOTE);
                    serviceNotification.creer(idAuteur,
                            "👍  User #" + idUser + " a voté pour votre thread : \"" + titreTh + "\"",
                            "vote");
                } else {
                    serviceReputation.deduirePoints(idAuteur, ServiceReputation.POINTS_UPVOTE);
                }
            }
            return typeVote;
        }
    }

    public String getVoteActuel(int idThread, int idUser) throws SQLException {
        String sql = "SELECT type_vote FROM votes WHERE id_thread = ? AND id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread); ps.setInt(2, idUser);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("type_vote");
        return null;
    }

    public int compterUpvotes(int idThread) throws SQLException   { return compterVotes(idThread, "up"); }
    public int compterDownvotes(int idThread) throws SQLException { return compterVotes(idThread, "down"); }
    public int calculerScore(int idThread) throws SQLException    { return compterUpvotes(idThread) - compterDownvotes(idThread); }

    private int compterVotes(int idThread, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM votes WHERE id_thread = ? AND type_vote = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread); ps.setString(2, type);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    private void insererVote(int idThread, int idUser, String typeVote) throws SQLException {
        String sql = "INSERT INTO votes (id_thread, id_user, type_vote) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread); ps.setInt(2, idUser); ps.setString(3, typeVote);
        ps.executeUpdate();
    }

    private void supprimerVote(int idThread, int idUser) throws SQLException {
        String sql = "DELETE FROM votes WHERE id_thread = ? AND id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread); ps.setInt(2, idUser);
        ps.executeUpdate();
    }

    private void changerVote(int idThread, int idUser, String nouveauType) throws SQLException {
        String sql = "UPDATE votes SET type_vote = ?, date_vote = NOW() WHERE id_thread = ? AND id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nouveauType); ps.setInt(2, idThread); ps.setInt(3, idUser);
        ps.executeUpdate();
    }
}