package controllers.operation;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

import services.OperationService;
import entities.Operation;

import java.sql.SQLException;
import java.util.List;

public class ListeOpController {

    @FXML
    private VBox listContainer;

    @FXML
    private TextField searchField;


    OperationService service = new OperationService();

    @FXML
    public void initialize() {
        afficherOperations();

        // 🔎 recherche temps réel
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    if (newVal == null || newVal.isEmpty()) {
                        afficherListe(service.afficher());
                    } else {
                        afficherListe(service.rechercherParType(newVal));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private void afficherListe(List<Operation> list) {
        listContainer.getChildren().clear();

        for (Operation op : list) {
            VBox card = createCard(op);
            listContainer.getChildren().add(card);
        }
    }

    public void afficherOperations() {
        try {
            afficherListe(service.afficher());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void supprimerOperation(Operation op) {
        // Création de l'alerte de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'opération");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'opération \"" + op.getType_operation() + "\" ?");

        // Attendre la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(op);
                afficherOperations(); // refresh
            } catch (SQLException e) {
                // Afficher une alerte d'erreur en cas de problème
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Erreur lors de la suppression");
                errorAlert.setContentText("Impossible de supprimer l'opération : " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void terminerOperation(Operation op) {
        try {
            service.terminer(op);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        afficherOperations(); // refresh
    }

    private void updateOperation(Operation op) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/operation/ModifierOp.fxml")
            );

            Parent root = loader.load();

            ModifierOpController controller = loader.getController();
            controller.setOperation(op);

            // 🔥 ON PASSE LA LISTE
            controller.setListeController(this);

            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(
                    getClass().getResource("/styles/style_css.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setTitle("Modifier l'opération");
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private VBox createCard(Operation op) {

        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // =====================
        // Ligne du haut
        // =====================
        HBox header = new HBox(10);

        Label type = new Label(op.getType_operation());
        type.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("edit-button");

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("danger-button");

        header.getChildren().addAll(type, spacer);

        if (op.getStatut() != null && op.getStatut().equalsIgnoreCase("en cours")) {
            Button btnTerminer = new Button("Terminer");
            btnTerminer.getStyleClass().add("success-button");
            btnTerminer.setOnAction(e -> terminerOperation(op));
            header.getChildren().add(btnTerminer);
        }

        header.getChildren().addAll(btnEdit, btnDelete);

        // =====================
        // Infos
        // =====================
        Label dateDebut = new Label("Début : " + op.getDate_debut());
        Label dateFin = new Label("Fin : " + op.getDate_fin());
        Label statut = new Label("Statut : " + op.getStatut());
        Label equipementLabel = new Label("Équipement : " + op.getNomEquipement());
        card.getChildren().addAll(header, equipementLabel, dateDebut, dateFin, statut);


        // =====================
        // ACTIONS
        // =====================

        btnDelete.setOnAction(e -> supprimerOperation(op));

        btnEdit.setOnAction(e -> updateOperation(op));

        return card;
    }

    @FXML
    private void trier() {
        try {
            afficherListe(service.trierParNom());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
