package services.advancedfeatures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entities.advancedfeatures.Weather;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherService {

    private static final Logger LOGGER = Logger.getLogger(WeatherService.class.getName());
    private static final String API_KEY = "365b2999f38ef18672fba0abd909f8dc";
    private static final String BASE_URL = "http://api.weatherstack.com/current";

    public Weather getWeather(String city) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            String urlString = BASE_URL + "?access_key=" + API_KEY + "&query=" + encodedCity;
            URL url = new URL(urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                LOGGER.log(Level.WARNING, "Erreur HTTP: " + responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return parseWeatherData(response.toString());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des données météo", e);
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture des ressources", e);
            }
        }
    }

    private Weather parseWeatherData(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                LOGGER.log(Level.WARNING, "Erreur API: " + error.get("info").getAsString());
                return null;
            }

            JsonObject location = root.getAsJsonObject("location");
            String cityName = location.get("name").getAsString();
            String country = location.get("country").getAsString();
            String localTime = location.get("localtime").getAsString();

            JsonObject current = root.getAsJsonObject("current");
            int temperature = current.get("temperature").getAsInt();
            int feelsLike = current.get("feelslike").getAsInt();
            int humidity = current.get("humidity").getAsInt();
            double windSpeed = current.get("wind_speed").getAsDouble();

            String description = current.getAsJsonArray("weather_descriptions").size() > 0
                ? current.getAsJsonArray("weather_descriptions").get(0).getAsString()
                : "N/A";

            String icon = current.getAsJsonArray("weather_icons").size() > 0
                ? current.getAsJsonArray("weather_icons").get(0).getAsString()
                : "";

            return new Weather(cityName, country, temperature, description, icon,
                             humidity, windSpeed, feelsLike, localTime);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du parsing des données JSON", e);
            return null;
        }
    }

    public Weather getDefaultWeather() {
        return getWeather("Tunis");
    }
}

