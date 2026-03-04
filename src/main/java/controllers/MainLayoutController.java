package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

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

    @FXML
    public void goToAdd() {
        loadPage("/views/operation/AjouterOp.fxml");
    }

    @FXML
    public void goToList() {
        loadPage("/views/operation/ListeOp.fxml");
    }

    @FXML
    public void goToAddEq(ActionEvent actionEvent) {
        loadPage("/views/equipement/AjouterEq.fxml");
    }

    @FXML
    public void goToListEq(ActionEvent actionEvent) {
        loadPage("/views/equipement/ListeEq.fxml");
    }

    @FXML
    public void goToDashboard(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/Dashboard.fxml");
    }

    @FXML
    public void goToWeather(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/WeatherView.fxml");
    }

    @FXML
    public void goToChatbot(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/ChatbotView.fxml");
    }

    @FXML
    public void goToPlantDisease(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/PlantDiseaseView.fxml");
    }

    @FXML
    public void goToIrrigation(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/IrrigationView.fxml");
    }

    @FXML
    public void goToRotation(ActionEvent actionEvent) {
        loadPage("/views/advancedfeatures/RotationCultureView.fxml");
    }

}
