package controllers.commande;

import entities.commande.Todo;
import services.commande.TodoDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;
import tools.myConnection;
public class TodoController {

    @FXML private TextField consultantIdField;
    @FXML private TextField taskField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TableView<Todo> todoTable;
    @FXML private TableColumn<Todo, String> todoConsultantColumn;
    @FXML private TableColumn<Todo, String> todoTaskColumn;
    @FXML private TableColumn<Todo, String> todoStatusColumn;
    @FXML private TableColumn<Todo, Void> todoActionsColumn;
    @FXML private Button addTodoBtn, refreshTodoBtn;
    @FXML private Label todoCountLabel;

    private List<Todo> allTodos;
    private TodoDAO todoDAO;

    @FXML
    public void initialize() {
        todoDAO = new TodoDAO();

        // Initialize status combo
        statusCombo.getItems().addAll("TODO", "DOING", "DONE");
        statusCombo.setValue("TODO");

        // Initialize table columns
        todoConsultantColumn.setCellValueFactory(new PropertyValueFactory<>("nomTache"));
        todoTaskColumn.setCellValueFactory(new PropertyValueFactory<>("tache"));
        todoStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Style status column based on value
        todoStatusColumn.setCellFactory(column -> new TableCell<Todo, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "TODO":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "DOING":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "DONE":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Add action buttons column (Modify and Delete)
        addActionButtonsToTable();

        // Set up event handlers
        addTodoBtn.setOnAction(e -> handleAddTodo());
        refreshTodoBtn.setOnAction(e -> loadTodos());

        // Load todos
        loadTodos();
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Todo, Void>, TableCell<Todo, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Todo, Void> call(final TableColumn<Todo, Void> param) {
                return new TableCell<>() {

                    private final Button modifyBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final HBox pane = new HBox(10, modifyBtn, deleteBtn);

                    {
                        // Use text buttons
                        modifyBtn.setText("✎");
                        modifyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");

                        deleteBtn.setText("✕");
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");

                        // Add tooltips
                        modifyBtn.setTooltip(new Tooltip("Modifier cette tâche"));
                        deleteBtn.setTooltip(new Tooltip("Supprimer cette tâche"));

                        // Set button widths
                        modifyBtn.setPrefWidth(50);
                        deleteBtn.setPrefWidth(50);

                        // Center the buttons in the cell
                        pane.setAlignment(Pos.CENTER);

                        // Set button actions
                        modifyBtn.setOnAction(event -> {
                            Todo todo = getTableView().getItems().get(getIndex());
                            handleModifyTodo(todo);
                        });

                        deleteBtn.setOnAction(event -> {
                            Todo todo = getTableView().getItems().get(getIndex());
                            handleDeleteTodo(todo);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        todoActionsColumn.setCellFactory(cellFactory);
    }

    private void handleAddTodo() {
        try {
            String consultantId = consultantIdField.getText().trim();
            String task = taskField.getText().trim();
            String status = statusCombo.getValue();

            // Validation
            if (consultantId.isEmpty()) {
                showAlert("Erreur de validation", "Veuillez entrer le nom du consultant");
                return;
            }

            if (task.isEmpty()) {
                showAlert("Erreur de validation", "Veuillez entrer une tâche");
                return;
            }

            if (status == null || status.isEmpty()) {
                showAlert("Erreur de validation", "Veuillez sélectionner un statut");
                return;
            }

            // Check if task already exists for this consultant
            Todo existingTodo = todoDAO.findTodo(consultantId, task);
            if (existingTodo != null) {
                showAlert("Erreur", "Cette tâche existe déjà pour ce consultant");
                return;
            }

            Todo todo = new Todo(consultantId, task, status);
            todoDAO.insertTodo(todo);

            showAlert("Succès", "Tâche ajoutée avec succès ✅");
            clearForm();
            loadTodos();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la tâche ❌");
            e.printStackTrace();
        }
    }

    private void handleModifyTodo(Todo todo) {
        try {
            // Create a dialog for modification
            Dialog<Todo> dialog = new Dialog<>();
            dialog.setTitle("Modifier la tâche");
            dialog.setHeaderText("Modification de la tâche");

            // Set the button types
            ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            // Create form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField consultantField = new TextField(todo.getNomTache());
            consultantField.setPromptText("Nom du consultant");
            consultantField.setPrefWidth(200);

            TextField taskField = new TextField(todo.getTache());
            taskField.setPromptText("Description de la tâche");
            taskField.setPrefWidth(300);

            ComboBox<String> statusField = new ComboBox<>();
            statusField.getItems().addAll("TODO", "DOING", "DONE");
            statusField.setValue(todo.getStatut());
            statusField.setPrefWidth(150);

            grid.add(new Label("Consultant:"), 0, 0);
            grid.add(consultantField, 1, 0);
            grid.add(new Label("Tâche:"), 0, 1);
            grid.add(taskField, 1, 1);
            grid.add(new Label("Statut:"), 0, 2);
            grid.add(statusField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // Store the original values
            String originalConsultant = todo.getNomTache();
            String originalTask = todo.getTache();
            String originalStatus = todo.getStatut();

            // Convert the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    // Validate fields
                    if (consultantField.getText().trim().isEmpty() ||
                            taskField.getText().trim().isEmpty() ||
                            statusField.getValue() == null) {
                        showAlert("Erreur", "Tous les champs doivent être remplis");
                        return null;
                    }

                    // Create updated todo
                    Todo updatedTodo = new Todo(
                            consultantField.getText().trim(),
                            taskField.getText().trim(),
                            statusField.getValue()
                    );
                    return updatedTodo;
                }
                return null;
            });

            Optional<Todo> result = dialog.showAndWait();
            result.ifPresent(updatedTodo -> {
                // Check if consultant or task changed
                if (!originalConsultant.equals(updatedTodo.getNomTache()) ||
                        !originalTask.equals(updatedTodo.getTache())) {
                    // Check if new combination already exists
                    Todo existing = todoDAO.findTodo(updatedTodo.getNomTache(), updatedTodo.getTache());
                    if (existing != null) {
                        showAlert("Erreur", "Cette tâche existe déjà pour ce consultant");
                        return;
                    }
                }

                // Create a copy of original for update
                Todo originalTodo = new Todo(originalConsultant, originalTask, originalStatus);
                todoDAO.updateTodo(originalTodo, updatedTodo);
                showAlert("Succès", "Tâche modifiée avec succès ✅");
                loadTodos();
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification de la tâche");
            e.printStackTrace();
        }
    }

    private void handleDeleteTodo(Todo todo) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la tâche");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette tâche ?\n\n" +
                "Consultant: " + todo.getNomTache() + "\n" +
                "Tâche: " + todo.getTache() + "\n" +
                "Statut: " + todo.getStatut());

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                todoDAO.deleteTodo(todo.getNomTache(), todo.getTache());
                loadTodos();
                showAlert("Succès", "Tâche supprimée avec succès ✅");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression de la tâche ❌");
                e.printStackTrace();
            }
        }
    }

    private void loadTodos() {
        try {
            allTodos = todoDAO.getAllTodos();
            displayTodos(allTodos);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des tâches");
            e.printStackTrace();
        }
    }

    private void displayTodos(List<Todo> todos) {
        ObservableList<Todo> observableTodos = FXCollections.observableArrayList(todos);
        todoTable.setItems(observableTodos);

        // Update count label
        int total = todos.size();
        todoCountLabel.setText("Total: " + total + " tâche" + (total > 1 ? "s" : ""));
    }

    private void clearForm() {
        consultantIdField.clear();
        taskField.clear();
        statusCombo.setValue("TODO");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}