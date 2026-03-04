package controllers.Dashbord;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardAgriculteurController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Text welcomeText;
    @FXML private Text dateText;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private StackPane contentArea;

    private User loggedInUser;


    // Méthode pour recevoir l'utilisateur connecté
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("✅ Utilisateur reçu dans DashboardAgriculteurController: " +
                (user != null ? user.getEmail() : "NULL"));

        if (user != null) {
            if (welcomeLabel != null) {
                welcomeLabel.setText(user.getPrenom() + " " + user.getNom());
            }
            if (welcomeText != null) {
                welcomeText.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom() + " !");
            }
        }
    }

    private void loadView(String fxmlPath) {
        if (contentArea == null) {
            System.err.println("❌ contentArea est null");
            return;
        }

        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("⚠️ Vue non trouvée: " + fxmlPath);
                showPlaceholder(fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent view = loader.load();

            // Passer l'utilisateur si le contrôleur a la méthode setLoggedInUser
            Object controller = loader.getController();
            if (controller != null && loggedInUser != null) {
                try {
                    controller.getClass().getMethod("setLoggedInUser", User.class)
                            .invoke(controller, loggedInUser);
                } catch (Exception ignored) {}
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            System.out.println("✅ Vue chargée: " + fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholder(fxmlPath);
        }
    }

    private void showPlaceholder(String fxmlPath) {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-padding: 50;");

        Label title = new Label("🚧 Vue en construction");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        Label info = new Label("Fichier: " + fxmlPath);
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");

        placeholder.getChildren().addAll(title, info);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(placeholder);
    }

    // Handlers de navigation
    @FXML public void handleAccueil(ActionEvent e) { loadView("/views/agriculteur/Accueil.fxml"); setStatus("Accueil"); }
    @FXML public void handleStatistiques(ActionEvent e) { loadView("/views/agriculteur/Statistiques.fxml"); setStatus("Statistiques"); }
    @FXML public void handleParametres(ActionEvent e) { loadView("/views/agriculteur/Parametres.fxml"); setStatus("Paramètres"); }
    @FXML public void handleMeteo(ActionEvent e) { loadView("/views/agriculteur/Meteo.fxml"); setStatus("Météo"); }

    // Opérations
    @FXML public void handleAjouterOperation(ActionEvent e) { loadView("/views/agriculteur/AjouterOperation.fxml"); setStatus("Ajouter opération"); }
    @FXML public void handleListeOperations(ActionEvent e) { loadView("/views/agriculteur/ListeOperations.fxml"); setStatus("Liste opérations"); }
    @FXML public void handleAjouterEquipement(ActionEvent e) { loadView("/views/agriculteur/AjouterEquipement.fxml"); setStatus("Ajouter équipement"); }
    @FXML public void handleListeEquipements(ActionEvent e) { loadView("/views/agriculteur/ListeEquipements.fxml"); setStatus("Liste équipements"); }

    // Outils
    @FXML public void handleAgriBot(ActionEvent e) { loadView("/views/agriculteur/AgriBot.fxml"); setStatus("AgriBot"); }
    @FXML public void handleAnalyseMaladie(ActionEvent e) { loadView("/views/agriculteur/AnalyseMaladie.fxml"); setStatus("Analyse Maladie"); }
    @FXML public void handleIrrigation(ActionEvent e) { loadView("/views/agriculteur/Irrigation.fxml"); setStatus("Irrigation"); }
    @FXML public void handleRotationCultures(ActionEvent e) { loadView("/views/agriculteur/RotationCultures.fxml"); setStatus("Rotation Cultures"); }

    // PROFIL - CORRIGÉ
    @FXML
    public void handleProfil(ActionEvent event) {
        try {
            System.out.println("🟢 Ouverture du profil...");

            if (loggedInUser == null) {
                System.out.println("❌ loggedInUser est NULL !");
                return;
            }

            System.out.println("👤 Utilisateur: " + loggedInUser.getPrenom() + " " + loggedInUser.getNom());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardUser.fxml"));
            Parent profilView = loader.load();

            DashboardUser controller = loader.getController();
            if (controller != null) {
                controller.setLoggedInUser(loggedInUser);
                System.out.println("✅ Utilisateur passé à DashboardUser");
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(profilView);
            setStatus("Mon Profil");

        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MODIFIER PROFIL
    @FXML
    public void handleEditProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent editView = loader.load();

            ProfileUserController controller = loader.getController();
            if (controller != null && loggedInUser != null) {
                controller.setUser(loggedInUser);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(editView);
            setStatus("Modifier Profil");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // DÉCONNEXION
    @FXML
    public void handleDeconnexion(ActionEvent event) {
        System.out.println("🚪 Déconnexion");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("FlahaSmart - Connexion");
                stage.setMaximized(true);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ DashboardAgriculteurController initialisé");

        // Log pour voir si l'utilisateur est déjà là
        if (loggedInUser == null) {
            System.out.println("⚠️ loggedInUser est NULL dans initialize");
        } else {
            System.out.println("👤 loggedInUser présent: " + loggedInUser.getEmail());
        }

        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH));

        if (dateText != null) dateText.setText(today);
        if (dateLabel != null) dateLabel.setText(today);

        loadView("/views/agriculteur/Accueil.fxml");
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
        System.out.println("📌 Section: " + msg);
    }

    private Stage getStage() {
        if (contentArea != null && contentArea.getScene() != null)
            return (Stage) contentArea.getScene().getWindow();
        return null;
    }
}