package controllers.equipement;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import entities.Equipement;
import services.EquipementService;

import java.sql.SQLException;

public class ModifierEqController {

    @FXML
    private TextField nomField;

    @FXML
    private ComboBox<String> typeCombo;

    private Equipement equipement;
    private ListeEqController listeController;

    EquipementService service = new EquipementService();

    public void setListeController(ListeEqController listeController) {
        this.listeController = listeController;
    }

    public void setEquipement(Equipement e) {
        this.equipement = e;

        nomField.setText(e.getType());
        typeCombo.setValue(e.getNom());
    }

    @FXML
    public void updateEquipement() {

        equipement.setNom(nomField.getText());
        equipement.setType(typeCombo.getValue());

        try {
            service.modifier(equipement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (listeController != null) {
            listeController.afficherEquipements();
        }

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
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
