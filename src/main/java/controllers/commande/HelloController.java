package controllers.commande;
import tools.myConnection;
import entities.commande.Article;
import entities.commande.Order;

import services.commande.ArticleDAO;

import services.commande.OrderDAO;
import services.commande.ConsultantBot;

import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

public class HelloController {
    // Articles components
    @FXML private TextField articleName, articleCategory, articleUnit, articleImageUrl;
    @FXML private TextArea articleDescription;
    @FXML private Spinner<Double> articlePrice, articleWeight;
    @FXML private Spinner<Integer> articleStock;
    @FXML private Button addArticleBtn, updateArticleBtn, clearArticleBtn;
    // Article validation labels
    @FXML private Label articleNameError, articleCategoryError, articleDescriptionError;
    @FXML private Label articlePriceError, articleStockError, articleWeightError, articleImageUrlError;

    // Orders components
    @FXML private TextField orderReference, orderStatus, orderPaymentMode, orderAddress;
    @FXML private Spinner<Double> orderTotal, orderFees;
    @FXML private DatePicker orderDate;
    @FXML private Button addOrderBtn, updateOrderBtn, deleteOrderBtn, clearOrderBtn;

    // Order validation labels
    @FXML private Label orderReferenceError, orderStatusError, orderPaymentError, orderAddressError;
    @FXML private Label orderTotalError, orderFeesError;

    // Articles Table components
    @FXML private TableView<Article> articlesTable;
    @FXML private TableColumn<Article, Integer> idColumn;
    @FXML private TableColumn<Article, String> nomColumn, categorieColumn, descriptionColumn, uniteColumn, imageUrlColumn;
    @FXML private TableColumn<Article, Double> prixColumn, poidsColumn;
    @FXML private TableColumn<Article, Integer> stockColumn;
    @FXML private TableColumn<Article, LocalDateTime> dateAjoutColumn;
    @FXML private Button refreshArticlesBtn, deleteArticleTableBtn;
    @FXML private Label articleCountLabel;
    @FXML private TextField deleteArticleIdField;

    // Articles Sorting and Search components
    @FXML private ComboBox<String> sortArticleCombo;
    @FXML private Button applyArticleSortBtn;
    @FXML private TextField searchArticleField;
    @FXML private Button searchArticleBtn;
    @FXML private Button clearArticleSearchBtn;

    // Orders Table components
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> orderRefColumn, orderStatusColumn, orderPaymentColumn, orderAddressColumn;
    @FXML private TableColumn<Order, LocalDate> orderDateColumn;
    @FXML private TableColumn<Order, Double> orderTotalColumn, orderFeesColumn;
    @FXML private Button refreshOrdersBtn, deleteOrderTableBtn;
    @FXML private Label orderCountLabel;
    @FXML private TextField deleteOrderIdField;

    // Orders Sorting and Search components
    @FXML private ComboBox<String> sortOrderCombo;
    @FXML private Button applyOrderSortBtn;
    @FXML private TextField searchOrderField;
    @FXML private Button searchOrderBtn;
    @FXML private Button clearOrderSearchBtn;

    // Statistics buttons
    @FXML private Button consulterPrixBtn;
    @FXML private Button consulterPoidsBtn;
    @FXML private Button consulterMontantBtn;
    @FXML private Button consulterFraisBtn;

    // Consultant Bot component
    @FXML private ConsultantBot consultantBot;

    // Store lists for client-side operations
    private List<Article> allArticles;
    private List<Order> allOrders;
    private String currentArticleSortOption = "Trier par défaut (ID)";
    private String currentOrderSortOption = "Trier par défaut (ID)";
    private String currentArticleSearchTerm = "";
    private String currentOrderSearchTerm = "";

    @FXML
    public void initialize() {
        initializeArticleSpinners();
        initializeOrderComponents();
        setupEventHandlers();
        initializeArticlesTable();
        initializeOrdersTable();
        loadArticles();
        loadOrders();

        // Add input listeners for real-time validation
        setupArticleValidation();
        setupOrderValidation();
    }

    // ===== ORDER STATISTICS BUTTON HANDLERS =====

    @FXML
    private void handleConsulterMontant() {
        if (allOrders == null || allOrders.isEmpty()) {
            showAlert("Information", "Aucune commande disponible pour afficher les statistiques de montants.");
            return;
        }

        try {
            // Create new stage for amount statistics
            Stage montantStage = new Stage();
            montantStage.setTitle("Statistiques des Montants");
            montantStage.initModality(Modality.APPLICATION_MODAL);

            // Create content
            VBox content = createMontantStatisticsView();
            content.getStyleClass().add("form-container");

            // Create scene and show - FIXED: Load CSS from correct path
            Scene scene = new Scene(content, 700, 650);

            // Try to load CSS from multiple possible locations
            String cssPath = "/com/example/flahasmarty/styles.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                // Try alternative path
                cssPath = "/styles.css";
                cssUrl = getClass().getResource(cssPath);
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[DEBUG] CSS loaded from: " + cssUrl);
            } else {
                System.out.println("[WARNING] CSS file not found, using default styles");
            }

            montantStage.setScene(scene);
            montantStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConsulterFrais() {
        if (allOrders == null || allOrders.isEmpty()) {
            showAlert("Information", "Aucune commande disponible pour afficher les statistiques de frais.");
            return;
        }

        try {
            // Create new stage for fees statistics
            Stage fraisStage = new Stage();
            fraisStage.setTitle("Statistiques des Frais de Livraison");
            fraisStage.initModality(Modality.APPLICATION_MODAL);

            // Create content
            VBox content = createFraisStatisticsView();
            content.getStyleClass().add("form-container");

            // Create scene and show - FIXED: Load CSS from correct path
            Scene scene = new Scene(content, 750, 650);

            // Try to load CSS from multiple possible locations
            String cssPath = "/com/example/flahasmarty/styles.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                // Try alternative path
                cssPath = "/styles.css";
                cssUrl = getClass().getResource(cssPath);
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[DEBUG] CSS loaded from: " + cssUrl);
            } else {
                System.out.println("[WARNING] CSS file not found, using default styles");
            }

            fraisStage.setScene(scene);
            fraisStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createMontantStatisticsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));

