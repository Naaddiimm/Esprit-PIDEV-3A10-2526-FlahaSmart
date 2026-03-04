package Controler;

import api.ApiClient;
import entities.thread;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import servise.ServiceThreads;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class AjouterThreadController {

    // =========================================================
    //  BASE DE TAGS AGRICOLES PAR CATÉGORIE
    // =========================================================
    private static final Map<String, List<String>> TAGS_PAR_CATEGORIE = new LinkedHashMap<>();

    static {
        TAGS_PAR_CATEGORIE.put("eau", Arrays.asList(
                "irrigation", "eau", "arrosage", "goutte-a-goutte", "drainage",
                "pompe", "canal", "pluie", "secheresse", "humidite", "nappe"
        ));
        TAGS_PAR_CATEGORIE.put("sol", Arrays.asList(
                "sol", "terre", "argile", "sable", "limon", "ph", "acidite",
                "erosion", "compost", "fumier", "matiere-organique", "labour"
        ));
        TAGS_PAR_CATEGORIE.put("cereales", Arrays.asList(
                "ble", "orge", "mais", "avoine", "sorgho", "cereales",
                "grain", "farine", "recolte", "moisson", "epi"
        ));
        TAGS_PAR_CATEGORIE.put("legumes", Arrays.asList(
                "tomate", "pomme-de-terre", "carotte", "oignon", "ail", "poivron",
                "courgette", "concombre", "laitue", "salade", "legumes", "potager"
        ));
        TAGS_PAR_CATEGORIE.put("fruits", Arrays.asList(
                "olive", "datte", "orange", "citron", "raisin", "figue",
                "grenade", "abricot", "peche", "arbres-fruitiers", "verger"
        ));
        TAGS_PAR_CATEGORIE.put("elevage", Arrays.asList(
                "vache", "mouton", "chevre", "poulet", "volaille", "elevage",
                "lait", "viande", "betail", "fourrage", "paturage", "veterinaire"
        ));
        TAGS_PAR_CATEGORIE.put("engrais", Arrays.asList(
                "engrais", "fertilisant", "azote", "phosphore", "potassium",
                "npk", "uree", "amendement", "nutrition", "carence"
        ));
        TAGS_PAR_CATEGORIE.put("maladies", Arrays.asList(
                "maladie", "champignon", "virus", "bacterie", "parasite",
                "insecte", "ravageur", "pucerons", "rouille", "mildiou",
                "traitement", "pesticide", "fongicide", "herbicide", "insecticide"
        ));
        TAGS_PAR_CATEGORIE.put("materiel", Arrays.asList(
                "tracteur", "moissonneuse", "semoir", "charrue", "pulverisateur",
                "materiel", "machine", "outil", "equipement", "serre"
        ));
        TAGS_PAR_CATEGORIE.put("techniques", Arrays.asList(
                "semis", "plantation", "taille", "greffage", "bouturage",
                "rotation", "jachere", "culture", "recolte", "stockage",
                "bio", "biologique", "permaculture", "agroforesterie"
        ));
        TAGS_PAR_CATEGORIE.put("meteo", Arrays.asList(
                "meteo", "temperature", "gel", "chaleur", "vent", "grele",
                "saison", "hiver", "ete", "printemps", "automne", "climat"
        ));
        TAGS_PAR_CATEGORIE.put("marche", Arrays.asList(
                "prix", "marche", "vente", "export", "subvention", "cooperative",
                "rentabilite", "cout", "revenu", "budget"
        ));
    }

    // =========================================================
    //  MÉTHODE PRINCIPALE AFFICHER
    // =========================================================
    public void afficher(Stage owner, ThreadController parent, int userId) {

        Stage stage = new Stage();
        stage.setTitle("Nouveau Thread");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        // ── HEADER ──────────────────────────────────────────────
        Label headerLabel = new Label("Nouveau Thread");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(headerLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(56);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setStyle("-fx-background-color: #6C63FF;");

        // ── CHAMPS ───────────────────────────────────────────────
        Label lblTitre = mkLabel("Titre *");
        TextField titreField = new TextField();
        titreField.setPromptText("Titre du thread...");
        styliserChamp(titreField);

        Label lblContenu = mkLabel("Contenu *");
        TextArea contenuArea = new TextArea();
        contenuArea.setPromptText("Décrivez votre question ou sujet...");
        contenuArea.setPrefHeight(120);
        contenuArea.setWrapText(true);
        contenuArea.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 8 12 8 12;");

        // ── TAGS ─────────────────────────────────────────────────
        Label lblTags = mkLabel("Tags");
        TextField tagsField = new TextField();
        tagsField.setPromptText("ex: irrigation, ble, sol ...");
        styliserChamp(tagsField);

        Button btnSuggerer = new Button("Suggerer des tags");
        btnSuggerer.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8;" +
                "-fx-padding: 8 14 8 14; -fx-cursor: hand;");

        HBox tagsRow = new HBox(10, tagsField, btnSuggerer);
        HBox.setHgrow(tagsField, Priority.ALWAYS);
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSuggeresTitle = new Label("Tags suggeres :");
        lblSuggeresTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        lblSuggeresTitle.setVisible(false);
        lblSuggeresTitle.setManaged(false);

        FlowPane tagsSuggeresPane = new FlowPane(8, 8);
        tagsSuggeresPane.setVisible(false);
        tagsSuggeresPane.setManaged(false);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        // ── ACTION SUGGÉRER (LOCAL) ───────────────────────────────
        btnSuggerer.setOnAction(e -> {
            String titre   = titreField.getText().trim();
            String contenu = contenuArea.getText().trim();

            if (titre.isEmpty() && contenu.isEmpty()) {
                statusLabel.setText("Remplissez d'abord le titre ou le contenu.");
                statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #EF4444;");
                return;
            }

            List<String> suggestions = suggererTagsLocaux(titre + " " + contenu);
            tagsSuggeresPane.getChildren().clear();

            if (suggestions.isEmpty()) {
                statusLabel.setText("Aucun tag trouve. Essayez d'autres mots-cles.");
                statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #EF4444;");
                return;
            }

            lblSuggeresTitle.setVisible(true);
            lblSuggeresTitle.setManaged(true);
            tagsSuggeresPane.setVisible(true);
            tagsSuggeresPane.setManaged(true);

            for (String tag : suggestions) {
                tagsSuggeresPane.getChildren().add(creerBoutonTag(tag, tagsField));
            }

            statusLabel.setText(suggestions.size() + " tags trouves. Cliquez pour les ajouter !");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669;");
        });

        // ── ERREUR + BOUTONS ─────────────────────────────────────
        Label erreurLabel = new Label("");
        erreurLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #EF4444;");
        erreurLabel.setWrapText(true);

        Button btnPublier = new Button("Publier");
        Button btnAnnuler = new Button("Annuler");

        btnPublier.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-padding: 10 28 10 28; -fx-cursor: hand;");
        btnAnnuler.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151;" +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-padding: 10 28 10 28; -fx-cursor: hand;");

        btnAnnuler.setOnAction(e -> stage.close());

        // =========================================================
        //  BOUTON PUBLIER — Pipeline complet :
        //  1. Vérifier similarité via ApiClient
        //  2. Passer par ApiClient.publierThread() → modération + sentiment + sauvegarde
        //  3. Mettre à jour les tags en base (car ModerationAPI ne gère pas les tags)
        // =========================================================
        btnPublier.setOnAction(e -> {
            String titre   = titreField.getText().trim();
            String contenu = contenuArea.getText().trim();
            String tags    = tagsField.getText().trim();

            if (titre.isEmpty() || contenu.isEmpty()) {
                erreurLabel.setText("Le titre et le contenu sont obligatoires.");
                return;
            }

            // Désactiver le bouton pendant le traitement
            btnPublier.setDisable(true);
            btnPublier.setText("Verification...");
            erreurLabel.setText("");

            new Thread(() -> {
                try {
                    // ── ÉTAPE 1 : Vérifier similarité ──────────────
                    ApiClient.ResultatSimilarite sim = ApiClient.verifierSimilarite(titre);

                    if (sim.similaire) {
                        Platform.runLater(() -> {
                            erreurLabel.setText("Thread similaire existant : \"" + sim.titreSimilaire
                                    + "\" (" + sim.score + "% de similarite). Modifiez votre titre.");
                            btnPublier.setDisable(false);
                            btnPublier.setText("Publier");
                        });
                        return;
                    }

                    // ── ÉTAPE 2 : Modération + Sentiment + Sauvegarde via API ──
                    // IMPORTANT : ModerationAPI.ThreadHandler sauvegarde lui-même le thread
                    // en appelant service.ajouterAvecStatut(t) — mais sans les tags.
                    // On doit donc mettre à jour les tags après.
                    Platform.runLater(() -> btnPublier.setText("Moderation..."));

                    ApiClient.ResultatAPI resultat = ApiClient.publierThread(titre, contenu, userId);

                    Platform.runLater(() -> {
                        if (!resultat.succes) {
                            // ── Thread bloqué par la modération ──
                            erreurLabel.setText("Publication refusee : "
                                    + (resultat.messageNotif != null ? resultat.messageNotif : "Contenu inapproprie."));
                            btnPublier.setDisable(false);
                            btnPublier.setText("Publier");
                            return;
                        }

                        // ── ÉTAPE 3 : Mettre à jour les tags si l'utilisateur en a saisi ──
                        // Le thread est déjà en base (inséré par ModerationAPI),
                        // on met à jour les tags via le dernier thread de cet utilisateur.
                        if (!tags.isEmpty()) {
                            try {
                                new ServiceThreads().mettreAJourTagsDernierThread(userId, tags);
                            } catch (SQLException ex) {
                                System.out.println("Warning tags : " + ex.getMessage());
                            }
                        }

                        parent.chargerThreads();

                        if ("signale".equals(resultat.statut)) {
                            parent.showMessage("Thread publie mais signale pour moderation.", "#F59E0B");
                        } else {
                            parent.showMessage("Thread publie avec succes !", "#059669");
                        }
                        stage.close();
                    });

                } catch (java.io.IOException ex) {
                    // ── API inaccessible → fallback direct en base ──
                    Platform.runLater(() -> {
                        try {
                            thread t = new thread();
                            t.setTitre(titre);
                            t.setContenu(contenu);
                            t.setTags(tags.isEmpty() ? "" : tags);
                            t.setId_user(userId);
                            t.setStatut("actif");
                            t.setSentiment("neutre");
                            t.setDate_creation(LocalDateTime.now());
                            t.setDate_update(LocalDateTime.now());

                            new ServiceThreads().ajouterAvecStatut(t);
                            parent.chargerThreads();
                            parent.showMessage("Thread publie (API indisponible).", "#6B7280");
                            stage.close();
                        } catch (SQLException sqlEx) {
                            erreurLabel.setText("Erreur : " + sqlEx.getMessage());
                            btnPublier.setDisable(false);
                            btnPublier.setText("Publier");
                        }
                    });
                }
            }).start();
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox boutons = new HBox(12, spacer, btnAnnuler, btnPublier);
        boutons.setAlignment(Pos.CENTER_LEFT);
        boutons.setPadding(new Insets(12, 24, 20, 24));
        boutons.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        VBox form = new VBox(14,
                lblTitre,    titreField,
                lblContenu,  contenuArea,
                lblTags,     tagsRow,
                statusLabel,
                lblSuggeresTitle,
                tagsSuggeresPane,
                erreurLabel
        );
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scroll);
        root.setBottom(boutons);

        stage.setScene(new Scene(root, 560, 580));
        stage.show();
    }

    // =========================================================
    //  ALGORITHME SUGGESTION LOCALE
    // =========================================================
    private List<String> suggererTagsLocaux(String texte) {
        String texteLower = texte.toLowerCase()
                .replace("é","e").replace("è","e").replace("ê","e")
                .replace("à","a").replace("â","a").replace("ô","o")
                .replace("î","i").replace("û","u").replace("ç","c");

        Map<String, Integer> scoreCategorie       = new LinkedHashMap<>();
        Map<String, String>  tagTrouvePourCategorie = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : TAGS_PAR_CATEGORIE.entrySet()) {
            int score = 0; String meilleurTag = null;
            for (String tag : entry.getValue()) {
                String tn = tag.replace("-"," ").replace("é","e").replace("è","e")
                        .replace("ê","e").replace("â","a").replace("î","i")
                        .replace("ô","o").replace("û","u").replace("ç","c");
                if (texteLower.contains(tn)) {
                    score += tn.length();
                    if (meilleurTag == null) meilleurTag = tag;
                }
            }
            if (score > 0 && meilleurTag != null) {
                scoreCategorie.put(entry.getKey(), score);
                tagTrouvePourCategorie.put(entry.getKey(), meilleurTag);
            }
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scoreCategorie.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        List<String> resultat = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            resultat.add(tagTrouvePourCategorie.get(entry.getKey()));
            if (resultat.size() == 5) break;
        }

        if (resultat.size() < 3) {
            for (String tag : Arrays.asList("agriculture", "conseil", "culture", "recolte", "terrain")) {
                if (!resultat.contains(tag)) { resultat.add(tag); if (resultat.size() == 3) break; }
            }
        }
        return resultat;
    }

    // =========================================================
    //  BOUTON TAG CLIQUABLE
    // =========================================================
    private Button creerBoutonTag(String tag, TextField tagsField) {
        final boolean[] selectionne = {false};
        Button btn = new Button("+ " + tag);
        btn.setStyle(styleTagNon());
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setOnAction(e -> {
            selectionne[0] = !selectionne[0];
            if (selectionne[0]) { btn.setText("✓ " + tag); btn.setStyle(styleTagOui()); ajouterTag(tag, tagsField); }
            else                { btn.setText("+ " + tag); btn.setStyle(styleTagNon()); retirerTag(tag, tagsField); }
        });
        return btn;
    }

    private void ajouterTag(String tag, TextField tagsField) {
        String actuel = tagsField.getText().trim();
        if (actuel.isEmpty()) { tagsField.setText(tag); return; }
        List<String> liste = new ArrayList<>();
        for (String t : actuel.split(",")) { String tr = t.trim(); if (!tr.isEmpty()) liste.add(tr); }
        if (!liste.contains(tag)) { liste.add(tag); tagsField.setText(String.join(", ", liste)); }
    }

    private void retirerTag(String tag, TextField tagsField) {
        List<String> liste = new ArrayList<>();
        for (String t : tagsField.getText().split(",")) {
            String tr = t.trim();
            if (!tr.isEmpty() && !tr.equalsIgnoreCase(tag)) liste.add(tr);
        }
        tagsField.setText(String.join(", ", liste));
    }

    // ── HELPERS STYLE ─────────────────────────────────────────
    private Label mkLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        return l;
    }
    private void styliserChamp(TextField field) {
        field.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 8 12 8 12;");
    }
    private String styleTagNon() {
        return "-fx-background-color: #EDE9FE; -fx-text-fill: #6C63FF;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 5 14 5 14;";
    }
    private String styleTagOui() {
        return "-fx-background-color: #6C63FF; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 5 14 5 14;";
    }
}