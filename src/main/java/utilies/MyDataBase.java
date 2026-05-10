package utilies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/flahasmart?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;
    private static MyDataBase instance;

    // Constructeur privé avec initialisation de la connexion
    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion BD réussie");
        } catch (SQLException e) {
            System.out.println("Erreur connexion BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Singleton
    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    // Getter avec reconnexion automatique
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Reconnexion à la base...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.out.println("Erreur reconnection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}