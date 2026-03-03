package controllers.Auth;

import controllers.Dashbord.DashboardAdmController;
import controllers.Dashbord.ClientHomeController;
import entities.User;
import entities.Role;
import services.UserService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML private TextField     username;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Text          errorMessage;
    @FXML private Button        registerButton;
    @FXML private TextField     captchaField;
    @FXML private ImageView     captchaImageView;
    @FXML private Button        refreshCaptchaButton;

    private final UserService userService      = new UserService();
    private final Random      random           = new Random();
    private String            currentCaptchaCode;
    private int               captchaAttempts  = 0;

    // ─── Caractères sans ambiguïté visuelle (0/O, 1/I/l retirés)
    // FIX #5 : suppression des caractères spéciaux (@#$%&) impossibles
    //          à saisir sur certains claviers
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // ─────────────────────────────────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Login controller initialisé");
        if (errorMessage != null) {
            errorMessage.setVisible(false);
        }
        // FIX #3 : generateCaptcha() n'est plus @FXML ; appelée normalement
        generateCaptcha();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAPTCHA
    // ─────────────────────────────────────────────────────────────────────────

    /** Génère un code de 6 caractères alphanumériques sans ambiguïté. */
    private String generateCaptchaCode() {
        StringBuilder code = new StringBuilder();
        int length = 6 + random.nextInt(3); // 6 à 8 caractères
        for (int i = 0; i < length; i++) {
            code.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return code.toString();
    }

    /** Crée l'image bruitée du captcha sur un Canvas. */
    private Image createComplexCaptchaImage(String code) {
        int width = 280, height = 80;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond dégradé
        gc.setFill(new javafx.scene.paint.LinearGradient(0, 0, width, height, false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.rgb(240, 240, 240)),
                new javafx.scene.paint.Stop(1, Color.rgb(220, 220, 220))));
        gc.fillRect(0, 0, width, height);

        // Bordure
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, width - 2, height - 2);

        // Lignes courbes de bruit
        gc.setStroke(Color.rgb(150, 150, 150, 0.3));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 8; i++) {
            double sx = random.nextInt(width), sy = random.nextInt(height);
            double ex = random.nextInt(width), ey = random.nextInt(height);
            gc.beginPath();
            gc.moveTo(sx, sy);
            gc.bezierCurveTo(
                    sx + random.nextInt(50) - 25, sy + random.nextInt(50) - 25,
                    ex + random.nextInt(50) - 25, ey + random.nextInt(50) - 25,
                    ex, ey);
            gc.stroke();
        }

        // Points de bruit
        gc.setFill(Color.rgb(100, 100, 100, 0.3));
        for (int i = 0; i < 80; i++) {
            gc.fillOval(random.nextInt(width), random.nextInt(height),
                    2 + random.nextInt(3), 2 + random.nextInt(3));
        }

        // Caractères
        double startX = 30;
        double y      = height / 2.0 + 10;
        for (int i = 0; i < code.length(); i++) {
            gc.setFill(Color.rgb(
                    50 + random.nextInt(150),
                    50 + random.nextInt(150),
                    50 + random.nextInt(150)));
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 32 + random.nextInt(12)));
            gc.save();
            gc.translate(startX + 10, y);
            gc.rotate(-15 + random.nextInt(30));
            gc.fillText(String.valueOf(code.charAt(i)), 0, 0);
            gc.restore();
            startX += 20 + random.nextInt(15);
        }

        return canvas.snapshot(null, null);
    }

    // FIX #3 : @FXML retiré — méthode interne appelée depuis initialize()
    //          et depuis handleRefreshCaptcha()
    private void generateCaptcha() {
        currentCaptchaCode = generateCaptchaCode();
        if (captchaImageView != null) {
            captchaImageView.setImage(createComplexCaptchaImage(currentCaptchaCode));
        }
    }

    @FXML
    private void handleRefreshCaptcha() {
        generateCaptcha();
        if (captchaField != null) captchaField.clear();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX #1 : méthode manquante référencée dans le FXML
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleForgotPasswordAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Mot de passe oublié");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page de réinitialisation.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Connexion
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String email          = username.getText().trim();
        String password       = passwordField.getText();
        String enteredCaptcha = captchaField != null ? captchaField.getText().trim() : "";

        // Champs obligatoires
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // Vérification du CAPTCHA
        if (captchaField != null) {
            if (enteredCaptcha.isEmpty()) {
                showError("Veuillez entrer le code de vérification.");
                generateCaptcha();
                return;
            }

            // FIX #2 : equals() (sensible à la casse) au lieu de equalsIgnoreCase()
            if (!enteredCaptcha.equals(currentCaptchaCode)) {
                captchaAttempts++;
                generateCaptcha();

                if (captchaAttempts >= 3) {
                    showError("Trop de tentatives échouées. Réessayez dans 30 secondes.");
                    loginButton.setDisable(true);
                    refreshCaptchaButton.setDisable(true);

                    // FIX #4 : PauseTransition au lieu d'un Thread brut
                    PauseTransition pause = new PauseTransition(Duration.seconds(30));
                    pause.setOnFinished(e -> {
                        loginButton.setDisable(false);
                        refreshCaptchaButton.setDisable(false);
                        captchaAttempts = 0;
                        generateCaptcha();
                        errorMessage.setVisible(false);
                    });
                    pause.play();
                } else {
                    showError("Code de vérification incorrect. (Tentative "
                            + captchaAttempts + "/3)");
                }
                return;
            }
        }

        // Authentification
        try {
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            if (user.getPassword() == null || !user.getPassword().startsWith("$2a$")) {
                showError("Erreur : mot de passe non sécurisé en base. Contactez l'administrateur.");
                generateCaptcha();
                return;
            }

            if (!BCrypt.checkpw(password, user.getPassword())) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            if (user.getActif() == null || !user.getActif()) {
                showError("Votre compte est désactivé.");
                generateCaptcha();
                return;
            }

            // Succès
            captchaAttempts = 0;
            errorMessage.setVisible(false);
            Session.login(user);
            System.out.println("✅ Connexion réussie pour : " + email + " | Rôle : " + user.getRole());
            redirectUser(user, event);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion : " + e.getMessage());
            generateCaptcha();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Redirection selon le rôle
    // ─────────────────────────────────────────────────────────────────────────

    private void redirectUser(User user, ActionEvent event) {
        try {
            Role  role  = user.getRole();
            Stage stage = (Stage) loginButton.getScene().getWindow();

            switch (role) {
                case ADMINISTRATEUR -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardAdm.fxml"));
                    Parent root = loader.load();
                    ((DashboardAdmController) loader.getController()).setLoggedInUser(user);
                    stage.setScene(new Scene(root));
                    stage.setTitle("FlahaSmart - Administration");
                }
                case CLIENT -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHome.fxml"));
                    Parent root = loader.load();
                    ((ClientHomeController) loader.getController()).setLoggedInUser(user);
                    stage.setScene(new Scene(root));
                    stage.setTitle("FlahaSmart - Accueil");
                }
                case AGRICULTEUR -> {
                    Parent root = FXMLLoader.load(getClass().getResource("/DashboardAgriculteur.fxml"));
                    stage.setScene(new Scene(root));
                    stage.setTitle("FlahaSmart - Espace Agriculteur");
                }
                default -> {
                    showError("Rôle non reconnu : " + role);
                    return;
                }
            }

            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de redirection : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inscription
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Register.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Inscription");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'inscription.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaire
    // ─────────────────────────────────────────────────────────────────────────

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisible(true);
        } else {
            System.err.println("❌ Erreur : " + message);
        }
    }
}
