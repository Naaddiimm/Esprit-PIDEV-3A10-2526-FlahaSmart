package controllers.Dashbord;

import entities.User;
import javafx.scene.control.Alert;
import org.w3c.dom.Text;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientHomeController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Text welcomeText;

    private User loggedInUser;
    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Client Home initialisé");
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userEmailLabel.setText(user.getEmail());
            if (welcomeText != null) {
                welcomeText.setTextContent("Bienvenue sur FlahaSmart, " + user.getPrenom() + " !");
            }
        }
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        System.out.println("Navigation vers Accueil");
    }

    @FXML
    private void handleMarketplace(ActionEvent event) {
        System.out.println("Navigation vers Marketplace");
        showNotification("Marketplace", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        System.out.println("Navigation vers Forum");
        showNotification("Forum", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleActualites(ActionEvent event) {
        System.out.println("Navigation vers Actualités");
        showNotification("Actualités", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleContact(ActionEvent event) {
        System.out.println("Navigation vers Contact");
        showNotification("Contact", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleNotifications(MouseEvent event) {
        System.out.println("Notifications");
        showNotification("Notifications", "Vous n'avez pas de nouvelles notifications");
    }

    @FXML
    private void handleOpenDashboard(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardUser.fxml"));
            Parent root = loader.load();

            DashboardUser controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Dashboard");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}