package controllers.Dashbord;

import entities.User;
import entities.Role;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardHomeController implements Initializable {

    @FXML
    private Text welcomeText;

    @FXML
    private Text dateText;

    @FXML
    private Text totalUsersText;

    @FXML
    private Text totalAdminsText;

    @FXML
    private Text totalAgriculteursText;

    @FXML
    private Text totalClientsText;

    @FXML
    private PieChart rolePieChart;

    @FXML
    private LineChart<String, Number> inscriptionChart;

    @FXML
    private TableView<User> recentUsersTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colNom;

    @FXML
    private TableColumn<User, String> colPrenom;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, Role> colRole;

    @FXML
    private TableColumn<User, String> colDate;

    private UserService userService = new UserService();
    private User loggedInAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📊 Initialisation du Dashboard Home...");

        // Formater la date du jour
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        dateText.setText("Aujourd'hui, nous sommes le " + today.format(formatter));

        // Initialiser le tableau des derniers utilisateurs
        setupTableColumns();

        // Charger les données
        loadStatistics();
        loadPieChart();
        loadLineChart();
        loadRecentUsers();
    }

    public void setLoggedInUser(User user) {
        this.loggedInAdmin = user;
        if (user != null && welcomeText != null) {
            welcomeText.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom() + " !");
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Formatage de la date
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate_creation() != null) {
                String dateStr = cellData.getValue().getDate_creation().toString().substring(0, 10);
                return new javafx.beans.property.SimpleStringProperty(dateStr);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    private void loadStatistics() {
        try {
            List<User> allUsers = userService.getEntities();

            if (allUsers == null || allUsers.isEmpty()) {
                System.out.println("⚠️ Aucun utilisateur trouvé");
                return;
            }

            long totalUsers = allUsers.size();
            long totalAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMINISTRATEUR).count();
            long totalAgriculteurs = allUsers.stream().filter(u -> u.getRole() == Role.AGRICULTEUR).count();
            long totalClients = allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count();

            totalUsersText.setText(String.valueOf(totalUsers));
            totalAdminsText.setText(String.valueOf(totalAdmins));
            totalAgriculteursText.setText(String.valueOf(totalAgriculteurs));
            totalClientsText.setText(String.valueOf(totalClients));

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement statistiques: " + e.getMessage());
        }
    }

    private void loadPieChart() {
        try {
            List<User> allUsers = userService.getEntities();

            if (allUsers == null || allUsers.isEmpty()) {
                return;
            }

            long totalAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMINISTRATEUR).count();
            long totalAgriculteurs = allUsers.stream().filter(u -> u.getRole() == Role.AGRICULTEUR).count();
            long totalClients = allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count();

            rolePieChart.getData().clear();

            PieChart.Data slice1 = new PieChart.Data("Administrateurs (" + totalAdmins + ")",
                    totalAdmins > 0 ? totalAdmins : 0.1);
            PieChart.Data slice2 = new PieChart.Data("Agriculteurs (" + totalAgriculteurs + ")",
                    totalAgriculteurs > 0 ? totalAgriculteurs : 0.1);
            PieChart.Data slice3 = new PieChart.Data("Clients (" + totalClients + ")",
                    totalClients > 0 ? totalClients : 0.1);

            rolePieChart.getData().addAll(slice1, slice2, slice3);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement graphique: " + e.getMessage());
        }
    }

    private void loadLineChart() {
        try {
            // Créer une série pour les inscriptions
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Inscriptions");

            // Données fictives pour l'exemple
            series.getData().add(new XYChart.Data<>("Jan", 5));
            series.getData().add(new XYChart.Data<>("Fév", 8));
            series.getData().add(new XYChart.Data<>("Mar", 12));
            series.getData().add(new XYChart.Data<>("Avr", 10));
            series.getData().add(new XYChart.Data<>("Mai", 15));
            series.getData().add(new XYChart.Data<>("Juin", 20));

            inscriptionChart.getData().clear();
            inscriptionChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement line chart: " + e.getMessage());
        }
    }

    private void loadRecentUsers() {
        try {
            List<User> recentUsers = userService.getRecentUsers(10);
            if (recentUsers != null && !recentUsers.isEmpty()) {
                recentUsersTable.getItems().setAll(recentUsers);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement utilisateurs récents: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllUsers() {
        navigateToListUser();
    }

    @FXML
    private void handleViewAdmins() {
        navigateToListUserWithFilter("ADMINISTRATEUR");
    }

    @FXML
    private void handleViewAgriculteurs() {
        navigateToListUserWithFilter("AGRICULTEUR");
    }

    @FXML
    private void handleViewClients() {
        navigateToListUserWithFilter("CLIENT");
    }

    private void navigateToListUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) recentUsersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Liste des utilisateurs");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToListUserWithFilter(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
            Parent root = loader.load();

            // Ici vous pouvez passer le filtre au contrôleur ListUser si nécessaire
            // controller.setFilter(role);

            Stage stage = (Stage) recentUsersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FlahaSmart - Liste des " + role.toLowerCase() + "s");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}