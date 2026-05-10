package controllers.produit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ApiConsommationDetailController {

    @FXML private Label productNameLabel;
    @FXML private TextArea detailsArea;
    @FXML private ProgressIndicator loadingIndicator; // optionnel, à ajouter dans le FXML

    private String productName;
    private int productId;

    // 🔑 Votre clé API USDA
    private static final String API_KEY = "gXHxasRpCovOf5XR3Wh9IyLHq3dhX2oIMcRAsXL3";
    private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1/";

    // Map de traduction français -> anglais pour les produits courants
    private static final Map<String, String> TRANSLATION_MAP = new HashMap<>();
    static {
        TRANSLATION_MAP.put("maïs", "corn");
        TRANSLATION_MAP.put("tomate", "tomato");
        TRANSLATION_MAP.put("pomme de terre", "potato");
        TRANSLATION_MAP.put("salade", "lettuce");
        TRANSLATION_MAP.put("carotte", "carrot");
        TRANSLATION_MAP.put("fraise", "strawberry");
        TRANSLATION_MAP.put("blé", "wheat");
        TRANSLATION_MAP.put("orge", "barley");
        TRANSLATION_MAP.put("tournesol", "sunflower");
        TRANSLATION_MAP.put("pois", "pea");
        // Ajoutez d'autres traductions si nécessaire
    }

    public void setProductName(String name) {
        this.productName = name;
        productNameLabel.setText("Détails pour : " + name);
        loadDataFromAPI(name);
    }

    public void setProductId(int id) {
        this.productId = id;
    }

    private void loadDataFromAPI(String productFullName) {
        // Afficher un indicateur de chargement
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        detailsArea.setText("Recherche en cours...");

        // Lancer l'appel API dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Extraire le type de produit (ex: "Maïs" depuis "Maïs Denté")
                String typeProduit = extractTypeProduit(productFullName);
                String searchTerm = translateToEnglish(typeProduit);

                HttpClient client = HttpClient.newHttpClient();
                ObjectMapper mapper = new ObjectMapper();

                // 1. Recherche de l'aliment
                String searchUrl = BASE_URL + "foods/search?api_key=" + API_KEY + "&query=" + searchTerm;
                HttpRequest searchRequest = HttpRequest.newBuilder()
                        .uri(URI.create(searchUrl))
                        .build();

                HttpResponse<String> searchResponse = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());
                JsonNode searchJson = mapper.readTree(searchResponse.body());
                JsonNode foods = searchJson.get("foods");

                StringBuilder resultBuilder = new StringBuilder();

                if (foods != null && foods.size() > 0) {
                    // Prendre le premier résultat (le plus pertinent)
                    JsonNode firstFood = foods.get(0);
                    String fdcId = firstFood.get("fdcId").asText();
                    String description = firstFood.get("description").asText();

                    resultBuilder.append("🔍 Aliment trouvé : ").append(description).append("\n\n");

                    // 2. Récupérer les détails complets
                    String detailUrl = BASE_URL + "food/" + fdcId + "?api_key=" + API_KEY;
                    HttpRequest detailRequest = HttpRequest.newBuilder()
                            .uri(URI.create(detailUrl))
                            .build();

                    HttpResponse<String> detailResponse = client.send(detailRequest, HttpResponse.BodyHandlers.ofString());
                    JsonNode detailJson = mapper.readTree(detailResponse.body());

                    // Catégorie
                    JsonNode category = detailJson.get("foodCategory");
                    if (category != null) {
                        resultBuilder.append("📂 Catégorie : ").append(category.get("description").asText()).append("\n");
                    }

                    // Nutriments principaux (pour 100g)
                    JsonNode nutrients = detailJson.get("foodNutrients");
                    resultBuilder.append("\n🥗 **Valeurs nutritionnelles pour 100g :**\n");
                    int count = 0;
                    for (JsonNode n : nutrients) {
                        if (count >= 15) break; // Limiter l'affichage pour éviter la surcharge
                        String nutrientName = n.get("nutrient").get("name").asText();
                        double amount = n.get("amount").asDouble();
                        String unit = n.get("nutrient").get("unitName").asText();
                        resultBuilder.append("• ").append(nutrientName).append(" : ")
                                .append(amount).append(" ").append(unit).append("\n");
                        count++;
                    }

                    // Ajouter d'autres infos si disponibles (ex: composants, etc.)
                    JsonNode foodComponents = detailJson.get("foodComponents");
                    if (foodComponents != null && foodComponents.size() > 0) {
                        resultBuilder.append("\n🔬 Composants :\n");
                        for (JsonNode comp : foodComponents) {
                            resultBuilder.append("• ").append(comp.get("name").asText()).append("\n");
                        }
                    }

                } else {
                    resultBuilder.append("❌ Aucun résultat trouvé pour \"").append(productFullName).append("\".\n");
                    resultBuilder.append("Essayez avec un terme plus générique (ex: 'tomate' au lieu de 'tomate coeur de boeuf').");
                }

                // Mise à jour de l'interface JavaFX sur le thread UI
                Platform.runLater(() -> {
                    detailsArea.setText(resultBuilder.toString());
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    detailsArea.setText("Erreur lors de l'appel API : " + e.getMessage());
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    /**
     * Extrait le type de produit à partir du nom complet (ex: "Maïs Denté" -> "Maïs")
     */
    private String extractTypeProduit(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        // Prendre le premier mot (séparé par un espace)
        String[] parts = fullName.split(" ");
        return parts[0].toLowerCase();
    }

    /**
     * Traduit le type de produit en anglais pour la recherche API
     */
    private String translateToEnglish(String frenchTerm) {
        return TRANSLATION_MAP.getOrDefault(frenchTerm, frenchTerm);
    }
}