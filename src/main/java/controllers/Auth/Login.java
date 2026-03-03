package controllers.Auth;

import controllers.Admin.ListUser;
import controllers.Dashbord.DashboardAdmController;
import controllers.Dashbord.ClientHomeController;
import entities.User;
import entities.Role;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Text errorMessage;

    @FXML
    private Button registerButton;

    // Champs pour le captcha
    @FXML
    private TextField captchaField;

    @FXML
    private ImageView captchaImageView;

    @FXML
    private Button refreshCaptchaButton;

    private UserService userService = new UserService();
    private String currentCaptchaCode;
    private int captchaAttempts = 0;
    private Random random = new Random();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Login controller initialisé");
        if (errorMessage != null) {
            errorMessage.setVisible(false);
        }

        // Générer un captcha au démarrage
        generateCaptcha();
    }

    /**
     * Génère un code captcha aléatoire (6-8 caractères)
     * Mélange de lettres majuscules et chiffres
     */
    private String generateCaptchaCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789@#$%&";
        StringBuilder code = new StringBuilder();

        // Longueur variable entre 6 et 8 caractères
        int length = 6 + random.nextInt(3);

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * Crée une image complexe du code captcha
     */
    private Image createComplexCaptchaImage(String code) {
        int width = 280;
        int height = 80;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond avec dégradé
        Color startColor = Color.rgb(240, 240, 240);
        Color endColor = Color.rgb(220, 220, 220);
        gc.setFill(new javafx.scene.paint.LinearGradient(0, 0, width, height, false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, startColor),
                new javafx.scene.paint.Stop(1, endColor)));
        gc.fillRect(0, 0, width, height);

        // Bordures
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, width-2, height-2);

        // Lignes de bruit courbes
        gc.setStroke(Color.rgb(150, 150, 150, 0.3));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 8; i++) {
            int startX = random.nextInt(width);
            int startY = random.nextInt(height);
            int endX = random.nextInt(width);
            int endY = random.nextInt(height);

            // Lignes courbes (bezier)
            double cp1x = startX + random.nextInt(50) - 25;
            double cp1y = startY + random.nextInt(50) - 25;
            double cp2x = endX + random.nextInt(50) - 25;
            double cp2y = endY + random.nextInt(50) - 25;

            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);
            gc.stroke();
        }

        // Lignes de bruit droites
        gc.setStroke(Color.rgb(100, 100, 100, 0.2));
        gc.setLineWidth(1);
        for (int i = 0; i < 15; i++) {
            gc.strokeLine(
                    random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height)
            );
        }

        // Points de bruit
        gc.setFill(Color.rgb(100, 100, 100, 0.3));
        for (int i = 0; i < 80; i++) {
            gc.fillOval(random.nextInt(width), random.nextInt(height), 2 + random.nextInt(3), 2 + random.nextInt(3));
        }

        // Dessiner chaque caractère avec des transformations aléatoires
        double startX = 30;
        double y = height / 2 + 10;

        for (int i = 0; i < code.length(); i++) {
            String charStr = String.valueOf(code.charAt(i));

            // Couleur aléatoire
            Color color = Color.rgb(
                    50 + random.nextInt(150),
                    50 + random.nextInt(150),
                    50 + random.nextInt(150)
            );
            gc.setFill(color);

            // Taille aléatoire
            int fontSize = 32 + random.nextInt(12);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, fontSize));

            // Rotation aléatoire (-15 à +15 degrés)
            double rotation = -15 + random.nextInt(30);

            // Sauvegarder l'état
            gc.save();

            // Appliquer la rotation autour du point du caractère
            gc.translate(startX + 10, y);
            gc.rotate(rotation);

            // Dessiner le caractère
            gc.fillText(charStr, 0, 0);

            // Restaurer l'état
            gc.restore();

            // Espacement variable
            startX += 20 + random.nextInt(15);
        }

        // Ajouter un effet de flou léger (simulé par des demi-teintes)
        gc.setGlobalAlpha(0.1);
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 20; i++) {
            gc.fillOval(random.nextInt(width), random.nextInt(height), 5, 5);
        }

        return canvas.snapshot(null, null);
    }

    /**
     * Génère un nouveau captcha
     */
    @FXML
    private void generateCaptcha() {
        currentCaptchaCode = generateCaptchaCode();
        System.out.println("🔐 Code captcha généré: " + currentCaptchaCode); // Pour debug

        if (captchaImageView != null) {
            Image captchaImage = createComplexCaptchaImage(currentCaptchaCode);
            captchaImageView.setImage(captchaImage);
        }
    }

    @FXML
    private void handleRefreshCaptcha() {
        generateCaptcha();
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String email = username.getText().trim();
        String password = passwordField.getText();
        String enteredCaptcha = captchaField != null ? captchaField.getText().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // Vérification du captcha
        if (captchaField != null) {
            if (enteredCaptcha.isEmpty()) {
                showError("Veuillez entrer le code de vérification.");
                generateCaptcha();
                return;
            }

            // Comparaison insensible à la casse
            if (!enteredCaptcha.equalsIgnoreCase(currentCaptchaCode)) {
                captchaAttempts++;
                showError("Code de vérification incorrect. (Tentative " + captchaAttempts + "/3)");
                generateCaptcha();

                if (captchaAttempts >= 3) {
                    showError("Trop de tentatives échouées. Réessayez plus tard.");
                    loginButton.setDisable(true);

                    // Réactiver après 30 secondes
                    new Thread(() -> {
                        try {
                            Thread.sleep(30000);
                            javafx.application.Platform.runLater(() -> {
                                loginButton.setDisable(false);
                                captchaAttempts = 0;
                                generateCaptcha();
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                return;
            }
        }

        try {
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            System.out.println("Utilisateur trouvé: " + user.getEmail());

            // Vérification du mot de passe
            boolean passwordMatches;

            if (user.getPassword().startsWith("$2a$")) {
                passwordMatches = BCrypt.checkpw(password, user.getPassword());
                System.out.println("Vérification BCrypt: " + passwordMatches);
            } else {
                passwordMatches = password.equals(user.getPassword());
                System.out.println("Vérification simple: " + passwordMatches);
            }

            if (!passwordMatches) {
                showError("Email ou mot de passe incorrect.");
                generateCaptcha();
                return;
            }

            if (user.getActif() == null || !user.getActif()) {
                showError("Votre compte est désactivé.");
                generateCaptcha();
                return;
            }

            // Réinitialiser les tentatives en cas de succès
            captchaAttempts = 0;
            errorMessage.setVisible(false);
            System.out.println("✅ Connexion réussie pour: " + email);

            redirectUser(user, event);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion: " + e.getMessage());
            generateCaptcha();
        }
    }

    private void redirectUser(User user, ActionEvent event) {
        try {
            Role role = user.getRole();
            Stage stage = (Stage) loginButton.getScene().getWindow();

            if (role == Role.ADMINISTRATEUR) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardAdm.fxml"));
                Parent root = loader.load();

                DashboardAdmController controller = loader.getController();
                controller.setLoggedInUser(user);

                stage.setScene(new Scene(root));
                stage.setTitle("FlahaSmart - Administration");

            } else if (role == Role.CLIENT) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHome.fxml"));
                Parent root = loader.load();

                ClientHomeController controller = loader.getController();
                controller.setLoggedInUser(user);

                stage.setScene(new Scene(root));
                stage.setTitle("FlahaSmart - Accueil");

            } else if (role == Role.AGRICULTEUR) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardAgriculteur.fxml"));
                Parent root = loader.load();

                stage.setScene(new Scene(root));
                stage.setTitle("FlahaSmart - Espace Agriculteur");

            } else {
                showError("Rôle non reconnu: " + role);
                return;
            }

            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de redirection: " + e.getMessage());
        }
    }

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

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisible(true);
        } else {
            System.err.println("❌ Erreur: " + message);
        }
    }
}