package controllers.commande;
import tools.myConnection;
import entities.commande.Todo;
import services.commande.TodoDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

public class TodoUpdateDialog extends Dialog<Todo> {

    private TextField searchConsultantField;
    private TextField searchTaskField;
    private TextField consultantField;
    private TextField taskField;
    private ComboBox<String> statutCombo;
    private Label statusLabel;
    private Button searchButton;

    private TodoDAO todoDAO;
    private Todo currentTodo;
    private Button confirmButton;

    public TodoUpdateDialog(Window owner) {
        try {
            todoDAO = new TodoDAO();

            // Create dialog
            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Mettre à jour une tâche");

            // Create content
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(createContent());

            // Add buttons
            ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            setDialogPane(dialogPane);

            // Get confirm button
            confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            if (confirmButton != null) {
                confirmButton.setDisable(true);
            }

            // Set result converter
            setResultConverter(buttonType -> {
                if (buttonType == confirmButtonType && validateForm()) {
                    updateTodoFromForm();
                    return currentTodo;
                }
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de créer la fenêtre: " + e.getMessage());
        }
    }

    private Node createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Search section
        Label searchLabel = new Label("🔍 RECHERCHER TÂCHE");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(10, 0, 10, 0));

        Label consultantLabel = new Label("Consultant:");
        consultantLabel.setStyle("-fx-font-weight: bold;");
        searchGrid.add(consultantLabel, 0, 0);

        searchConsultantField = new TextField();
        searchConsultantField.setPromptText("Nom du consultant");
        searchConsultantField.setPrefWidth(200);
        searchGrid.add(searchConsultantField, 1, 0);

        Label taskSearchLabel = new Label("Tâche:");
        taskSearchLabel.setStyle("-fx-font-weight: bold;");
        searchGrid.add(taskSearchLabel, 0, 1);

        searchTaskField = new TextField();
        searchTaskField.setPromptText("Description de la tâche");
        searchTaskField.setPrefWidth(200);
        searchGrid.add(searchTaskField, 1, 1);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        searchButton.setOnAction(e -> searchTodo());

        searchBox.getChildren().add(searchButton);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #bdc3c7;");

        // Form section
        Label formLabel = new Label("📝 MODIFIER LES INFORMATIONS");
        formLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 10 0 0 0;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Row 0: Consultant
        Label nomLabel = new Label("Consultant:");
        nomLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nomLabel, 0, 0);
        consultantField = new TextField();
        consultantField.setPromptText("Nom du consultant");
        consultantField.setPrefWidth(300);
        consultantField.setEditable(false);
        grid.add(consultantField, 1, 0);

        // Row 1: Tache
        Label tacheLabel = new Label("Tâche:");
        tacheLabel.setStyle("-fx-font-weight: bold;");
        grid.add(tacheLabel, 0, 1);
        taskField = new TextField();
        taskField.setPromptText("Description de la tâche");
        taskField.setPrefWidth(300);
        taskField.setEditable(false);
        grid.add(taskField, 1, 1);

        // Row 2: Statut
        Label statutLabel = new Label("Statut:");
        statutLabel.setStyle("-fx-font-weight: bold;");
        grid.add(statutLabel, 0, 2);
        statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("TODO", "DOING", "DONE");
        statutCombo.setPrefWidth(150);
        statutCombo.setEditable(false);
        statutCombo.setDisable(true);
        grid.add(statutCombo, 1, 2);

        // Status label
        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-padding: 10 0 0 0;");

        content.getChildren().addAll(
                searchLabel, searchGrid, searchBox, separator, formLabel,
                grid, statusLabel
        );

        return content;
    }

    private void searchTodo() {
        String consultant = searchConsultantField.getText().trim();
        String task = searchTaskField.getText().trim();

        if (consultant.isEmpty() || task.isEmpty()) {
            showStatus("Veuillez entrer le nom du consultant ET la tâche", true);
            return;
        }

        try {
            System.out.println("[DEBUG] Searching for todo: " + consultant + " - " + task);
            Todo found = todoDAO.findTodo(consultant, task);

            if (found != null) {
                currentTodo = found;
                populateForm(found);
                enableFormFields(true);
                showStatus("Tâche trouvée ! Vous pouvez maintenant modifier les informations.", false);
                searchConsultantField.setDisable(true);
                searchTaskField.setDisable(true);
                searchButton.setDisable(true);
                confirmButton.setDisable(false);
                System.out.println("[DEBUG] Todo found and form populated");
            } else {
                showStatus("Aucune tâche trouvée pour: " + consultant + " - " + task, true);
                clearForm();
                enableFormFields(false);
                confirmButton.setDisable(true);
            }

        } catch (Exception e) {
            showStatus("Erreur lors de la recherche: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void enableFormFields(boolean enable) {
        consultantField.setEditable(enable);
        taskField.setEditable(enable);
        statutCombo.setDisable(!enable);
        statutCombo.setEditable(false);

        // Also change background color to indicate editable state
        String style = enable ? "-fx-background-color: white;" : "-fx-background-color: #f0f0f0;";
        consultantField.setStyle(style);
        taskField.setStyle(style);
    }

    private void populateForm(Todo todo) {
        consultantField.setText(todo.getNomTache());
        taskField.setText(todo.getTache());
        statutCombo.setValue(todo.getStatut());
    }

    private void updateTodoFromForm() {
        if (currentTodo != null) {
            currentTodo.setNomTache(consultantField.getText().trim());
            currentTodo.setTache(taskField.getText().trim());
            currentTodo.setStatut(statutCombo.getValue());
        }
    }

    private boolean validateForm() {
        if (currentTodo == null) {
            showStatus("Veuillez d'abord rechercher une tâche", true);
            return false;
        }

        if (consultantField.getText().trim().isEmpty()) {
            showStatus("Le nom du consultant est obligatoire", true);
            return false;
        }

        if (taskField.getText().trim().isEmpty()) {
            showStatus("La description de la tâche est obligatoire", true);
            return false;
        }

        if (statutCombo.getValue() == null || statutCombo.getValue().isEmpty()) {
            showStatus("Le statut est obligatoire", true);
            return false;
        }

        showStatus("Formulaire valide !", false);
        return true;
    }

    private void clearForm() {
        consultantField.clear();
        taskField.clear();
        statutCombo.setValue(null);
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}