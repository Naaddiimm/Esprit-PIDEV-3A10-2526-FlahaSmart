package Controler;

import entities.thread;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import servise.ServiceThreads;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ModifierThreadController {

    private final ServiceThreads service = new ServiceThreads();

    public void afficher(Stage owner, ThreadController parent, thread threadCourant) {
        Stage stage = new Stage();
        stage.setTitle("Modifier le Thread");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        // Header orange
        Rectangle logoRect = new Rectangle(28, 28);
        logoRect.setArcWidth(6); logoRect.setArcHeight(6);
        logoRect.setFill(Color.WHITE);
        Label headerTitle = new Label("Modifier le Thread");
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(12, logoRect, headerTitle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(60);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setStyle("-fx-background-color: #F59E0B;");

        // ID
        Label idLabel = new Label("Thread #" + threadCourant.getId_thread());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        // Titre
        Label lblTitre = new Label("Titre *");
        lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField titreField = new TextField(threadCourant.getTitre());
        titreField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 10 14 10 14;");
        VBox vTitre = new VBox(6, lblTitre, titreField);

        // Contenu
        Label lblContenu = new Label("Contenu *");
        lblContenu.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextArea contenuField = new TextArea(threadCourant.getContenu());
        contenuField.setPrefRowCount(6);
        contenuField.setWrapText(true);
        contenuField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;");
        VBox vContenu = new VBox(6, lblContenu, contenuField);

        Label messageLabel = new Label("");
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #EF4444;");
        messageLabel.setWrapText(true);

        // Boutons
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 24 10 24; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> stage.close());

        Button btnModifier = new Button("✎  Enregistrer");
        btnModifier.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 24 10 24; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> {
            String titre   = titreField.getText().trim();
            String contenu = contenuField.getText().trim();
            if (titre.isEmpty()) {
                messageLabel.setText("⚠  Le titre est obligatoire !");
                return;
            }
            if (contenu.isEmpty()) {
                messageLabel.setText("⚠  Le contenu est obligatoire !");
                return;
            }
            threadCourant.setTitre(titre);
            threadCourant.setContenu(contenu);
            threadCourant.setDate_update(LocalDateTime.now());
            try {
                service.modifier(threadCourant);
                parent.chargerThreads();
                parent.showMessage("✓  Thread modifié avec succès !", "#059669");
                stage.close();
            } catch (IllegalArgumentException ex) {
                messageLabel.setText("⚠  " + ex.getMessage());
            } catch (SQLException ex) {
                messageLabel.setText("⚠  Erreur BD : " + ex.getMessage());
            }
        });

        HBox buttons = new HBox(12, btnAnnuler, btnModifier);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(20, idLabel, vTitre, vContenu, messageLabel, buttons);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: white;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);

        stage.setScene(new Scene(root, 600, 520));
        stage.showAndWait();
    }
}