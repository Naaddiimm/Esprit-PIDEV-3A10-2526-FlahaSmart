package Controler;

import entities.Notification;
import entities.Reputation;
import entities.thread;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import servise.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThreadController {

    private final ServiceThreads      service             = new ServiceThreads();
    private final ServiceJaime        serviceJaime        = new ServiceJaime();
    private final ServiceCommentaire  serviceCommentaire  = new ServiceCommentaire();
    private final ServiceVote         serviceVote         = new ServiceVote();
    private final ServiceReputation   serviceReputation   = new ServiceReputation();
    private final ServiceNotification serviceNotification = new ServiceNotification();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final int CURRENT_USER_ID = 5;

    // Palette de couleurs pour les avatars — une couleur par utilisateur
    private static final String[] AVATAR_COULEURS = {
            "#EDE9FE", "#FEF3C7", "#ECFDF5", "#FEE2E2", "#E0E7FF",
            "#F3E8FF", "#ECFDF5", "#FFF7ED", "#F0FDF4", "#EFF6FF"
    };
    private static final String[] AVATAR_TEXTES = {
            "#6C63FF", "#D97706", "#059669", "#DC2626", "#4338CA",
            "#9333EA", "#059669", "#EA580C", "#16A34A", "#2563EB"
    };

    private VBox         threadsContainer;
    private Label        totalLabel;
    private Label        messageLabel;
    private TextField    searchField;
    private List<thread> allThreads;
    private Stage        primaryStage;
    private boolean      trierParScore = false;

    private Label reputationPointsLabel;
    private Label reputationBadgeLabel;
    private Label notifBadgeLabel;
    private Timeline notifTimeline;

    public void afficher(Stage stage) {
        this.primaryStage = stage;

        threadsContainer = new VBox(12);
        threadsContainer.setPadding(new Insets(24));

        searchField = new TextField();
        searchField.setPromptText("Rechercher un thread...");
        searchField.setPrefWidth(320);
        searchField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 8 12 8 12;");

        messageLabel = new Label("");
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669;");

        totalLabel = new Label("0");
        totalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6C63FF;");

        reputationPointsLabel = new Label("0 pts");
        reputationBadgeLabel  = new Label("Debutant");
        notifBadgeLabel       = new Label("");

        notifBadgeLabel.setVisible(false);
        notifBadgeLabel.setStyle(
                "-fx-background-color: #EF4444; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 10px;" +
                        "-fx-font-weight: bold; -fx-padding: 1 5 1 5;");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F0F0FA;");
        root.setTop(buildNavBar());
        root.setCenter(buildCenter());

        stage.setTitle("FlahaSmart — Forum");
        stage.setScene(new Scene(root, 1200, 759));
        stage.show();

        chargerThreads();
        chargerReputation();
        chargerNotifBadge();
        demarrerRafraichissementAuto();
    }

    // =========================================================
    //  NAVBAR
    // =========================================================
    private HBox buildNavBar() {
        HBox nav = new HBox();
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPrefHeight(64);
        nav.setPadding(new Insets(0, 24, 0, 24));
        nav.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        Rectangle logo = new Rectangle(32, 32);
        logo.setArcWidth(8); logo.setArcHeight(8);
        logo.setFill(Color.web("#6C63FF"));
        Label appName = new Label("FlahaSmart");
        appName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #111827;");
        Label appSub = new Label("Forum");
        appSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        VBox logoText = new VBox(2, appName, appSub);
        HBox logoBox = new HBox(8, logo, logoText);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(200);

        HBox links = new HBox(32,
                mkNavLabel("", false),
                mkNavLabel("", false),
                mkNavLabel("Forum", true),
                mkNavLabel("", false));
        links.setAlignment(Pos.CENTER);

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

        Button cloche = new Button("Notification");
        cloche.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-padding: 7 12 7 12; -fx-cursor: hand;");
        cloche.setOnAction(e -> ouvrirNotifications());

        StackPane clocheStack = new StackPane(cloche, notifBadgeLabel);
        StackPane.setAlignment(notifBadgeLabel, Pos.TOP_RIGHT);

        // Avatar utilisateur connecté dans la navbar
        StackPane navAvatar = creerAvatar(CURRENT_USER_ID, 16);

        HBox avatars = new HBox(16, clocheStack, navAvatar);
        avatars.setAlignment(Pos.CENTER);

        nav.getChildren().addAll(logoBox, sp1, links, sp2, avatars);
        return nav;
    }

    private Label mkNavLabel(String text, boolean active) {
        Label l = new Label(text);
        l.setStyle(active
                ? "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;"
                : "-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        return l;
    }

    // =========================================================
    //  CENTER
    // =========================================================
    private HBox buildCenter() {
        HBox center = new HBox();
        center.setPrefHeight(695);
        center.getChildren().addAll(buildSidebarLeft(), buildMain(), buildSidebarRight());
        return center;
    }

    // =========================================================
    //  SIDEBAR GAUCHE
    // =========================================================
    private VBox buildSidebarLeft() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setPrefHeight(695);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 0 1 0 0;");

        sidebar.getChildren().addAll(
                mkSidebarItem("Home", false),
                mkSidebarItem("My Threads", true),
                mkSidebarItem("", false)
        );

        Label courseTitle = new Label("");
        courseTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
        VBox.setMargin(courseTitle, new Insets(20, 0, 8, 0));

        Circle dot = new Circle(5); dot.setFill(Color.web("#6C63FF"));
        Label cName = new Label("FlahaSmart");
        cName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label cCode = new Label("Forum");
        cCode.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
        VBox cText = new VBox(2, cName, cCode);
        HBox courseItem = new HBox(8, dot, cText);
        courseItem.setAlignment(Pos.CENTER_LEFT);
        courseItem.setPadding(new Insets(8, 12, 8, 12));
        courseItem.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8;");

        Button btnNew = new Button("+ New Thread");
        btnNew.setMaxWidth(Double.MAX_VALUE);
        btnNew.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 12; -fx-cursor: hand;");
        btnNew.setOnAction(e -> ouvrirAjouter());
        VBox.setMargin(btnNew, new Insets(24, 0, 0, 0));

        Label statsTxt = new Label("Total threads :");
        statsTxt.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        HBox statsRow = new HBox(8, statsTxt, totalLabel);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        Label statsTitle = new Label("STATS");
        statsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
        VBox statsBox = new VBox(8, statsTitle, statsRow);
        statsBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 12;");
        VBox.setMargin(statsBox, new Insets(24, 0, 0, 0));

        Label repTitle = new Label("MA REPUTATION");
        repTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
        VBox.setMargin(repTitle, new Insets(20, 0, 8, 0));

        reputationBadgeLabel.setStyle("-fx-font-size: 18px;");
        reputationPointsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #6C63FF;");

        Label repExplication = new Label("Thread +5 | Upvote +3\nCommentaire +2 | Like +1");
        repExplication.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");

        VBox repBox = new VBox(6, reputationBadgeLabel, reputationPointsLabel, repExplication);
        repBox.setStyle("-fx-background-color: #F5F3FF; -fx-background-radius: 10;" +
                "-fx-border-color: #DDD6FE; -fx-border-radius: 10; -fx-padding: 12;");

        sidebar.getChildren().addAll(courseTitle, courseItem, btnNew, statsBox, repTitle, repBox);
        return sidebar;
    }

    private HBox mkSidebarItem(String text, boolean active) {
        Label l = new Label(text);
        l.setStyle(active
                ? "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #6C63FF;"
                : "-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        HBox box = new HBox(l);
        box.setPadding(new Insets(8, 12, 8, 12));
        if (active) box.setStyle("-fx-background-color: #EDE9FE; -fx-background-radius: 8;");
        return box;
    }

    // =========================================================
    //  ZONE PRINCIPALE
    // =========================================================
    private VBox buildMain() {
        VBox main = new VBox();
        HBox.setHgrow(main, Priority.ALWAYS);
        main.setPrefHeight(695);

        Button btnSearch = new Button("Rechercher");
        btnSearch.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        btnSearch.setOnAction(e -> rechercherThreads());

        Button btnRefresh = new Button("Actualiser");
        btnRefresh.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> rafraichir());

        Button btnTriDate = new Button("Par date");
        btnTriDate.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");

        Button btnTriScore = new Button("Top votes");
        btnTriScore.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");

        btnTriDate.setOnAction(e -> {
            trierParScore = false;
            btnTriDate.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            btnTriScore.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            chargerThreads();
        });

        btnTriScore.setOnAction(e -> {
            trierParScore = true;
            btnTriScore.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            btnTriDate.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            chargerThreads();
        });

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox searchBar = new HBox(10, searchField, btnSearch, btnRefresh, btnTriDate, btnTriScore, sp, messageLabel);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(16, 24, 16, 24));
        searchBar.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        ScrollPane scroll = new ScrollPane(threadsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        main.getChildren().addAll(searchBar, scroll);
        return main;
    }

    // =========================================================
    //  SIDEBAR DROITE
    // =========================================================
    private VBox buildSidebarRight() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setPrefHeight(695);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 0 1;");

        Label title = new Label("FlahaSmart");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label group = new Label("Plateforme agricole");
        group.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        Rectangle img = new Rectangle(200, 100);
        img.setArcWidth(12); img.setArcHeight(12);
        img.setFill(Color.web("#D1D5DB"));
        VBox.setMargin(img, new Insets(16, 0, 0, 0));
        Label badge = new Label("Community Forum");
        badge.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669;" +
                "-fx-background-radius: 20; -fx-font-size: 11px; -fx-padding: 4 12 4 12;");
        VBox.setMargin(badge, new Insets(12, 0, 0, 0));

        Label legendTitle = new Label("BADGES");
        legendTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
        VBox.setMargin(legendTitle, new Insets(20, 0, 8, 0));

        VBox legendBox = new VBox(6,
                mkBadgeLegende("Debutant",  "0 - 10 pts",   "#D1FAE5", "#065F46"),
                mkBadgeLegende("Actif",     "11 - 50 pts",  "#D1FAE5", "#065F46"),
                mkBadgeLegende("Expert",    "51 - 100 pts", "#FEF3C7", "#92400E"),
                mkBadgeLegende("Maitre",    "100+ pts",     "#EDE9FE", "#6C63FF")
        );

        sidebar.getChildren().addAll(title, group, img, badge, legendTitle, legendBox);
        return sidebar;
    }

    private HBox mkBadgeLegende(String badge, String pts, String bg, String color) {
        Label lBadge = new Label(badge);
        lBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lPts = new Label(pts);
        lPts.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox row = new HBox(8, lBadge, sp, lPts);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; -fx-padding: 6 10 6 10;");
        return row;
    }

    // =========================================================
    //  AVATAR avec initiales — méthode réutilisable
    // =========================================================
    private StackPane creerAvatar(int idUser, int rayon) {
        int idx = idUser % AVATAR_COULEURS.length;
        String bgColor  = AVATAR_COULEURS[idx];
        String txtColor = AVATAR_TEXTES[idx];

        String initiales = "U" + idUser;
        int fontSize = rayon > 14 ? 11 : 9;

        Label lblInitiales = new Label(initiales);
        lblInitiales.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold; -fx-text-fill: " + txtColor + ";");
        lblInitiales.setAlignment(Pos.CENTER);

        int taille = rayon * 2;
        StackPane avatar = new StackPane(lblInitiales);
        avatar.setPrefSize(taille, taille);
        avatar.setMinSize(taille, taille);
        avatar.setMaxSize(taille, taille);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: " + rayon + ";" +
                        "-fx-border-color: " + txtColor + ";" +
                        "-fx-border-radius: " + rayon + ";" +
                        "-fx-border-width: 2;"
        );
        return avatar;
    }

    // =========================================================
    //  CHARGEMENT
    // =========================================================
    public void chargerThreads() {
        try {
            allThreads = trierParScore ? service.recupererParScore() : service.recuperer();
            afficherThreads(allThreads);
            totalLabel.setText(String.valueOf(allThreads.size()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    public void chargerReputation() {
        try {
            Reputation rep = serviceReputation.getReputation(CURRENT_USER_ID);
            reputationPointsLabel.setText(rep.getPoints() + " pts");
            reputationBadgeLabel.setText(rep.getBadge());
        } catch (SQLException ignored) {}
    }

    private void chargerNotifBadge() {
        try {
            int count = serviceNotification.compterNonLues(CURRENT_USER_ID);
            if (count > 0) {
                notifBadgeLabel.setText(String.valueOf(count));
                notifBadgeLabel.setVisible(true);
            } else {
                notifBadgeLabel.setVisible(false);
            }
        } catch (SQLException ignored) {}
    }

    private void demarrerRafraichissementAuto() {
        notifTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            chargerNotifBadge();
            chargerReputation();
        }));
        notifTimeline.setCycleCount(Timeline.INDEFINITE);
        notifTimeline.play();
    }

    // =========================================================
    //  AFFICHAGE THREADS
    // =========================================================
    private void afficherThreads(List<thread> threads) {
        threadsContainer.getChildren().clear();
        if (threads == null || threads.isEmpty()) {
            VBox empty = new VBox(16);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(80));
            Label msg1 = new Label("Aucun thread pour l'instant");
            msg1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            Label msg2 = new Label("Cliquez sur '+ New Thread' pour creer votre premier thread");
            msg2.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF;");
            Button btn = new Button("+ Creer un thread");
            btn.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 13px;" +
                    "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 12 24 12 24; -fx-cursor: hand;");
            btn.setOnAction(e -> ouvrirAjouter());
            empty.getChildren().addAll(msg1, msg2, btn);
            threadsContainer.getChildren().add(empty);
            return;
        }
        for (thread t : threads) threadsContainer.getChildren().add(creerCarteThread(t));
    }

    // =========================================================
    //  CARTE THREAD
    // =========================================================
    private VBox creerCarteThread(thread t) {
        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3); -fx-padding: 18 20 18 20;");

        // VOTES
        int score = 0;
        String voteActuel = null;
        try {
            score      = serviceVote.calculerScore(t.getId_thread());
            voteActuel = serviceVote.getVoteActuel(t.getId_thread(), CURRENT_USER_ID);
        } catch (SQLException ignored) {}

        Button btnUp    = new Button("▲");
        Button btnDown  = new Button("▼");
        Label  scoreLbl = new Label(String.valueOf(score));
        appliquerStyleVote(btnUp, btnDown, scoreLbl, voteActuel, score);

        final String[] voteRef = {voteActuel};
        btnUp.setOnAction(e -> {
            try {
                String nouveau = serviceVote.voter(t.getId_thread(), CURRENT_USER_ID, "up");
                voteRef[0] = nouveau;
                int sc = serviceVote.calculerScore(t.getId_thread());
                scoreLbl.setText(String.valueOf(sc));
                appliquerStyleVote(btnUp, btnDown, scoreLbl, nouveau, sc);
                chargerReputation(); chargerNotifBadge();
            } catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
        });

        btnDown.setOnAction(e -> {
            try {
                String nouveau = serviceVote.voter(t.getId_thread(), CURRENT_USER_ID, "down");
                voteRef[0] = nouveau;
                int sc = serviceVote.calculerScore(t.getId_thread());
                scoreLbl.setText(String.valueOf(sc));
                appliquerStyleVote(btnUp, btnDown, scoreLbl, nouveau, sc);
                chargerReputation(); chargerNotifBadge();
            } catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
        });

        VBox voteBox = new VBox(4, btnUp, scoreLbl, btnDown);
        voteBox.setAlignment(Pos.CENTER);
        voteBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 8 12 8 12;");
        voteBox.setPrefWidth(56);

        // ── AVATAR avec initiales ──────────────────────────────
        StackPane avatar = creerAvatar(t.getId_user(), 20);

        Label titreLabel = new Label(t.getTitre());
        titreLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        titreLabel.setWrapText(true);

        Label dateLabel = new Label("Cree le " + t.getDate_creation().format(DATE_FMT));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        Label sentimentBadge = creerBadgeSentiment(t.getSentiment());
        VBox titreBox = new VBox(3, titreLabel, dateLabel);
        HBox.setHgrow(titreBox, Priority.ALWAYS);

        Label badgeUser = new Label("U" + t.getId_user());
        badgeUser.setStyle("-fx-background-color: #E0E7FF; -fx-text-fill: #4338CA;" +
                "-fx-background-radius: 20; -fx-font-size: 10px; -fx-padding: 3 10 3 10;");

        HBox header = new HBox(12, avatar, titreBox, sentimentBadge, badgeUser);
        header.setAlignment(Pos.CENTER_LEFT);

        Label contenuLabel = new Label(t.getContenu());
        contenuLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");
        contenuLabel.setWrapText(true);
        contenuLabel.setMaxWidth(Double.MAX_VALUE);

        HBox tagsBox = creerTagsBox(t.getTags());

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #F3F4F6; -fx-min-height: 1; -fx-max-height: 1;");

        // J'AIME
        boolean dejaAime = false;
        int nbLikes = 0;
        try {
            dejaAime = serviceJaime.aDejaAime(t.getId_thread(), CURRENT_USER_ID);
            nbLikes  = serviceJaime.compter(t.getId_thread());
        } catch (SQLException ignored) {}

        Button likeBtn   = new Button("J'aime");
        Label  likeCount = new Label(nbLikes + " j'aime");
        likeBtn.setStyle(styleLike(dejaAime));
        likeCount.setStyle(styleCompte(dejaAime));

        likeBtn.setOnAction(e -> {
            try {
                boolean nowLiked = serviceJaime.toggle(t.getId_thread(), CURRENT_USER_ID);
                int     newCount = serviceJaime.compter(t.getId_thread());
                likeCount.setText(newCount + " j'aime");
                likeBtn.setStyle(styleLike(nowLiked));
                likeCount.setStyle(styleCompte(nowLiked));
                chargerReputation(); chargerNotifBadge();
            } catch (SQLException ex) { showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
        });

        // COMMENTAIRES
        int nbCommentaires = 0;
        try { nbCommentaires = serviceCommentaire.compter(t.getId_thread()); } catch (SQLException ignored) {}

        Button commentBtn = new Button(nbCommentaires + " commentaire(s)");
        commentBtn.setStyle("-fx-background-color: #EDE9FE; -fx-text-fill: #6C63FF; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
        commentBtn.setOnAction(e -> {
            new CommentaireController().afficher(primaryStage, t.getId_thread(), CURRENT_USER_ID);
            try {
                commentBtn.setText(serviceCommentaire.compter(t.getId_thread()) + " commentaire(s)");
                chargerReputation(); chargerNotifBadge();
            } catch (SQLException ignored) {}
        });

        Label idLabel = new Label("Thread #" + t.getId_thread());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #D1D5DB;");

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> ouvrirModifier(t));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerThread(t));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, idLabel, likeBtn, likeCount, commentBtn, spacer, btnModifier, btnSupprimer);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, header, contenuLabel, tagsBox, sep, actions);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox carteInterne = new HBox(16, voteBox, content);
        carteInterne.setAlignment(Pos.TOP_LEFT);
        carte.getChildren().add(carteInterne);
        return carte;
    }

    // =========================================================
    //  NOTIFICATIONS
    // =========================================================
    private void ouvrirNotifications() {
        Stage notifStage = new Stage();
        notifStage.setTitle("Notifications");
        notifStage.initModality(Modality.APPLICATION_MODAL);
        notifStage.initOwner(primaryStage);

        Label titre = new Label("Notifications");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox header = new HBox(titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(56);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setStyle("-fx-background-color: #6C63FF;");

        VBox listeBox = new VBox(8);
        listeBox.setPadding(new Insets(16));

        try {
            List<Notification> notifs = serviceNotification.getNotifications(CURRENT_USER_ID);
            if (notifs.isEmpty()) {
                Label vide = new Label("Aucune notification pour l'instant.");
                vide.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF;");
                VBox emptyBox = new VBox(vide);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(48));
                listeBox.getChildren().add(emptyBox);
            } else {
                for (Notification n : notifs) {
                    VBox carte = new VBox(4);
                    String bg     = n.isLu() ? "white" : "#F5F3FF";
                    String border = n.isLu() ? "#E5E7EB" : "#DDD6FE";
                    carte.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;" +
                            "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-padding: 12 16 12 16;");
                    Label msg = new Label(n.getMessage());
                    msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
                    msg.setWrapText(true); msg.setMaxWidth(420);
                    Label date = new Label(n.getDate_notif().format(TIME_FMT));
                    date.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
                    carte.getChildren().addAll(msg, date);
                    if (!n.isLu()) {
                        Label newBadge = new Label("Nouveau");
                        newBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #6C63FF; -fx-font-weight: bold;");
                        carte.getChildren().add(newBadge);
                    }
                    listeBox.getChildren().add(carte);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        ScrollPane scroll = new ScrollPane(listeBox);
        scroll.setFitToWidth(true); scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color: #F9FAFB; -fx-background: #F9FAFB; -fx-border-color: transparent;");

        Button btnToutLire = new Button("Tout marquer comme lu");
        btnToutLire.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        btnToutLire.setOnAction(e -> {
            try { serviceNotification.marquerToutesLues(CURRENT_USER_ID); chargerNotifBadge(); }
            catch (SQLException ex) { ex.printStackTrace(); }
            notifStage.close();
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        btnFermer.setOnAction(e -> notifStage.close());

        HBox boutons = new HBox(12, btnFermer, btnToutLire);
        boutons.setAlignment(Pos.CENTER_RIGHT);
        boutons.setPadding(new Insets(12, 16, 16, 16));
        boutons.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setTop(header); root.setCenter(scroll); root.setBottom(boutons);

        try { serviceNotification.marquerToutesLues(CURRENT_USER_ID); }
        catch (SQLException ignored) {}

        notifStage.setScene(new Scene(root, 480, 520));
        notifStage.showAndWait();
        chargerNotifBadge();
    }

    // =========================================================
    //  HELPERS STYLE
    // =========================================================
    private void appliquerStyleVote(Button btnUp, Button btnDown, Label scoreLabel, String voteActuel, int score) {
        String scoreColor = score > 0 ? "#059669" : score < 0 ? "#EF4444" : "#6B7280";
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");
        btnUp.setStyle("up".equals(voteActuel)
                ? "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;"
                : "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        btnDown.setStyle("down".equals(voteActuel)
                ? "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;"
                : "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
    }

    private Label creerBadgeSentiment(String sentiment) {
        String txt, bg, color;
        if (sentiment == null) sentiment = "neutre";
        switch (sentiment.toLowerCase()) {
            case "positif": txt = "Positif"; bg = "#ECFDF5"; color = "#059669"; break;
            case "negatif": txt = "Negatif"; bg = "#FEF2F2"; color = "#EF4444"; break;
            default:        txt = "Neutre";  bg = "#F3F4F6"; color = "#6B7280"; break;
        }
        Label badge = new Label(txt);
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + color + ";" +
                "-fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10 3 10;");
        return badge;
    }

    private HBox creerTagsBox(String tags) {
        HBox tagsBox = new HBox(6);
        tagsBox.setAlignment(Pos.CENTER_LEFT);
        if (tags == null || tags.trim().isEmpty()) {
            tagsBox.setVisible(false); tagsBox.setManaged(false); return tagsBox;
        }
        for (String tag : tags.split(",")) {
            String tagTrim = tag.trim();
            if (!tagTrim.isEmpty()) {
                Label tagLabel = new Label("# " + tagTrim);
                tagLabel.setStyle("-fx-background-color: #EDE9FE; -fx-text-fill: #6C63FF;" +
                        "-fx-background-radius: 20; -fx-font-size: 11px; -fx-padding: 3 10 3 10;");
                tagsBox.getChildren().add(tagLabel);
            }
        }
        return tagsBox;
    }

    private String styleLike(boolean liked) {
        return liked
                ? "-fx-background-color: #FFE4E6; -fx-text-fill: #E11D48; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-cursor: hand;"
                : "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-cursor: hand;";
    }

    private String styleCompte(boolean liked) {
        return liked ? "-fx-font-size: 12px; -fx-text-fill: #E11D48; -fx-font-weight: bold;"
                : "-fx-font-size: 12px; -fx-text-fill: #9CA3AF;";
    }

    // =========================================================
    //  ACTIONS
    // =========================================================
    private void rechercherThreads() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { afficherThreads(allThreads); return; }
        List<thread> filtres = allThreads.stream()
                .filter(t -> t.getTitre().toLowerCase().contains(query)
                        || t.getContenu().toLowerCase().contains(query)
                        || (t.getTags() != null && t.getTags().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        afficherThreads(filtres);
        showMessage(filtres.size() + " resultat(s) trouve(s)", "#6C63FF");
    }

    private void rafraichir() {
        searchField.clear();
        messageLabel.setText("");
        chargerThreads();
        chargerReputation();
        chargerNotifBadge();
    }

    private void supprimerThread(thread t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le thread ?");
        confirm.setContentText("« " + t.getTitre() + " » sera definitivement supprime.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(t);
                showMessage("Thread supprime avec succes", "#059669");
                chargerThreads();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    public void ouvrirAjouter() {
        new AjouterThreadController().afficher(primaryStage, this, CURRENT_USER_ID);
    }

    private void ouvrirModifier(thread t) {
        new ModifierThreadController().afficher(primaryStage, this, t);
    }

    public void showMessage(String msg, String color) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}