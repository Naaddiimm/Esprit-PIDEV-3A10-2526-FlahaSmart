package controllers.equipement;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import entities.Equipement;
import services.EquipementService;

public class Ajout_Eq_Controller {

    @FXML
    private TextField nomField;

    @FXML
    private ComboBox<String> typeCombo;


    private EquipementService service = new EquipementService();

    // ================================
    // AJOUT
    // ================================
    @FXML
    public void ajouterEquipement() {

        try {
            String nom = nomField.getText();
            String type = typeCombo.getValue();

            // petite validation
            if (nom.isEmpty() || type == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            Equipement e = new Equipement();
            e.setNom(nom);
            e.setType(type);

            service.ajouter(e);

            showAlert("Succès", "Équipement ajouté avec succès !");

            viderChamps();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ajout.");
        }
    }

    // ================================
    // RESET
    // ================================
    private void viderChamps() {
        nomField.clear();
       // typeField.clear();
    }

    // ================================
    // POPUP
    // ================================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll(
                "Machine",
                "Irrigation",
                "Élevage",
                "Outil manuel",
                "Transport"

        );
    }

}
