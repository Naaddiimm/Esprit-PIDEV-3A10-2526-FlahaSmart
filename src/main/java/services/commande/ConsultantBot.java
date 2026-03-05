package services.commande;
import tools.myConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConsultantBot extends VBox {

    private TextField questionInput;
    private Button consulterButton;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "deepseek-r1:1.5b";

    public ConsultantBot() {
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #ffffff; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        // Title
        Label title = new Label("🤖 Consultation IA");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");
        title.setAlignment(Pos.CENTER);
        setAlignment(Pos.TOP_CENTER);

        // Model badge
        Label modelBadge = new Label("deepseek-r1:1.5b");
        modelBadge.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-background-color: #e8f5e9; -fx-padding: 8 25; -fx-background-radius: 25; -fx-font-size: 14px; -fx-border-color: #a5d6a7; -fx-border-width: 1; -fx-border-radius: 25;");
        modelBadge.setAlignment(Pos.CENTER);

        // Description
        Label description = new Label("Posez votre question à l'assistant IA");
        description.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px; -fx-padding: 0 0 20 0;");
        description.setAlignment(Pos.CENTER);

        // Input section
        VBox inputSection = new VBox(10);
        inputSection.setPadding(new Insets(20));
        inputSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 15; -fx-background-radius: 15; -fx-border-color: #c8e6c9; -fx-border-width: 1;");

        Label questionLabel = new Label("Votre question:");
        questionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 16px;");

        HBox inputBox = new HBox(15);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        questionInput = new TextField();
        questionInput.setPromptText("Ex: Quels sont les articles les plus vendus ?");
        questionInput.setPrefHeight(50);
        questionInput.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px; -fx-padding: 10 15; -fx-border-color: #c8e6c9; -fx-border-width: 1;");
        HBox.setHgrow(questionInput, Priority.ALWAYS);

        consulterButton = new Button("Consulter");
        consulterButton.setPrefHeight(50);
        consulterButton.setPrefWidth(150);
        consulterButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        consulterButton.setOnMouseEntered(e -> consulterButton.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        consulterButton.setOnMouseExited(e -> consulterButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));

        inputBox.getChildren().addAll(questionInput, consulterButton);

        // Status section
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("Prêt");
        statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-style: italic; -fx-font-size: 13px;");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(25, 25);
        loadingIndicator.setVisible(false);
        loadingIndicator.setStyle("-fx-progress-color: #2e7d32;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBox.getChildren().addAll(spacer, statusLabel, loadingIndicator);

        inputSection.getChildren().addAll(questionLabel, inputBox, statusBox);

        // Info section
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(20, 0, 0, 0));

        Label info1 = new Label("⚡ Réponse instantanée");
        info1.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-background-color: #e8f5e9; -fx-padding: 5 15; -fx-background-radius: 20;");

        Label info2 = new Label("🎯 Modèle deepseek-r1");
        info2.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-background-color: #e8f5e9; -fx-padding: 5 15; -fx-background-radius: 20;");

        Label info3 = new Label("💬 Consultation unique");
        info3.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-background-color: #e8f5e9; -fx-padding: 5 15; -fx-background-radius: 20;");

        infoBox.getChildren().addAll(info1, info2, info3);

        // Add all components
        getChildren().addAll(title, modelBadge, description, inputSection, infoBox);
    }

    private void setupEventHandlers() {
        consulterButton.setOnAction(e -> handleConsultation());
        questionInput.setOnAction(e -> handleConsultation());
    }

    private void handleConsultation() {
        String question = questionInput.getText().trim();

        if (question.isEmpty()) {
            showStatus("Veuillez entrer une question", true);
            return;
        }

        // Disable input during consultation
        questionInput.setDisable(true);
        consulterButton.setDisable(true);
        loadingIndicator.setVisible(true);
        showStatus("Consultation en cours...", false);

        // Send to Ollama
        sendToOllama(question);
    }

    private void showStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            if (isError) {
                statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-style: italic; -fx-font-size: 13px;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-style: italic; -fx-font-size: 13px;");
            }
        });
    }

    private void sendToOllama(String question) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URI uri = new URI(OLLAMA_URL);
                URL url = uri.toURL();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);

                // Create JSON payload with fixed model
                String payload = String.format(
                        "{\"model\":\"%s\",\"prompt\":\"%s (Réponds en français)\",\"stream\":false}",
                        MODEL_NAME,
                        escapeJson(question)
                );

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }

                    // Parse JSON response
                    String jsonResponse = response.toString();
                    String answer = extractResponseFromJson(jsonResponse);

                    if (answer != null && !answer.isEmpty()) {
                        showConsultationResult(question, answer);
                    } else {
                        showError("Réponse vide du serveur");
                    }
                } else {
                    // Read error stream
                    StringBuilder errorResponse = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            errorResponse.append(responseLine.trim());
                        }
                    }
                    showError("Code " + responseCode + ": " + errorResponse.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = e.getMessage();
                if (e instanceof java.net.ConnectException) {
                    errorMsg = "Impossible de se connecter à Ollama. Vérifiez que le service est lancé sur http://localhost:11434";
                }
                showError(errorMsg);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                // Re-enable input
                Platform.runLater(() -> {
                    questionInput.setDisable(false);
                    consulterButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    questionInput.clear();
                    showStatus("Prêt", false);
                });
            }
        }).start();
    }

    private void showConsultationResult(String question, String answer) {
        Platform.runLater(() -> {
            // Create popup stage
            Stage popupStage = new Stage();
            popupStage.setTitle("Résultat de la consultation");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setMinWidth(600);
            popupStage.setMinHeight(500);

            // Create main container
            VBox container = new VBox(20);
            container.setPadding(new Insets(25));
            container.setStyle("-fx-background-color: #ffffff;");

            // Header
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label iconLabel = new Label("🤖");
            iconLabel.setStyle("-fx-font-size: 32px;");

            VBox titleBox = new VBox(5);
            Label titleLabel = new Label("Consultation IA");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

            Label modelLabel = new Label("deepseek-r1:1.5b");
            modelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #e8f5e9; -fx-padding: 3 10; -fx-background-radius: 15;");

            titleBox.getChildren().addAll(titleLabel, modelLabel);
            headerBox.getChildren().addAll(iconLabel, titleBox);

            // Question section
            VBox questionBox = new VBox(10);
            questionBox.setPadding(new Insets(15));
            questionBox.setStyle("-fx-background-color: #e8f5e9; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #c8e6c9; -fx-border-width: 1;");

            Label questionHeader = new Label("📝 Votre question :");
            questionHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 14px;");

            Text questionText = new Text(question);
            questionText.setStyle("-fx-font-size: 14px;");
            TextFlow questionFlow = new TextFlow(questionText);
            questionFlow.setPadding(new Insets(10, 0, 0, 0));

            questionBox.getChildren().addAll(questionHeader, questionFlow);

            // Answer section
            VBox answerBox = new VBox(10);
            answerBox.setPadding(new Insets(15));
            answerBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #cccccc; -fx-border-width: 1;");
            VBox.setVgrow(answerBox, Priority.ALWAYS);

            Label answerHeader = new Label("💬 Réponse :");
            answerHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 14px;");

            Text answerText = new Text(answer);
            answerText.setStyle("-fx-font-size: 14px; -fx-line-spacing: 5;");
            TextFlow answerFlow = new TextFlow(answerText);
            answerFlow.setPadding(new Insets(10, 0, 0, 0));

            // Add scroll pane for long answers
            ScrollPane scrollPane = new ScrollPane(answerFlow);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

            answerBox.getChildren().addAll(answerHeader, scrollPane);

            // Close button
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
            closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;"));
            closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;"));
            closeButton.setOnAction(e -> popupStage.close());

            HBox buttonBox = new HBox(closeButton);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            // Add all to container
            container.getChildren().addAll(headerBox, questionBox, answerBox, buttonBox);

            // Create scene and show
            Scene scene = new Scene(container);
            popupStage.setScene(scene);
            popupStage.showAndWait();
        });
    }

    private void showError(String error) {
        Platform.runLater(() -> {
            // Create error popup
            Stage errorStage = new Stage();
            errorStage.setTitle("Erreur");
            errorStage.initModality(Modality.APPLICATION_MODAL);
            errorStage.setMinWidth(400);
            errorStage.setMinHeight(200);

            VBox container = new VBox(20);
            container.setPadding(new Insets(25));
            container.setStyle("-fx-background-color: #ffffff;");
            container.setAlignment(Pos.CENTER);

            Label iconLabel = new Label("❌");
            iconLabel.setStyle("-fx-font-size: 48px;");

            Label errorLabel = new Label("Erreur de consultation");
            errorLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c62828;");

            Label messageLabel = new Label(error);
            messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(350);
            messageLabel.setAlignment(Pos.CENTER);

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 8; -fx-cursor: hand;");
            closeButton.setOnAction(e -> errorStage.close());

            container.getChildren().addAll(iconLabel, errorLabel, messageLabel, closeButton);

            Scene scene = new Scene(container);
            errorStage.setScene(scene);
            errorStage.showAndWait();
        });
    }

    private String extractResponseFromJson(String json) {
        try {
            String responseKey = "\"response\":\"";
            int startIndex = json.indexOf(responseKey);
            if (startIndex != -1) {
                startIndex += responseKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}