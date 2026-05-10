package controllers.produit;

import entities.StockProduit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import services.StockProduitService;
import utilies.QRCodeGenerator;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class StockAgrichController {



    @FXML private TextField typeField;
    @FXML private TextField varieteField;
    @FXML private ComboBox<String> statutBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Label errorLabel;
    @FXML private FlowPane container;
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
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        loadProduits();
    }

    private boolean validateFields(){
        errorLabel.setText("");
        resetStyles();
        boolean valid = true;

        String type = typeField.getText().trim();
        String variete = varieteField.getText().trim();

        if(type.length() < 3){
            markError(typeField,"Type min 3 caractères");
            valid = false;
        }
        else if(!type.matches("[a-zA-ZÀ-ÿ ]+")){
            markError(typeField,"Type invalide (lettres uniquement)");
            valid = false;
        }

        if(variete.length() < 2){
            markError(varieteField,"Variété trop courte");
            valid = false;
        }
        else if(!variete.matches("[a-zA-ZÀ-ÿ0-9 ]+")){
            markError(varieteField,"Variété invalide");
            valid = false;
        }

        if(dateDebutPicker.getValue()==null){
            markError(dateDebutPicker,"Date début obligatoire");
            valid = false;
        }
        else if(dateDebutPicker.getValue().isBefore(LocalDate.now())){
            markError(dateDebutPicker,"Date doit être aujourd'hui ou future");
            valid = false;
        }

        if(dateFinPicker.getValue()!=null && dateDebutPicker.getValue()!=null){
            if(dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())){
                markError(dateFinPicker,"Date fin > date début");
                valid = false;
            }

            Period period = Period.between(dateDebutPicker.getValue(), dateFinPicker.getValue());
            int totalDays = period.getYears()*365 + period.getMonths()*30 + period.getDays();
            if(totalDays > 365){
                markError(dateFinPicker,"Durée max = 365 jours");
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

                errorLabel.setText("✔ Produit ajouté");
            } else {
                fillProduitFromForm(selectedProduit);
                service.modifier(selectedProduit);

                String data = "Produit: " + selectedProduit.getTypeProduit() + " - " + selectedProduit.getVariete() + "\nID: " + selectedProduit.getIdProduit();
                String qrPath = QRCodeGenerator.generateQRCode(data, selectedProduit.getIdProduit());
                service.updateQrCode(selectedProduit.getIdProduit(), qrPath);

                errorLabel.setText("✔ Produit modifié");
                selectedProduit = null;
            }

            clearForm();
            loadProduits();
        } catch(Exception e){
            errorLabel.setText("Erreur : "+e.getMessage());
        }
    }

    public void deleteProduit(int id){
        try{
            service.supprimer(id);
            loadProduits();
            // Ajuster la page si nécessaire après suppression
            int maxPage = (int) Math.ceil((double) allProduits.size() / ITEMS_PER_PAGE) - 1;
            if (currentPage > maxPage && currentPage > 0) {
                currentPage--;
            }
            displayPage();
        } catch(Exception e){
            errorLabel.setText("Suppression impossible");
        }
    }

    public void editProduit(StockProduit p){
        selectedProduit = p;
        typeField.setText(p.getTypeProduit());
        varieteField.setText(p.getVariete());
        statutBox.setValue(p.getStatut());
        dateDebutPicker.setValue(p.getDateDebut());
        dateFinPicker.setValue(p.getDateFinEstimee());
        errorLabel.setText("Mode modification actif");
    }

    public void loadProduits(){
        try{
            allProduits = service.afficher();
            currentPage = 0;
            displayPage();
        } catch(Exception e){
            errorLabel.setText("Erreur chargement");
            e.printStackTrace();
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
                        getClass().getResource("/produit/CardStockAgri.fxml")
                );

                Parent card = loader.load();
                CardStockAchriController controller = loader.getController();
                controller.setData(p, this);
                container.getChildren().add(card);
            }

            int pageCount = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
            pageLabel.setText("Page " + (currentPage + 1) + " / " + (pageCount == 0 ? 1 : pageCount));

            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= pageCount - 1);
        } catch (Exception e) {
            errorLabel.setText("Erreur affichage");
            e.printStackTrace();
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
        s.setIdUser(1);
        if(dateFinPicker.getValue()!=null && dateFinPicker.getValue().isBefore(LocalDate.now()))
            s.setStatut("terminé");
        else
            s.setStatut("en cours");
    }

    private void clearForm(){
        typeField.clear();
        varieteField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        statutBox.setValue("en cours");
    }

    private void markError(Control field,String message){
        field.setStyle("-fx-border-color:red;-fx-border-width:2;");
        errorLabel.setText(message);
    }

    private void resetStyles(){
        typeField.setStyle("");
        varieteField.setStyle("");
        dateDebutPicker.setStyle("");
        dateFinPicker.setStyle("");
    }
}