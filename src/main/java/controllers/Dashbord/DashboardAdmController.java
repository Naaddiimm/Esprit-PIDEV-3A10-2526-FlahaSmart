package controllers.Dashbord;

import controllers.Admin.ListUser;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardAdmController implements Initializable {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label currentDateLabel;

    @FXML
    private Label currentViewLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button addAdminBtn;

    @FXML
    private Button statsBtn;

    @FXML
    private Button marketplaceBtn;

    @FXML
    private Button forumBtn;

    @FXML
    private Button productsBtn;

    @FXML
    private Button ordersBtn;

    @FXML
    private Button settingsBtn;

    private User loggedInAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📊 Dashboard Admin initialisé avec menu latéral");

        try {
            // Afficher la date du jour
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
            if (currentDateLabel != null) {
                currentDateLabel.setText(today.format(formatter));
            }

            // Charger la vue par défaut
            loadView("/ListUser.fxml");
            updateCurrentView("Liste des utilisateurs");

        } catch (Exception e) {
            System.err.println("❌ Erreur dans initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInAdmin = user;
        if (user != null && adminNameLabel != null) {
            adminNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView("/DashboardHome.fxml");
        updateCurrentView("Tableau de bord");
    }

    @FXML
    private void handleListUsers(ActionEvent event) {
        loadView("/ListUser.fxml");
        updateCurrentView("Liste des utilisateurs");
    }

    @FXML
    private void handleAddAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un administrateur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Rafraîchir la liste des utilisateurs si elle est affichée
            refreshCurrentView();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void handleStats(ActionEvent event) {
        loadView("/Statistiques.fxml");
        updateCurrentView("Statistiques");
    }

    @FXML
    private void handleMarketplace(ActionEvent event) {
        showAlert("Marketplace", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        showAlert("Forum", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleProducts(ActionEvent event) {
        showAlert("Produits", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleOrders(ActionEvent event) {
        showAlert("Commandes", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showAlert("Paramètres", "Cette fonctionnalité sera bientôt disponible");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginAdmin.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Connexion Admin");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            // Si c'est ListUser, passer l'utilisateur connecté
            if (fxmlFile.equals("/ListUser.fxml") && loggedInAdmin != null) {
                Object controller = loader.getController();
                if (controller instanceof ListUser) {
                    ((ListUser) controller).setLoggedInUser(loggedInAdmin);
                }
            }

            contentPane.getChildren().setAll(view);
            System.out.println("✅ Vue chargée: " + fxmlFile);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + ": " + e.getMessage());
            showAlert("Erreur", "Impossible de charger " + fxmlFile);
        }
    }

    private void refreshCurrentView() {
        if (currentViewLabel != null) {
            String currentView = currentViewLabel.getText();
            if ("Liste des utilisateurs".equals(currentView)) {
                loadView("/ListUser.fxml");
            }
        }
    }

    private void updateCurrentView(String viewName) {
        if (currentViewLabel != null) {
            currentViewLabel.setText(viewName);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}