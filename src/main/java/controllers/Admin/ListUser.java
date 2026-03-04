package controllers.Admin;

import entities.User;
import entities.Role;
import services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListUser implements Initializable {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nomColumn;

    @FXML
    private TableColumn<User, String> prenomColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, Role> roleColumn;

    @FXML
    private TableColumn<User, String> telephoneColumn;

    @FXML
    private TableColumn<User, String> villeColumn;

    @FXML
    private TableColumn<User, Boolean> actifColumn;

    @FXML
    private TableColumn<User, String> photoColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnStats;

    @FXML
    private Button btnAddUser;

    @FXML
    private Button btnExport;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnClearFilter;

    @FXML
    private Label statsText;

    @FXML
    private Label paginationLabel;

    @FXML
    private Button btnPrevPage;

    @FXML
    private Button btnNextPage;

    private UserService userService = new UserService();
    private ObservableList<User> originalUserList;
    private ObservableList<User> currentPageList;
    private int currentPage = 0;
    private int pageSize = 10;
    private User loggedInAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ ListUser controller initialisé");

        // Configuration des colonnes
        setupTableColumns();

        // Configuration des filtres
        setupFilters();

        // Charger les utilisateurs
        loadUsers();

        // Mettre à jour les statistiques
        updateStats();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_user"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        photoColumn.setCellValueFactory(new PropertyValueFactory<>("photo_profil"));

        // Configuration de la colonne photo
        setupPhotoColumn();

        // Configuration de la colonne statut
        setupActifColumn();

        // Configuration de la colonne rôle
        setupRoleColumn();

        // Configuration de la colonne actions
        setupActionColumn();
    }

    private void setupPhotoColumn() {
        photoColumn.setCellFactory(column -> new TableCell<User, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
                imageView.setClip(clip);
            }

            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);
                if (empty || photoPath == null || photoPath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(photoPath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), 40, 40, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(new Label("📷"));
                        }
                    } catch (Exception e) {
                        setGraphic(new Label("📷"));
                    }
                }
            }
        });
    }

    private void setupActifColumn() {
        actifColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(5);
                    Label label = new Label();

                    if (actif) {
                        circle.setFill(javafx.scene.paint.Color.GREEN);
                        label.setText("Actif");
                        label.setStyle("-fx-text-fill: #27ae60;");
                    } else {
                        circle.setFill(javafx.scene.paint.Color.RED);
                        label.setText("Inactif");
                        label.setStyle("-fx-text-fill: #ef4444;");
                    }

                    box.getChildren().addAll(circle, label);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupRoleColumn() {
        roleColumn.setCellFactory(column -> new TableCell<User, Role>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role.toString());
                    switch (role) {
                        case ADMINISTRATEUR:
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        case AGRICULTEUR:
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case CLIENT:
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private final Button btnView = new Button("Voir");
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnView, btnEdit, btnDelete);

            {
                btnView.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");

                btnView.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    viewUser(user);
                });

                btnEdit.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                btnDelete.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
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
        });
    }

    private void setupFilters() {
        filterComboBox.getItems().addAll(
                "Tous les rôles",
                "Administrateurs",
                "Agriculteurs",
                "Clients",
                "Actifs",
                "Inactifs"
        );
        filterComboBox.setValue("Tous les rôles");

        // Ajouter un écouteur pour le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers();
        });

        // Ajouter un écouteur pour le filtre
        filterComboBox.setOnAction(event -> filterUsers());
    }

    public void setLoggedInUser(User user) {
        this.loggedInAdmin = user;
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement de la liste...");
        loadUsers();
        updateStats();
    }

    @FXML
    private void handleStats() {
        // Rediriger vers la page des statistiques
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnStats.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les statistiques");
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir après ajout
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void handleExport() {
        // TODO: Implémenter l'export des données
        showAlert(Alert.AlertType.INFORMATION, "Export", "Fonctionnalité d'export à venir");
    }

    @FXML
    private void handleSearch() {
        filterUsers();
    }

    @FXML
    private void handleClearFilter() {
        searchField.clear();
        filterComboBox.setValue("Tous les rôles");
        loadUsers();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void handleNextPage() {
        if ((currentPage + 1) * pageSize < originalUserList.size()) {
            currentPage++;
            updatePagination();
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getEntities();
            originalUserList = FXCollections.observableArrayList(users);
            currentPage = 0;
            updatePagination();
            System.out.println("✅ " + users.size() + " utilisateurs chargés");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs");
        }
    }

    private void filterUsers() {
        if (originalUserList == null) return;

        String searchText = searchField.getText().toLowerCase();
        String filter = filterComboBox.getValue();

        List<User> filteredList = originalUserList.stream()
                .filter(user -> {
                    // Filtre de recherche
                    boolean matchesSearch = searchText.isEmpty() ||
                            (user.getNom() != null && user.getNom().toLowerCase().contains(searchText)) ||
                            (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(searchText)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText)) ||
                            (user.getTelephone() != null && user.getTelephone().contains(searchText));

                    // Filtre de rôle/statut
                    boolean matchesFilter = true;
                    if (filter != null) {
                        switch (filter) {
                            case "Administrateurs":
                                matchesFilter = user.getRole() == Role.ADMINISTRATEUR;
                                break;
                            case "Agriculteurs":
                                matchesFilter = user.getRole() == Role.AGRICULTEUR;
                                break;
                            case "Clients":
                                matchesFilter = user.getRole() == Role.CLIENT;
                                break;
                            case "Actifs":
                                matchesFilter = user.getActif() != null && user.getActif();
                                break;
                            case "Inactifs":
                                matchesFilter = user.getActif() == null || !user.getActif();
                                break;
                            default:
                                matchesFilter = true;
                        }
                    }

                    return matchesSearch && matchesFilter;
                })
                .collect(Collectors.toList());

        originalUserList = FXCollections.observableArrayList(filteredList);
        currentPage = 0;
        updatePagination();
        updateStats();
    }

    private void updatePagination() {
        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, originalUserList.size());

        if (fromIndex < originalUserList.size()) {
            List<User> pageUsers = originalUserList.subList(fromIndex, toIndex);
            userTable.setItems(FXCollections.observableArrayList(pageUsers));
        } else {
            userTable.setItems(FXCollections.emptyObservableList());
        }

        // Mettre à jour le label de pagination
        if (paginationLabel != null) {
            paginationLabel.setText(String.format("Affichage %d-%d sur %d utilisateurs",
                    fromIndex + 1, toIndex, originalUserList.size()));
        }

        // Activer/désactiver les boutons de pagination
        if (btnPrevPage != null) {
            btnPrevPage.setDisable(currentPage == 0);
        }
        if (btnNextPage != null) {
            btnNextPage.setDisable((currentPage + 1) * pageSize >= originalUserList.size());
        }
    }

    private void updateStats() {
        if (statsText != null && originalUserList != null) {
            statsText.setText("Total: " + originalUserList.size() + " utilisateurs");
        }
    }

    public void viewUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur de visualisation
            // AfficherUser controller = loader.getController();
            // controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur de modification
            // UpdateUser controller = loader.getController();
            // controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir après modification
            handleRefresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getPrenom() + " " + user.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteEntity(user);
                handleRefresh();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}