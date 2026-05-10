package controllers.produit;

import entities.StockProduit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;

public class CardStockController {

    @FXML
    private Label typeLabel;
    @FXML
    private Label varieteLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label statutLabel;

    @FXML
    private Button deleteBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button consumptionBtn; // NOUVEAU BOUTON

    private StockProduit produit;
    private StockBackController parent;

    public void setData(StockProduit p, StockBackController parentController) {
        this.produit = p;
        this.parent = parentController;

        typeLabel.setText(p.getTypeProduit());
        varieteLabel.setText(p.getVariete());
        dateLabel.setText(p.getDateDebut().toString());
        statutLabel.setText(p.getStatut());

        // Gérer la visibilité des boutons si parent est null
        if (parent == null) {
            deleteBtn.setVisible(false);
            editBtn.setVisible(false);
            consumptionBtn.setVisible(false);
        } else {
            // Ajouter l'icône au bouton consommation si ce n'est pas déjà fait dans le FXML
            if (consumptionBtn.getGraphic() == null) {
                FontIcon icon = new FontIcon("fas-chart-line");
                icon.setIconSize(14);
                icon.setIconColor(javafx.scene.paint.Color.WHITE);
                consumptionBtn.setGraphic(icon);
            }
        }
        if (produit.getCodeQr() == null || produit.getCodeQr().isEmpty()) {
            qrBtn.setDisable(true);
            qrBtn.setTooltip(new Tooltip("QR code non généré"));
        } else {
            qrBtn.setDisable(false);
        }

    }

    // Méthode pour ouvrir le popup de consommation
    @FXML
    private void openConsumption() {
        try {
            // Charger le FXML du popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit/ConsommationPopup.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer les données du produit
            ConsommationPopupController controller = loader.getController();
            controller.setProductId(produit.getIdProduit());
            controller.setProductName(produit.getTypeProduit() + " " + produit.getVariete());

            // Créer et configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Gestion Consommation - " + produit.getTypeProduit());

            Scene scene = new Scene(root);
            scene.setFill(null); // Fond transparent
            scene.getStylesheets().add(getClass().getResource("/produit/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de consommation");
        }
    }

    @FXML
    private void editItem() {
        if (parent != null) {
            parent.editProduit(produit);
        } else {
            showAlert("Information", "Fonction d'édition non disponible");
        }
    }

    @FXML
    private void deleteItem() {
        if (parent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le produit");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer " + produit.getTypeProduit() + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    parent.deleteProduit(produit);
                }
            });
        } else {
            showAlert("Information", "Fonction de suppression non disponible");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML private Button qrBtn;
        @FXML
        private void openQRCode() {
            if (produit.getCodeQr() == null || produit.getCodeQr().isEmpty()) {
                showAlert("QR Code non disponible", "Aucun QR code généré pour ce produit.");
                return;
            }

            try {
                // Charger l'image à partir du chemin
                File file = new File(produit.getCodeQr());
                if (!file.exists()) {
                    showAlert("Fichier introuvable", "Le fichier QR code est introuvable.");
                    return;
                }

                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(300);
                imageView.setFitHeight(300);

                StackPane root = new StackPane(imageView);
                root.setStyle("-fx-background-color: white; -fx-padding: 20;");

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("QR Code - " + produit.getTypeProduit());
                stage.setScene(scene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'afficher le QR code.");

        }
    }
}
