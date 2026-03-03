package com.example.flahasmarty;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import java.time.LocalDate;

public class OrderUpdateDialog extends Dialog<Order> {

    private TextField searchIdField;
    private TextField referenceField;
    private DatePicker dateField;
    private TextField statutField;
    private TextField modePaiementField;
    private TextField adresseField;
    private TextField montantField;
    private TextField fraisField;
    private Label statusLabel;
    private Button searchButton;

    private OrderDAO orderDAO;
    private Order currentOrder;
    private Button confirmButton;

    public OrderUpdateDialog(Window owner) {
        try {
            orderDAO = new OrderDAO();

            // Create dialog
            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Mettre à jour une commande");

            // Create content
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(createContent());

            // Add buttons
            ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            setDialogPane(dialogPane);

            // Get confirm button
            confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            if (confirmButton != null) {
                confirmButton.setDisable(true);
            }

            // Set result converter
            setResultConverter(buttonType -> {
                if (buttonType == confirmButtonType && validateForm()) {
                    updateOrderFromForm();
                    return currentOrder;
                }
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de créer la fenêtre: " + e.getMessage());
        }
    }

    private Node createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Search section
        Label searchLabel = new Label("🔍 RECHERCHER COMMANDE PAR ID");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID Commande:");
        idLabel.setMinWidth(100);
        idLabel.setStyle("-fx-font-weight: bold;");

        searchIdField = new TextField();
        searchIdField.setPromptText("Entrez l'ID de la commande");
        searchIdField.setPrefWidth(200);

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton.setOnAction(e -> searchOrder());

        searchBox.getChildren().addAll(idLabel, searchIdField, searchButton);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #bdc3c7;");

        // Form section
        Label formLabel = new Label("📝 MODIFIER LES INFORMATIONS");
        formLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 10 0 0 0;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Row 0: Référence
        Label refLabel = new Label("Référence:");
        refLabel.setStyle("-fx-font-weight: bold;");
        grid.add(refLabel, 0, 0);
        referenceField = new TextField();
        referenceField.setPromptText("Référence de la commande");
        referenceField.setPrefWidth(300);
        referenceField.setEditable(false); // Disable until order is found
        grid.add(referenceField, 1, 0);

        // Row 1: Date
        Label dateLabel = new Label("Date commande:");
        dateLabel.setStyle("-fx-font-weight: bold;");
        grid.add(dateLabel, 0, 1);
        dateField = new DatePicker();
        dateField.setPromptText("Sélectionner une date");
        dateField.setPrefWidth(200);
        dateField.setEditable(false); // Disable until order is found
        grid.add(dateField, 1, 1);

        // Row 2: Statut
        Label statutLabel = new Label("Statut:");
        statutLabel.setStyle("-fx-font-weight: bold;");
        grid.add(statutLabel, 0, 2);
        statutField = new TextField();
        statutField.setPromptText("ex: En cours, Livrée, Annulée");
        statutField.setEditable(false); // Disable until order is found
        grid.add(statutField, 1, 2);

        // Row 3: Mode paiement
        Label modeLabel = new Label("Mode paiement:");
        modeLabel.setStyle("-fx-font-weight: bold;");
        grid.add(modeLabel, 0, 3);
        modePaiementField = new TextField();
        modePaiementField.setPromptText("ex: Carte, Virement, Espèces");
        modePaiementField.setEditable(false); // Disable until order is found
        grid.add(modePaiementField, 1, 3);

        // Row 4: Adresse
        Label adresseLabel = new Label("Adresse:");
        adresseLabel.setStyle("-fx-font-weight: bold;");
        grid.add(adresseLabel, 0, 4);
        adresseField = new TextField();
        adresseField.setPromptText("Adresse de livraison");
        adresseField.setEditable(false); // Disable until order is found
        grid.add(adresseField, 1, 4);

        // Row 5: Montant total
        Label montantLabel = new Label("Montant total (€):");
        montantLabel.setStyle("-fx-font-weight: bold;");
        grid.add(montantLabel, 0, 5);
        montantField = new TextField();
        montantField.setPromptText("0.00");
        montantField.setEditable(false); // Disable until order is found
        grid.add(montantField, 1, 5);

        // Row 6: Frais livraison
        Label fraisLabel = new Label("Frais livraison (€):");
        fraisLabel.setStyle("-fx-font-weight: bold;");
        grid.add(fraisLabel, 0, 6);
        fraisField = new TextField();
        fraisField.setPromptText("0.00");
        fraisField.setEditable(false); // Disable until order is found
        grid.add(fraisField, 1, 6);

        // Status label
        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-padding: 10 0 0 0;");

        // Add number validation
        addNumberValidation();

        content.getChildren().addAll(
                searchLabel, searchBox, separator, formLabel,
                grid, statusLabel
        );

        return content;
    }

    private void searchOrder() {
        String idText = searchIdField.getText().trim();

        if (idText.isEmpty()) {
            showStatus("Veuillez entrer un ID de commande", true);
            return;
        }

        try {
            int id = Integer.parseInt(idText);

            System.out.println("[DEBUG] Searching for order ID: " + id);
            Order found = orderDAO.getOrderById(id);

            if (found != null) {
                currentOrder = found;
                populateForm(found);
                enableFormFields(true);
                showStatus("Commande trouvée ! Vous pouvez maintenant modifier les informations.", false);
                searchIdField.setDisable(true);
                searchButton.setDisable(true);
                confirmButton.setDisable(false);
                System.out.println("[DEBUG] Order found and form populated");
            } else {
                showStatus("Aucune commande trouvée avec l'ID: " + id, true);
                clearForm();
                enableFormFields(false);
            }

        } catch (NumberFormatException e) {
            showStatus("L'ID doit être un nombre entier", true);
        }
    }

    private void enableFormFields(boolean enable) {
        referenceField.setEditable(enable);
        dateField.setEditable(enable);
        statutField.setEditable(enable);
        modePaiementField.setEditable(enable);
        adresseField.setEditable(enable);
        montantField.setEditable(enable);
        fraisField.setEditable(enable);

        // Also change background color to indicate editable state
        String style = enable ? "-fx-background-color: white;" : "-fx-background-color: #f0f0f0;";
        referenceField.setStyle(style);
        statutField.setStyle(style);
        modePaiementField.setStyle(style);
        adresseField.setStyle(style);
        montantField.setStyle(style);
        fraisField.setStyle(style);
    }

    private void populateForm(Order order) {
        referenceField.setText(order.getReference());
        dateField.setValue(order.getDateCommande());
        statutField.setText(order.getStatut());
        modePaiementField.setText(order.getModePaiement());
        adresseField.setText(order.getAdresseLivraison());
        montantField.setText(String.valueOf(order.getMontantTotal()));
        fraisField.setText(String.valueOf(order.getFraisLivraison()));
    }

    private void updateOrderFromForm() {
        if (currentOrder != null) {
            currentOrder.setReference(referenceField.getText().trim());
            currentOrder.setDateCommande(dateField.getValue());
            currentOrder.setStatut(statutField.getText().trim());
            currentOrder.setModePaiement(modePaiementField.getText().trim());
            currentOrder.setAdresseLivraison(adresseField.getText().trim());
            currentOrder.setMontantTotal(Double.parseDouble(montantField.getText().trim()));
            currentOrder.setFraisLivraison(Double.parseDouble(fraisField.getText().trim()));
        }
    }

    private boolean validateForm() {
        if (currentOrder == null) {
            showStatus("Veuillez d'abord rechercher une commande", true);
            return false;
        }

        if (referenceField.getText().trim().isEmpty()) {
            showStatus("La référence est obligatoire", true);
            return false;
        }

        if (dateField.getValue() == null) {
            showStatus("La date est obligatoire", true);
            return false;
        }

        if (statutField.getText().trim().isEmpty()) {
            showStatus("Le statut est obligatoire", true);
            return false;
        }

        if (modePaiementField.getText().trim().isEmpty()) {
            showStatus("Le mode de paiement est obligatoire", true);
            return false;
        }

        if (adresseField.getText().trim().isEmpty()) {
            showStatus("L'adresse est obligatoire", true);
            return false;
        }

        try {
            Double.parseDouble(montantField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Le montant doit être un nombre valide", true);
            return false;
        }

        try {
            Double.parseDouble(fraisField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Les frais doivent être un nombre valide", true);
            return false;
        }

        showStatus("Formulaire valide !", false);
        return true;
    }

    private void clearForm() {
        referenceField.clear();
        dateField.setValue(null);
        statutField.clear();
        modePaiementField.clear();
        adresseField.clear();
        montantField.clear();
        fraisField.clear();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addNumberValidation() {
        montantField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                montantField.setText(oldValue);
            }
        });

        fraisField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                fraisField.setText(oldValue);
            }
        });
    }
}