package controllers.advancedfeatures;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import entities.advancedfeatures.IrrigationPlan;
import services.advancedfeatures.IrrigationService;

import java.time.LocalDate;

/**
 * Contrôleur pour le système intelligent d'irrigation
 */
public class IrrigationController {

    @FXML private VBox mainContainer;
    @FXML private ComboBox<String> cultureCombo;
    @FXML private TextField surfaceField;
    @FXML private TextField villeField;
    @FXML private DatePicker derniereDatePicker;
    @FXML private TextField quantitePrecedenteField;
    @FXML private VBox resultContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private IrrigationService irrigationService;

    @FXML
    public void initialize() {
        irrigationService = new IrrigationService();

        // Charger les types de cultures
        cultureCombo.getItems().addAll(irrigationService.getTypesCultures());
        cultureCombo.getSelectionModel().selectFirst();

        // Valeurs par défaut
        surfaceField.setText("1.0");
        villeField.setText("Tunis");
        quantitePrecedenteField.setText("");

        loadingIndicator.setVisible(false);

        // Configurer le DatePicker pour n'accepter que les dates passées
        derniereDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Désactiver les dates d'aujourd'hui et futures
                if (date.isEqual(LocalDate.now()) || date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fecaca; -fx-text-fill: #991b1b;");
                }
            }
        });
    }

    /**
     * Génère un plan d'irrigation
     */
    @FXML
    private void handleGenererPlan() {
        String culture = cultureCombo.getValue();
        String surfaceText = surfaceField.getText().trim();
        String ville = villeField.getText().trim();

        // Validation
        if (culture == null || culture.isEmpty()) {
            showError("Veuillez sélectionner un type de culture");
            return;
        }

        double surface;
        try {
            surface = Double.parseDouble(surfaceText);
            if (surface <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Surface invalide. Entrez un nombre positif.");
            return;
        }

        if (ville.isEmpty()) {
            showError("Veuillez entrer une ville pour la météo");
            return;
        }

        // Récupérer et valider les critères d'historique
        LocalDate derniereDate = derniereDatePicker.getValue();

        // Validation: la date doit être dans le passé (ni présent ni futur)
        if (derniereDate != null) {
            if (derniereDate.isEqual(LocalDate.now()) || derniereDate.isAfter(LocalDate.now())) {
                showError("La dernière date d'irrigation doit être dans le passé (avant aujourd'hui)");
                return;
            }
        }

        // Validation: si une date est saisie, la quantité est obligatoire
        String quantiteText = quantitePrecedenteField.getText().trim();
        double quantitePrecedente = 0;

        if (derniereDate != null) {
            if (quantiteText.isEmpty()) {
                showError("La quantité d'eau précédente est obligatoire si vous avez saisi une date");
                return;
            }
            try {
                quantitePrecedente = Double.parseDouble(quantiteText);
                if (quantitePrecedente <= 0) {
                    showError("La quantité d'eau précédente doit être un nombre positif");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Quantité d'eau invalide. Entrez un nombre positif (ex: 5000)");
                return;
            }
        } else if (!quantiteText.isEmpty()) {
            // Si quantité saisie mais pas de date, avertir
            showError("Veuillez saisir la dernière date d'irrigation pour utiliser la quantité précédente");
            return;
        }

        // Afficher chargement
        loadingIndicator.setVisible(true);
        statusLabel.setText("🔄 Analyse en cours...");
        statusLabel.setStyle("-fx-text-fill: #6b7280;");
        resultContainer.getChildren().clear();

        // Variables finales pour le thread
        final LocalDate finalDerniereDate = derniereDate;
        final double finalQuantitePrecedente = quantitePrecedente;

        // Générer le plan dans un thread séparé
        new Thread(() -> {
            IrrigationPlan plan = irrigationService.genererPlanIrrigation(
                culture, surface, ville, finalDerniereDate, finalQuantitePrecedente
            );

            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                statusLabel.setText("✅ Plan généré avec succès");
                statusLabel.setStyle("-fx-text-fill: #059669;");

                displayIrrigationPlan(plan);
            });
        }).start();
    }

    /**
     * Affiche le plan d'irrigation généré
     */
    private void displayIrrigationPlan(IrrigationPlan plan) {
        resultContainer.getChildren().clear();

        // Card principale
        VBox mainCard = new VBox(15);
        mainCard.setPadding(new Insets(25));
        mainCard.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f0fdf4); " +
                         "-fx-background-radius: 16; " +
                         "-fx-border-color: #d1fae5; " +
                         "-fx-border-radius: 16; " +
                         "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.2), 20, 0, 0, 8);");

        // En-tête avec priorité
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Badge priorité
        Label prioriteBadge = new Label(plan.getPrioriteIcon() + " " + plan.getPriorite());
        String prioriteColor = plan.getPriorite().equals("URGENT") ? "#dc2626" :
                              (plan.getPriorite().equals("NORMAL") ? "#f59e0b" : "#10b981");
        prioriteBadge.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + prioriteColor + "; " +
                              "-fx-background-color: " + prioriteColor + "22; " +
                              "-fx-padding: 5 15; -fx-background-radius: 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Culture
        Label cultureLabel = new Label("🌾 " + plan.getTypeCulture().toUpperCase());
        cultureLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        header.getChildren().addAll(cultureLabel, spacer, prioriteBadge);

        // Grille d'informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30);
        infoGrid.setVgap(15);

        // Ligne 1
        addInfoItem(infoGrid, 0, 0, "📅 Date", plan.getDateIrrigation().toString());
        addInfoItem(infoGrid, 1, 0, "⏰ Heure optimale", plan.getHeureOptimale());
        addInfoItem(infoGrid, 2, 0, "⏱ Durée", plan.getDureeMinutes() + " min");

        // Ligne 2
        addInfoItem(infoGrid, 0, 1, "💧 Besoin en eau", plan.getBesoinEauFormate());
        addInfoItem(infoGrid, 1, 1, "📐 Surface", plan.getSurfaceHectares() + " ha");
        addInfoItem(infoGrid, 2, 1, "🌤 Météo", plan.getConditionMeteo());

        // Données météo détaillées
        HBox meteoBox = new HBox(20);
        meteoBox.setAlignment(Pos.CENTER_LEFT);
        meteoBox.setPadding(new Insets(15));
        meteoBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 12;");

        addMeteoStat(meteoBox, "🌡", plan.getTemperature() + "°C", "Température");
        addMeteoStat(meteoBox, "💨", plan.getHumiditeAir() + "%", "Humidité air");
        addMeteoStat(meteoBox, "💧", plan.getHumiditeSol() + "%", "Humidité sol");
        addMeteoStat(meteoBox, "🌧", plan.getPrecipitationsPrevues() + "mm", "Précipitations");

        // Données historique irrigation
        HBox historiqueBox = new HBox(20);
        historiqueBox.setAlignment(Pos.CENTER_LEFT);
        historiqueBox.setPadding(new Insets(15));
        historiqueBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 12;");

        // Dernière irrigation
        String joursDepuisText = plan.getJoursDepuisDerniereIrrigation() >= 0
            ? plan.getJoursDepuisDerniereIrrigation() + " j"
            : "N/A";
        addMeteoStat(historiqueBox, "📅", joursDepuisText, "Depuis dernière");

        // Quantité précédente
        String quantiteText = plan.getQuantiteEauPrecedente() > 0
            ? plan.getQuantiteEauPrecedenteFormate()
            : "N/A";
        addMeteoStat(historiqueBox, "💦", quantiteText, "Qté précédente");

        mainCard.getChildren().addAll(header, infoGrid, meteoBox, historiqueBox);
        resultContainer.getChildren().add(mainCard);
    }

    private void addInfoItem(GridPane grid, int col, int row, String title, String value) {
        VBox box = new VBox(3);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        box.getChildren().addAll(titleLabel, valueLabel);
        grid.add(box, col, row);
    }

    private void addMeteoStat(HBox container, String icon, String value, String label) {
        VBox stat = new VBox(2);
        stat.setAlignment(Pos.CENTER);
        stat.setPadding(new Insets(5, 15, 5, 15));

        Label iconLabel = new Label(icon + " " + value);
        iconLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

        stat.getChildren().addAll(iconLabel, labelLabel);
        container.getChildren().add(stat);
    }

    /**
     * Crée une opération d'irrigation à partir du plan
     */


    private void showError(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }
}
