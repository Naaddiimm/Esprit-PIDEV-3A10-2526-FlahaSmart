package api;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON  = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .build();

    // ===== Résultat publication =====
    public static class ResultatAPI {
        public boolean succes;
        public String  statut;
        public String  typeNotif;
        public String  titreNotif;
        public String  messageNotif;
    }

    // ===== Résultat similarité =====
    public static class ResultatSimilarite {
        public boolean similaire;
        public int     score;
        public String  titreSimilaire;
        public int     idSimilaire;
        public String  message;
    }

    // ===== Publier un thread =====
    public static ResultatAPI publierThread(String titre, String contenu, int idUser) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("titre",   titre);
        body.addProperty("contenu", contenu);
        body.addProperty("id_user", idUser);
        return appelerAPI("/threads", body);
    }

    // ===== Publier un commentaire =====
    public static ResultatAPI publierCommentaire(int idThread, int idUser, String contenu) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("id_thread", idThread);
        body.addProperty("id_user",   idUser);
        body.addProperty("contenu",   contenu);
        return appelerAPI("/commentaires", body);
    }

    // ===== Vérifier similarité =====
    public static ResultatSimilarite verifierSimilarite(String titre) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("titre", titre);

        RequestBody requestBody = RequestBody.create(new Gson().toJson(body), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/similarite")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("Similarité réponse : " + responseBody);

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            ResultatSimilarite resultat = new ResultatSimilarite();
            resultat.similaire     = json.has("similaire")       && json.get("similaire").getAsBoolean();
            resultat.score         = json.has("score")           ? json.get("score").getAsInt()           : 0;
            resultat.titreSimilaire = json.has("titre_similaire") ? json.get("titre_similaire").getAsString() : "";
            resultat.idSimilaire   = json.has("id_similaire")    ? json.get("id_similaire").getAsInt()    : 0;
            resultat.message       = json.has("message")         ? json.get("message").getAsString()      : "";
            return resultat;
        }
    }

    // ===== Appel générique =====
    private static ResultatAPI appelerAPI(String endpoint, JsonObject bodyJson) throws IOException {
        RequestBody body = RequestBody.create(new Gson().toJson(bodyJson), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("API réponse : " + responseBody);

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            ResultatAPI resultat = new ResultatAPI();
            resultat.succes = json.has("succes") && json.get("succes").getAsBoolean();
            resultat.statut = json.has("statut") ? json.get("statut").getAsString() : "actif";

            if (json.has("notification")) {
                JsonObject notif  = json.getAsJsonObject("notification");
                resultat.typeNotif    = notif.has("type")    ? notif.get("type").getAsString()    : "succes";
                resultat.titreNotif   = notif.has("titre")   ? notif.get("titre").getAsString()   : "";
                resultat.messageNotif = notif.has("message") ? notif.get("message").getAsString() : "";
            }
            return resultat;
        }
    }
}
