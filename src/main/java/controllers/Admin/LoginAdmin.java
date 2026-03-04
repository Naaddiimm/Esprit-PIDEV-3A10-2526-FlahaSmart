package controllers.Admin;

import entities.Role;  // Import ajouté
import entities.User;
import services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginAdmin {
    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Text errorMessage;

    private UserService userService = new UserService();

    @FXML
    protected void handleTogglePasswordVisibility(ActionEvent event) {
        if (passwordField.isVisible()) {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordTextField.setVisible(true);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
        }
    }

    @FXML
    protected void handleLogin(ActionEvent event) {
        String enteredUsername = username.getText();
        String enteredPassword = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        // Validation des champs
        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showError("Veuillez saisir votre email et votre mot de passe.");
            return;
        }

        try {
            // Vérifier dans la base de données
            User authenticatedUser = userService.authenticate(enteredUsername, enteredPassword);

            if (authenticatedUser != null) {
                // Vérifier si l'utilisateur a le rôle ADMINISTRATEUR
                if (authenticatedUser.getRole() == Role.ADMINISTRATEUR) {  // CORRECTION ICI
                    // Vérifier si le compte est actif
                    if (authenticatedUser.getActif() != null && authenticatedUser.getActif()) {
                        // Connexion réussie - Redirection vers le tableau de bord
                        redirectToDashboard(event, authenticatedUser);
                    } else {
                        showError("Votre compte est désactivé. Veuillez contacter l'administrateur.");
                    }
                } else {
                    showError("Accès non autorisé. Vous devez être administrateur.");
                }
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données.");
        }
    }

    private void redirectToDashboard(ActionEvent event, User admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DashboardAdm.fxml"));
            Parent root = loader.load();

            // Si votre DashboardAdm a besoin de l'utilisateur connecté, vous pouvez passer les données
            // DashboardAdm controller = loader.getController();
            // controller.setLoggedInUser(admin);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement du tableau de bord.");
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);

        // Optionnel : utiliser aussi une alerte pour plus de visibilité
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de connexion");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleForgotPassword(ActionEvent event) {
        // Rediriger vers la page de réinitialisation de mot de passe
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}