        // Title
        Label title = new Label("💰 Statistiques des Montants");
        title.getStyleClass().add("form-title");

        // Summary statistics
        double maxMontant = allOrders.stream().mapToDouble(Order::getMontantTotal).max().orElse(0);
        double minMontant = allOrders.stream().mapToDouble(Order::getMontantTotal).min().orElse(0);
        double avgMontant = allOrders.stream().mapToDouble(Order::getMontantTotal).average().orElse(0);
        double totalMontant = allOrders.stream().mapToDouble(Order::getMontantTotal).sum();

        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getStyleClass().add("stats-summary");

        VBox maxCard = createStatCard("Montant max", String.format("%.2f €", maxMontant));
        VBox minCard = createStatCard("Montant min", String.format("%.2f €", minMontant));
        VBox avgCard = createStatCard("Montant moyen", String.format("%.2f €", avgMontant));
        VBox totalCard = createStatCard("Total", String.format("%.2f €", totalMontant));

        summaryBox.getChildren().addAll(maxCard, minCard, avgCard, totalCard);

        // Pie chart for montant distribution by order
        Label chartTitle = new Label("Répartition des montants par commande");
        chartTitle.getStyleClass().add("section-title");

        // Sort orders by montant for better visualization
        List<Order> sortedOrders = new ArrayList<>(allOrders);
        sortedOrders.sort(Comparator.comparingDouble(Order::getMontantTotal).reversed());

        // Create pie chart data (top 10 orders or all if less)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        int count = 0;
        for (Order order : sortedOrders) {
            if (count < 10) { // Show top 10 orders
                pieChartData.add(new PieChart.Data(
                        order.getReference() + " - " + String.format("%.2f €", order.getMontantTotal()),
                        order.getMontantTotal()
                ));
                count++;
            } else {
                break;
            }
        }

        // Create pie chart
        PieChart montantChart = new PieChart(pieChartData);
        montantChart.setTitle("Top 10 commandes par montant");
        montantChart.setPrefWidth(600);
        montantChart.setPrefHeight(350);
        montantChart.setStyle("-fx-font-size: 11px;");
        montantChart.setLabelsVisible(true);
        montantChart.setLegendVisible(true);

