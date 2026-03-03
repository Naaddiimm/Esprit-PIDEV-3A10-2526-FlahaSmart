package controllers.Auth;

import entities.User;
import services.EmailService;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.ResourceBundle;

public class ForgotPassword implements Initializable {

    // ── Étape 1 : saisie de l'email ──
    @FXML private VBox stepEmail;
    @FXML private TextField emailField;
    @FXML private Button sendCodeButton;

    // ── Étape 2 : saisie du code ──
    @FXML private VBox stepCode;
    @FXML private TextField tokenField;
    @FXML private Button verifyCodeButton;

    // ── Étape 3 : nouveau mot de passe ──
    @FXML private VBox stepPassword;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Button resetButton;

    // ── Messages ──
    @FXML private Text errorMessage;
    @FXML private Text successMessage;

    @FXML private Button backToLoginButton;

    private final UserService userService = new UserService();
    private String currentEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showStep(1);
        hideMessages();
    }

    // ─── Étape 1 : Envoyer le code ────────────────────────────────────────────

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez saisir votre adresse email.");
            return;
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            // Message volontairement vague pour la sécurité
            showError("Si cet email est associé à un compte, vous recevrez un code.");
            return;
        }

        currentEmail = email;
        sendCodeButton.setDisable(true);
        sendCodeButton.setText("Envoi en cours...");

        // Envoyer le code par email
        EmailService.sendPasswordResetEmail(email, user.getPrenom());

        javafx.application.Platform.runLater(() -> {
            sendCodeButton.setDisable(false);
            sendCodeButton.setText("Renvoyer le code");
            showSuccess("Code envoyé ! Vérifiez votre boîte email.");
            showStep(2);
        });
    }

    // ─── Étape 2 : Vérifier le code ───────────────────────────────────────────

    @FXML
    private void handleVerifyCode() {
        String token = tokenField.getText().trim();

        if (token.isEmpty()) {
            showError("Veuillez entrer le code reçu par email.");
            return;
        }

        if (!token.matches("\\d{6}")) {
            showError("Le code doit contenir 6 chiffres.");
            return;
        }

        boolean valid = EmailService.verifyResetToken(currentEmail, token);

        if (!valid) {
            showError("Code incorrect ou expiré. Veuillez en demander un nouveau.");
            return;
        }

        showSuccess("Code validé ✓");
        showStep(3);
    }

    // ─── Étape 3 : Nouveau mot de passe ───────────────────────────────────────

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirm    = confirmNewPasswordField.getText();

        if (newPassword.isEmpty()) {
            showError("Veuillez saisir un nouveau mot de passe.");
            return;
        }
        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!newPassword.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            User user = userService.getUserByEmail(currentEmail);
            if (user == null) {
                showError("Utilisateur introuvable.");
                return;
            }

            // Hasher le nouveau mot de passe
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            user.setPassword(hashed);
            userService.updateEntity(user);

            showSuccess("✅ Mot de passe réinitialisé avec succès !");
            resetButton.setDisable(true);

            // Retourner au login après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::navigateToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    // ─── Navigation entre étapes ──────────────────────────────────────────────

    private void showStep(int step) {
        if (stepEmail    != null) stepEmail.setVisible(step == 1);
        if (stepCode     != null) stepCode.setVisible(step == 2);
        if (stepPassword != null) stepPassword.setVisible(step == 3);

        if (stepEmail    != null) stepEmail.setManaged(step == 1);
        if (stepCode     != null) stepCode.setManaged(step == 2);
        if (stepPassword != null) stepPassword.setManaged(step == 3);
    }

    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Connexion");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String msg) {
        hideMessages();
        if (errorMessage != null) {
            errorMessage.setText("⚠ " + msg);
            errorMessage.setVisible(true);
        }
    }

    private void showSuccess(String msg) {
        hideMessages();
        if (successMessage != null) {
            successMessage.setText(msg);
            successMessage.setVisible(true);
        }
    }

    private void hideMessages() {
        if (errorMessage   != null) errorMessage.setVisible(false);
        if (successMessage != null) successMessage.setVisible(false);
    }
}
