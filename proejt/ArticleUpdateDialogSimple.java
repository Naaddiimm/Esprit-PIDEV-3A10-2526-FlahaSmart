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

public class ArticleUpdateDialogSimple extends Dialog<Article> {

    private TextField searchIdField;
    private TextField nomField;
    private TextField categorieField;
    private TextArea descriptionArea;
    private TextField prixField;
    private TextField stockField;
    private TextField poidsField;
    private TextField uniteField;
    private TextField imageUrlField;
    private Label statusLabel;
    private Button searchButton;

    private ArticleDAO articleDAO;
    private Article currentArticle;
    private Button confirmButton;

    public ArticleUpdateDialogSimple(Window owner) {
        try {
            articleDAO = new ArticleDAO();

            // Create dialog
            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Mettre à jour un article");

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

            // Set result converter - FIXED: Make sure we return the updated article
            setResultConverter(buttonType -> {
                if (buttonType == confirmButtonType) {
                    if (validateForm()) {
                        updateArticleFromForm();
                        return currentArticle;
                    } else {
                        // If validation fails, consume the event and return null
                        return null;
                    }
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
        Label searchLabel = new Label("🔍 RECHERCHER ARTICLE PAR ID");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID Article:");
        idLabel.setMinWidth(100);
        idLabel.setStyle("-fx-font-weight: bold;");

        searchIdField = new TextField();
        searchIdField.setPromptText("Entrez l'ID de l'article");
        searchIdField.setPrefWidth(200);

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton.setOnAction(e -> searchArticle());

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

        // Row 0: Nom
        Label nomLabel = new Label("Nom:");
        nomLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nomLabel, 0, 0);
        nomField = new TextField();
        nomField.setPromptText("Nom de l'article");
        nomField.setPrefWidth(300);
        nomField.setEditable(false);
        grid.add(nomField, 1, 0);

        // Row 1: Catégorie
        Label catLabel = new Label("Catégorie:");
        catLabel.setStyle("-fx-font-weight: bold;");
        grid.add(catLabel, 0, 1);
        categorieField = new TextField();
        categorieField.setPromptText("Catégorie");
        categorieField.setEditable(false);
        grid.add(categorieField, 1, 1);

        // Row 2: Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        grid.add(descLabel, 0, 2);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setEditable(false);
        grid.add(descriptionArea, 1, 2);

        // Row 3: Prix
        Label prixLabel = new Label("Prix (€):");
        prixLabel.setStyle("-fx-font-weight: bold;");
        grid.add(prixLabel, 0, 3);
        prixField = new TextField();
        prixField.setPromptText("0.00");
        prixField.setEditable(false);
        grid.add(prixField, 1, 3);

        // Row 4: Stock
        Label stockLabel = new Label("Stock:");
        stockLabel.setStyle("-fx-font-weight: bold;");
        grid.add(stockLabel, 0, 4);
        stockField = new TextField();
        stockField.setPromptText("0");
        stockField.setEditable(false);
        grid.add(stockField, 1, 4);

        // Row 5: Poids
        Label poidsLabel = new Label("Poids (kg):");
        poidsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(poidsLabel, 0, 5);
        poidsField = new TextField();
        poidsField.setPromptText("0.0");
        poidsField.setEditable(false);
        grid.add(poidsField, 1, 5);

        // Row 6: Unité
        Label uniteLabel = new Label("Unité:");
        uniteLabel.setStyle("-fx-font-weight: bold;");
        grid.add(uniteLabel, 0, 6);
        uniteField = new TextField();
        uniteField.setPromptText("ex: pièce, kg");
        uniteField.setEditable(false);
        grid.add(uniteField, 1, 6);

        // Row 7: Image URL
        Label imageLabel = new Label("Image URL:");
        imageLabel.setStyle("-fx-font-weight: bold;");
        grid.add(imageLabel, 0, 7);
        imageUrlField = new TextField();
        imageUrlField.setPromptText("https://...");
        imageUrlField.setEditable(false);
        grid.add(imageUrlField, 1, 7);

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

    private void searchArticle() {
        String idText = searchIdField.getText().trim();

        if (idText.isEmpty()) {
            showStatus("Veuillez entrer un ID d'article", true);
            return;
        }

        try {
            int id = Integer.parseInt(idText);

            System.out.println("[DEBUG] Searching for article ID: " + id);
            Article found = articleDAO.getArticleById(id);

            if (found != null) {
                currentArticle = found;
                populateForm(found);
                enableFormFields(true);
                showStatus("Article trouvé ! Vous pouvez maintenant modifier les informations.", false);
                searchIdField.setDisable(true);
                searchButton.setDisable(true);
                confirmButton.setDisable(false);
                System.out.println("[DEBUG] Article found and form populated");
            } else {
                showStatus("Aucun article trouvé avec l'ID: " + id, true);
                clearForm();
                enableFormFields(false);
                confirmButton.setDisable(true);
            }

        } catch (NumberFormatException e) {
            showStatus("L'ID doit être un nombre entier", true);
        }
    }

    private void enableFormFields(boolean enable) {
        nomField.setEditable(enable);
        categorieField.setEditable(enable);
        descriptionArea.setEditable(enable);
        prixField.setEditable(enable);
        stockField.setEditable(enable);
        poidsField.setEditable(enable);
        uniteField.setEditable(enable);
        imageUrlField.setEditable(enable);

        // Also change background color to indicate editable state
        String style = enable ? "-fx-background-color: white;" : "-fx-background-color: #f0f0f0;";
        nomField.setStyle(style);
        categorieField.setStyle(style);
        descriptionArea.setStyle(style);
        prixField.setStyle(style);
        stockField.setStyle(style);
        poidsField.setStyle(style);
        uniteField.setStyle(style);
        imageUrlField.setStyle(style);
    }

    private void populateForm(Article article) {
        nomField.setText(article.getNom());
        categorieField.setText(article.getCategorie());
        descriptionArea.setText(article.getDescription());
        prixField.setText(String.valueOf(article.getPrix()));
        stockField.setText(String.valueOf(article.getStock()));
        poidsField.setText(String.valueOf(article.getPoids()));
        uniteField.setText(article.getUnite());
        imageUrlField.setText(article.getImageUrl());
    }

    private void updateArticleFromForm() {
        if (currentArticle != null) {
            currentArticle.setNom(nomField.getText().trim());
            currentArticle.setCategorie(categorieField.getText().trim());
            currentArticle.setDescription(descriptionArea.getText().trim());
            currentArticle.setPrix(Double.parseDouble(prixField.getText().trim()));
            currentArticle.setStock(Integer.parseInt(stockField.getText().trim()));
            currentArticle.setPoids(Double.parseDouble(poidsField.getText().trim()));
            currentArticle.setUnite(uniteField.getText().trim());
            currentArticle.setImageUrl(imageUrlField.getText().trim());

            System.out.println("[DEBUG] Article updated in dialog: " + currentArticle.getId());
            System.out.println("[DEBUG] New values: " + currentArticle.getNom() + ", " + currentArticle.getPrix());
        }
    }

    private boolean validateForm() {
        if (currentArticle == null) {
            showStatus("Veuillez d'abord rechercher un article", true);
            return false;
        }

        if (nomField.getText().trim().isEmpty()) {
            showStatus("Le nom est obligatoire", true);
            return false;
        }

        if (categorieField.getText().trim().isEmpty()) {
            showStatus("La catégorie est obligatoire", true);
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showStatus("La description est obligatoire", true);
            return false;
        }

        try {
            Double.parseDouble(prixField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Le prix doit être un nombre valide", true);
            return false;
        }

        try {
            Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Le stock doit être un nombre entier valide", true);
            return false;
        }

        try {
            Double.parseDouble(poidsField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Le poids doit être un nombre valide", true);
            return false;
        }

        if (imageUrlField.getText().trim().isEmpty()) {
            showStatus("L'URL de l'image est obligatoire", true);
            return false;
        }

        if (!imageUrlField.getText().trim().startsWith("https://")) {
            showStatus("L'URL doit commencer par https://", true);
            return false;
        }

        showStatus("Formulaire valide !", false);
        return true;
    }

    private void clearForm() {
        nomField.clear();
        categorieField.clear();
        descriptionArea.clear();
        prixField.clear();
        stockField.clear();
        poidsField.clear();
        uniteField.clear();
        imageUrlField.clear();
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
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                prixField.setText(oldValue);
            }
        });

        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(oldValue);
            }
        });

        poidsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                poidsField.setText(oldValue);
            }
        });
    }
}