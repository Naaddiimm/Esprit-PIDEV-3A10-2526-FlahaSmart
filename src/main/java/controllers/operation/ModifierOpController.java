package controllers.operation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import entities.Equipement;
import entities.Operation;
import services.EquipementService;
import services.OperationService;

import java.sql.Date;
import java.sql.SQLException;

public class ModifierOpController {

    @FXML
    private TextField typeField;

    @FXML
    private ComboBox<Equipement> equipementCombo;


    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    private int ancienEquipementId;

    private Operation operation;

    private ListeOpController listeController;

    OperationService service = new OperationService();
    EquipementService equipementService = new EquipementService();

    public void setListeController(ListeOpController listeController) {
        this.listeController = listeController;
    }


    // ================================
    // recevoir les données
    // ================================
    public void setOperation(Operation op) {
        this.operation = op;

        typeField.setText(op.getType_operation());
        dateDebutPicker.setValue(op.getDate_debut().toLocalDate());
        dateFinPicker.setValue(op.getDate_fin().toLocalDate());
        for (Equipement e : equipementCombo.getItems()) {
            if (e.getId_equipement() == op.getId_equipement()) {
                equipementCombo.setValue(e);
                break;
            }
        }
        ancienEquipementId = op.getId_equipement();

    }

    @FXML
    public void updateOperation() {

        String erreurs = validerChamps();

        if (!erreurs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Veuillez corriger :");
            alert.setContentText(erreurs);
            alert.show();
            return;
        }
        operation.setType_operation(typeField.getText());
        operation.setDate_debut(Date.valueOf(dateDebutPicker.getValue()));
        operation.setDate_fin(Date.valueOf(dateFinPicker.getValue()));
        Equipement eq = equipementCombo.getValue();
        operation.setId_equipement(eq.getId_equipement());


        try {
            service.modifier(operation, ancienEquipementId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // refresh la liste
        if (listeController != null) {
            listeController.afficherOperations();
        }

        // fermer la fenêtre
        Stage stage = (Stage) typeField.getScene().getWindow();
        stage.close();
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

            if (!type.matches("[a-zA-Z ]+")) {
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
