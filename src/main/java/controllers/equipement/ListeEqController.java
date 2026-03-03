package controllers.equipement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import entities.Equipement;
import services.EquipementService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ListeEqController {

    @FXML
    private VBox listContainer;

    @FXML
    private TextField searchField;

    EquipementService service = new EquipementService();

    @FXML
    public void initialize() {
        afficherEquipements();

        // Listener pour la recherche en temps réel
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldVal, newVal) -> {
                rechercherParNom(newVal);
            });
        }
    }

    public void afficherEquipements() {
        listContainer.getChildren().clear();

        try {
            List<Equipement> list = service.afficher();

            for (Equipement e : list) {
                VBox card = createCard(e);
                listContainer.getChildren().add(card);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private VBox createCard(Equipement e) {

        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // header
        HBox header = new HBox(10);

        Label nom = new Label(e.getType());
        nom.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("edit-button");

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("danger-button");

        header.getChildren().addAll(nom, spacer, btnEdit, btnDelete);

        // infos
        Label type = new Label("Type : " + e.getNom());
        Label etat = new Label("État : " + e.getEtat());

        if (!e.getEtat().equalsIgnoreCase("libre")) {
            btnDelete.setDisable(true);
        }


        card.getChildren().addAll(header, type, etat);

        // actions
        btnDelete.setOnAction(ev -> {
            // Création de l'alerte de confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer l'équipement");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer l'équipement \"" + e.getType() + "\" ?");

            // Attendre la réponse de l'utilisateur
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(e);
                    afficherEquipements();
                } catch (SQLException ex) {
                    // Afficher une alerte d'erreur en cas de problème
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText("Erreur lors de la suppression");
                    errorAlert.setContentText("Impossible de supprimer l'équipement : " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });

        btnEdit.setOnAction(ev -> ouvrirModification(e));


        return card;
    }

    private void ouvrirModification(Equipement e) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/equipement/ModifierEq.fxml")
            );

            Parent root = loader.load();

            ModifierEqController controller = loader.getController();
            controller.setEquipement(e);
            controller.setListeController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Modifier équipement");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public void rechercherParNom(String nom) {
        listContainer.getChildren().clear();

        try {
            List<Equipement> list;
            if (nom == null || nom.trim().isEmpty()) {
                list = service.afficher();
            } else {
                list = service.rechercherParNom(nom);
            }

            for (Equipement e : list) {
                VBox card = createCard(e);
                listContainer.getChildren().add(card);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public void trier(ActionEvent actionEvent) {
        listContainer.getChildren().clear();

        try {
            List<Equipement> list = service.trierParNom();

            for (Equipement e : list) {
                VBox card = createCard(e);
                listContainer.getChildren().add(card);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
