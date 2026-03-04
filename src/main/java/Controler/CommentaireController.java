package Controler;

import api.ApiClient;
import entities.Commentaire;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import servise.ServiceCommentaire;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentaireController {

    private final ServiceCommentaire service = new ServiceCommentaire();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private VBox commentairesContainer;
    private VBox notificationBox;
    private int  currentIdThread;

    public void afficher(Stage owner, int idThread, int idUser) {
        this.currentIdThread = idThread;

        Stage stage = new Stage();
        stage.setTitle("Commentaires — Thread #" + idThread);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        // Header
        Label headerTitle = new Label("💬  Commentaires — Thread #" + idThread);
        headerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(headerTitle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(56);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setStyle("-fx-background-color: #6C63FF;");

        // Liste commentaires
        commentairesContainer = new VBox(10);
        commentairesContainer.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(commentairesContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.setStyle("-fx-background-color: #F9FAFB; -fx-background: #F9FAFB; -fx-border-color: transparent;");

        // Notification box
        notificationBox = new VBox(6);
        notificationBox.setVisible(false);
        notificationBox.setManaged(false);

        // Formulaire
        Label lblAjouter = new Label("Votre commentaire");
        lblAjouter.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        TextArea champTexte = new TextArea();
        champTexte.setPromptText("Écrivez votre commentaire...");
        champTexte.setPrefRowCount(3);
        champTexte.setWrapText(true);
        champTexte.setStyle(styleNormal());

        Button btnPublier = new Button("＋  Publier");
        btnPublier.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 24 10 24; -fx-cursor: hand;");

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 24 10 24; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> stage.close());

        btnPublier.setOnAction(e -> {
            String texte = champTexte.getText().trim();

            champTexte.setStyle(styleNormal());
            cacherNotif();

            if (texte.isEmpty()) {
                champTexte.setStyle(styleErreur());
                afficherNotif("⚠️", "Champ vide", "Le commentaire ne peut pas être vide !", "warning");
                return;
            }

            btnPublier.setDisable(true);
            btnPublier.setText("⏳  Vérification...");

            new Thread(() -> {
                try {
                    ApiClient.ResultatAPI resultat = ApiClient.publierCommentaire(idThread, idUser, texte);

                    javafx.application.Platform.runLater(() -> {

                        if ("banni".equals(resultat.statut)) {
                            champTexte.setStyle(styleErreur());
                            afficherNotif("🚫", resultat.titreNotif, resultat.messageNotif, "ban");

                        } else {
                            champTexte.clear();
                            champTexte.setStyle(styleNormal());
                            afficherNotif("✅", "Publié !", "Commentaire publié avec succès !", "succes");
                            chargerCommentaires();
                        }

                        btnPublier.setDisable(false);
                        btnPublier.setText("＋  Publier");
                    });

                } catch (IOException ex) {
                    javafx.application.Platform.runLater(() -> {
                        afficherNotif("❌", "Erreur API",
                                "Impossible de contacter l'API : " + ex.getMessage(), "error");
                        btnPublier.setDisable(false);
                        btnPublier.setText("＋  Publier");
                    });
                }
            }).start();
        });

        HBox boutons = new HBox(12, btnFermer, btnPublier);
        boutons.setAlignment(Pos.CENTER_RIGHT);

        VBox formBox = new VBox(10, lblAjouter, champTexte, notificationBox, boutons);
        formBox.setPadding(new Insets(16, 24, 24, 24));
        formBox.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scroll);
        root.setBottom(formBox);
        root.setStyle("-fx-background-color: #F9FAFB;");

        stage.setScene(new Scene(root, 600, 620));
        chargerCommentaires();
        stage.showAndWait();
    }

    // =========================================================
    //  CHARGEMENT COMMENTAIRES
    // =========================================================
    private void chargerCommentaires() {
        commentairesContainer.getChildren().clear();
        try {
            List<Commentaire> liste = service.recupererParThread(currentIdThread);
            if (liste.isEmpty()) {
                Label vide = new Label("Aucun commentaire pour l'instant. Soyez le premier !");
                vide.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF;");
                VBox emptyBox = new VBox(vide);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(48));
                commentairesContainer.getChildren().add(emptyBox);
                return;
            }
            for (Commentaire c : liste) {
                commentairesContainer.getChildren().add(creerCarte(c));
            }
        } catch (SQLException e) {
            afficherNotif("❌", "Erreur", e.getMessage(), "error");
        }
    }

    // =========================================================
    //  CARTE COMMENTAIRE avec badge sentiment
    // =========================================================
    private VBox creerCarte(Commentaire c) {
        VBox carte = new VBox(6);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);" +
                "-fx-padding: 12 16 12 16;");

        Circle avatar = new Circle(13);
        avatar.setFill(Color.web("#E0E7FF"));
        avatar.setStroke(Color.web("#6C63FF"));
        avatar.setStrokeWidth(1.5);

        Label userLabel = new Label("User #" + c.getId_user());
        userLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4338CA;");

        // ===== BADGE SENTIMENT =====
        Label sentimentBadge = creerBadgeSentiment(c.getSentiment());

        Label dateLabel = new Label(c.getDate_creation().format(DATE_FMT));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnSuppr = new Button("✕");
        btnSuppr.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444;" +
                "-fx-font-size: 11px; -fx-cursor: hand;");
        btnSuppr.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer ce commentaire ?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        service.supprimer(c);
                        chargerCommentaires();
                    } catch (SQLException ex) {
                        afficherNotif("❌", "Erreur", ex.getMessage(), "error");
                    }
                }
            });
        });

        HBox headerRow = new HBox(8, avatar, userLabel, sentimentBadge, sp, dateLabel, btnSuppr);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label contenuLabel = new Label(c.getContenu());
        contenuLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        contenuLabel.setWrapText(true);
        contenuLabel.setMaxWidth(Double.MAX_VALUE);

        carte.getChildren().addAll(headerRow, contenuLabel);
        return carte;
    }

    // =========================================================
    //  BADGE SENTIMENT
    // =========================================================
    private Label creerBadgeSentiment(String sentiment) {
        String emoji, bg, color;

        if (sentiment == null) sentiment = "neutre";

        switch (sentiment.toLowerCase()) {
            case "positif":
                emoji = "😊 Positif"; bg = "#ECFDF5"; color = "#059669"; break;
            case "negatif":
                emoji = "😠 Négatif"; bg = "#FEF2F2"; color = "#EF4444"; break;
            default:
                emoji = "😐 Neutre";  bg = "#F3F4F6"; color = "#6B7280"; break;
        }

        Label badge = new Label(emoji);
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + color + ";" +
                "-fx-background-radius: 20; -fx-font-size: 11px;" +
                "-fx-font-weight: bold; -fx-padding: 3 10 3 10;");
        return badge;
    }

    // =========================================================
    //  NOTIFICATIONS
    // =========================================================
    private void afficherNotif(String icon, String titre, String message, String type) {
        notificationBox.getChildren().clear();

        String bg, border, color;
        switch (type) {
            case "ban":
                bg = "#FEF2F2"; border = "#EF4444"; color = "#991B1B"; break;
            case "warning":
                bg = "#FFFBEB"; border = "#F59E0B"; color = "#92400E"; break;
            case "succes":
                bg = "#ECFDF5"; border = "#10B981"; color = "#065F46"; break;
            default:
                bg = "#FEF2F2"; border = "#EF4444"; color = "#991B1B";
        }

        Label lblTitre = new Label(icon + "  " + titre);
        lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(520);

        notificationBox.getChildren().addAll(lblTitre, lblMsg);
        notificationBox.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + ";" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");
        notificationBox.setVisible(true);
        notificationBox.setManaged(true);
    }

    private void cacherNotif() {
        notificationBox.setVisible(false);
        notificationBox.setManaged(false);
    }

    private String styleNormal() {
        return "-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;";
    }

    private String styleErreur() {
        return "-fx-background-color: #FEF2F2; -fx-border-color: #EF4444;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;";
    }
}