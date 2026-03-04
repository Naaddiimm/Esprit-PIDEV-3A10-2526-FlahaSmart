package servise;

import utils.MyDataBase;

import java.sql.*;

public class ServiceJaime {

    private final Connection        connection;
    private final ServiceReputation  serviceReputation  = new ServiceReputation();
    private final ServiceThreads     serviceThreads     = new ServiceThreads();
    private final ServiceNotification serviceNotification = new ServiceNotification();

    public ServiceJaime() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public boolean toggle(int idThread, int idUser) throws SQLException {
        int    idAuteur = serviceThreads.getIdUserParThread(idThread);
        String titreTh  = serviceThreads.getTitreParId(idThread);

        if (aDejaAime(idThread, idUser)) {
            // Retirer le like
            String sql = "DELETE FROM jaime WHERE id_thread = ? AND id_user = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idThread); ps.setInt(2, idUser);
            ps.executeUpdate();
            if (idAuteur != 0 && idAuteur != idUser)
                serviceReputation.deduirePoints(idAuteur, ServiceReputation.POINTS_LIKE);
            return false;
        } else {
            // Ajouter le like
            String sql = "INSERT INTO jaime (id_thread, id_user) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idThread); ps.setInt(2, idUser);
            ps.executeUpdate();

            if (idAuteur != 0 && idAuteur != idUser) {
                serviceReputation.ajouterPoints(idAuteur, ServiceReputation.POINTS_LIKE);
                // 🔔 Notification
                serviceNotification.creer(idAuteur,
                        "❤️  User #" + idUser + " a aimé votre thread : \"" + titreTh + "\"",
                        "like");
            }
            return true;
        }
    }

    public boolean aDejaAime(int idThread, int idUser) throws SQLException {
        String sql = "SELECT COUNT(*) FROM jaime WHERE id_thread = ? AND id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread); ps.setInt(2, idUser);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1) > 0;
        return false;
    }

    public int compter(int idThread) throws SQLException {
        String sql = "SELECT COUNT(*) FROM jaime WHERE id_thread = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idThread);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}