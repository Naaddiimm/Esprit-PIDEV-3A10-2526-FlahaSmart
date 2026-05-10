package controllers.produit;



import entities.StockProduit;
import services.StockProduitService;
import utilies.QRCodeGenerator;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockBackController {

    @FXML private TextField typeField;
    @FXML private TextField varieteField;
    @FXML private ComboBox<String> statutBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Label errorLabel;
    @FXML private FlowPane container;
    @FXML private StackPane toastContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private final StockProduitService service = new StockProduitService();
    private StockProduit selectedProduit = null;
    private List<StockProduit> allProduits = new ArrayList<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        statutBox.getItems().addAll("en cours", "terminé", "en croissance");
        statutBox.setValue("en cours");

        dateDebutPicker.setDayCellFactory(dp -> new DateCell(){
            @Override
            public void updateItem(LocalDate date, boolean empty){
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        loadProduits();
    }

    private void showToast(String message, String styleClass){
        if(toastContainer == null) return;
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", styleClass);
        toastContainer.getChildren().add(toast);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        PauseTransition stay = new PauseTransition(Duration.seconds(2));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }

    private boolean validateFields(){
        resetStyles();
        errorLabel.setText("");
        boolean valid = true;

        String type = typeField.getText().trim();
        String variete = varieteField.getText().trim();

        if(type.length() < 3){
            markError(typeField, "Type min 3 caractères");
            valid = false;
        }
        else if(!type.matches("[a-zA-ZÀ-ÿ ]+")){
            markError(typeField, "Type invalide (lettres uniquement)");
            valid = false;
        }

        if(variete.length() < 2){
            markError(varieteField, "Variété trop courte");
            valid = false;
        }
        else if(!variete.matches("[a-zA-ZÀ-ÿ0-9 ]+")){
            markError(varieteField, "Variété invalide");
            valid = false;
        }

        if(dateDebutPicker.getValue() == null){
            markError(dateDebutPicker, "Date début obligatoire");
            valid = false;
        }

        if(dateFinPicker.getValue() != null && dateDebutPicker.getValue() != null){
            if(dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())){
                markError(dateFinPicker, "Date fin > date début");
                valid = false;
            }
            long days = dateFinPicker.getValue().toEpochDay() - dateDebutPicker.getValue().toEpochDay();
            if(days > 365){
                markError(dateFinPicker, "Durée max 365 jours");
                valid = false;
            }
        }

        return valid;
    }

    @FXML
    public void add(){
        try{
            if(!validateFields()) return;

            if(selectedProduit == null){
                StockProduit s = new StockProduit();
                fillProduitFromForm(s);
                service.ajouter(s);

                String data = "Produit: " + s.getTypeProduit() + " - " + s.getVariete() + "\nID: " + s.getIdProduit();
                String qrPath = QRCodeGenerator.generateQRCode(data, s.getIdProduit());
                service.updateQrCode(s.getIdProduit(), qrPath);

                showToast("Produit ajouté avec succès", "toast-success");
            } else {
                fillProduitFromForm(selectedProduit);
                service.modifier(selectedProduit);

                String data = "Produit: " + selectedProduit.getTypeProduit() + " - " + selectedProduit.getVariete() + "\nID: " + selectedProduit.getIdProduit();
                String qrPath = QRCodeGenerator.generateQRCode(data, selectedProduit.getIdProduit());
                service.updateQrCode(selectedProduit.getIdProduit(), qrPath);

                showToast("Produit modifié avec succès", "toast-success");
                selectedProduit = null;
            }

            clearForm();
            loadProduits();
        } catch(Exception e){
            showToast("Erreur : " + e.getMessage(), "toast-error");
        }
    }

    public void deleteProduit(StockProduit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce produit ?");
        alert.setContentText(produit.getTypeProduit() + " - " + produit.getVariete());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(produit.getIdProduit());
                    showToast("Produit supprimé avec succès", "toast-success");
                    loadProduits();

                    if (selectedProduit != null && selectedProduit.getIdProduit() == produit.getIdProduit()) {
                        clearForm();
                        selectedProduit = null;
                    }

                    // Ajuster la page si nécessaire
                    int maxPage = (int) Math.ceil((double) allProduits.size() / ITEMS_PER_PAGE) - 1;
                    if (currentPage > maxPage && currentPage > 0) {
                        currentPage--;
                    }
                    displayPage();
                } catch(Exception e) {
                    e.printStackTrace();
                    showToast("Suppression impossible : " + e.getMessage(), "toast-error");
                }
            }
        });
    }

    public void editProduit(StockProduit p){
        selectedProduit = p;
        typeField.setText(p.getTypeProduit());
        varieteField.setText(p.getVariete());
        statutBox.setValue(p.getStatut());
        dateDebutPicker.setValue(p.getDateDebut());
        dateFinPicker.setValue(p.getDateFinEstimee());
        showToast("Mode modification activé pour : " + p.getTypeProduit(), "toast-warning");
    }

    public void loadProduits(){
        try{
            allProduits = service.afficher();
            currentPage = 0;
            displayPage();
        } catch(Exception e){
            e.printStackTrace();
            showToast("Erreur chargement : " + e.getMessage(), "toast-error");
        }
    }

    private void displayPage() {
        try {
            container.getChildren().clear();
            int total = allProduits.size();
            int start = currentPage * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, total);

            for (int i = start; i < end; i++) {
                StockProduit p = allProduits.get(i);
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/produit/CardStock.fxml")
                );
                Parent card = loader.load();
                CardStockController controller = loader.getController();
                controller.setData(p, this);
                container.getChildren().add(card);
            }

            int pageCount = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
            pageLabel.setText("Page " + (currentPage + 1) + " / " + (pageCount == 0 ? 1 : pageCount));

            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= pageCount - 1);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Erreur affichage", "toast-error");
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            displayPage();
        }
    }

    @FXML
    private void nextPage() {
        int pageCount = (int) Math.ceil((double) allProduits.size() / ITEMS_PER_PAGE);
        if (currentPage < pageCount - 1) {
            currentPage++;
            displayPage();
        }
    }

    private void fillProduitFromForm(StockProduit s){
        s.setTypeProduit(typeField.getText());
        s.setVariete(varieteField.getText());
        s.setDateDebut(dateDebutPicker.getValue());
        s.setDateFinEstimee(dateFinPicker.getValue());
        s.setStatut(statutBox.getValue());
        s.setIdUser(1);
    }

    private void clearForm(){
        typeField.clear();
        varieteField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        statutBox.setValue("en cours");
        selectedProduit = null;
    }

    private void markError(Control field, String message){
        field.setStyle("-fx-border-color:#e53935;-fx-border-width:2;");
        errorLabel.setText(message);
    }

    private void resetStyles(){
        typeField.setStyle("");
        varieteField.setStyle("");
        dateDebutPicker.setStyle("");
        dateFinPicker.setStyle("");
    }
}