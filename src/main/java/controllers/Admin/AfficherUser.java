package controllers.Admin;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class AfficherUser {

    @FXML
    private Label idLabel;

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label villeLabel;

    @FXML
    private Label actifLabel;

    @FXML
    private Label dateCreationLabel;

    @FXML
    private Label photoLabel;

    @FXML
    private Button backButton;

    @FXML
    private ImageView photoImageView;

    public void setUserData(User user) {
        idLabel.setText("ID: " + user.getId_user());
        nomLabel.setText("Nom: " + user.getNom());
        prenomLabel.setText("Prénom: " + user.getPrenom());
        emailLabel.setText("Email: " + user.getEmail());
        roleLabel.setText("Rôle: " + user.getRole());
        telephoneLabel.setText("Téléphone: " + (user.getTelephone() != null ? user.getTelephone() : "Non renseigné"));
        adresseLabel.setText("Adresse: " + (user.getAdresse() != null ? user.getAdresse() : "Non renseignée"));
        villeLabel.setText("Ville: " + (user.getVille() != null ? user.getVille() : "Non renseignée"));

        // Gestion du statut actif/inactif
        String statut = user.getActif() != null && user.getActif() ? "Actif" : "Inactif";
        actifLabel.setText("Statut: " + statut);

        dateCreationLabel.setText("Date d'inscription: " + user.getDate_creation());

        // Charger l'image de profil
        if (user.getPhoto_profil() != null && !user.getPhoto_profil().isEmpty()) {
            try {
                // Supposons que le chemin est relatif au projet ou absolu
                String imagePath = user.getPhoto_profil();
                Image image = new Image("file:" + imagePath, true); // Charger l'image en arrière-plan
                photoImageView.setImage(image);
                photoLabel.setVisible(false);
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
                photoLabel.setText("Photo: " + user.getPhoto_profil());
                photoImageView.setImage(null);
            }
        } else {
            photoLabel.setText("Photo: Non disponible");
            photoImageView.setImage(null);
        }
    }

    @FXML
    private void goBack() {
        try {
            // Charger la vue de la liste des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle et changer sa racine
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            Scene currentScene = currentStage.getScene();
            currentScene.setRoot(root);

            // Optionnel: mettre à jour le titre
            currentStage.setTitle("Liste des Utilisateurs");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}