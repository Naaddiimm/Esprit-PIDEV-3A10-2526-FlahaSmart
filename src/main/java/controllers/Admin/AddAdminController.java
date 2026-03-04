package controllers.Admin;

import entities.User;
import entities.Role;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddAdminController implements Initializable {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField villeField;

    @FXML
    private TextField adresseField;

    @FXML
    private Text errorMessage;

    @FXML
    private Text successMessage;

    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Formulaire d'ajout d'admin initialisé");
    }

    @FXML
    private void handleAddAdmin() {
        // Réinitialiser les messages
        errorMessage.setVisible(false);
        successMessage.setVisible(false);

        // Validation des champs
        if (!validateInputs()) {
            return;
        }

        // Vérifier si l'email existe déjà
        if (userService.getUserByEmail(emailField.getText().trim()) != null) {
            showError("Cet email est déjà utilisé");
            return;
        }

        // Créer le nouvel administrateur
        User admin = new User();
        admin.setNom(nomField.getText().trim());
        admin.setPrenom(prenomField.getText().trim());
        admin.setEmail(emailField.getText().trim());
        admin.setPassword(passwordField.getText()); // Sera haché par UserService
        admin.setTelephone(telephoneField.getText().trim().isEmpty() ? null : telephoneField.getText().trim());
        admin.setAdresse(adresseField.getText().trim().isEmpty() ? null : adresseField.getText().trim());
        admin.setVille(villeField.getText().trim());
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setActif(true);
        admin.setDate_creation(Timestamp.from(Instant.now()));

        // Ajouter à la base de données
        userService.addEntity(admin);

        // Message de succès
        showSuccess("✅ Administrateur ajouté avec succès !");

        // Vider les champs
        clearFields();

        // Fermer la fenêtre après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) nomField.getScene().getWindow();
                    stage.close();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean validateInputs() {
        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire");
            return false;
        }

        // Validation du prénom
        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire");
            return false;
        }

        // Validation de l'email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("L'email est obligatoire");
            return false;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            showError("Format d'email invalide");
            return false;
        }

        // Validation de la ville
        if (villeField.getText().trim().isEmpty()) {
            showError("La ville est obligatoire");
            return false;
        }

        // Validation du mot de passe
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showError("Le mot de passe est obligatoire");
            return false;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        // Validation de la confirmation
        if (!password.equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
            return false;
        }

        // Validation du téléphone (optionnel mais doit être valide)
        String telephone = telephoneField.getText().trim();
        if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
            showError("Le téléphone doit contenir 8 chiffres");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        villeField.clear();
        adresseField.clear();
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