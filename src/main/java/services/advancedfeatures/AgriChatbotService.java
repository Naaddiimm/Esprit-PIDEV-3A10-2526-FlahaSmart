package services.advancedfeatures;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AgriChatbotService {

    private static final String API_KEY = "";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final HttpClient httpClient;
    private final Gson gson;
    private final List<JsonObject> conversationHistory;

    // Contexte système pour orienter les réponses.
    private static final String SYSTEM_CONTEXT = """
        Tu es AgriBot , un assistant agricole intelligent et expert en agriculture.
        Tu dois répondre uniquement aux questions liées à l'agriculture, l'agronomie, et les sujets connexes.
     
        Tes domaines d'expertise incluent :
        - Cultures et plantations (semis, rotation, calendrier)
        - Irrigation et gestion de l'eau
        - Météo et son impact sur l'agriculture
        - Équipements agricoles (tracteurs, serres, drones, capteurs IoT)
        - Fertilisation et engrais (NPK, compost, fumier)
        - Maladies des plantes et parasites
        - Qualité du sol et compostage
        - Agriculture biologique et durable
        - Technologies agricoles modernes (agriculture de précision, IoT)
        - Récolte et conservation
        - Élevage et animaux de ferme
        
        Instructions importantes :
        1. Réponds toujours en français
        2. Sois amical et utilise des emojis appropriés (🌾, 🚜, 💧, 🌱, etc.)
        3. Donne des conseils pratiques et concrets
        4. Si la question n'est pas liée à l'agriculture, explique poliment que tu es spécialisé en agriculture et propose ton aide sur ce sujet
        5. Formate tes réponses de manière claire avec des listes à puces quand c'est pertinent
        6. Adapte tes conseils selon la saison actuelle (nous sommes en février)
        """;

    public AgriChatbotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
    }

    public String getResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Désolé, je n'ai pas compris. Pouvez-vous reformuler votre question ?";
        }

        try {
            // Construire le corps de la requête
            JsonObject requestBody = buildRequestBody(userMessage);

            // Créer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            // Envoyer la requête et obtenir la réponse
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String aiResponse = parseGeminiResponse(response.body());

                // Ajouter à l'historique de conversation
                addToHistory("user", userMessage);
                addToHistory("model", aiResponse);

                return aiResponse;
            } else {
                System.err.println("Erreur API Gemini: " + response.statusCode() + " - " + response.body());
                return getFallbackResponse(userMessage);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API Gemini: " + e.getMessage());
            e.printStackTrace();
            return getFallbackResponse(userMessage);
        }
    }

    private JsonObject buildRequestBody(String userMessage) {
        JsonObject requestBody = new JsonObject();

        // Configuration de génération
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 1024);
        generationConfig.addProperty("topP", 0.95);
        requestBody.add("generationConfig", generationConfig);

        // Instructions système
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemTextPart = new JsonObject();
        systemTextPart.addProperty("text", SYSTEM_CONTEXT);
        systemParts.add(systemTextPart);
        systemInstruction.add("parts", systemParts);
        requestBody.add("systemInstruction", systemInstruction);

        // Contenu de la conversation
        JsonArray contents = new JsonArray();

        // Ajouter l'historique de conversation (limité aux 10 derniers messages)
        int startIndex = Math.max(0, conversationHistory.size() - 10);
        for (int i = startIndex; i < conversationHistory.size(); i++) {
            contents.add(conversationHistory.get(i));
        }

        // Ajouter le nouveau message utilisateur
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userTextPart = new JsonObject();
        userTextPart.addProperty("text", userMessage);
        userParts.add(userTextPart);
        userContent.add("parts", userParts);
        contents.add(userContent);

        requestBody.add("contents", contents);

        return requestBody;
    }

    private void addToHistory(String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", role);
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", text);
        parts.add(textPart);
        content.add("parts", parts);
        conversationHistory.add(content);

        // Limiter l'historique à 20 messages pour éviter de dépasser les limites
        if (conversationHistory.size() > 20) {
            conversationHistory.remove(0);
            conversationHistory.remove(0);
        }
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("candidates")) {
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    if (firstCandidate.has("content")) {
                        JsonObject content = firstCandidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject firstPart = parts.get(0).getAsJsonObject();
                                if (firstPart.has("text")) {
                                    return firstPart.get("text").getAsString();
                                }
                            }
                        }
                    }
                }
            }

            return "Désolé, je n'ai pas pu générer une réponse. Veuillez réessayer.";

        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de la réponse: " + e.getMessage());
            return "Erreur lors du traitement de la réponse. Veuillez réessayer.";
        }
    }

    /**
     * Réponse de secours en cas d'erreur avec l'API
     */
    private String getFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        if (message.matches(".*(bonjour|salut|hello|hi|hey|bonsoir).*")) {
            return "Bonjour ! 🌾 Je suis AgriBot, votre assistant agricole. " +
                   "Je rencontre actuellement des difficultés de connexion, mais je reste à votre service !";
        }

        if (message.matches(".*(aide|help|menu).*")) {
            return "ℹ️ Je peux vous aider sur :\n\n" +
                   "🌾 Cultures et plantations\n" +
                   "💧 Irrigation et gestion de l'eau\n" +
                   "🌤️ Impact de la météo\n" +
                   "🚜 Équipements agricoles\n" +
                   "🌿 Fertilisation et engrais\n" +
                   "🐛 Maladies et parasites\n" +
                   "🌍 Qualité du sol et compost\n\n" +
                   "⚠️ Note: La connexion au serveur IA est temporairement indisponible.";
        }

        return " Je rencontre actuellement des difficultés de connexion avec le serveur IA.\n\n" +
               "Veuillez réessayer dans quelques instants ou vérifier votre connexion internet.\n\n" +
               "En attendant, je reste votre assistant agricole ! 🌱";
    }

    /**
     * Réinitialiser l'historique de conversation
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * Vérifier si le service est connecté
     */
    public boolean isConnected() {
        try {
            JsonObject testRequest = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            content.addProperty("role", "user");
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", "test");
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            testRequest.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(testRequest)))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}
