package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myConnection {
    // CORRECTION : Ajoutez useSSL=false et permettez la récupération de clé publique
    private String url = "jdbc:mysql://localhost:3306/flahasmart?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String login = "root";
    private String password = "";
    private Connection con;
    private static myConnection instance;

    private myConnection() {
        establishConnection();
    }

    public static myConnection getInstance() {
        if (instance == null) {
            instance = new myConnection();
        }
        return instance;
    }

    private void establishConnection() {
        try {
            // Charger le driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(url, login, password);
            System.out.println("✅ Connexion établie avec succès à la base de données: flahasmart");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getCnx() {
        try {
            if (con == null || con.isClosed()) {
                System.out.println("⚠️ Reconnexion...");
                establishConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}