package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class MyDataBase {
    private final String URL = "jdbc:mysql://localhost:3306/flahasmart";
    private final String USER="root";
    private final String PASS="";
    private Connection connection;

    private static MyDataBase instance; //bch nasna3 objet (static ya3ni partager par tout les instances ya3ni ki tsir modification fi instance tsir fi lbe9i lkol

    // 1) rendre le constructeur private


    private MyDataBase() {
        try {
            connection=DriverManager.getConnection(URL,USER,PASS);
             System.out.println("Connected to database successfully");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    // 3ala5atr l objet static ly sna3tou hatitou private lezm naccédilou ken bl get donc lezm nasna3 l gettres mte3ha
    public static MyDataBase getInstance(){
        if (instance == null)
            instance = new MyDataBase();
            return  instance;
        }

    public Connection getConnection(){
     return connection;
    }

}