        // Add detailed statistics table
        Label tableTitle = new Label("Détail des montants");
        tableTitle.getStyleClass().add("section-title");

        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(10));
        statsBox.getStyleClass().add("stats-section");

        // Create a grid for montant ranges
        Map<String, Integer> montantRanges = new HashMap<>();
        montantRanges.put("0-50 €", 0);
        montantRanges.put("50-100 €", 0);
        montantRanges.put("100-200 €", 0);
        montantRanges.put("200-500 €", 0);
        montantRanges.put("500+ €", 0);

        for (Order order : allOrders) {
            double montant = order.getMontantTotal();
            if (montant <= 50) {
                montantRanges.put("0-50 €", montantRanges.get("0-50 €") + 1);
            } else if (montant <= 100) {
                montantRanges.put("50-100 €", montantRanges.get("50-100 €") + 1);
            } else if (montant <= 200) {
                montantRanges.put("100-200 €", montantRanges.get("100-200 €") + 1);
            } else if (montant <= 500) {
                montantRanges.put("200-500 €", montantRanges.get("200-500 €") + 1);
            } else {
                montantRanges.put("500+ €", montantRanges.get("500+ €") + 1);
            }
        }

        Label rangeTitle = new Label("Distribution par tranche de montant:");
        rangeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        statsBox.getChildren().add(rangeTitle);

        for (Map.Entry<String, Integer> entry : montantRanges.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = (entry.getValue() * 100.0) / allOrders.size();
                Label rangeLabel = new Label(String.format("%s: %d commande(s) (%.1f%%)",
                        entry.getKey(), entry.getValue(), percentage));
                rangeLabel.setStyle("-fx-font-size: 12px;");
                statsBox.getChildren().add(rangeLabel);
            }
        }

        // Close button
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        // Center the close button
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(title, summaryBox, chartTitle, montantChart, tableTitle, statsBox, buttonBox);

        return container;
    }

    private VBox createFraisStatisticsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));

        // Title
        Label title = new Label("🚚 Statistiques des Frais de Livraison");
        title.getStyleClass().add("form-title");

        // Summary statistics
        double maxFrais = allOrders.stream().mapToDouble(Order::getFraisLivraison).max().orElse(0);
        double minFrais = allOrders.stream().mapToDouble(Order::getFraisLivraison).min().orElse(0);
        double avgFrais = allOrders.stream().mapToDouble(Order::getFraisLivraison).average().orElse(0);
        double totalFrais = allOrders.stream().mapToDouble(Order::getFraisLivraison).sum();

        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getStyleClass().add("stats-summary");

        VBox maxCard = createStatCard("Frais max", String.format("%.2f €", maxFrais));
        VBox minCard = createStatCard("Frais min", String.format("%.2f €", minFrais));
        VBox avgCard = createStatCard("Frais moyen", String.format("%.2f €", avgFrais));
        VBox totalCard = createStatCard("Total frais", String.format("%.2f €", totalFrais));

        summaryBox.getChildren().addAll(maxCard, minCard, avgCard, totalCard);

        // Line chart for frais progression
        Label chartTitle = new Label("Évolution des frais de livraison (du plus bas au plus haut)");
        chartTitle.getStyleClass().add("section-title");

        // Sort orders by frais
        List<Order> sortedOrders = new ArrayList<>(allOrders);
        sortedOrders.sort(Comparator.comparingDouble(Order::getFraisLivraison));

        // Create line chart
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Commandes (triées par frais)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frais de livraison (€)");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Courbe des frais de livraison");
        lineChart.setPrefWidth(650);
        lineChart.setPrefHeight(350);
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Frais de livraison");

        for (int i = 0; i < sortedOrders.size(); i++) {
            Order order = sortedOrders.get(i);
            series.getData().add(new XYChart.Data<>(i + 1, order.getFraisLivraison()));
        }

        lineChart.getData().add(series);

        // Style the line chart
        lineChart.setStyle("-fx-font-size: 12px;");
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: #2e7d32; -fx-stroke-width: 2px;");
        }

        // Add statistics by range
        Label statsTitle = new Label("Statistiques détaillées");
        statsTitle.getStyleClass().add("section-title");

        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(10));
        statsBox.getStyleClass().add("stats-section");

        // Calculate quartiles
        int size = sortedOrders.size();
        if (size > 0) {
            double q1 = sortedOrders.get(size / 4).getFraisLivraison();
            double median = sortedOrders.get(size / 2).getFraisLivraison();
            double q3 = sortedOrders.get(3 * size / 4).getFraisLivraison();

            Label quartileLabel = new Label(String.format("Quartiles des frais:"));
            quartileLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            statsBox.getChildren().add(quartileLabel);

            statsBox.getChildren().add(new Label(String.format("• Q1 (25%% des commandes): %.2f €", q1)));
            statsBox.getChildren().add(new Label(String.format("• Médiane (50%%): %.2f €", median)));
            statsBox.getChildren().add(new Label(String.format("• Q3 (75%%): %.2f €", q3)));
        }

        // Frais ranges
        Label rangeTitle = new Label("\nDistribution par tranche de frais:");
        rangeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        statsBox.getChildren().add(rangeTitle);

        Map<String, Integer> fraisRanges = new HashMap<>();
        fraisRanges.put("0-5 €", 0);
        fraisRanges.put("5-10 €", 0);
        fraisRanges.put("10-15 €", 0);
        fraisRanges.put("15-20 €", 0);
        fraisRanges.put("20+ €", 0);

        for (Order order : allOrders) {
            double frais = order.getFraisLivraison();
            if (frais <= 5) {
                fraisRanges.put("0-5 €", fraisRanges.get("0-5 €") + 1);
            } else if (frais <= 10) {
                fraisRanges.put("5-10 €", fraisRanges.get("5-10 €") + 1);
            } else if (frais <= 15) {
                fraisRanges.put("10-15 €", fraisRanges.get("10-15 €") + 1);
            } else if (frais <= 20) {
                fraisRanges.put("15-20 €", fraisRanges.get("15-20 €") + 1);
            } else {
                fraisRanges.put("20+ €", fraisRanges.get("20+ €") + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : fraisRanges.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = (entry.getValue() * 100.0) / allOrders.size();
                statsBox.getChildren().add(new Label(String.format("%s: %d commande(s) (%.1f%%)",
                        entry.getKey(), entry.getValue(), percentage)));
            }
        }

        // Close button
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        // Center the close button
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(title, summaryBox, chartTitle, lineChart, statsTitle, statsBox, buttonBox);

        return container;
    }

    private VBox createStatCard(String label, String value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(120);
        card.getStyleClass().add("stat-card");

        Label labelField = new Label(label);
        labelField.getStyleClass().add("stat-label");

        Label valueField = new Label(value);
        valueField.getStyleClass().add("stat-value");

        card.getChildren().addAll(labelField, valueField);
        return card;
    }

    // ===== ARTICLE STATISTICS BUTTON HANDLERS =====

    @FXML
    private void handleConsulterPrix() {
        if (allArticles == null || allArticles.isEmpty()) {
            showAlert("Information", "Aucun article disponible pour afficher les statistiques de prix.");
            return;
        }

        try {
            Stage prixStage = new Stage();
            prixStage.setTitle("Statistiques des Prix");
            prixStage.initModality(Modality.APPLICATION_MODAL);

            VBox content = createPriceStatisticsView();
            content.getStyleClass().add("form-container");

            // Create scene and show - FIXED: Load CSS from correct path
            Scene scene = new Scene(content, 700, 600);

            // Try to load CSS from multiple possible locations
            String cssPath = "/com/example/flahasmarty/styles.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                // Try alternative path
                cssPath = "/styles.css";
                cssUrl = getClass().getResource(cssPath);
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[DEBUG] CSS loaded from: " + cssUrl);
            } else {
                System.out.println("[WARNING] CSS file not found, using default styles");
            }

            prixStage.setScene(scene);
            prixStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConsulterPoids() {
        if (allArticles == null || allArticles.isEmpty()) {
            showAlert("Information", "Aucun article disponible pour afficher les statistiques de poids.");
            return;
        }

        try {
            Stage poidsStage = new Stage();
            poidsStage.setTitle("Statistiques des Poids");
            poidsStage.initModality(Modality.APPLICATION_MODAL);

            VBox content = createWeightStatisticsView();
            content.getStyleClass().add("form-container");

            // Create scene and show - FIXED: Load CSS from correct path
            Scene scene = new Scene(content, 700, 700);

            // Try to load CSS from multiple possible locations
            String cssPath = "/com/example/flahasmarty/styles.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl == null) {
                // Try alternative path
                cssPath = "/styles.css";
                cssUrl = getClass().getResource(cssPath);
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("[DEBUG] CSS loaded from: " + cssUrl);
            } else {
                System.out.println("[WARNING] CSS file not found, using default styles");
            }

            poidsStage.setScene(scene);
            poidsStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createPriceStatisticsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));

        Label title = new Label("📊 Statistiques des Prix");
        title.getStyleClass().add("form-title");

        double maxPrice = allArticles.stream().mapToDouble(Article::getPrix).max().orElse(0);
        double minPrice = allArticles.stream().mapToDouble(Article::getPrix).min().orElse(0);
        double avgPrice = allArticles.stream().mapToDouble(Article::getPrix).average().orElse(0);

        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getStyleClass().add("stats-summary");

        VBox maxCard = createStatCard("Prix maximum", String.format("%.2f €", maxPrice));
        VBox minCard = createStatCard("Prix minimum", String.format("%.2f €", minPrice));
        VBox avgCard = createStatCard("Prix moyen", String.format("%.2f €", avgPrice));

        summaryBox.getChildren().addAll(maxCard, minCard, avgCard);

        Label barsTitle = new Label("Prix des articles (du plus bas au plus haut)");
        barsTitle.getStyleClass().add("section-title");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.getStyleClass().add("stats-scroll");

        VBox barsContainer = new VBox(10);
        barsContainer.setPadding(new Insets(10));

        List<Article> sortedArticles = new ArrayList<>(allArticles);
        sortedArticles.sort(Comparator.comparingDouble(Article::getPrix));

        double maxPriceForBar = maxPrice > 0 ? maxPrice : 1.0;

        for (Article article : sortedArticles) {
            VBox articleBox = new VBox(5);
            articleBox.setPadding(new Insets(5, 0, 5, 0));

            Label nameLabel = new Label(article.getNom() + " - " + String.format("%.2f €", article.getPrix()));
            nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

            ProgressBar progressBar = new ProgressBar(article.getPrix() / maxPriceForBar);
            progressBar.setPrefWidth(600);
            progressBar.setPrefHeight(25);

            String barStyle = "-fx-accent: ";
            double percentage = article.getPrix() / maxPriceForBar;
            if (percentage < 0.33) {
                barStyle += "#4caf50;";
            } else if (percentage < 0.66) {
                barStyle += "#ff9800;";
            } else {
                barStyle += "#f44336;";
            }
            progressBar.setStyle(barStyle);

            articleBox.getChildren().addAll(nameLabel, progressBar);
            barsContainer.getChildren().add(articleBox);
        }

        scrollPane.setContent(barsContainer);

        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(title, summaryBox, barsTitle, scrollPane, buttonBox);

        return container;
    }

    private VBox createWeightStatisticsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));

        Label title = new Label("⚖️ Statistiques des Poids");
        title.getStyleClass().add("form-title");

        double maxWeight = allArticles.stream().mapToDouble(Article::getPoids).max().orElse(0);
        double minWeight = allArticles.stream().mapToDouble(Article::getPoids).min().orElse(0);
        double avgWeight = allArticles.stream().mapToDouble(Article::getPoids).average().orElse(0);
        double totalWeight = allArticles.stream().mapToDouble(Article::getPoids).sum();

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(15);
        summaryGrid.setVgap(10);
        summaryGrid.setPadding(new Insets(15));
        summaryGrid.setAlignment(Pos.CENTER);
        summaryGrid.getStyleClass().add("stats-summary");

        VBox maxCard = createStatCard("Poids maximum", String.format("%.2f kg", maxWeight));
        VBox minCard = createStatCard("Poids minimum", String.format("%.2f kg", minWeight));
        VBox avgCard = createStatCard("Poids moyen", String.format("%.2f kg", avgWeight));
        VBox totalCard = createStatCard("Poids total", String.format("%.2f kg", totalWeight));

        summaryGrid.add(maxCard, 0, 0);
        summaryGrid.add(minCard, 1, 0);
        summaryGrid.add(avgCard, 2, 0);
        summaryGrid.add(totalCard, 3, 0);

        Label chartTitle = new Label("Répartition des poids par catégorie");
        chartTitle.getStyleClass().add("section-title");

        Map<String, Integer> weightRanges = new HashMap<>();
        weightRanges.put("0-1 kg", 0);
        weightRanges.put("1-5 kg", 0);
        weightRanges.put("5-10 kg", 0);
        weightRanges.put("10-20 kg", 0);
        weightRanges.put("20+ kg", 0);

        for (Article article : allArticles) {
            double weight = article.getPoids();
            if (weight <= 1) {
                weightRanges.put("0-1 kg", weightRanges.get("0-1 kg") + 1);
            } else if (weight <= 5) {
                weightRanges.put("1-5 kg", weightRanges.get("1-5 kg") + 1);
            } else if (weight <= 10) {
                weightRanges.put("5-10 kg", weightRanges.get("5-10 kg") + 1);
            } else if (weight <= 20) {
                weightRanges.put("10-20 kg", weightRanges.get("10-20 kg") + 1);
            } else {
                weightRanges.put("20+ kg", weightRanges.get("20+ kg") + 1);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : weightRanges.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }

        PieChart weightChart = new PieChart(pieChartData);
        weightChart.setTitle("Distribution par poids");
        weightChart.setPrefWidth(600);
        weightChart.setPrefHeight(350);
        weightChart.setStyle("-fx-font-size: 12px;");
        weightChart.setLabelsVisible(true);
        weightChart.setLegendVisible(true);

        VBox legendBox = new VBox(5);
        legendBox.setPadding(new Insets(10));
        legendBox.getStyleClass().add("stats-section");

        Label legendTitle = new Label("Détail par catégorie:");
        legendTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        legendBox.getChildren().add(legendTitle);

        for (Map.Entry<String, Integer> entry : weightRanges.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = (entry.getValue() * 100.0) / allArticles.size();
                Label rangeLabel = new Label(String.format("%s: %d article(s) (%.1f%%)",
                        entry.getKey(), entry.getValue(), percentage));
                rangeLabel.setStyle("-fx-font-size: 12px;");
                legendBox.getChildren().add(rangeLabel);
            }
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(title, summaryGrid, chartTitle, weightChart, legendBox, buttonBox);

        return container;
    }

    // ===== ARTICLE VALIDATION METHODS =====

    private void setupArticleValidation() {
        articleName.textProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleName();
        });

        articleCategory.textProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleCategory();
        });

        articleDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleDescription();
        });

        articlePrice.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateArticlePrice();
        });

        articleStock.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleStock();
        });

        articleWeight.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleWeight();
        });

        articleImageUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            validateArticleImageUrl();
        });
    }

    private boolean validateArticleName() {
        String name = articleName.getText().trim();

        if (name.isEmpty()) {
            articleNameError.setText("Le nom est requis");
            articleNameError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (name.length() < 3) {
            articleNameError.setText("Le nom doit contenir au moins 3 caractères");
            articleNameError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (name.length() > 15) {
            articleNameError.setText("Le nom ne peut pas dépasser 15 caractères");
            articleNameError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (!name.matches("[a-zA-Z\\s]+")) {
            articleNameError.setText("Le nom ne peut contenir que des lettres et espaces");
            articleNameError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleNameError.setText("✓ Valide");
            articleNameError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleCategory() {
        String category = articleCategory.getText().trim();

        if (category.isEmpty()) {
            articleCategoryError.setText("La catégorie est requise");
            articleCategoryError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (category.length() < 5) {
            articleCategoryError.setText("La catégorie doit contenir au moins 5 caractères");
            articleCategoryError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (category.length() > 20) {
            articleCategoryError.setText("La catégorie ne peut pas dépasser 20 caractères");
            articleCategoryError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleCategoryError.setText("✓ Valide");
            articleCategoryError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleDescription() {
        String description = articleDescription.getText().trim();

        if (description.isEmpty()) {
            articleDescriptionError.setText("La description est requise");
            articleDescriptionError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (description.length() < 6) {
            articleDescriptionError.setText("La description doit contenir au moins 6 caractères");
            articleDescriptionError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleDescriptionError.setText("✓ Valide");
            articleDescriptionError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticlePrice() {
        double price = articlePrice.getValue();

        if (price <= 0) {
            articlePriceError.setText("Le prix doit être supérieur à 0");
            articlePriceError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articlePriceError.setText("✓ Valide");
            articlePriceError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleStock() {
        int stock = articleStock.getValue();

        if (stock <= 0) {
            articleStockError.setText("Le stock doit être supérieur à 0");
            articleStockError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleStockError.setText("✓ Valide");
            articleStockError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleWeight() {
        double weight = articleWeight.getValue();

        if (weight <= 0) {
            articleWeightError.setText("Le poids doit être supérieur à 0");
            articleWeightError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleWeightError.setText("✓ Valide");
            articleWeightError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleImageUrl() {
        String url = articleImageUrl.getText().trim();

        if (url.isEmpty()) {
            articleImageUrlError.setText("L'URL de l'image est requise");
            articleImageUrlError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (!url.startsWith("https://")) {
            articleImageUrlError.setText("L'URL doit commencer par https://");
            articleImageUrlError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            articleImageUrlError.setText("✓ Valide");
            articleImageUrlError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateArticleForm() {
        boolean isValid = true;

        isValid &= validateArticleName();
        isValid &= validateArticleCategory();
        isValid &= validateArticleDescription();
        isValid &= validateArticlePrice();
        isValid &= validateArticleStock();
        isValid &= validateArticleWeight();
        isValid &= validateArticleImageUrl();

        return isValid;
    }

    // ===== ORDER VALIDATION METHODS =====

    private void setupOrderValidation() {
        orderReference.textProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderReference();
        });

        orderStatus.textProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderStatus();
        });

        orderPaymentMode.textProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderPaymentMode();
        });

        orderAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderAddress();
        });

        orderTotal.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderTotal();
        });

        orderFees.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateOrderFees();
        });
    }

    private boolean validateOrderReference() {
        String reference = orderReference.getText().trim();

        if (reference.isEmpty()) {
            orderReferenceError.setText("La référence est requise");
            orderReferenceError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (reference.length() < 3) {
            orderReferenceError.setText("La référence doit contenir au moins 3 caractères");
            orderReferenceError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (reference.length() > 15) {
            orderReferenceError.setText("La référence ne peut pas dépasser 15 caractères");
            orderReferenceError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderReferenceError.setText("✓ Valide");
            orderReferenceError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderStatus() {
        String status = orderStatus.getText().trim();

        if (status.isEmpty()) {
            orderStatusError.setText("Le statut est requis");
            orderStatusError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (status.length() < 3) {
            orderStatusError.setText("Le statut doit contenir au moins 3 caractères");
            orderStatusError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (status.length() > 10) {
            orderStatusError.setText("Le statut ne peut pas dépasser 10 caractères");
            orderStatusError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderStatusError.setText("✓ Valide");
            orderStatusError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderPaymentMode() {
        String payment = orderPaymentMode.getText().trim();

        if (payment.isEmpty()) {
            orderPaymentError.setText("Le mode de paiement est requis");
            orderPaymentError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (payment.length() < 3) {
            orderPaymentError.setText("Le mode de paiement doit contenir au moins 3 caractères");
            orderPaymentError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (payment.length() > 10) {
            orderPaymentError.setText("Le mode de paiement ne peut pas dépasser 10 caractères");
            orderPaymentError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderPaymentError.setText("✓ Valide");
            orderPaymentError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderAddress() {
        String address = orderAddress.getText().trim();

        if (address.isEmpty()) {
            orderAddressError.setText("L'adresse est requise");
            orderAddressError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (address.length() < 2) {
            orderAddressError.setText("L'adresse doit contenir au moins 2 caractères");
            orderAddressError.setStyle("-fx-text-fill: red;");
            return false;
        } else if (address.length() > 30) {
            orderAddressError.setText("L'adresse ne peut pas dépasser 30 caractères");
            orderAddressError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderAddressError.setText("✓ Valide");
            orderAddressError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderTotal() {
        double total = orderTotal.getValue();

        if (total <= 0) {
            orderTotalError.setText("Le montant total doit être supérieur à 0");
            orderTotalError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderTotalError.setText("✓ Valide");
            orderTotalError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderFees() {
        double fees = orderFees.getValue();

        if (fees <= 0) {
            orderFeesError.setText("Les frais de livraison doivent être supérieurs à 0");
            orderFeesError.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            orderFeesError.setText("✓ Valide");
            orderFeesError.setStyle("-fx-text-fill: green;");
            return true;
        }
    }

    private boolean validateOrderForm() {
        boolean isValid = true;

        isValid &= validateOrderReference();
        isValid &= validateOrderStatus();
        isValid &= validateOrderPaymentMode();
        isValid &= validateOrderAddress();
        isValid &= validateOrderTotal();
        isValid &= validateOrderFees();

        return isValid;
    }

    // ===== ARTICLES METHODS =====

    @FXML
    private void handleAddArticle() {
        try {
            if (!validateArticleForm()) {
                showAlert("Erreur de validation", "Veuillez corriger les erreurs dans le formulaire");
                return;
            }

            String nom = articleName.getText().trim();
            String categorie = articleCategory.getText().trim();
            String description = articleDescription.getText().trim();
            double prix = articlePrice.getValue();
            int stock = articleStock.getValue();
            double poids = articleWeight.getValue();
            String unite = articleUnit.getText().trim();
            String imageUrl = articleImageUrl.getText().trim();

            Article article = new Article(nom, description, categorie, prix, stock);
            article.setPoids(poids);
            article.setUnite(unite);
            article.setImageUrl(imageUrl);
            article.setIdUser(1);

            ArticleDAO dao = new ArticleDAO();
            dao.insertArticle(article);

            showAlert("Succès", "Article ajouté avec succès ✅");
            clearArticleFields();
            loadArticles();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout ❌");
            e.printStackTrace();
        }
    }

    private void updateArticle() {
        try {
            ArticleUpdateDialogSimple dialog = new ArticleUpdateDialogSimple(
                    updateArticleBtn.getScene().getWindow()
            );

            dialog.showAndWait().ifPresent(updatedArticle -> {
                ArticleDAO dao = new ArticleDAO();
                dao.updateArticle(updatedArticle);
                showAlert("Succès", "Article mis à jour avec succès ✅");
                loadArticles();
                clearArticleForm();
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour ❌");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteArticleFromTable() {
        try {
            String idText = deleteArticleIdField.getText().trim();

            if (idText.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un ID d'article à supprimer");
                return;
            }

            int id = Integer.parseInt(idText);

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer l'article");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet article (ID: " + id + ")?");

            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }

            ArticleDAO dao = new ArticleDAO();
            dao.deleteArticle(id);

            deleteArticleIdField.clear();
            loadArticles();
            showAlert("Succès", "Article supprimé avec succès");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID doit être un nombre entier");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression");
            e.printStackTrace();
        }
    }

    private void initializeArticleSpinners() {
        articlePrice.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 999999, 0, 0.01));
        articleStock.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0, 1));
        articleWeight.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 999999, 0, 0.1));
    }

    private void clearArticleFields() {
        articleName.clear();
        articleCategory.clear();
        articleDescription.clear();
        articleUnit.clear();
        articleImageUrl.clear();
        articlePrice.getValueFactory().setValue(0.0);
        articleStock.getValueFactory().setValue(0);
        articleWeight.getValueFactory().setValue(0.0);

        articleNameError.setText("");
        articleCategoryError.setText("");
        articleDescriptionError.setText("");
        articlePriceError.setText("");
        articleStockError.setText("");
        articleWeightError.setText("");
        articleImageUrlError.setText("");
    }

    private void clearArticleForm() {
        clearArticleFields();
    }

    private void initializeArticlesTable() {
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nomColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        categorieColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategorie()));
        descriptionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        prixColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        stockColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        poidsColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPoids()).asObject());
        uniteColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUnite()));
        imageUrlColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getImageUrl()));

        dateAjoutColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateAjout()));

        dateAjoutColumn.setCellFactory(column -> new TableCell<Article, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });

        refreshArticlesBtn.setOnAction(e -> loadArticles());

        sortArticleCombo.getItems().addAll(
                "Trier par défaut (ID)",
                "Prix (croissant)",
                "Prix (décroissant)",
                "Date (plus récent)",
                "Date (plus ancien)",
                "Nom (A-Z)",
                "Nom (Z-A)"
        );
        sortArticleCombo.setValue("Trier par défaut (ID)");

        searchArticleField.setOnAction(e -> searchArticles());
    }

    @FXML
    private void searchArticles() {
        currentArticleSearchTerm = searchArticleField.getText().trim();

        if (currentArticleSearchTerm.isEmpty()) {
            loadArticles();
        } else {
            try {
                ArticleDAO dao = new ArticleDAO();
                List<Article> searchResults = dao.searchArticlesByName(currentArticleSearchTerm);
                allArticles = searchResults;
                applyArticleSorting(searchResults);
                articleCountLabel.setText("Résultats: " + searchResults.size() + " articles pour \"" + currentArticleSearchTerm + "\"");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la recherche");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void clearArticleSearch() {
        searchArticleField.clear();
        currentArticleSearchTerm = "";
        loadArticles();
    }

    private void loadArticles() {
        try {
            ArticleDAO dao = new ArticleDAO();
            allArticles = dao.getAllArticles();
            applyArticleSorting(allArticles);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des articles");
            e.printStackTrace();
        }
    }

    @FXML
    private void sortArticles() {
        currentArticleSortOption = sortArticleCombo.getValue();
        if (allArticles != null) {
            applyArticleSorting(allArticles);
        }
    }

    private void applyArticleSorting(List<Article> articlesToSort) {
        List<Article> sortedList = new ArrayList<>(articlesToSort);

        switch (currentArticleSortOption) {
            case "Prix (croissant)":
                sortedList.sort((a1, a2) -> Double.compare(a1.getPrix(), a2.getPrix()));
                break;
            case "Prix (décroissant)":
                sortedList.sort((a1, a2) -> Double.compare(a2.getPrix(), a1.getPrix()));
                break;
            case "Date (plus récent)":
                sortedList.sort((a1, a2) -> {
                    if (a1.getDateAjout() == null && a2.getDateAjout() == null) return 0;
                    if (a1.getDateAjout() == null) return 1;
                    if (a2.getDateAjout() == null) return -1;
                    return a2.getDateAjout().compareTo(a1.getDateAjout());
                });
                break;
            case "Date (plus ancien)":
                sortedList.sort((a1, a2) -> {
                    if (a1.getDateAjout() == null && a2.getDateAjout() == null) return 0;
                    if (a1.getDateAjout() == null) return 1;
                    if (a2.getDateAjout() == null) return -1;
                    return a1.getDateAjout().compareTo(a2.getDateAjout());
                });
                break;
            case "Nom (A-Z)":
                sortedList.sort((a1, a2) -> a1.getNom().compareToIgnoreCase(a2.getNom()));
                break;
            case "Nom (Z-A)":
                sortedList.sort((a1, a2) -> a2.getNom().compareToIgnoreCase(a1.getNom()));
                break;
            default:
                sortedList.sort((a1, a2) -> Integer.compare(a1.getId(), a2.getId()));
                break;
        }

        ObservableList<Article> articles = FXCollections.observableArrayList(sortedList);
        articlesTable.setItems(articles);

        if (!currentArticleSearchTerm.isEmpty()) {
            articleCountLabel.setText("Résultats: " + sortedList.size() + " articles pour \"" + currentArticleSearchTerm + "\"");
        } else {
            articleCountLabel.setText("Total: " + sortedList.size() + " articles");
        }
    }

    // ===== ORDERS METHODS =====

    @FXML
    private void handleAddOrder() {
        try {
            if (!validateOrderForm()) {
                showAlert("Erreur de validation", "Veuillez corriger les erreurs dans le formulaire");
                return;
            }

            String reference = orderReference.getText().trim();
            LocalDate date = orderDate.getValue();
            String statut = orderStatus.getText().trim();
            String modePaiement = orderPaymentMode.getText().trim();
            String adresse = orderAddress.getText().trim();
            double montant = orderTotal.getValue();
            double frais = orderFees.getValue();

            Order order = new Order(reference, date, statut, modePaiement, adresse, montant, frais, 1);

            OrderDAO dao = new OrderDAO();
            dao.insertOrder(order);

            showAlert("Succès", "Commande ajoutée avec succès ✅");
            clearOrderForm();
            loadOrders();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout ❌");
            e.printStackTrace();
        }
    }

    private void updateOrder() {
        try {
            OrderUpdateDialog dialog = new OrderUpdateDialog(updateOrderBtn.getScene().getWindow());

            dialog.showAndWait().ifPresent(updatedOrder -> {
                OrderDAO dao = new OrderDAO();
                dao.updateOrder(updatedOrder);
                showAlert("Succès", "Commande mise à jour avec succès ✅");
                loadOrders();
                clearOrderForm();
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour ❌");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteOrderFromTable() {
        try {
            String idText = deleteOrderIdField.getText().trim();

            if (idText.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un ID de commande à supprimer");
                return;
            }

            int id = Integer.parseInt(idText);

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer la commande");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande (ID: " + id + ")?");

            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }

            OrderDAO dao = new OrderDAO();
            dao.deleteOrder(id);

            deleteOrderIdField.clear();
            loadOrders();
            showAlert("Succès", "Commande supprimée avec succès");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID doit être un nombre entier");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression");
            e.printStackTrace();
        }
    }

    private void initializeOrderComponents() {
        orderTotal.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 999999, 0, 0.01));
        orderFees.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 999999, 0, 0.01));
        orderDate.setValue(LocalDate.now());
    }

    private void clearOrderForm() {
        orderReference.clear();
        orderDate.setValue(LocalDate.now());
        orderStatus.clear();
        orderPaymentMode.clear();
        orderAddress.clear();
        orderTotal.getValueFactory().setValue(0.0);
        orderFees.getValueFactory().setValue(0.0);

        orderReferenceError.setText("");
        orderStatusError.setText("");
        orderPaymentError.setText("");
        orderAddressError.setText("");
        orderTotalError.setText("");
        orderFeesError.setText("");
    }

    private void initializeOrdersTable() {
        orderIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        orderRefColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReference()));
        orderDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateCommande()));

        orderDateColumn.setCellFactory(column -> new TableCell<Order, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        orderStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));
        orderPaymentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModePaiement()));
        orderAddressColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAdresseLivraison()));
        orderTotalColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getMontantTotal()).asObject());

        orderTotalColumn.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", montant));
                }
            }
        });

        orderFeesColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getFraisLivraison()).asObject());

        orderFeesColumn.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double frais, boolean empty) {
                super.updateItem(frais, empty);
                if (empty || frais == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", frais));
                }
            }
        });

        refreshOrdersBtn.setOnAction(e -> loadOrders());

        sortOrderCombo.getItems().addAll(
                "Trier par défaut (ID)",
                "Date (plus récente)",
                "Date (plus ancienne)",
                "Montant (croissant)",
                "Montant (décroissant)",
                "Référence (A-Z)",
                "Référence (Z-A)"
        );
        sortOrderCombo.setValue("Trier par défaut (ID)");

        searchOrderField.setOnAction(e -> searchOrders());
    }

    @FXML
    private void searchOrders() {
        currentOrderSearchTerm = searchOrderField.getText().trim();

        if (currentOrderSearchTerm.isEmpty()) {
            loadOrders();
        } else {
            try {
                OrderDAO dao = new OrderDAO();
                List<Order> searchResults = dao.searchOrdersByReference(currentOrderSearchTerm);
                allOrders = searchResults;
                applyOrderSorting(searchResults);
                orderCountLabel.setText("Résultats: " + searchResults.size() + " commandes pour \"" + currentOrderSearchTerm + "\"");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la recherche");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void clearOrderSearch() {
        searchOrderField.clear();
        currentOrderSearchTerm = "";
        loadOrders();
    }

    private void loadOrders() {
        try {
            OrderDAO dao = new OrderDAO();
            allOrders = dao.getAllOrders();
            applyOrderSorting(allOrders);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des commandes");
            e.printStackTrace();
        }
    }

    @FXML
    private void sortOrders() {
        currentOrderSortOption = sortOrderCombo.getValue();
        if (allOrders != null) {
            applyOrderSorting(allOrders);
        }
    }

    private void applyOrderSorting(List<Order> ordersToSort) {
        List<Order> sortedList = new ArrayList<>(ordersToSort);

        switch (currentOrderSortOption) {
            case "Date (plus récente)":
                sortedList.sort((o1, o2) -> {
                    if (o1.getDateCommande() == null && o2.getDateCommande() == null) return 0;
                    if (o1.getDateCommande() == null) return 1;
                    if (o2.getDateCommande() == null) return -1;
                    return o2.getDateCommande().compareTo(o1.getDateCommande());
                });
                break;
            case "Date (plus ancienne)":
                sortedList.sort((o1, o2) -> {
                    if (o1.getDateCommande() == null && o2.getDateCommande() == null) return 0;
                    if (o1.getDateCommande() == null) return 1;
                    if (o2.getDateCommande() == null) return -1;
                    return o1.getDateCommande().compareTo(o2.getDateCommande());
                });
                break;
            case "Montant (croissant)":
                sortedList.sort((o1, o2) -> Double.compare(o1.getMontantTotal(), o2.getMontantTotal()));
                break;
            case "Montant (décroissant)":
                sortedList.sort((o1, o2) -> Double.compare(o2.getMontantTotal(), o1.getMontantTotal()));
                break;
            case "Référence (A-Z)":
                sortedList.sort((o1, o2) -> o1.getReference().compareToIgnoreCase(o2.getReference()));
                break;
            case "Référence (Z-A)":
                sortedList.sort((o1, o2) -> o2.getReference().compareToIgnoreCase(o1.getReference()));
                break;
            default:
                sortedList.sort((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));
                break;
        }

        ObservableList<Order> orders = FXCollections.observableArrayList(sortedList);
        ordersTable.setItems(orders);

        if (!currentOrderSearchTerm.isEmpty()) {
            orderCountLabel.setText("Résultats: " + sortedList.size() + " commandes pour \"" + currentOrderSearchTerm + "\"");
        } else {
            orderCountLabel.setText("Total: " + sortedList.size() + " commandes");
        }
    }

    private void setupEventHandlers() {
        // Articles
        addArticleBtn.setOnAction(e -> handleAddArticle());
        updateArticleBtn.setOnAction(e -> updateArticle());
        clearArticleBtn.setOnAction(e -> clearArticleForm());
        deleteArticleTableBtn.setOnAction(e -> handleDeleteArticleFromTable());

        // Articles Search and Sort
        searchArticleBtn.setOnAction(e -> searchArticles());
        clearArticleSearchBtn.setOnAction(e -> clearArticleSearch());
        applyArticleSortBtn.setOnAction(e -> sortArticles());

        // Orders
        addOrderBtn.setOnAction(e -> handleAddOrder());
        updateOrderBtn.setOnAction(e -> updateOrder());
        deleteOrderBtn.setOnAction(e -> handleDeleteOrderFromTable());
        clearOrderBtn.setOnAction(e -> clearOrderForm());

        // Orders Search and Sort
        searchOrderBtn.setOnAction(e -> searchOrders());
        clearOrderSearchBtn.setOnAction(e -> clearOrderSearch());
        applyOrderSortBtn.setOnAction(e -> sortOrders());

        // Article Statistics buttons
        if (consulterPrixBtn != null) {
            consulterPrixBtn.setOnAction(e -> handleConsulterPrix());
        }
        if (consulterPoidsBtn != null) {
            consulterPoidsBtn.setOnAction(e -> handleConsulterPoids());
        }

        // Order Statistics buttons
        if (consulterMontantBtn != null) {
            consulterMontantBtn.setOnAction(e -> handleConsulterMontant());
        }
        if (consulterFraisBtn != null) {
            consulterFraisBtn.setOnAction(e -> handleConsulterFrais());
        }

        // Consultant Bot - No need for additional setup as it's self-contained
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}