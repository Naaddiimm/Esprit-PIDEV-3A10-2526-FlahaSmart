package controllers.produit;

import entities.StockProduit;
import javafx.event.ActionEvent;
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

import java.io.File;
import java.io.IOException;

public class CardStockFrontController {
    @FXML private Button qrBtn;


    @FXML private Label typeLabel;
    @FXML private Label varieteLabel;
    @FXML private Label dateLabel;
    @FXML private Label statutLabel;
    @FXML private Button viewConsommationBtn;

    private StockProduit produit;

    public void setData(StockProduit p) {
        this.produit = p;
        typeLabel.setText(p.getTypeProduit());
        varieteLabel.setText(p.getVariete());
        dateLabel.setText(p.getDateDebut().toString());
        statutLabel.setText(p.getStatut());
        if (produit.getCodeQr() == null || produit.getCodeQr().isEmpty()) {
            qrBtn.setDisable(true);
            qrBtn.setTooltip(new Tooltip("QR code non généré"));
        } else {
            qrBtn.setDisable(false);
            qrBtn.setTooltip(null);
        }
    }

    @FXML
    private void openConsommationDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit/ConsommationDetailPopup.fxml"));
            Parent root = loader.load();
            ConsommationDetailController controller = loader.getController();
            controller.setProductId(produit.getIdProduit());
            controller.setProductName(produit.getTypeProduit() + " " + produit.getVariete());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Détails de consommation - " + produit.getTypeProduit());

            Scene scene = new Scene(root);
            scene.setFill(null);
            scene.getStylesheets().add(getClass().getResource("/produit/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML

    public void openApiDetail(ActionEvent actionEvent) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit/popup.fxml"));
                Parent root = loader.load();
                ApiConsommationDetailController controller = loader.getController();
                controller.setProductId(produit.getIdProduit());
                controller.setProductName(produit.getTypeProduit() + " " + produit.getVariete());
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setTitle("Détails de consommation - " + produit.getTypeProduit());

                Scene scene = new Scene(root);
                scene.setFill(null);
                scene.getStylesheets().add(getClass().getResource("/produit/css/style.css").toExternalForm());

                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @FXML
    private void openQRCode() {
        if (produit == null || produit.getCodeQr() == null || produit.getCodeQr().isEmpty()) {
            showAlert("QR Code non disponible", "Aucun QR code généré pour ce produit.");
            return;
        }

        try {
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}