package controllers.advancedfeatures;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import entities.advancedfeatures.Weather;
import services.advancedfeatures.WeatherService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherWidgetController {

    private static final Logger LOGGER = Logger.getLogger(WeatherWidgetController.class.getName());

    @FXML
    private VBox weatherContainer;

    @FXML
    private TextField cityInput;

    @FXML
    private Label locationLabel;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label humidityLabel;

    @FXML
    private Label windLabel;

    @FXML
    private Label feelsLikeLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private ImageView weatherIcon;

    @FXML
    private Label loadingLabel;

    @FXML
    private VBox tipsContainer;

    private WeatherService weatherService;

    @FXML
    public void initialize() {
        weatherService = new WeatherService();
        loadDefaultWeather();
    }

    private void loadDefaultWeather() {
        loadWeather("Tunis");
    }

    @FXML
    private void onSearchWeather() {
        String city = cityInput.getText();
        if (city != null && !city.trim().isEmpty()) {
            loadWeather(city.trim());
        }
    }

    private void loadWeather(String city) {
        showLoading(true);

        Task<Weather> task = new Task<Weather>() {
            @Override
            protected Weather call() throws Exception {
                return weatherService.getWeather(city);
            }
        };

        task.setOnSucceeded(event -> {
            Weather weather = task.getValue();
            if (weather != null) {
                updateWeatherDisplay(weather);
            } else {
                showError("Impossible de récupérer les données météo");
            }
            showLoading(false);
        });

        task.setOnFailed(event -> {
            showError("Erreur lors du chargement");
            showLoading(false);
            LOGGER.log(Level.SEVERE, "Erreur task météo", task.getException());
        });

        new Thread(task).start();
    }

    private void updateWeatherDisplay(Weather weather) {
        Platform.runLater(() -> {
            locationLabel.setText(weather.getLocation() + ", " + weather.getCountry());
            temperatureLabel.setText(weather.getTemperature() + "°C");
            descriptionLabel.setText(weather.getDescription());

            // Mise à jour des labels avec vérification null
            if (humidityLabel != null) {
                humidityLabel.setText(weather.getHumidity() + "%");
            }
            if (windLabel != null) {
                windLabel.setText(weather.getWindSpeed() + " km/h");
            }
            if (feelsLikeLabel != null) {
                feelsLikeLabel.setText(weather.getFeelsLike() + "°C");
            }
            if (timeLabel != null) {
                timeLabel.setText("🕐 Mise à jour: " + weather.getLocalTime());
            }

            // Générer et afficher les conseils agricoles dynamiques
            updateAgriculturalTips(weather);
        });
    }

    /**
     * Génère des conseils agricoles professionnels basés sur les données météo
     */
    private void updateAgriculturalTips(Weather weather) {
        if (tipsContainer == null) return;

        tipsContainer.getChildren().clear();
        List<AgriculturalTip> tips = generateTips(weather);

        for (AgriculturalTip tip : tips) {
            HBox tipItem = createTipItem(tip);
            tipsContainer.getChildren().add(tipItem);
        }
    }

    private List<AgriculturalTip> generateTips(Weather weather) {
        List<AgriculturalTip> tips = new ArrayList<>();
        int temp = weather.getTemperature();
        int humidity = weather.getHumidity();
        double wind = weather.getWindSpeed();
        int feelsLike = weather.getFeelsLike();
        String description = weather.getDescription().toLowerCase();

        // === ANALYSE DE LA TEMPÉRATURE ===
        if (temp >= 35) {
            tips.add(new AgriculturalTip("ALERTE CANICULE",
                "Température extrême de " + temp + "°C. Irriguer abondamment 2x/jour (matin 6h et soir 20h). " +
                "Installer des filets d'ombrage. Suspendre toute transplantation.", "critical"));
        } else if (temp >= 30) {
            tips.add(new AgriculturalTip("Chaleur intense",
                "Avec " + temp + "°C, augmentez l'irrigation de 30-40%. Paillez les cultures pour conserver l'humidité. " +
                "Évitez les travaux du sol entre 11h et 16h.", "warning"));
        } else if (temp >= 25) {
            tips.add(new AgriculturalTip("Conditions estivales",
                "Température de " + temp + "°C idéale pour tomates, poivrons, aubergines. " +
                "Maintenez une irrigation régulière le matin.", "good"));
        } else if (temp >= 15) {
            tips.add(new AgriculturalTip("Température optimale",
                "Conditions idéales (" + temp + "°C) pour la plupart des cultures maraîchères. " +
                "Moment propice pour semis et plantations.", "optimal"));
        } else if (temp >= 5) {
            tips.add(new AgriculturalTip("Temps frais",
                "À " + temp + "°C, privilégiez les cultures résistantes au froid : choux, épinards, carottes. " +
                "Réduisez l'arrosage de 20%.", "info"));
        } else if (temp >= 0) {
            tips.add(new AgriculturalTip("RISQUE DE GEL",
                "Température proche de 0°C (" + temp + "°C). Protégez les cultures sensibles avec voiles d'hivernage. " +
                "Rentrez les plantes en pot. Évitez d'arroser le soir.", "critical"));
        } else {
            tips.add(new AgriculturalTip("GEL CONFIRMÉ",
                "Gel à " + temp + "°C! Aucune intervention extérieure. Vérifiez les protections des cultures permanentes. " +
                "Reportez tous les travaux agricoles.", "critical"));
        }

        // === ANALYSE DE L'HUMIDITÉ ===
        if (humidity >= 85) {
            tips.add(new AgriculturalTip("Humidité très élevée",
                "Avec " + humidity + "% d'humidité, risque élevé de mildiou et oïdium. " +
                "Espacez les plants, évitez l'arrosage foliaire. Traitez préventivement au cuivre.", "warning"));
        } else if (humidity >= 70) {
            tips.add(new AgriculturalTip("Humidité importante",
                humidity + "% d'humidité. Surveillez les maladies fongiques. " +
                "Binez régulièrement pour aérer le sol. Arrosez au pied uniquement.", "info"));
        } else if (humidity >= 40) {
            tips.add(new AgriculturalTip("Humidité idéale",
                "Humidité de " + humidity + "% parfaite pour la croissance. " +
                "Conditions optimales pour les traitements foliaires.", "optimal"));
        } else {
            tips.add(new AgriculturalTip("Air très sec",
                "Seulement " + humidity + "% d'humidité. Augmentez la fréquence d'irrigation. " +
                "Brumisez les cultures fragiles. Attention au stress hydrique.", "warning"));
        }

        // === ANALYSE DU VENT ===
        if (wind >= 40) {
            tips.add(new AgriculturalTip("VENT VIOLENT",
                "Vent de " + wind + " km/h! Suspendez toute pulvérisation. " +
                "Tuteurez et protégez les cultures hautes. Reportez les semis.", "critical"));
        } else if (wind >= 25) {
            tips.add(new AgriculturalTip("Vent fort",
                "Vent de " + wind + " km/h. Évitez les traitements phytosanitaires (dérive importante). " +
                "Renforcez les tuteurs et protections.", "warning"));
        } else if (wind >= 15) {
            tips.add(new AgriculturalTip("Vent modéré",
                "Vent de " + wind + " km/h. Traitements possibles avec précaution. " +
                "Bonne ventilation réduisant les maladies fongiques.", "info"));
        } else {
            tips.add(new AgriculturalTip("Vent faible",
                "Vent calme (" + wind + " km/h). Conditions idéales pour pulvérisations et traitements. " +
                "Attention à la stagnation d'humidité.", "optimal"));
        }

        // === ANALYSE DES CONDITIONS MÉTÉO (description) ===
        if (description.contains("rain") || description.contains("pluie") || description.contains("shower")) {
            tips.add(new AgriculturalTip("Pluie détectée",
                "Suspendez l'irrigation manuelle. Reportez les traitements de 24-48h après la pluie. " +
                "Vérifiez le drainage des parcelles. Opportunité de repiquage!", "info"));
        } else if (description.contains("cloud") || description.contains("nuage") || description.contains("overcast")) {
            tips.add(new AgriculturalTip("Temps couvert",
                "Lumière diffuse favorable aux semis et repiquages. " +
                "Réduisez légèrement l'arrosage. Moment idéal pour le désherbage.", "good"));
        } else if (description.contains("sun") || description.contains("clear") || description.contains("soleil")) {
            tips.add(new AgriculturalTip("Temps ensoleillé",
                "Photosynthèse maximale. Arrosez tôt le matin (avant 9h) ou tard le soir (après 19h). " +
                "Évitez les transplantations aux heures chaudes.", "good"));
        }

        // === ANALYSE RESSENTI vs RÉEL ===
        int diffRessentie = feelsLike - temp;
        if (diffRessentie <= -5) {
            tips.add(new AgriculturalTip("Refroidissement éolien",
                "Ressenti de " + feelsLike + "°C (vs " + temp + "°C réel). Le vent accentue le froid. " +
                "Les cultures fragiles peuvent souffrir davantage.", "info"));
        } else if (diffRessentie >= 5) {
            tips.add(new AgriculturalTip("Chaleur ressentie accrue",
                "Ressenti de " + feelsLike + "°C (vs " + temp + "°C réel). Stress thermique probable. " +
                "Renforcez l'ombrage et l'hydratation des cultures.", "warning"));
        }

        // === CONSEIL DE PLANIFICATION ===
        tips.add(new AgriculturalTip("Recommandation du jour",
            generateDailyRecommendation(temp, humidity, wind, description), "planning"));

        return tips;
    }

    private String generateDailyRecommendation(int temp, int humidity, double wind, String description) {
        StringBuilder reco = new StringBuilder();

        // Calcul d'un score de conditions
        int score = 100;
        if (temp < 5 || temp > 35) score -= 40;
        else if (temp < 10 || temp > 30) score -= 20;
        if (humidity > 85 || humidity < 30) score -= 20;
        if (wind > 25) score -= 30;
        else if (wind > 15) score -= 10;
        if (description.contains("rain")) score -= 15;

        if (score >= 80) {
            reco.append("EXCELLENTES CONDITIONS - ");
            reco.append("Journée idéale pour : semis, plantations, traitements, récoltes. Profitez-en!");
        } else if (score >= 60) {
            reco.append("CONDITIONS ACCEPTABLES - ");
            reco.append("Travaux légers possibles. Évitez les interventions sensibles aux heures extrêmes.");
        } else if (score >= 40) {
            reco.append("CONDITIONS DIFFICILES - ");
            reco.append("Limitez-vous à l'entretien courant. Surveillez attentivement vos cultures.");
        } else {
            reco.append("CONDITIONS DÉFAVORABLES - ");
            reco.append("Reportez les travaux agricoles. Focus sur la protection des cultures en place.");
        }

        return reco.toString();
    }

    private HBox createTipItem(AgriculturalTip tip) {
        HBox tipBox = new HBox(12);
        tipBox.setAlignment(Pos.CENTER_LEFT);
        tipBox.setPadding(new Insets(14, 18, 14, 18));

        // Style selon le niveau
        String bgColor, borderColor, titleColor;
        switch (tip.level) {
            case "critical":
                bgColor = "#fef2f2"; borderColor = "#ef4444"; titleColor = "#dc2626";
                break;
            case "warning":
                bgColor = "#fffbeb"; borderColor = "#f59e0b"; titleColor = "#d97706";
                break;
            case "optimal":
                bgColor = "#f0fdf4"; borderColor = "#22c55e"; titleColor = "#16a34a";
                break;
            case "good":
                bgColor = "#ecfdf5"; borderColor = "#10b981"; titleColor = "#059669";
                break;
            case "planning":
                bgColor = "#eff6ff"; borderColor = "#3b82f6"; titleColor = "#2563eb";
                break;
            default:
                bgColor = "#f9fafb"; borderColor = "#6b7280"; titleColor = "#4b5563";
        }

        tipBox.setStyle("-fx-background-color: " + bgColor + "; " +
                       "-fx-border-color: " + borderColor + "; " +
                       "-fx-border-width: 0 0 0 5; " +
                       "-fx-background-radius: 8; " +
                       "-fx-border-radius: 8;");

        // Contenu sans icône
        VBox contentBox = new VBox(6);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(tip.title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");

        Label descLabel = new Label(tip.description);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(350);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563; -fx-line-spacing: 2;");

        contentBox.getChildren().addAll(titleLabel, descLabel);
        tipBox.getChildren().add(contentBox);

        return tipBox;
    }

    /**
     * Classe interne pour représenter un conseil agricole
     */
    private static class AgriculturalTip {
        String title;
        String description;
        String level; // critical, warning, optimal, good, info, planning

        AgriculturalTip(String title, String description, String level) {
            this.title = title;
            this.description = description;
            this.level = level;
        }
    }

    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            if (loadingLabel != null) {
                loadingLabel.setVisible(show);
                loadingLabel.setManaged(show);
            }
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            locationLabel.setText(message);
            temperatureLabel.setText("--");
            descriptionLabel.setText("");
        });
    }
}

