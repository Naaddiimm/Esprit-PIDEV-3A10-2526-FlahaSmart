package controllers.Dashbord;

import entities.User;
import entities.Role;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {

    @FXML
    private BarChart<String, Number> usersBarChart;

    @FXML
    private Label totalUsersStat;

    @FXML
    private Label totalAdminsStat;

    @FXML
    private Label totalAgriculteursStat;

    @FXML
    private Label totalClientsStat;

    @FXML
    private Label activeUsersStat;

    @FXML
    private Label inactiveUsersStat;

    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📈 Initialisation des statistiques...");
        loadStatistics();
        loadBarChart();
    }

    private void loadStatistics() {
        try {
            // CORRECTION: getAllUsers() -> getEntities()
            List<User> allUsers = userService.getEntities();

            long totalUsers = allUsers.size();
            long totalAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMINISTRATEUR).count();
            long totalAgriculteurs = allUsers.stream().filter(u -> u.getRole() == Role.AGRICULTEUR).count();
            long totalClients = allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count();
            long activeUsers = allUsers.stream().filter(u -> u.getActif() != null && u.getActif()).count();
            long inactiveUsers = totalUsers - activeUsers;

            totalUsersStat.setText(String.valueOf(totalUsers));
            totalAdminsStat.setText(String.valueOf(totalAdmins));
            totalAgriculteursStat.setText(String.valueOf(totalAgriculteurs));
            totalClientsStat.setText(String.valueOf(totalClients));
            activeUsersStat.setText(String.valueOf(activeUsers));
            inactiveUsersStat.setText(String.valueOf(inactiveUsers));

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement statistiques: " + e.getMessage());
        }
    }

    private void loadBarChart() {
        try {
            // CORRECTION: getAllUsers() -> getEntities()
            List<User> allUsers = userService.getEntities();

            long totalAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMINISTRATEUR).count();
            long totalAgriculteurs = allUsers.stream().filter(u -> u.getRole() == Role.AGRICULTEUR).count();
            long totalClients = allUsers.stream().filter(u -> u.getRole() == Role.CLIENT).count();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Utilisateurs");

            if (totalAdmins > 0) series.getData().add(new XYChart.Data<>("Administrateurs", totalAdmins));
            if (totalAgriculteurs > 0) series.getData().add(new XYChart.Data<>("Agriculteurs", totalAgriculteurs));
            if (totalClients > 0) series.getData().add(new XYChart.Data<>("Clients", totalClients));

            usersBarChart.getData().clear();
            usersBarChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement graphique: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement des statistiques...");
        usersBarChart.getData().clear();
        loadStatistics();
        loadBarChart();
    }
}