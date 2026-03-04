package controllers;

import controllers.forum.ThreadController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML
    private BorderPane root;

    private void loadPage(String path) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(path));
            root.setCenter(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        goToDashboard(null);
    }

    @FXML public void goToAdd()                       { loadPage("/views/operation/AjouterOp.fxml"); }
    @FXML public void goToList()                      { loadPage("/views/operation/ListeOp.fxml"); }
    @FXML public void goToAddEq(ActionEvent e)        { loadPage("/views/equipement/AjouterEq.fxml"); }
    @FXML public void goToListEq(ActionEvent e)       { loadPage("/views/equipement/ListeEq.fxml"); }
    @FXML public void goToDashboard(ActionEvent e)    { loadPage("/views/advancedfeatures/Dashboard.fxml"); }
    @FXML public void goToWeather(ActionEvent e)      { loadPage("/views/advancedfeatures/WeatherView.fxml"); }
    @FXML public void goToChatbot(ActionEvent e)      { loadPage("/views/advancedfeatures/ChatbotView.fxml"); }
    @FXML public void goToPlantDisease(ActionEvent e) { loadPage("/views/advancedfeatures/PlantDiseaseView.fxml"); }
    @FXML public void goToIrrigation(ActionEvent e)   { loadPage("/views/advancedfeatures/IrrigationView.fxml"); }
    @FXML public void goToRotation(ActionEvent e)     { loadPage("/views/advancedfeatures/RotationCultureView.fxml"); }

    // =========================================================
    //  FORUM — ouvre le Forum dans une nouvelle fenêtre
    //  Le Forum est en pure Java (pas FXML)
    // =========================================================
    @FXML
    public void goToForum(ActionEvent event) {
        try {
            // Démarrer l'API de modération
            try {
                Class<?> api = Class.forName("api.ModerationAPI");
                api.getMethod("demarrer").invoke(null);
            } catch (Exception ignored) {} // déjà démarrée ou absente

            Stage forumStage = new Stage();
            new ThreadController().afficher(forumStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}