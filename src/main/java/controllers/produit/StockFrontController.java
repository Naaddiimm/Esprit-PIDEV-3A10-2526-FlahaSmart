package controllers.produit;


import com.google.gson.Gson;
import entities.StockProduit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.StockProduitService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StockFrontController {
    @FXML
    private Button aiAssistantButton;  // le bouton flottant

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_KEY = "2b10TGeJf8lYvs8hisUAUZ7qe";
    @FXML private FlowPane container;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private Label activeCountLabel;
    @FXML private Label growthCountLabel;
    @FXML private Label doneCountLabel;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageLabel;

    private final StockProduitService service = new StockProduitService();
    private List<StockProduit> data = new ArrayList<>();
    private List<StockProduit> filteredSortedList = new ArrayList<>(); // liste après filtres/tri

    // Pagination
    private int currentPage = 1;
    private final int itemsPerPage = 5;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        filterBox.getItems().addAll("Tous", "en cours", "en croissance", "terminé");
        filterBox.setValue("Tous");

        sortBox.getItems().addAll("A-Z", "Z-A", "Date récente", "Date ancienne");
        sortBox.setValue("A-Z");

        // Écouteurs pour les filtres (remettent à la page 1)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            refresh();
        });
        filterBox.setOnAction(e -> {
            currentPage = 1;
            refresh();
        });
        sortBox.setOnAction(e -> {
            currentPage = 1;
            refresh();
        });

        // Écouteurs pour les boutons de pagination
        prevPageButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                refresh();
            }
        });
        nextPageButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refresh();
            }
        });

        load();
    }

    private void load() {
        try {
            data = service.afficher();
            updateStats();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        long active = data.stream()
                .filter(p -> {
                    String s = p.getStatut();
                    return s != null && (s.equalsIgnoreCase("en cours") || s.equalsIgnoreCase("en croissance"));
                })
                .count();

        long growth = data.stream()
                .filter(p -> p.getStatut().equalsIgnoreCase("en croissance"))
                .count();

        long done = data.stream()
                .filter(p -> p.getStatut().equalsIgnoreCase("terminé"))
                .count();

        activeCountLabel.setText(String.valueOf(active));
        growthCountLabel.setText(String.valueOf(growth));
        doneCountLabel.setText(String.valueOf(done));
    }

    private void refresh() {
        // 1. Appliquer les filtres et le tri sur la liste complète
        Stream<StockProduit> stream = data.stream();

        String filter = filterBox.getValue();
        if (filter != null && !filter.equals("Tous")) {
            stream = stream.filter(p -> p.getStatut().equalsIgnoreCase(filter));
        }

        String keyword = searchField.getText();
        if (keyword != null && !keyword.isEmpty()) {
            String lower = keyword.toLowerCase();
            stream = stream.filter(p ->
                    p.getTypeProduit().toLowerCase().contains(lower) ||
                            p.getVariete().toLowerCase().contains(lower)
            );
        }

        List<StockProduit> list = stream.collect(Collectors.toList());

        String sort = sortBox.getValue();
        if (sort != null) {
            switch (sort) {
                case "A-Z":
                    list.sort(Comparator.comparing(StockProduit::getTypeProduit));
                    break;
                case "Z-A":
                    list.sort(Comparator.comparing(StockProduit::getTypeProduit).reversed());
                    break;
                case "Date récente":
                    list.sort(Comparator.comparing(StockProduit::getDateDebut).reversed());
                    break;
                case "Date ancienne":
                    list.sort(Comparator.comparing(StockProduit::getDateDebut));
                    break;
            }
        }

        filteredSortedList = list;

        // 2. Calculer la pagination
        totalPages = (int) Math.ceil((double) filteredSortedList.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1; // évite division par zéro
        if (currentPage > totalPages) currentPage = totalPages;

        // 3. Extraire la sous-liste pour la page courante
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredSortedList.size());
        List<StockProduit> pageItems = (fromIndex < filteredSortedList.size())
                ? filteredSortedList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        // 4. Afficher les cartes
        container.getChildren().clear();
        try {
            for (StockProduit p : pageItems) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/produit/CardStockFront.fxml")
                );
                Parent card = loader.load();
                CardStockFrontController controller = loader.getController();
                controller.setData(p);
                container.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Mettre à jour les boutons de pagination
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);
    }
    @FXML
    private void handleAIAssistant() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez une photo de plante malade");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
        );
        Stage stage = (Stage) aiAssistantButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            analyzeImage(selectedFile);
        }
    }

    private void analyzeImage(File imageFile) {
        javafx.application.Platform.runLater(() -> aiAssistantButton.setDisable(true));

        new Thread(() -> {
            try {
                // Lire les bytes de l'image
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String boundary = "Boundary-" + System.currentTimeMillis();

                // Construire le corps multipart
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

                // Ajouter le champ 'images' (fichier)
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"images\"; filename=\"").append(imageFile.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(imageFile.getName())).append("\r\n");
                writer.append("Content-Transfer-Encoding: binary\r\n");
                writer.append("\r\n");
                writer.flush();
                baos.write(imageBytes);
                baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

                // Ajouter le champ 'organs'
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"organs\"\r\n");
                writer.append("\r\n");
                writer.append("auto\r\n");
                writer.flush();

                // Fin du multipart
                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();

                byte[] bodyBytes = baos.toByteArray();

                String url = "https://my-api.plantnet.org/v2/diseases/identify?api-key=" + API_KEY + "&lang=fr";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Gson gson = new Gson();
                    Map<String, Object> responseMap = gson.fromJson(response.body(), Map.class);
                    List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
                    javafx.application.Platform.runLater(() -> showResults(results));
                } else {
                    String errorMsg = "Erreur API : " + response.statusCode() + "\n" + response.body();
                    javafx.application.Platform.runLater(() -> showError(errorMsg));
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> showError("Erreur : " + e.getMessage()));
            } finally {
                javafx.application.Platform.runLater(() -> aiAssistantButton.setDisable(false));
            }
        }).start();
    }
    private void showResults(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            showError("Aucune maladie détectée.");
            return;
        }

        Stage resultStage = new Stage();
        resultStage.initModality(Modality.APPLICATION_MODAL);
        resultStage.setTitle("Résultats de l'analyse");

        // Créer un ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: transparent;");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Maladies détectées :");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        root.getChildren().add(titleLabel);

        for (Map<String, Object> maladie : results) {
            String description = (String) maladie.get("description");
            Double score = (Double) maladie.get("score");
            String name = (String) maladie.get("name");

            VBox card = new VBox(5);
            card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9;");

            Label maladieLabel = new Label(description != null ? description : "Maladie inconnue");
            maladieLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label scoreLabel = new Label(String.format("Confiance : %.2f%%", score * 100));
            scoreLabel.setStyle("-fx-text-fill: #666;");

            card.getChildren().addAll(maladieLabel, scoreLabel);

            if (name != null) {
                Label codeLabel = new Label("Code EPPO : " + name);
                codeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                card.getChildren().add(codeLabel);
            }

            root.getChildren().add(card);
        }

        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-padding: 10 20;");
        closeButton.setOnAction(e -> resultStage.close());
        root.getChildren().add(closeButton);

        scrollPane.setContent(root);  // Le VBox devient le contenu du ScrollPane

        Scene scene = new Scene(scrollPane, 400, 500);
        resultStage.setScene(scene);
        resultStage.show();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}