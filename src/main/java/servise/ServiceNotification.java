package servise;

import entities.Notification;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification {

    private final Connection connection;

    public ServiceNotification() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // =========================================================
    //  CRÉER UNE NOTIFICATION
    // =========================================================
    public void creer(int idUser, String message, String type) throws SQLException {
        String sql = "INSERT INTO notifications (id_user, message, type, lu) VALUES (?, ?, ?, 0)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ps.setString(2, message);
        ps.setString(3, type);
        ps.executeUpdate();
        System.out.println("🔔 Notification créée pour user #" + idUser + " : " + message);
    }

    // =========================================================
    //  RÉCUPÉRER LES NOTIFICATIONS D'UN UTILISATEUR
    // =========================================================
    public List<Notification> getNotifications(int idUser) throws SQLException {
        List<Notification> liste = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE id_user = ? ORDER BY date_notif DESC LIMIT 20";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new Notification(
                    rs.getInt("id_notif"),
                    rs.getInt("id_user"),
                    rs.getString("message"),
                    rs.getString("type"),
                    rs.getBoolean("lu"),
                    rs.getTimestamp("date_notif").toLocalDateTime()
            ));
        }
        return liste;
    }

    // =========================================================
    //  COMPTER LES NON LUES
    // =========================================================
    public int compterNonLues(int idUser) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE id_user = ? AND lu = 0";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    // =========================================================
    //  MARQUER TOUTES COMME LUES
    // =========================================================
    public void marquerToutesLues(int idUser) throws SQLException {
        String sql = "UPDATE notifications SET lu = 1 WHERE id_user = ? AND lu = 0";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ps.executeUpdate();
    }

    // =========================================================
    //  MARQUER UNE COMME LUE
    // =========================================================
    public void marquerLue(int idNotif) throws SQLException {
        String sql = "UPDATE notifications SET lu = 1 WHERE id_notif = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idNotif);
        ps.executeUpdate();
    }

    // =========================================================
    //  SUPPRIMER TOUTES LES NOTIFICATIONS D'UN USER
    // =========================================================
    public void supprimerTout(int idUser) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ps.executeUpdate();
    }
}
