package controllers.Dashbord;

import entities.User;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileUserController implements Initializable {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField villeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Text errorMessage;

    @FXML
    private Text successMessage;

    private User currentUser;
    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ ProfileUserController initialisé");
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
            adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
            villeField.setText(currentUser.getVille() != null ? currentUser.getVille() : "");
        }
    }

    @FXML
    private void handleSave() {
        // Validation
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom ne peut pas être vide");
            return;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom ne peut pas être vide");
            return;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("L'email ne peut pas être vide");
            return;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide");
            return;
        }

        // Mise à jour des informations
        currentUser.setNom(nomField.getText().trim());
        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim().isEmpty() ? null : telephoneField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim().isEmpty() ? null : adresseField.getText().trim());
        currentUser.setVille(villeField.getText().trim().isEmpty() ? null : villeField.getText().trim());

        // Changement de mot de passe si demandé
        if (!newPasswordField.getText().isEmpty()) {
            if (newPasswordField.getText().length() < 6) {
                showError("Le mot de passe doit contenir au moins 6 caractères");
                return;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showError("Les mots de passe ne correspondent pas");
                return;
            }

            currentUser.setPassword(newPasswordField.getText());
        }

        // Sauvegarde
        userService.updateEntity(currentUser);

        showSuccess("✅ Profil mis à jour avec succès !");

        // Fermeture automatique après 1.5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) nomField.getScene().getWindow();
                    stage.close();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
        successMessage.setVisible(false);
    }

    private void showSuccess(String message) {
        successMessage.setText(message);
        successMessage.setVisible(true);
        errorMessage.setVisible(false);
    }
}