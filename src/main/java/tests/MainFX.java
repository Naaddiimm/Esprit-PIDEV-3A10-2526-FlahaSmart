package tests;

import api.ModerationAPI;
import Controler.ThreadController;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Démarrer l'API REST au lancement de l'application
        try {
            ModerationAPI.demarrer();
        } catch (Exception e) {
            System.out.println("⚠️ Impossible de démarrer l'API : " + e.getMessage());
        }

        new ThreadController().afficher(primaryStage);
    }

    @Override
    public void stop() {
        // Arrêter l'API proprement à la fermeture
        ModerationAPI.arreter();
        System.out.println("API arrêtée.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}