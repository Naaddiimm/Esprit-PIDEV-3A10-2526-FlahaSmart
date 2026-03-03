package com.example.flahasmarty;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConsultantBot extends VBox {

    private ListView<TextFlow> chatHistory;
    private TextField messageInput;
    private Button sendButton;
    private ComboBox<String> modelComboBox;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private Button clearButton;

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public ConsultantBot() {
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("form-container");

        // Title
        Label title = new Label("🤖 Consultant IA");
        title.getStyleClass().add("form-title");

        // Model selection
        HBox modelBox = new HBox(10);
        modelBox.setAlignment(Pos.CENTER_LEFT);

        Label modelLabel = new Label("Modèle:");
        modelLabel.getStyleClass().add("form-label");

        modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll("deepseek-r1:1.5b", "llama2", "mistral", "codellama");
        modelComboBox.setValue("deepseek-r1:1.5b");
        modelComboBox.setPrefWidth(200);
        modelComboBox.getStyleClass().add("form-combo");

        statusLabel = new Label("Prêt");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-text-fill: #2e7d32;");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(20, 20);
        loadingIndicator.setVisible(false);

        modelBox.getChildren().addAll(modelLabel, modelComboBox, statusLabel, loadingIndicator);

        // Chat history area
        Label chatLabel = new Label("💬 Conversation");
        chatLabel.getStyleClass().add("section-title");

        chatHistory = new ListView<>();
        chatHistory.setPrefHeight(450);
        chatHistory.setCellFactory(lv -> new TextFlowCell());
        chatHistory.getStyleClass().add("chat-history");

        // Message input area
        Label inputLabel = new Label("Votre message:");
        inputLabel.getStyleClass().add("form-label");

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        messageInput = new TextField();
        messageInput.setPromptText("Posez votre question au consultant...");
        messageInput.setPrefHeight(40);
        messageInput.getStyleClass().add("form-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        sendButton = new Button("Envoyer");
        sendButton.setPrefHeight(40);
        sendButton.setPrefWidth(120);
        sendButton.getStyleClass().add("btn-primary");

        clearButton = new Button("Effacer");
        clearButton.setPrefHeight(40);
        clearButton.setPrefWidth(120);
        clearButton.getStyleClass().add("btn-light");

        inputBox.getChildren().addAll(messageInput, sendButton, clearButton);

        // Welcome message
        addWelcomeMessage();

        // Add all components
        getChildren().addAll(title, modelBox, chatLabel, chatHistory, inputLabel, inputBox);
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());
        clearButton.setOnAction(e -> clearChat());
    }

    private void addWelcomeMessage() {
        TextFlow welcomeFlow = new TextFlow();
        Text prefix = new Text("🤖 ");
        prefix.setFill(Color.web("#2e7d32"));
        prefix.setStyle("-fx-font-weight: bold;");

        Text message = new Text("Bonjour ! Je suis votre consultant IA. Comment puis-je vous aider aujourd'hui ?");
        message.setFill(Color.web("#4D6F50"));

        welcomeFlow.getChildren().addAll(prefix, message);
        chatHistory.getItems().add(welcomeFlow);
    }

    private void clearChat() {
        chatHistory.getItems().clear();
        addWelcomeMessage();
        messageInput.clear();
    }

    private void sendMessage() {
        String question = messageInput.getText().trim();

        if (question.isEmpty()) {
            showStatus("Veuillez entrer une question", true);
            return;
        }

        // Add user message to chat
        addUserMessage(question);

        // Clear input
        messageInput.clear();

        // Send to Ollama
        sendToOllama(question);
    }

    private void addUserMessage(String message) {
        Platform.runLater(() -> {
            TextFlow userFlow = new TextFlow();
            userFlow.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

            Text userText = new Text(message + " 👤");
            userText.setFill(Color.web("#2c3e50"));
            userText.setStyle("-fx-font-weight: bold;");

            userFlow.getChildren().add(userText);
            chatHistory.getItems().add(userFlow);

            // Scroll to bottom
            chatHistory.scrollTo(chatHistory.getItems().size() - 1);
        });
    }

    private void addBotMessage(String message) {
        Platform.runLater(() -> {
            TextFlow botFlow = new TextFlow();

            Text prefix = new Text("🤖 ");
            prefix.setFill(Color.web("#2e7d32"));
            prefix.setStyle("-fx-font-weight: bold;");

            Text botText = new Text(message);
            botText.setFill(Color.web("#4D6F50"));

            botFlow.getChildren().addAll(prefix, botText);
            chatHistory.getItems().add(botFlow);

            // Scroll to bottom
            chatHistory.scrollTo(chatHistory.getItems().size() - 1);

            // Hide loading indicator
            loadingIndicator.setVisible(false);
            sendButton.setDisable(false);
            messageInput.setDisable(false);
            showStatus("Prêt", false);
        });
    }

    private void addErrorMessage(String error) {
        Platform.runLater(() -> {
            TextFlow errorFlow = new TextFlow();

            Text prefix = new Text("⚠ ");
            prefix.setFill(Color.RED);
            prefix.setStyle("-fx-font-weight: bold;");

            Text errorText = new Text("Erreur: " + error);
            errorText.setFill(Color.RED);

            errorFlow.getChildren().addAll(prefix, errorText);
            chatHistory.getItems().add(errorFlow);

            // Scroll to bottom
            chatHistory.scrollTo(chatHistory.getItems().size() - 1);

            // Hide loading indicator
            loadingIndicator.setVisible(false);
            sendButton.setDisable(false);
            messageInput.setDisable(false);
            showStatus("Erreur", true);
        });
    }

    private void showStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            if (isError) {
                statusLabel.setStyle("-fx-text-fill: #c62828;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #2e7d32;");
            }
        });
    }

    private void sendToOllama(String question) {
        // Show loading indicator
        loadingIndicator.setVisible(true);
        sendButton.setDisable(true);
        messageInput.setDisable(true);
        showStatus("Envoi en cours...", false);

        // Run in background thread
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(OLLAMA_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);

                // Create JSON payload
                String selectedModel = modelComboBox.getValue();
                String payload = String.format(
                        "{\"model\":\"%s\",\"prompt\":\"%s (Réponds en français)\",\"stream\":false}",
                        selectedModel,
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
                        addBotMessage(answer);
                    } else {
                        addErrorMessage("Réponse vide du serveur");
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
                    addErrorMessage("Code " + responseCode + ": " + errorResponse.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = e.getMessage();
                if (e instanceof java.net.ConnectException) {
                    errorMsg = "Impossible de se connecter à Ollama. Vérifiez que le service est lancé sur http://localhost:11434";
                }
                addErrorMessage(errorMsg);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private String extractResponseFromJson(String json) {
        try {
            // Simple JSON parsing without external library
            // Look for "response":"value" pattern
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

    // Custom ListView Cell for TextFlow
    private static class TextFlowCell extends ListCell<TextFlow> {
        @Override
        protected void updateItem(TextFlow item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(item);
                setStyle("-fx-background-color: transparent; -fx-padding: 5;");
            }
        }
    }
}