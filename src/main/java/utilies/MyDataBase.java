package utilies;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    final String URL = "jdbc:mysql://127.0.0.1:3306/flahasmart";
    final String USER = "root";
    final String PASSWORD = "";

    private Connection connection;
    private static MyDataBase instance;

    private MyDataBase() {}   // constructeur vide

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to database successfully");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return connection;
    }
}
