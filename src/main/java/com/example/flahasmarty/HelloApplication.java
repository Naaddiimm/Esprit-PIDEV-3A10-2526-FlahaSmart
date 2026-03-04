package com.example.flahasmarty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("Gestion - Articles & Commandes");

        // Load CSS with fallback
        String cssPath = "/com/example/flahasmarty/styles.css";
        URL cssUrl = HelloApplication.class.getResource(cssPath);
        if (cssUrl == null) {
            // Try alternative path
            cssPath = "/styles.css";
            cssUrl = HelloApplication.class.getResource(cssPath);
        }

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("[DEBUG] Main CSS loaded from: " + cssUrl);
        } else {
            System.out.println("[WARNING] Main CSS file not found!");
        }

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}