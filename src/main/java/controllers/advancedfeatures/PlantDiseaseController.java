package controllers.advancedfeatures;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import services.advancedfeatures.PlantDiseaseService;
import services.advancedfeatures.PlantDiseaseService.PlantIdentificationResult;

import java.io.File;
import java.util.List;

/**
 * Contrôleur pour la détection de maladies des plantes
 */
public class PlantDiseaseController {

    @FXML
    private VBox mainContainer;

    @FXML
    private ImageView uploadedImage;

    @FXML
    private Label uploadLabel;

    @FXML
    private VBox dropZone;

    @FXML
    private VBox resultsContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private ScrollPane resultsScrollPane;

    private PlantDiseaseService plantDiseaseService;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        plantDiseaseService = new PlantDiseaseService();
        loadingIndicator.setVisible(false);
        resultsScrollPane.setVisible(false);

        // Configurer la zone de drop
        setupDropZone();
    }

    private void setupDropZone() {
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                dropZone.setStyle(dropZone.getStyle() + "-fx-border-color: #10b981;");
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            var files = event.getDragboard().getFiles();
            if (!files.isEmpty()) {
                File file = files.get(0);
                if (isImageFile(file)) {
                    loadImage(file);
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle(dropZone.getStyle().replace("-fx-border-color: #10b981;", ""));
        });
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
               name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de plante");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            loadImage(file);
        }
    }

    private void loadImage(File file) {
        selectedImageFile = file;
        try {
            Image image = new Image(file.toURI().toString(), 300, 300, true, true);
            uploadedImage.setImage(image);
            uploadedImage.setVisible(true);
            uploadLabel.setText("📷 " + file.getName());
            statusLabel.setText("Image chargée. Cliquez sur 'Analyser' pour identifier la plante.");
            statusLabel.setStyle("-fx-text-fill: #059669;");
        } catch (Exception e) {
            showError("Impossible de charger l'image: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnalyze() {
        if (selectedImageFile == null) {
            showError("Veuillez d'abord sélectionner une image.");
            return;
        }

        // Afficher le chargement
        loadingIndicator.setVisible(true);
        statusLabel.setText("🔍 Analyse en cours...");
        statusLabel.setStyle("-fx-text-fill: #6b7280;");
        resultsScrollPane.setVisible(false);
        resultsContainer.getChildren().clear();

        // Exécuter l'analyse dans un thread séparé
        new Thread(() -> {
            try {
                List<PlantIdentificationResult> results = plantDiseaseService.identifyPlant(
                        selectedImageFile.getAbsolutePath()
                );

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (results.isEmpty()) {
                        statusLabel.setText("❌ Aucune plante identifiée. Essayez avec une autre image.");
                        statusLabel.setStyle("-fx-text-fill: #dc2626;");
                    } else {
                        statusLabel.setText("✅ " + results.size() + " résultat(s) trouvé(s)");
                        statusLabel.setStyle("-fx-text-fill: #059669;");
                        displayResults(results);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showError("Erreur lors de l'analyse: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayResults(List<PlantIdentificationResult> results) {
        resultsContainer.getChildren().clear();
        resultsScrollPane.setVisible(true);

        for (int i = 0; i < results.size(); i++) {
            PlantIdentificationResult result = results.get(i);
            VBox resultCard = createResultCard(result, i + 1);
            resultsContainer.getChildren().add(resultCard);
        }
    }

    private VBox createResultCard(PlantIdentificationResult result, int rank) {
        VBox card = new VBox(12);
        card.getStyleClass().add("result-card");
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f0fdf4); " +
                     "-fx-background-radius: 16; " +
                     "-fx-border-color: #d1fae5; " +
                     "-fx-border-radius: 16; " +
                     "-fx-border-width: 1; " +
                     "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.15), 15, 0, 0, 5);");

        // En-tête avec rang et score
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Badge de rang
        Label rankBadge = new Label("#" + rank);
        rankBadge.setStyle("-fx-background-color: linear-gradient(to right, #059669, #10b981); " +
                          "-fx-text-fill: white; " +
                          "-fx-font-weight: bold; " +
                          "-fx-font-size: 14px; " +
                          "-fx-padding: 5 12; " +
                          "-fx-background-radius: 20;");

        // Score avec couleur dynamique
        Label scoreLabel = new Label(result.getScorePercentage());
        String scoreColor = result.getScore() > 0.5 ? "#059669" :
                           (result.getScore() > 0.2 ? "#f59e0b" : "#dc2626");
        scoreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");

        // Indicateur de confiance
        String confidenceText = result.getScore() > 0.5 ? "✅ Confiance élevée" :
                               (result.getScore() > 0.2 ? "⚠️ Confiance moyenne" : "❓ Confiance faible");
        Label confidenceLabel = new Label(confidenceText);
        confidenceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + scoreColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox scoreBox = new VBox(2);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        scoreBox.getChildren().addAll(scoreLabel, confidenceLabel);

        header.getChildren().addAll(rankBadge, spacer, scoreBox);

        // Nom scientifique
        Label scientificName = new Label("🌿 " + result.getScientificName());
        scientificName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #065f46;");
        scientificName.setWrapText(true);

        // Nom commun
        Label commonName = new Label("📝 Nom commun: " + result.getCommonName());
        commonName.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        commonName.setWrapText(true);

        // Informations taxonomiques
        HBox taxonomyBox = new HBox(20);
        taxonomyBox.setAlignment(Pos.CENTER_LEFT);

        VBox familyBox = new VBox(2);
        Label familyTitle = new Label("Famille");
        familyTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        Label familyValue = new Label(result.getFamily());
        familyValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        familyBox.getChildren().addAll(familyTitle, familyValue);

        VBox genusBox = new VBox(2);
        Label genusTitle = new Label("Genre");
        genusTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        Label genusValue = new Label(result.getGenus());
        genusValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        genusBox.getChildren().addAll(genusTitle, genusValue);

        taxonomyBox.getChildren().addAll(familyBox, genusBox);

        // Barre de progression du score
        ProgressBar progressBar = new ProgressBar(result.getScore());
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: " + scoreColor + ";");

        // Images de référence (si disponibles)
        if (!result.getImageUrls().isEmpty()) {
            Label refLabel = new Label("📸 Images de référence:");
            refLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-padding: 10 0 5 0;");

            HBox imagesBox = new HBox(10);
            imagesBox.setAlignment(Pos.CENTER_LEFT);

            for (int i = 0; i < Math.min(3, result.getImageUrls().size()); i++) {
                try {
                    ImageView refImage = new ImageView(new Image(result.getImageUrls().get(i), 80, 80, true, true));
                    refImage.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                    refImage.setFitWidth(80);
                    refImage.setFitHeight(80);

                    // Style pour le conteneur de l'image
                    StackPane imageContainer = new StackPane(refImage);
                    imageContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                                           "-fx-padding: 3; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");

                    imagesBox.getChildren().add(imageContainer);
                } catch (Exception e) {
                    // Ignorer les erreurs de chargement d'images
                }
            }

            card.getChildren().addAll(header, scientificName, commonName, taxonomyBox, progressBar, refLabel, imagesBox);
        } else {
            card.getChildren().addAll(header, scientificName, commonName, taxonomyBox, progressBar);
        }

        // Conseils
        String advice = plantDiseaseService.getPlantAdvice(result);

        TitledPane advicePane = new TitledPane();
        advicePane.setText("💡 Conseils");
        advicePane.setExpanded(false);
        advicePane.setStyle("-fx-font-size: 12px;");

        Label adviceLabel = new Label(advice);
        adviceLabel.setWrapText(true);
        adviceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-padding: 10;");
        advicePane.setContent(adviceLabel);

        card.getChildren().add(advicePane);

        return card;
    }

    @FXML
    private void handleClear() {
        selectedImageFile = null;
        uploadedImage.setImage(null);
        uploadedImage.setVisible(false);
        uploadLabel.setText("📁 Glissez une image ici ou cliquez pour parcourir");
        statusLabel.setText("");
        resultsContainer.getChildren().clear();
        resultsScrollPane.setVisible(false);
    }

    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }
}

