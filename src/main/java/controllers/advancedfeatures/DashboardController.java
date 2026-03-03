package controllers.advancedfeatures;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import entities.Equipement;
import services.EquipementService;
import services.OperationService;
import entities.Operation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML
    private VBox statsContainer;

    @FXML
    private HBox cardsContainer;

    @FXML
    private VBox operationsByTypeContainer;

    @FXML
    private VBox equipementUsageContainer;

    @FXML
    private VBox recentOperationsContainer;

    @FXML
    private VBox equipementStateContainer;

    @FXML
    private VBox urgentOperationsContainer;

    EquipementService eqService = new EquipementService();
    OperationService opService = new OperationService();

    // 🎨 palette couleurs
    private final String[] colors = {
            "#10b981", "#3b82f6", "#f59e0b", "#ef4444", "#8b5cf6",
            "#ec4899", "#06b6d4", "#84cc16"
    };

    @FXML
    public void initialize() {
        chargerCardsOperations();
        chargerOperationsByType();
        chargerEquipementUsage();
        chargerOperationsUrgentes();
        chargerStatsTypes();
    }

    private void chargerStatsTypes() {

        try {
            statsContainer.getChildren().clear();

            var list = eqService.afficher();

            if (list.isEmpty()) return;

            Map<String, Long> map = list.stream()
                    .collect(Collectors.groupingBy(
                            Equipement::getNom,
                            Collectors.counting()
                    ));

            int total = list.size();

            int index = 0;

            for (String type : map.keySet()) {

                double percent = (map.get(type) * 100.0) / total;

                HBox row = createStatRow(type, percent, index);
                statsContainer.getChildren().add(row);

                index++;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des stats types", e);
        }
    }

    private HBox createStatRow(String type, double percent, int index) {

        Label name = new Label(type);
        name.setPrefWidth(120);

        Label value = new Label(String.format("%.0f%%", percent));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ProgressBar bar = new ProgressBar(percent / 100);
        bar.setPrefWidth(200);

        // couleur automatique
        String color = colors[index % colors.length];
        bar.setStyle("-fx-accent: " + color + ";");
        value.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

        VBox content = new VBox(5, new HBox(name, spacer, value), bar);

        return new HBox(content);
    }

    // ===============================
    // CARDS operations
    // ===============================
    private void chargerCardsOperations() {
        try {
            cardsContainer.getChildren().clear();

            List<Operation> ops = opService.afficher();
            int total = ops.size();
            long enCours = ops.stream().filter(o -> o.getStatut() != null && o.getStatut().equalsIgnoreCase("en cours")).count();
            long termines = ops.stream().filter(o -> o.getStatut() != null && (o.getStatut().equalsIgnoreCase("terminer") || o.getStatut().equalsIgnoreCase("terminé"))).count();

            double pctEnCours = total == 0 ? 0 : (enCours * 100.0) / total;
            double pctTermines = total == 0 ? 0 : (termines * 100.0) / total;

            VBox cardTotal = createCard("Les Opérations", String.valueOf(total), "#3b82f6", "");
            VBox cardEnCours = createCard("En cours", String.valueOf(enCours), "red", "");
            VBox cardTermine = createCard("Terminé", String.valueOf(termines), "#22c55e", "");

            cardsContainer.getChildren().addAll(cardTotal, cardEnCours, cardTermine);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des cards operations", e);
        }
    }

    private VBox createCard(String title, String mainValue, String color, String subtitle) {
        Label titleLabel = new Label(title);
        // rendu : titre visible en haut de la card
        titleLabel.getStyleClass().clear();
        titleLabel.getStyleClass().add("card-title");
        // appliquer la couleur au titre pour rendre chaque card distincte
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-font-weight: 600;");

        Label valueLabel = new Label(mainValue);
        valueLabel.getStyleClass().add("card-value");

        Label subLabel = new Label(subtitle);
        subLabel.getStyleClass().add("card-subtitle");

        // suppression de la barre de pourcentage : on n'ajoute plus le ProgressBar
        VBox box = new VBox(8, titleLabel, valueLabel, subLabel);
        box.getStyleClass().add("stat-card");
        box.setPrefWidth(200);

        return box;
    }

    // ===============================
    // STATISTIQUE 1: Opérations par type
    // ===============================
    private void chargerOperationsByType() {
        try {
            operationsByTypeContainer.getChildren().clear();

            List<Operation> ops = opService.afficher();
            if (ops.isEmpty()) {
                operationsByTypeContainer.getChildren().add(createEmptyMessage("Aucune opération"));
                return;
            }

            Map<String, Long> typeCount = ops.stream()
                    .filter(o -> o.getType_operation() != null)
                    .collect(Collectors.groupingBy(
                            Operation::getType_operation,
                            Collectors.counting()
                    ));

            int total = ops.size();
            int index = 0;

            for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
                double percent = (entry.getValue() * 100.0) / total;
                HBox row = createAdvancedStatRow(
                        entry.getKey(),
                        entry.getValue().intValue(),
                        percent,
                        colors[index % colors.length]
                );
                operationsByTypeContainer.getChildren().add(row);
                index++;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur chargement opérations par type", e);
        }
    }

    // ===============================
    // STATISTIQUE 2: Utilisation des équipements
    // ===============================
    private void chargerEquipementUsage() {
        try {
            equipementUsageContainer.getChildren().clear();

            List<Equipement> equipements = eqService.afficher();
            List<Operation> operations = opService.afficher();

            if (equipements.isEmpty()) {
                equipementUsageContainer.getChildren().add(createEmptyMessage("Aucun équipement"));
                return;
            }

            // Équipements utilisés dans des opérations en cours
            Set<Integer> equipementsEnCours = operations.stream()
                    .filter(o -> o.getStatut() != null && o.getStatut().equalsIgnoreCase("en cours"))
                    .map(Operation::getId_equipement)
                    .collect(Collectors.toSet());

            int totalEq = equipements.size();
            int enUtilisation = equipementsEnCours.size();
            int disponibles = totalEq - enUtilisation;

            double pctUtilisation = totalEq == 0 ? 0 : (enUtilisation * 100.0) / totalEq;

            // Card pour taux d'utilisation
            VBox usageCard = createUsageCard("Taux d'utilisation", pctUtilisation, "#10b981");
            equipementUsageContainer.getChildren().add(usageCard);

            // Mini stats
            HBox miniStats = new HBox(15);
            miniStats.setAlignment(Pos.CENTER);
            miniStats.getChildren().addAll(
                    createMiniStat("🟢 Disponibles", String.valueOf(disponibles), "#22c55e"),
                    createMiniStat("🔴 En utilisation", String.valueOf(enUtilisation), "#ef4444")
            );
            equipementUsageContainer.getChildren().add(miniStats);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur chargement utilisation équipements", e);
        }
    }

    // ===============================
    // STATISTIQUE 3: Opérations récentes
    // ===============================

    // ===============================
    // STATISTIQUE 4: Opérations urgentes (3 plus proches)
    // ===============================
    private void chargerOperationsUrgentes() {
        try {
            if (urgentOperationsContainer == null) return;

            urgentOperationsContainer.getChildren().clear();

            List<Operation> ops = opService.afficher();
            LocalDate today = LocalDate.now();

            // Filtrer les opérations en cours avec date de fin future et trier par proximité
            List<Operation> operationsProches = ops.stream()
                    .filter(o -> o.getStatut() != null && o.getStatut().equalsIgnoreCase("en cours"))
                    .filter(o -> o.getDate_fin() != null)
                    .filter(o -> {
                        LocalDate dateFin = o.getDate_fin().toLocalDate();
                        return !dateFin.isBefore(today);
                    })
                    .sorted(Comparator.comparing(o -> o.getDate_fin().toLocalDate()))
                    .limit(3)
                    .collect(Collectors.toList());

            if (operationsProches.isEmpty()) {
                urgentOperationsContainer.getChildren().add(createEmptyMessage("Aucune opération urgente"));
                return;
            }

            // Titre de la section
            Label titleSection = new Label("⚡ Opérations les plus proches");
            titleSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-padding: 0 0 10 0;");
            urgentOperationsContainer.getChildren().add(titleSection);

            for (Operation op : operationsProches) {
                VBox card = createUrgentOperationCard(op, today);
                urgentOperationsContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur chargement opérations urgentes", e);
        }
    }

    private VBox createUrgentOperationCard(Operation op, LocalDate today) {
        LocalDate dateFin = op.getDate_fin().toLocalDate();
        long joursRestants = ChronoUnit.DAYS.between(today, dateFin);

        // Déterminer le style selon l'urgence
        String bgColor;
        String borderColor;
        String urgenceIcon;
        String urgenceText;

        if (joursRestants <= 3) {
            // URGENT - Rouge
            bgColor = "#fef2f2";
            borderColor = "#ef4444";
            urgenceIcon = "🚨";
            urgenceText = "URGENT";
        } else if (joursRestants <= 7) {
            // Attention - Orange
            bgColor = "#fffbeb";
            borderColor = "#f59e0b";
            urgenceIcon = "⚠️";
            urgenceText = "Attention";
        } else {
            // Normal - Vert
            bgColor = "#f0fdf4";
            borderColor = "#22c55e";
            urgenceIcon = "✅";
            urgenceText = "OK";
        }

        // Header avec icône et type d'opération
        Label iconLabel = new Label(urgenceIcon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label typeLabel = new Label(op.getType_operation() != null ? op.getType_operation() : "N/A");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge urgence
        Label urgenceBadge = new Label(urgenceText);
        urgenceBadge.setStyle("-fx-background-color: " + borderColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox header = new HBox(10, iconLabel, typeLabel, spacer, urgenceBadge);
        header.setAlignment(Pos.CENTER_LEFT);

        // Équipement
        Label eqLabel = new Label("🔧 " + (op.getNomEquipement() != null ? op.getNomEquipement() : "Équipement #" + op.getId_equipement()));
        eqLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Dates
        String dateDebutStr = op.getDate_debut() != null ? op.getDate_debut().toString() : "N/A";
        String dateFinStr = op.getDate_fin() != null ? op.getDate_fin().toString() : "N/A";

        Label datesLabel = new Label("📅 " + dateDebutStr + " → " + dateFinStr);
        datesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Jours restants avec style dynamique
        Label joursLabel = new Label("⏰ " + joursRestants + " jour" + (joursRestants > 1 ? "s" : "") + " restant" + (joursRestants > 1 ? "s" : ""));
        joursLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");

        // Animation pour les opérations urgentes (effet clignotant simulé par style)
        if (joursRestants <= 3) {
            joursLabel.setStyle(joursLabel.getStyle() + " -fx-effect: dropshadow(gaussian, #ef4444, 5, 0.5, 0, 0);");
        }

        VBox card = new VBox(8, header, eqLabel, datesLabel, joursLabel);
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 12; -fx-border-width: 2; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        return card;
    }



    // ===============================
    // HELPERS - Méthodes utilitaires
    // ===============================

    private HBox createAdvancedStatRow(String label, int count, double percent, String color) {
        // Icône/Badge
        Label badge = new Label(String.valueOf(count));
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 5 12; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Label du type
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pourcentage
        Label percentLabel = new Label(String.format("%.1f%%", percent));
        percentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        // Progress bar
        ProgressBar bar = new ProgressBar(percent / 100);
        bar.setPrefWidth(80);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + color + ";");

        HBox row = new HBox(12, badge, nameLabel, spacer, percentLabel, bar);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-padding: 10;");

        return row;
    }

    private VBox createUsageCard(String title, double percent, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        Label percentLabel = new Label(String.format("%.0f%%", percent));
        percentLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        ProgressBar bar = new ProgressBar(percent / 100);
        bar.setPrefWidth(200);
        bar.setPrefHeight(10);
        bar.setStyle("-fx-accent: " + color + ";");

        VBox card = new VBox(8, titleLabel, percentLabel, bar);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 12; -fx-padding: 20;");

        return card;
    }

    private VBox createMiniStat(String label, String value, String color) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        VBox box = new VBox(3, valueLabel, labelText);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 10;");

        return box;
    }

    private HBox createRecentOperationRow(Operation op) {
        // Statut badge
        String statusColor = op.getStatut() != null && op.getStatut().equalsIgnoreCase("en cours")
                ? "#f59e0b" : "#22c55e";
        String statusIcon = op.getStatut() != null && op.getStatut().equalsIgnoreCase("en cours")
                ? "⏳" : "✅";

        Label statusLabel = new Label(statusIcon);
        statusLabel.setStyle("-fx-font-size: 16px;");

        // Type d'opération
        Label typeLabel = new Label(op.getType_operation() != null ? op.getType_operation() : "N/A");
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        // Équipement
        Label eqLabel = new Label(op.getNomEquipement() != null ? op.getNomEquipement() : "Équipement #" + op.getId_equipement());
        eqLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        VBox infoBox = new VBox(2, typeLabel, eqLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date
        String dateStr = op.getDate_debut() != null ? op.getDate_debut().toString() : "N/A";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        // Statut texte
        Label statusText = new Label(op.getStatut() != null ? op.getStatut() : "N/A");
        statusText.setStyle("-fx-font-size: 11px; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");

        VBox rightBox = new VBox(2, dateLabel, statusText);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(12, statusLabel, infoBox, spacer, rightBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12; " +
                "-fx-border-color: #f3f4f6; -fx-border-radius: 10; -fx-border-width: 1;");

        return row;
    }

    private Label createEmptyMessage(String message) {
        Label label = new Label("📭 " + message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 20;");
        return label;
    }

}
