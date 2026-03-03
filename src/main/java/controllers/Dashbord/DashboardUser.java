package controllers.Dashbord;

import entities.User;
import services.UserService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardUser implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label villeLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Text memberSinceText;

    @FXML
    private Button logoutButton;

    private User loggedInUser;
    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ DashboardUser initialisé");
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user != null) {
            loadUserData();
            System.out.println("✅ Utilisateur connecté: " + user.getEmail());
        }
    }

    private void loadUserData() {
        if (loggedInUser != null) {
            // Message de bienvenue
            String prenom = loggedInUser.getPrenom() != null ? loggedInUser.getPrenom() : "";
            String nom = loggedInUser.getNom() != null ? loggedInUser.getNom() : "";
            welcomeLabel.setText("Bienvenue, " + prenom + " " + nom + " !");

            // Informations personnelles
            nomLabel.setText(loggedInUser.getNom() != null ? loggedInUser.getNom() : "Non renseigné");
            prenomLabel.setText(loggedInUser.getPrenom() != null ? loggedInUser.getPrenom() : "Non renseigné");
            emailLabel.setText(loggedInUser.getEmail() != null ? loggedInUser.getEmail() : "Non renseigné");
            telephoneLabel.setText(loggedInUser.getTelephone() != null ? loggedInUser.getTelephone() : "Non renseigné");
            adresseLabel.setText(loggedInUser.getAdresse() != null ? loggedInUser.getAdresse() : "Non renseigné");
            villeLabel.setText(loggedInUser.getVille() != null ? loggedInUser.getVille() : "Non renseigné");

            // Rôle
            String role = loggedInUser.getRole() != null ? loggedInUser.getRole().toString() : "Client";
            userRoleLabel.setText(role);

            // Date d'inscription
            if (loggedInUser.getDate_creation() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                memberSinceText.setText(loggedInUser.getDate_creation().toLocalDateTime().format(formatter));
            } else {
                memberSinceText.setText("Date inconnue");
            }
        }
    }

    // MÉTHODE POUR MODIFIER LE PROFIL (ajoutée)
    @FXML
    private void handleEditProfile() {
        try {
            // Charger la fenêtre de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté au contrôleur
            ProfileUserController controller = loader.getController();
            controller.setUser(loggedInUser);

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Modifier mon profil");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre parente
            stage.setResizable(false);

            // Animation d'ouverture
            root.setOpacity(0);
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Attendre que la fenêtre soit fermée
            stage.setOnHidden(e -> {
                // Recharger les données après modification
                User updatedUser = userService.getUserById(loggedInUser.getId_user());
                if (updatedUser != null) {
                    loggedInUser = updatedUser;
                    loadUserData();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Animation de sortie
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), welcomeLabel.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
                stage.setMaximized(true);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }

}