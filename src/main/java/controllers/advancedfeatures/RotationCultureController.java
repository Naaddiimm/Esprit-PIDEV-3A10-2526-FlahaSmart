package controllers.advancedfeatures;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import entities.advancedfeatures.Parcelle;
import services.advancedfeatures.RotationCultureService;

import java.util.List;

/**
 * Contrôleur pour le système de rotation des cultures
 */
public class RotationCultureController {

    @FXML private TextField surfaceField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private ComboBox<String> derniereCultureCombo;
    @FXML private ComboBox<String> avantDerniereCultureCombo;
    @FXML private Slider azoteSlider;
    @FXML private Slider phosphoreSlider;
    @FXML private Slider potassiumSlider;
    @FXML private Slider phSlider;
    @FXML private Spinner<Integer> anneesJachereSpinner;
    @FXML private Label azoteValue;
    @FXML private Label phosphoreValue;
    @FXML private Label potassiumValue;
    @FXML private Label phValue;
    @FXML private VBox resultContainer;
    @FXML private Label statusLabel;
    @FXML private Spinner<Integer> anneesRotationSpinner;

    private RotationCultureService rotationService;

    @FXML
    public void initialize() {
        rotationService = new RotationCultureService();

        // Charger les types de sol
        typeSolCombo.getItems().addAll(rotationService.getTypesSol());
        typeSolCombo.getSelectionModel().selectFirst();

        // Charger les cultures
        List<String> cultures = rotationService.getCulturesDisponibles();
        derniereCultureCombo.getItems().add("Aucune");
        derniereCultureCombo.getItems().addAll(cultures);
        derniereCultureCombo.getSelectionModel().selectFirst();

        avantDerniereCultureCombo.getItems().add("Aucune");
        avantDerniereCultureCombo.getItems().addAll(cultures);
        avantDerniereCultureCombo.getSelectionModel().selectFirst();

        // Configurer les sliders
        configureSlider(azoteSlider, azoteValue, "N");
        configureSlider(phosphoreSlider, phosphoreValue, "P");
        configureSlider(potassiumSlider, potassiumValue, "K");
        configurePhSlider();

        // Configurer les spinners
        anneesJachereSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        anneesRotationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4));

        // Valeurs par défaut
        surfaceField.setText("5.0");
    }

    private void configureSlider(Slider slider, Label valueLabel, String nutriment) {
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            valueLabel.setText(value + "/10");

            String color;
            if (value >= 7) color = "#059669";
            else if (value >= 5) color = "#f59e0b";
            else color = "#dc2626";

            valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        });
        valueLabel.setText((int) slider.getValue() + "/10");
    }

    private void configurePhSlider() {
        phSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            phValue.setText(String.valueOf(value));

            String color;
            if (value >= 6.0 && value <= 7.5) color = "#059669";
            else if (value >= 5.5 && value <= 8.0) color = "#f59e0b";
            else color = "#dc2626";

            phValue.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        });
        phValue.setText(String.valueOf(phSlider.getValue()));
    }

    @FXML
    private void handleAnalyser() {
        // Validation
        double surface;
        try {
            surface = Double.parseDouble(surfaceField.getText().trim());
            if (surface <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Surface invalide");
            return;
        }

        // Créer la parcelle
        Parcelle parcelle = new Parcelle(surface, typeSolCombo.getValue());

        String derniereCulture = derniereCultureCombo.getValue();
        if (!"Aucune".equals(derniereCulture)) {
            parcelle.setDerniereCulture(derniereCulture);
        }

        String avantDerniere = avantDerniereCultureCombo.getValue();
        if (!"Aucune".equals(avantDerniere)) {
            parcelle.setAvantDerniereCulture(avantDerniere);
        }

        parcelle.setNiveauAzote((int) azoteSlider.getValue());
        parcelle.setNiveauPhosphore((int) phosphoreSlider.getValue());
        parcelle.setNiveauPotassium((int) potassiumSlider.getValue());
        parcelle.setPh(Math.round(phSlider.getValue() * 10.0) / 10.0);
        parcelle.setAnneesDepuisJachere(anneesJachereSpinner.getValue());

        // Générer le plan de rotation
        List<String> planRotation = rotationService.genererPlanRotation(parcelle, anneesRotationSpinner.getValue());

        // Afficher les résultats
        afficherResultats(parcelle, planRotation);

        statusLabel.setText("✅ Analyse terminée");
        statusLabel.setStyle("-fx-text-fill: #059669;");
    }

    private void afficherResultats(Parcelle parcelle, List<String> planRotation) {
        resultContainer.getChildren().clear();

        // Card résumé parcelle
        VBox resumeCard = createResumeCard(parcelle);
        resultContainer.getChildren().add(resumeCard);

        // Card plan de rotation pluriannuel
        VBox planCard = createPlanRotationCard(planRotation);
        resultContainer.getChildren().add(planCard);
    }

    private VBox createResumeCard(Parcelle parcelle) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f0fdf4); " +
                     "-fx-background-radius: 16; -fx-border-color: #d1fae5; -fx-border-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.15), 15, 0, 0, 5);");

        Label title = new Label("🌍 Parcelle - " + parcelle.getSurfaceHectares() + " ha");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        HBox infoBox = new HBox(25);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        VBox solBox = createInfoBox("Type de sol", parcelle.getTypeSolIcon() + " " + parcelle.getTypeSol());
        VBox surfaceBox = createInfoBox("Surface", parcelle.getSurfaceHectares() + " ha");
        VBox fertBox = createInfoBox("Fertilité", String.format("%.1f/10", parcelle.getFertiliteGlobale()));
        VBox phBox = createInfoBox("pH", String.valueOf(parcelle.getPh()));

        infoBox.getChildren().addAll(solBox, surfaceBox, fertBox, phBox);

        // Nutriments
        HBox nutriBox = new HBox(15);
        nutriBox.setAlignment(Pos.CENTER_LEFT);
        nutriBox.setPadding(new Insets(10));
        nutriBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10;");

        nutriBox.getChildren().addAll(
            createNutrientBadge("N", parcelle.getNiveauAzote()),
            createNutrientBadge("P", parcelle.getNiveauPhosphore()),
            createNutrientBadge("K", parcelle.getNiveauPotassium())
        );

        card.getChildren().addAll(title, infoBox, nutriBox);
        return card;
    }

    private VBox createInfoBox(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);

        Label labelL = new Label(label);
        labelL.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        Label valueL = new Label(value);
        valueL.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        box.getChildren().addAll(labelL, valueL);
        return box;
    }

    private HBox createNutrientBadge(String nutrient, int value) {
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(5, 12, 5, 12));

        String color = value >= 7 ? "#059669" : (value >= 5 ? "#f59e0b" : "#dc2626");
        badge.setStyle("-fx-background-color: " + color + "20; -fx-background-radius: 15;");

        Label nutrientL = new Label(nutrient + ":");
        nutrientL.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label valueL = new Label(value + "/10");
        valueL.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");

        badge.getChildren().addAll(nutrientL, valueL);
        return badge;
    }

    private VBox createPlanRotationCard(List<String> planRotation) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        Label title = new Label("📅 Plan de Rotation sur " + planRotation.size() + " ans");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        HBox planBox = new HBox(10);
        planBox.setAlignment(Pos.CENTER_LEFT);

        int year = java.time.LocalDate.now().getYear();
        for (int i = 0; i < planRotation.size(); i++) {
            VBox yearBox = new VBox(5);
            yearBox.setAlignment(Pos.CENTER);
            yearBox.setPadding(new Insets(10));
            yearBox.setStyle("-fx-background-color: linear-gradient(to bottom, #d1fae5, #a7f3d0); " +
                           "-fx-background-radius: 10;");

            Label yearLabel = new Label(String.valueOf(year + i));
            yearLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #065f46;");

            Label cultureLabel = new Label(capitalizeFirst(planRotation.get(i)));
            cultureLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #047857;");

            yearBox.getChildren().addAll(yearLabel, cultureLabel);
            planBox.getChildren().add(yearBox);

            // Ajouter flèche entre les années
            if (i < planRotation.size() - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-font-size: 16px; -fx-text-fill: #059669;");
                planBox.getChildren().add(arrow);
            }
        }

        card.getChildren().addAll(title, planBox);
        return card;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @FXML
    private void handleReset() {
        surfaceField.setText("5.0");
        typeSolCombo.getSelectionModel().selectFirst();
        derniereCultureCombo.getSelectionModel().selectFirst();
        avantDerniereCultureCombo.getSelectionModel().selectFirst();
        azoteSlider.setValue(5);
        phosphoreSlider.setValue(5);
        potassiumSlider.setValue(5);
        phSlider.setValue(6.5);
        anneesJachereSpinner.getValueFactory().setValue(0);
        anneesRotationSpinner.getValueFactory().setValue(4);

        resultContainer.getChildren().clear();
        statusLabel.setText("");
    }

    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }
}

