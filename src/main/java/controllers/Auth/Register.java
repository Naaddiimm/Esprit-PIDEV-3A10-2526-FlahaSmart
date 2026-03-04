package controllers.Auth;

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
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import javafx.stage.Modality;

public class Register implements Initializable {

    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField email;
    @FXML
    private TextField telephone;
    @FXML
    private TextField adresse;
    @FXML
    private TextField ville;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private CheckBox termsCheckBox;
    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;
    @FXML
    private Button mapButton;
    @FXML
    private Text errorMessage;
    @FXML
    private Text passwordError;

    private Stage mapStage;
    private UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Register controller initialisé");

        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("Agriculteur", "Client");
            roleComboBox.setValue("Client");
        }

        if (registerButton != null) {
            registerButton.setDisable(true);
        }

        if (termsCheckBox != null) {
            termsCheckBox.selectedProperty().addListener((obs, old, newValue) -> {
                if (registerButton != null) {
                    registerButton.setDisable(!newValue);
                }
            });
        }

        if (errorMessage != null) errorMessage.setVisible(false);
        if (passwordError != null) passwordError.setVisible(false);
    }

    @FXML
    private void handleOpenMap() {
        try {
            mapStage = new Stage();
            mapStage.setTitle("Sélectionnez votre adresse");
            mapStage.initModality(Modality.APPLICATION_MODAL);
            mapStage.setWidth(900);
            mapStage.setHeight(650);

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            URL mapUrl = getClass().getResource("/map.html");
            if (mapUrl == null) {
                showError("Fichier map.html non trouvé !");
                return;
            }

            webEngine.load(mapUrl.toExternalForm());

            webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
                if (newVal == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("java", this);
                    System.out.println("✅ Objet Java exposé à JavaScript");
                }
            });

            Scene scene = new Scene(webView);
            mapStage.setScene(scene);
            mapStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur carte: " + e.getMessage());
        }
    }

    public void setSelectedLocation(double latitude, double longitude) {
        System.out.println("📍 Coordonnées reçues: " + latitude + ", " + longitude);

        // Récupérer l'adresse en français
        String address = getAddressInFrench(latitude, longitude);

        javafx.application.Platform.runLater(() -> {
            adresse.setText(address);

            // Extraire la ville en français
            String city = extractCityInFrench(address);
            if (city != null && !city.isEmpty()) {
                ville.setText(city);
                System.out.println("🏙️ Ville en français: " + city);
            }

            if (mapStage != null) {
                mapStage.close();
            }
        });
    }

    /**
     * Récupère l'adresse en français depuis les coordonnées
     */
    private String getAddressInFrench(double lat, double lon) {
        try {
            // Paramètres pour obtenir l'adresse en français
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&accept-language=fr";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "FlahaSmart/1.0");
            conn.setRequestProperty("Accept-Language", "fr");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());

                // Extraire les informations d'adresse en français
                JSONObject addressJson = json.optJSONObject("address");
                if (addressJson != null) {
                    // Construire une adresse française lisible
                    StringBuilder frenchAddress = new StringBuilder();

                    String road = addressJson.optString("road", "");
                    String houseNumber = addressJson.optString("house_number", "");
                    String city = addressJson.optString("city", "");
                    if (city.isEmpty()) city = addressJson.optString("town", "");
                    if (city.isEmpty()) city = addressJson.optString("village", "");
                    String county = addressJson.optString("county", "");
                    String state = addressJson.optString("state", "");
                    String country = addressJson.optString("country", "");

                    // Construire l'adresse formatée à la française
                    if (!houseNumber.isEmpty() && !road.isEmpty()) {
                        frenchAddress.append(houseNumber).append(" ").append(road);
                    } else if (!road.isEmpty()) {
                        frenchAddress.append(road);
                    }

                    if (!city.isEmpty()) {
                        if (frenchAddress.length() > 0) frenchAddress.append(", ");
                        frenchAddress.append(city);
                    } else if (!county.isEmpty()) {
                        if (frenchAddress.length() > 0) frenchAddress.append(", ");
                        frenchAddress.append(county);
                    }

                    if (!state.isEmpty() && !state.equals(city)) {
                        if (frenchAddress.length() > 0) frenchAddress.append(", ");
                        frenchAddress.append(state);
                    }

                    if (!country.isEmpty()) {
                        if (frenchAddress.length() > 0) frenchAddress.append(", ");
                        frenchAddress.append(country);
                    }

                    // Mettre à jour la ville directement
                    if (!city.isEmpty()) {
                        String finalCity = city;
                        javafx.application.Platform.runLater(() -> {
                            ville.setText(finalCity);
                        });
                    }

                    if (frenchAddress.length() > 0) {
                        return frenchAddress.toString();
                    }
                }

                return json.optString("display_name", "Adresse inconnue");
            }
            return "Adresse inconnue";

        } catch (Exception e) {
            e.printStackTrace();
            return "Adresse inconnue";
        }
    }

    /**
     * Extrait la ville en français de l'adresse
     */
    private String extractCityInFrench(String fullAddress) {
        if (fullAddress == null || fullAddress.isEmpty()) return "";

        try {
            // Séparer par les virgules
            String[] parts = fullAddress.split(",");

            // Chercher la ville (souvent avant le pays)
            for (int i = parts.length - 2; i >= 0; i--) {
                String part = parts[i].trim();
                // Ignorer les codes postaux et les petits mots
                if (!part.matches("\\d+") && part.length() > 2 && part.length() < 40) {
                    return part;
                }
            }

            return parts[0].trim();

        } catch (Exception e) {
            return "";
        }
    }

    @FXML
    private void handleRegister() {
        if (!validateInputs()) {
            return;
        }

        try {
            String emailText = email.getText().trim();

            User existingUser = userService.getUserByEmail(emailText);
            if (existingUser != null) {
                showError("Cet email est déjà utilisé.");
                return;
            }

            String selectedRoleText = roleComboBox.getValue();
            Role selectedRole = "Agriculteur".equals(selectedRoleText) ?
                    Role.AGRICULTEUR : Role.CLIENT;

            User user = new User();
            user.setNom(nom.getText().trim());
            user.setPrenom(prenom.getText().trim());
            user.setEmail(emailText);
            // Hash BCrypt
            user.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(12)));
            user.setTelephone(telephone.getText().trim().isEmpty() ? null : telephone.getText().trim());
            user.setAdresse(adresse.getText().trim().isEmpty() ? null : adresse.getText().trim());
            user.setVille(ville.getText().trim());
            user.setRole(selectedRole);
            user.setActif(true);
            user.setDate_creation(Timestamp.from(Instant.now()));
            user.setPhoto_profil(null);

            userService.addEntity(user);

            // Email de bienvenue
            EmailService.sendWelcomeEmail(emailText, nom.getText().trim(), selectedRole.name());

            showSuccess("✅ Inscription réussie ! Un email de bienvenue vous a été envoyé.");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> navigateToLogin());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        errorMessage.setVisible(false);
        passwordError.setVisible(false);

        if (nom.getText() == null || nom.getText().trim().isEmpty()) {
            showError("Veuillez saisir votre nom.");
            return false;
        }

        if (prenom.getText() == null || prenom.getText().trim().isEmpty()) {
            showError("Veuillez saisir votre prénom.");
            return false;
        }

        String emailText = email.getText();
        if (emailText == null || emailText.trim().isEmpty()) {
            showError("Veuillez saisir votre email.");
            return false;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", emailText)) {
            showError("Format d'email invalide.");
            return false;
        }

        String password = passwordField.getText();
        if (password == null || password.isEmpty()) {
            showError("Veuillez saisir un mot de passe.");
            return false;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }

        if (!password.equals(confirmPasswordField.getText())) {
            passwordError.setText("Les mots de passe ne correspondent pas.");
            passwordError.setVisible(true);
            return false;
        }

        String phone = telephone.getText();
        if (phone != null && !phone.trim().isEmpty() && !phone.matches("\\d{8}")) {
            showError("Le téléphone doit contenir 8 chiffres.");
            return false;
        }

        if (ville.getText() == null || ville.getText().trim().isEmpty()) {
            showError("Veuillez saisir votre ville.");
            return false;
        }

        if (roleComboBox.getValue() == null) {
            showError("Veuillez sélectionner un rôle.");
            return false;
        }

        if (!termsCheckBox.isSelected()) {
            showError("Vous devez accepter les conditions.");
            return false;
        }

        return true;
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        navigateToLogin();
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setStyle("-fx-fill: #ef4444;");
        errorMessage.setVisible(true);
    }

    private void showSuccess(String message) {
        errorMessage.setText(message);
        errorMessage.setStyle("-fx-fill: #4caf50; -fx-font-weight: bold;");
        errorMessage.setVisible(true);
    }
}