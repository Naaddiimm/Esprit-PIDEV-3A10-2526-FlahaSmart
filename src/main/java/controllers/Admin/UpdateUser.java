package controllers.Admin;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import entities.User;
import entities.Role;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class UpdateUser {

    @FXML
    private TextField idField;

    @FXML
    private TextField nomField, prenomField, emailField, telephoneField, adresseField, villeField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<Role> roleComboBox;  // ComboBox de type Role

    @FXML
    private CheckBox actifCheckBox;

    @FXML
    private ImageView photoView;

    @FXML
    private Button choosePhotoButton, updateButton;

    @FXML
    private Button backButton;

    @FXML
    private FontAwesomeIconView iconView;

    private User user;
    private UserService userService = new UserService();
    private String newPhotoPath;

    @FXML
    public void initialize() {
        // Configurer le champ ID pour qu'il soit en lecture seule et gris
        idField.setEditable(false);
        idField.setStyle("-fx-background-color: lightgray;");

        if (iconView != null) {
            iconView.setGlyphName("ARROW_LEFT");
            iconView.setSize("24");
        }

        // Remplir la ComboBox des rôles avec les valeurs de l'énumération
        roleComboBox.getItems().setAll(Role.values());
    }

    public void setUserData(User user) {
        if (user == null) {
            System.out.println("Erreur: utilisateur null");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non trouvé", "Impossible de charger les données de l'utilisateur.");
            return;
        }

        this.user = user;

        // Remplir les champs avec les données utilisateur
        idField.setText(String.valueOf(user.getId_user()));
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        telephoneField.setText(user.getTelephone());
        adresseField.setText(user.getAdresse());
        villeField.setText(user.getVille());

        // Gérer le rôle
        if (user.getRole() != null) {
            roleComboBox.setValue(user.getRole());
        }

        // Gérer le statut actif
        actifCheckBox.setSelected(user.getActif() != null ? user.getActif() : true);

        // Charger la photo
        if (user.getPhoto_profil() != null && !user.getPhoto_profil().isEmpty()) {
            try {
                Image image = loadImage(user.getPhoto_profil());
                if (image != null) {
                    photoView.setImage(image);
                }
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de la photo: " + e.getMessage());
            }
        }

        System.out.println("Données utilisateur chargées avec succès: " + user.getEmail());
    }

    private Image loadImage(String imagePath) {
        try {
            if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
                return new Image(imagePath, 150, 150, true, true);
            } else {
                File file = new File(imagePath);
                if (file.exists()) {
                    return new Image(file.toURI().toString(), 150, 150, true, true);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement image: " + e.getMessage());
        }
        return null;
    }

    @FXML
    private void updateUser() {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur", "Aucun utilisateur à mettre à jour.");
            return;
        }

        try {
            // Récupérer les données des champs
            String email = emailField.getText();
            String password = passwordField.getText();
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String telephone = telephoneField.getText();
            String adresse = adresseField.getText();
            String ville = villeField.getText();

            // CORRECTION ICI : Utiliser Role (pas User.Role)
            Role role = roleComboBox.getValue();

            Boolean actif = actifCheckBox.isSelected();

            // Validation des champs
            validateEmail(email);
            validatePassword(password);
            validateNom(nom);
            validatePrenom(prenom);
            validateTelephone(telephone);

            // Champs optionnels (peuvent être null)
            if (adresse != null && adresse.trim().isEmpty()) {
                adresse = null;
            }
            if (ville != null && ville.trim().isEmpty()) {
                ville = null;
            }

            // Validation du rôle
            if (role == null) {
                throw new IllegalArgumentException("Veuillez sélectionner un rôle.");
            }

            // Mettre à jour les champs de l'utilisateur
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setPassword(password);
            user.setTelephone(telephone);
            user.setAdresse(adresse);
            user.setVille(ville);
            user.setRole(role);      // Maintenant role est de type Role
            user.setActif(actif);

            // Mettre à jour la photo si une nouvelle a été choisie
            if (newPhotoPath != null && !newPhotoPath.isEmpty()) {
                user.setPhoto_profil(newPhotoPath);
            }

            // Mettre à jour l'utilisateur dans la base de données
            userService.updateEntity(user);  // Utilisez updateEntity au lieu de updateUser

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mise à jour réussie",
                    "L'utilisateur a été mis à jour avec succès !");

            // Retourner à la liste des utilisateurs
            goBack();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", null, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour", e.getMessage());
        }
    }

    // Méthodes de validation
    private void validateEmail(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir un email.");
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            throw new IllegalArgumentException("L'email n'est pas dans un format valide.");
        }
    }

    private void validatePassword(String password) throws IllegalArgumentException {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir un mot de passe.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
    }

    private void validateNom(String nom) throws IllegalArgumentException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir un nom.");
        }
    }

    private void validatePrenom(String prenom) throws IllegalArgumentException {
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new IllegalArgumentException("Veuillez saisir un prénom.");
        }
    }

    private void validateTelephone(String telephone) throws IllegalArgumentException {
        if (telephone != null && !telephone.trim().isEmpty()) {
            if (!telephone.matches("\\d{8}")) {
                throw new IllegalArgumentException("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            }
        }
    }

    @FXML
    private void choosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(photoView.getScene().getWindow());

        if (file != null) {
            try {
                // Sauvegarder le chemin de la nouvelle photo
                newPhotoPath = file.getAbsolutePath();

                // Afficher l'image dans l'ImageView
                Image image = new Image(file.toURI().toString());
                photoView.setImage(image);

                System.out.println("Photo sélectionnée: " + newPhotoPath);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                        "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.getScene().setRoot(root);
            currentStage.setTitle("Liste des Utilisateurs");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de navigation",
                    "Impossible de retourner à la liste des utilisateurs.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}