package controllers.operation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import entities.Equipement;
import entities.Operation;
import services.EquipementService;
import services.OperationService;

import java.sql.Date;
import java.sql.SQLException;

public class Ajout_Op_Controller  {

    @FXML
    private TextField typeField;

    @FXML
    private ComboBox<Equipement> equipementCombo;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    OperationService service = new OperationService();
    EquipementService equipementService = new EquipementService();

    @FXML
    public void ajouterOperation() {

        try {

            String erreurs = validerChamps();

            if (!erreurs.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de saisie");
                alert.setHeaderText("Veuillez corriger :");
                alert.setContentText(erreurs);
                alert.show();
                return;
            }

            Operation op = new Operation();

            op.setType_operation(typeField.getText());
            op.setDate_debut(Date.valueOf(dateDebutPicker.getValue()));
            op.setDate_fin(Date.valueOf(dateFinPicker.getValue()));

            Equipement eq = equipementCombo.getValue();
            op.setId_equipement(eq.getId_equipement());


            service.ajouter(op);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Opération ajoutée !");
            alert.show();

            viderChamps();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void viderChamps() {
        typeField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        equipementCombo.getItems().clear();
        try {
            equipementCombo.getItems().addAll(
                    equipementService.afficherLibres()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    public void initialize() {
        try {

            equipementCombo.getItems().addAll(
                    equipementService.afficherLibres()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String validerChamps() {

        StringBuilder errors = new StringBuilder();

        // =====================
        // type obligatoire
        // =====================
        String type = typeField.getText();

        if (type == null || type.isEmpty()) {
            errors.append("• Type est obligatoire\n");
        }
        else {
            if (type.length() < 3) {
                errors.append("• Type doit contenir au moins 3 caractères\n");
            }

            if(!type.matches("[\\p{L}\\d ]+")) {
                errors.append("• Type ne doit pas contenir de caractères spéciaux\n");
            }
        }

        // =====================
        // équipement
        // =====================
        if (equipementCombo.getValue() == null) {
            errors.append("• Choisir un équipement\n");
        }

        // =====================
        // dates
        // =====================
        if (dateDebutPicker.getValue() == null) {
            errors.append("• Date début obligatoire\n");
        }

        if (dateFinPicker.getValue() == null) {
            errors.append("• Date fin obligatoire\n");
        }

        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {

            if (dateDebutPicker.getValue().isBefore(java.time.LocalDate.now())) {
                errors.append("• Date début ne peut pas être dans le passé\n");
            }

            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                errors.append("• Date fin doit être après date début\n");
            }
        }

        return errors.toString();
    }

}
