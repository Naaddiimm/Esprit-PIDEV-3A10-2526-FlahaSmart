package services.advancedfeatures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la détection de maladies des plantes via l'API PlantNet
 */
public class PlantDiseaseService {

    private static final String API_KEY = "2b10m96CfdIir4kW0Mq6l2Xru";
    private static final String API_URL = "https://my-api.plantnet.org/v2/identify/all";

    /**
     * Classe interne pour représenter un résultat d'identification
     */
    public static class PlantIdentificationResult {
        private String scientificName;
        private String commonName;
        private double score;
        private String family;
        private String genus;
        private List<String> imageUrls;

        public PlantIdentificationResult(String scientificName, String commonName,
                                         double score, String family, String genus) {
            this.scientificName = scientificName;
            this.commonName = commonName;
            this.score = score;
            this.family = family;
            this.genus = genus;
            this.imageUrls = new ArrayList<>();
        }

        // Getters
        public String getScientificName() { return scientificName; }
        public String getCommonName() { return commonName; }
        public double getScore() { return score; }
        public String getFamily() { return family; }
        public String getGenus() { return genus; }
        public List<String> getImageUrls() { return imageUrls; }

        public void addImageUrl(String url) { imageUrls.add(url); }

        public String getScorePercentage() {
            return String.format("%.1f%%", score * 100);
        }
    }

    /**
     * Identifie une plante à partir d'une image
     * @param imagePath Chemin vers l'image de la plante
     * @return Liste des résultats d'identification
     */
    public List<PlantIdentificationResult> identifyPlant(String imagePath) throws Exception {
        List<PlantIdentificationResult> results = new ArrayList<>();

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new FileNotFoundException("L'image n'existe pas: " + imagePath);
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

        URL url = new URL(API_URL + "?include-related-images=true&no-reject=false&lang=fr&api-key=" + API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            // Ajouter l'image
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"images\"; filename=\"")
                  .append(imageFile.getName()).append("\"").append("\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append("\r\n");
            writer.append("\r\n");
            writer.flush();

            Files.copy(imageFile.toPath(), outputStream);
            outputStream.flush();

            writer.append("\r\n");

            // Ajouter l'organe (leaf par défaut - feuille)
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"organs\"").append("\r\n");
            writer.append("\r\n");
            writer.append("leaf").append("\r\n");

            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                results = parseResponse(response.toString());
            }
        } else {
            // Lire le message d'erreur
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new Exception("Erreur API PlantNet (" + responseCode + "): " + errorResponse.toString());
            }
        }

        connection.disconnect();
        return results;
    }

    /**
     * Parse la réponse JSON de l'API PlantNet
     */
    private List<PlantIdentificationResult> parseResponse(String jsonResponse) {
        List<PlantIdentificationResult> results = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("results")) {
                JsonArray resultsArray = root.getAsJsonArray("results");

                for (JsonElement element : resultsArray) {
                    JsonObject result = element.getAsJsonObject();

                    double score = result.has("score") ? result.get("score").getAsDouble() : 0;

                    JsonObject species = result.has("species") ?
                            result.getAsJsonObject("species") : new JsonObject();

                    String scientificName = species.has("scientificNameWithoutAuthor") ?
                            species.get("scientificNameWithoutAuthor").getAsString() : "Inconnu";

                    // Récupérer le nom commun en français
                    String commonName = "Nom commun non disponible";
                    if (species.has("commonNames")) {
                        JsonArray commonNames = species.getAsJsonArray("commonNames");
                        if (commonNames.size() > 0) {
                            commonName = commonNames.get(0).getAsString();
                        }
                    }

                    // Récupérer la famille
                    String family = "Famille inconnue";
                    if (species.has("family") && species.getAsJsonObject("family").has("scientificNameWithoutAuthor")) {
                        family = species.getAsJsonObject("family").get("scientificNameWithoutAuthor").getAsString();
                    }

                    // Récupérer le genre
                    String genus = "Genre inconnu";
                    if (species.has("genus") && species.getAsJsonObject("genus").has("scientificNameWithoutAuthor")) {
                        genus = species.getAsJsonObject("genus").get("scientificNameWithoutAuthor").getAsString();
                    }

                    PlantIdentificationResult identResult = new PlantIdentificationResult(
                            scientificName, commonName, score, family, genus
                    );

                    // Récupérer les images de référence
                    if (result.has("images")) {
                        JsonArray images = result.getAsJsonArray("images");
                        for (JsonElement imgElement : images) {
                            JsonObject imgObj = imgElement.getAsJsonObject();
                            if (imgObj.has("url") && imgObj.getAsJsonObject("url").has("m")) {
                                identResult.addImageUrl(imgObj.getAsJsonObject("url").get("m").getAsString());
                            }
                        }
                    }

                    results.add(identResult);

                    // Limiter à 5 résultats
                    if (results.size() >= 5) break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Fournit des conseils basés sur l'identification de la plante
     */
    public String getPlantAdvice(PlantIdentificationResult result) {
        StringBuilder advice = new StringBuilder();

        if (result.getScore() > 0.5) {
            advice.append("✅ Identification fiable (").append(result.getScorePercentage()).append(")\n\n");
        } else if (result.getScore() > 0.2) {
            advice.append("⚠️ Identification possible (").append(result.getScorePercentage()).append(")\n\n");
        } else {
            advice.append("❓ Identification incertaine (").append(result.getScorePercentage()).append(")\n\n");
        }

        advice.append("📌 Conseils généraux pour cette plante:\n");
        advice.append("• Surveillez les signes de maladies sur les feuilles\n");
        advice.append("• Vérifiez l'humidité du sol régulièrement\n");
        advice.append("• Assurez-vous d'un bon drainage\n");
        advice.append("• Consultez un expert agricole pour un diagnostic précis");

        return advice.toString();
    }
}

