package api;

import com.google.gson.*;
import com.sun.net.httpserver.*;
import okhttp3.*;
import servise.ServiceCommentaire;
import servise.ServiceThreads;
import entities.thread;
import entities.Commentaire;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ModerationAPI {

    private static final int    PORT    = 8080;
    private static HttpServer   server;

    private static final String API_KEY        = "cmeKBOabvJHdMH4RigJBmSG6nGFvLR9X8pzwS8vL";
    private static final String URL_PROFANITY  = "https://api.api-ninjas.com/v1/profanityfilter?text=";
    private static final String URL_SENTIMENT  = "https://api.api-ninjas.com/v1/sentiment?text=";

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    // =========================================================
    //  DÉMARRER / ARRÊTER
    // =========================================================
    public static void demarrer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/threads",      new ThreadHandler());
        server.createContext("/api/commentaires", new CommentaireHandler());
        server.createContext("/api/ping",         new PingHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("✅ API Modération + Sentiment démarrée sur http://localhost:" + PORT);
        server.createContext("/api/similarite", new SimilariteHandler());
    }

    public static void arreter() {
        if (server != null) server.stop(0);
        System.out.println("API arrêtée.");
    }

    // =========================================================
    //  PING
    // =========================================================
    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            envoyerReponse(exchange, 200,
                    "{\"status\":\"ok\",\"message\":\"API FlahaSmart OK\"}");
        }
    }

    // =========================================================
    //  HANDLER THREADS
    // =========================================================
    static class ThreadHandler implements HttpHandler {
        private final ServiceThreads service = new ServiceThreads();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                envoyerReponse(exchange, 405, "{\"erreur\":\"Methode non autorisee\"}");
                return;
            }

            String body = lireBody(exchange);
            JsonObject json;
            try {
                json = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                envoyerReponse(exchange, 400, "{\"erreur\":\"JSON invalide\"}");
                return;
            }

            String titre   = json.has("titre")   ? json.get("titre").getAsString()  : "";
            String contenu = json.has("contenu")  ? json.get("contenu").getAsString() : "";
            int    idUser  = json.has("id_user")  ? json.get("id_user").getAsInt()   : 0;

            if (titre.isEmpty() || contenu.isEmpty() || idUser == 0) {
                envoyerReponse(exchange, 400,
                        "{\"erreur\":\"titre, contenu et id_user obligatoires\"}");
                return;
            }

            String texteComplet = titre + " " + contenu;

            // ===== ÉTAPE 1 : Vérification modération =====
            ResultatModeration moderation = analyserProfanity(texteComplet);

            if (moderation.inapproprie) {
                // BLOQUÉ → pas de sauvegarde
                JsonObject reponse = new JsonObject();
                JsonObject notif   = new JsonObject();
                reponse.addProperty("succes", false);
                reponse.addProperty("statut", "banni");
                notif.addProperty("type",    "ban");
                notif.addProperty("titre",   "🚫 Thread bloqué !");
                notif.addProperty("message",
                        "Votre thread n'a pas été publié.\n" +
                                moderation.raison + "\n" +
                                "Veuillez corriger votre contenu et réessayer.");
                reponse.add("notification", notif);
                System.out.println("🚫 Thread BLOQUÉ");
                envoyerReponse(exchange, 200, new Gson().toJson(reponse));
                return;
            }

            // ===== ÉTAPE 2 : Analyse sentiment =====
            String sentiment = analyserSentiment(texteComplet);
            System.out.println("😊 Sentiment détecté : " + sentiment);

            // ===== ÉTAPE 3 : Sauvegarder =====
            thread t = new thread(titre, contenu, LocalDateTime.now(), LocalDateTime.now(), idUser);
            t.setStatut("actif");
            t.setSentiment(sentiment);

            try {
                service.ajouterAvecStatut(t);

                JsonObject reponse = new JsonObject();
                JsonObject notif   = new JsonObject();

                reponse.addProperty("succes",    true);
                reponse.addProperty("statut",    "actif");
                reponse.addProperty("sentiment", sentiment);
                reponse.addProperty("id_thread", t.getId_thread());

                notif.addProperty("type",    "succes");
                notif.addProperty("titre",   "✅ Publié !");
                notif.addProperty("message", "Thread publié avec succès !");

                reponse.add("notification", notif);
                envoyerReponse(exchange, 200, new Gson().toJson(reponse));

            } catch (Exception e) {
                envoyerReponse(exchange, 500,
                        "{\"erreur\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // =========================================================
    //  HANDLER COMMENTAIRES
    // =========================================================
    static class CommentaireHandler implements HttpHandler {
        private final ServiceCommentaire service = new ServiceCommentaire();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                envoyerReponse(exchange, 405, "{\"erreur\":\"Methode non autorisee\"}");
                return;
            }

            String body = lireBody(exchange);
            JsonObject json;
            try {
                json = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                envoyerReponse(exchange, 400, "{\"erreur\":\"JSON invalide\"}");
                return;
            }

            int    idThread = json.has("id_thread") ? json.get("id_thread").getAsInt()  : 0;
            int    idUser   = json.has("id_user")   ? json.get("id_user").getAsInt()    : 0;
            String contenu  = json.has("contenu")   ? json.get("contenu").getAsString() : "";

            if (contenu.isEmpty() || idThread == 0 || idUser == 0) {
                envoyerReponse(exchange, 400,
                        "{\"erreur\":\"id_thread, id_user et contenu obligatoires\"}");
                return;
            }

            // ===== ÉTAPE 1 : Modération =====
            ResultatModeration moderation = analyserProfanity(contenu);

            if (moderation.inapproprie) {
                JsonObject reponse = new JsonObject();
                JsonObject notif   = new JsonObject();
                reponse.addProperty("succes", false);
                reponse.addProperty("statut", "banni");
                notif.addProperty("type",    "ban");
                notif.addProperty("titre",   "🚫 Commentaire bloqué !");
                notif.addProperty("message",
                        "Votre commentaire n'a pas été publié.\n" +
                                moderation.raison + "\n" +
                                "Veuillez corriger votre message et réessayer.");
                reponse.add("notification", notif);
                System.out.println("🚫 Commentaire BLOQUÉ");
                envoyerReponse(exchange, 200, new Gson().toJson(reponse));
                return;
            }

            // ===== ÉTAPE 2 : Sentiment =====
            String sentiment = analyserSentiment(contenu);
            System.out.println("😊 Sentiment détecté : " + sentiment);

            // ===== ÉTAPE 3 : Sauvegarder =====
            Commentaire c = new Commentaire(idThread, idUser, contenu, LocalDateTime.now());
            c.setStatut("actif");
            c.setSentiment(sentiment);

            try {
                service.ajouterAvecStatut(c);

                JsonObject reponse = new JsonObject();
                JsonObject notif   = new JsonObject();

                reponse.addProperty("succes",    true);
                reponse.addProperty("statut",    "actif");
                reponse.addProperty("sentiment", sentiment);

                notif.addProperty("type",    "succes");
                notif.addProperty("titre",   "✅ Publié !");
                notif.addProperty("message", "Commentaire publié avec succès !");

                reponse.add("notification", notif);
                envoyerReponse(exchange, 200, new Gson().toJson(reponse));

            } catch (Exception e) {
                envoyerReponse(exchange, 500,
                        "{\"erreur\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // =========================================================
    //  MODÉRATION - API Ninjas Profanity
    // =========================================================
    static class ResultatModeration {
        boolean inapproprie = false;
        String  raison      = "";
    }

    static ResultatModeration analyserProfanity(String texte) {
        ResultatModeration resultat = new ResultatModeration();
        try {
            String encoded = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            Request request = new Request.Builder()
                    .url(URL_PROFANITY + encoded)
                    .addHeader("X-Api-Key", API_KEY)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return resultat;

                String body = response.body().string();
                System.out.println("Profanity API : " + body);

                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                if (json.has("has_profanity") && json.get("has_profanity").getAsBoolean()) {
                    resultat.inapproprie = true;
                    String censored = json.has("censored") ? json.get("censored").getAsString() : "";
                    resultat.raison  = "Contenu inapproprié : \"" + censored + "\"";
                }
            }
        } catch (Exception e) {
            System.out.println("Profanity API inaccessible : " + e.getMessage());
        }
        return resultat;
    }

    // =========================================================
    //  SENTIMENT - API Ninjas Sentiment
    // =========================================================
    static String analyserSentiment(String texte) {
        try {
            String encoded = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            Request request = new Request.Builder()
                    .url(URL_SENTIMENT + encoded)
                    .addHeader("X-Api-Key", API_KEY)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body().string();
                // ← AFFICHE LA RÉPONSE BRUTE COMPLÈTE
                System.out.println("=== SENTIMENT BRUT === : " + body);
                System.out.println("=== CODE HTTP === : " + response.code());

                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                if (json.has("sentiment")) {
                    String s = json.get("sentiment").getAsString().toLowerCase();
                    System.out.println("=== VALEUR SENTIMENT === : " + s);
                    if (s.contains("positive")) return "positif";
                    if (s.contains("negative")) return "negatif";
                    return "neutre";
                }
            }
        } catch (Exception e) {
            System.out.println("Sentiment API erreur : " + e.getMessage());
        }
        return "neutre";
    }

    // =========================================================
    //  UTILITAIRES HTTP
    // =========================================================
    static String lireBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void envoyerReponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    // =========================================================
    //  HANDLER SIMILARITÉ : POST /api/similarite
    //  À ajouter dans ModerationAPI.java
    //  Et ajouter dans demarrer() :
    //  server.createContext("/api/similarite", new SimilariteHandler());
    // =========================================================
    static class SimilariteHandler implements HttpHandler {
        private final ServiceThreads service = new ServiceThreads();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                envoyerReponse(exchange, 405, "{\"erreur\":\"Methode non autorisee\"}");
                return;
            }

            String body = lireBody(exchange);
            JsonObject json;
            try {
                json = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                envoyerReponse(exchange, 400, "{\"erreur\":\"JSON invalide\"}");
                return;
            }

            String nouveauTitre = json.has("titre") ? json.get("titre").getAsString() : "";
            if (nouveauTitre.isEmpty()) {
                envoyerReponse(exchange, 400, "{\"erreur\":\"titre obligatoire\"}");
                return;
            }

            // Récupérer tous les threads existants
            try {
                java.util.List<entities.thread> threads = service.recuperer();

                String threadSimilaireTitre = null;
                double maxScore = 0;
                int    threadSimilaireId = 0;

                for (entities.thread t : threads) {
                    double score = calculerSimilarite(nouveauTitre, t.getTitre());
                    System.out.println("Similarité entre \"" + nouveauTitre
                            + "\" et \"" + t.getTitre() + "\" = " + Math.round(score * 100) + "%");
                    if (score > maxScore) {
                        maxScore             = score;
                        threadSimilaireTitre = t.getTitre();
                        threadSimilaireId    = t.getId_thread();
                    }
                }

                JsonObject reponse = new JsonObject();
                reponse.addProperty("score",     Math.round(maxScore * 100));

                if (maxScore >= 0.7) {
                    // Thread similaire trouvé
                    reponse.addProperty("similaire",       true);
                    reponse.addProperty("titre_similaire", threadSimilaireTitre);
                    reponse.addProperty("id_similaire",    threadSimilaireId);
                    reponse.addProperty("message",
                            "⚠️ Un thread similaire existe déjà !\n" +
                                    "Thread existant : \"" + threadSimilaireTitre + "\"\n" +
                                    "Similarité : " + Math.round(maxScore * 100) + "%\n" +
                                    "Voulez-vous quand même publier ?");
                    System.out.println("⚠️ Thread similaire détecté : " + threadSimilaireTitre
                            + " (" + Math.round(maxScore * 100) + "%)");
                } else {
                    reponse.addProperty("similaire", false);
                    reponse.addProperty("message",   "Aucun thread similaire trouvé.");
                }

                envoyerReponse(exchange, 200, new Gson().toJson(reponse));

            } catch (java.sql.SQLException e) {
                envoyerReponse(exchange, 500, "{\"erreur\":\"" + e.getMessage() + "\"}");
            }
        }

        // Calcul similarité via API Ninjas Text Similarity
        private double calculerSimilarite(String texte1, String texte2) {
            try {
                // API Ninjas Text Similarity
                JsonObject bodyJson = new JsonObject();
                bodyJson.addProperty("text_1", texte1);
                bodyJson.addProperty("text_2", texte2);

                RequestBody requestBody = RequestBody.create(
                        new Gson().toJson(bodyJson),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url("https://api.api-ninjas.com/v1/textsimilarity")
                        .addHeader("X-Api-Key", API_KEY)
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) return 0;

                    String responseBody = response.body().string();
                    System.out.println("Text Similarity API : " + responseBody);

                    // Réponse : {"similarity": 0.95}
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("similarity")) {
                        return json.get("similarity").getAsDouble();
                    }
                }
            } catch (Exception e) {
                System.out.println("Text Similarity API erreur : " + e.getMessage());
            }
            return 0;
        }
    }
}