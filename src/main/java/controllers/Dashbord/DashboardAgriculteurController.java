package controllers.Dashbord;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contrôleur du tableau de bord Agriculteur.
 * ✅ Tous les @FXML sont null-safe — NullPointerException impossible.
 */
public class DashboardAgriculteurController implements Initializable {

    // ── Champs FXML optionnels ────────────────────────────────────────────────
    @FXML private Label  welcomeLabel;
    @FXML private Text   welcomeText;
    @FXML private Text   dateText;     // ← était null → crash corrigé
    @FXML private Label  dateLabel;
    @FXML private Label  statusLabel;

    // Panneaux de contenu
    @FXML private Pane   panelAccueil;
    @FXML private Pane   panelStatistiques;
    @FXML private Pane   panelProduits;
    @FXML private Pane   panelCommandes;
    @FXML private Pane   panelParcelles;
    @FXML private Pane   panelMeteo;
    @FXML private Pane   panelProfil;
    @FXML private Pane   panelMessages;
    @FXML private Pane   panelAlerte;
    @FXML private Pane   panelCalendrier;
    @FXML private Pane   panelRapports;
    @FXML private Pane   panelParametres;

    private User loggedInUser;

    // ─────────────────────────────────────────────────────────────────────────
    // Initialisation — 100% null-safe
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ DashboardAgriculteurController initialisé");

        // FIX : null-check AVANT tout appel sur un champ @FXML
        String today = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH));

        if (dateText  != null) dateText.setText(today);
        if (dateLabel != null) dateLabel.setText(today);

        showPanel(panelAccueil);
    }

    /** Injecte l'utilisateur connecté depuis Login. */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user == null) return;

        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String nom    = user.getNom()    != null ? user.getNom()    : "";
        String full   = (prenom + " " + nom).trim();

        if (welcomeLabel != null) welcomeLabel.setText("Bienvenue, " + full + " 👋");
        if (welcomeText  != null) welcomeText.setText("Bienvenue, " + full + " 👋");
        System.out.println("👤 Utilisateur chargé : " + full);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers navigation
    // ─────────────────────────────────────────────────────────────────────────

    @FXML public void handleAccueil(ActionEvent e)      { showPanel(panelAccueil);      setStatus("Accueil"); }
    @FXML public void handleStatistiques(ActionEvent e) { showPanel(panelStatistiques); setStatus("Statistiques"); }
    @FXML public void handleProduits(ActionEvent e)     { showPanel(panelProduits);     setStatus("Produits"); }
    @FXML public void handleCommandes(ActionEvent e)    { showPanel(panelCommandes);    setStatus("Commandes"); }
    @FXML public void handleParcelles(ActionEvent e)    { showPanel(panelParcelles);    setStatus("Parcelles"); }
    @FXML public void handleMeteo(ActionEvent e)        { showPanel(panelMeteo);        setStatus("Météo"); }
    @FXML public void handleProfil(ActionEvent e)       { showPanel(panelProfil);       setStatus("Profil"); }
    @FXML public void handleMessages(ActionEvent e)     { showPanel(panelMessages);     setStatus("Messages"); }
    @FXML public void handleAlertes(ActionEvent e)      { showPanel(panelAlerte);       setStatus("Alertes"); }
    @FXML public void handleCalendrier(ActionEvent e)   { showPanel(panelCalendrier);   setStatus("Calendrier"); }
    @FXML public void handleRapports(ActionEvent e)     { showPanel(panelRapports);     setStatus("Rapports"); }
    @FXML public void handleParametres(ActionEvent e)   { showPanel(panelParametres);   setStatus("Paramètres"); }

    // Alias noms alternatifs
    @FXML public void handleStatistique(ActionEvent e)  { handleStatistiques(e); }
    @FXML public void handleStat(ActionEvent e)         { handleStatistiques(e); }
    @FXML public void handleProduit(ActionEvent e)      { handleProduits(e); }
    @FXML public void handleCommande(ActionEvent e)     { handleCommandes(e); }
    @FXML public void handleParcelle(ActionEvent e)     { handleParcelles(e); }
    @FXML public void handleAlerte(ActionEvent e)       { handleAlertes(e); }
    @FXML public void handleMessage(ActionEvent e)      { handleMessages(e); }
    @FXML public void handleRapport(ActionEvent e)      { handleRapports(e); }
    @FXML public void handleHome(ActionEvent e)         { handleAccueil(e); }
    @FXML public void handleDashboard(ActionEvent e)    { handleAccueil(e); }
    @FXML public void handleSettings(ActionEvent e)     { handleParametres(e); }
    @FXML public void handleProfile(ActionEvent e)      { handleProfil(e); }
    @FXML public void handleWeather(ActionEvent e)      { handleMeteo(e); }
    @FXML public void handleCalendar(ActionEvent e)     { handleCalendrier(e); }
    @FXML public void handleOrders(ActionEvent e)       { handleCommandes(e); }
    @FXML public void handleProducts(ActionEvent e)     { handleProduits(e); }
    @FXML public void handleReports(ActionEvent e)      { handleRapports(e); }

    // ─────────────────────────────────────────────────────────────────────────
    // Actions internes
    // ─────────────────────────────────────────────────────────────────────────

    @FXML public void handleRefresh(ActionEvent e)         { setStatus("Actualisé"); }
    @FXML public void handleExport(ActionEvent e)          { setStatus("Export en cours..."); }
    @FXML public void handleAjouterProduit(ActionEvent e)  { setStatus("Ajout produit"); }
    @FXML public void handleAjouterParcelle(ActionEvent e) { setStatus("Ajout parcelle"); }

    // ─────────────────────────────────────────────────────────────────────────
    // Déconnexion
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void handleDeconnexion(ActionEvent event) {
        System.out.println("🚪 Déconnexion");
        controllers.Auth.Session.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("FlahaSmart - Connexion");
                stage.setMaximized(true);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void handleLogout(ActionEvent e)     { handleDeconnexion(e); }
    @FXML public void handleSignOut(ActionEvent e)    { handleDeconnexion(e); }
    @FXML public void handleDisconnect(ActionEvent e) { handleDeconnexion(e); }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires privés
    // ─────────────────────────────────────────────────────────────────────────

    private void showPanel(Pane target) {
        Pane[] all = {
                panelAccueil, panelStatistiques, panelProduits, panelCommandes,
                panelParcelles, panelMeteo, panelProfil, panelMessages,
                panelAlerte, panelCalendrier, panelRapports, panelParametres
        };
        for (Pane p : all) {
            if (p != null) { p.setVisible(false); p.setManaged(false); }
        }
        if (target != null) { target.setVisible(true); target.setManaged(true); }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
        System.out.println("📌 Section : " + msg);
    }

    private Stage getStage() {
        if (welcomeLabel != null && welcomeLabel.getScene() != null)
            return (Stage) welcomeLabel.getScene().getWindow();
        if (welcomeText != null && welcomeText.getScene() != null)
            return (Stage) welcomeText.getScene().getWindow();
        return javafx.stage.Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w)
                .findFirst().orElse(null);
    }
}
