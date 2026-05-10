package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scheduler.NotificationScheduler;
import tools.myConnection;
import utilies.MyDataBase;

import java.sql.Connection;

public class home extends Application {

    public static void main(String[] args) {
        // Tester la connexion au démarrage
        try {
            Connection cnx = myConnection.getInstance().getCnx();
            if (cnx != null) {
                System.out.println("✅ Base de données connectée");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion DB: " + e.getMessage());
        }

        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            NotificationScheduler.startDailyNotification();

            //stage.setFullScreen(true);
            //stage.setMaximized(true);
            scene.getStylesheets().add(
                    getClass().getResource("/styles/style_css.css").toExternalForm()
            );
            System.out.println("Test connexion: " + MyDataBase.getInstance().getConnection());

            stage.setTitle("FlahaSmart - Connexion");
            stage.show();

            System.out.println("✅ Application démarrée avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur de lecture FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}