package services.advancedfeatures;

import entities.advancedfeatures.Parcelle;
import entities.advancedfeatures.RecommandationCulture;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de Rotation des Cultures Optimisée
 * Analyse l'historique, le sol et propose des rotations intelligentes
 */
public class RotationCultureService {

    // Familles de cultures
    private static final Map<String, String> FAMILLES_CULTURES = new HashMap<>();

    // Cultures incompatibles (ne pas planter après)
    private static final Map<String, List<String>> INCOMPATIBILITES = new HashMap<>();

    // Cultures bénéfiques (excellentes après)
    private static final Map<String, List<String>> BENEFIQUES = new HashMap<>();

    // Besoins en nutriments par culture (N, P, K)
    private static final Map<String, int[]> BESOINS_NUTRIMENTS = new HashMap<>();

    // Apports en nutriments (cultures fixatrices d'azote, etc.)
    private static final Map<String, int[]> APPORTS_NUTRIMENTS = new HashMap<>();

    // Préférences de sol par culture
    private static final Map<String, List<String>> PREFERENCES_SOL = new HashMap<>();

    // Préférences de pH par culture [min, max]
    private static final Map<String, double[]> PREFERENCES_PH = new HashMap<>();

    // Périodes optimales de plantation
    private static final Map<String, String> PERIODES_PLANTATION = new HashMap<>();

    static {
        initFamillesCultures();
        initIncompatibilites();
        initBenefiques();
        initBesoinsNutriments();
        initApportsNutriments();
        initPreferencesSol();
        initPreferencesPh();
        initPeriodesPlanation();
    }

    private static void initFamillesCultures() {
        // Légumineuses (fixatrices d'azote)
        FAMILLES_CULTURES.put("haricot", "Légumineuses");
        FAMILLES_CULTURES.put("pois", "Légumineuses");
        FAMILLES_CULTURES.put("lentille", "Légumineuses");
        FAMILLES_CULTURES.put("fève", "Légumineuses");
        FAMILLES_CULTURES.put("soja", "Légumineuses");
        FAMILLES_CULTURES.put("trèfle", "Légumineuses");
        FAMILLES_CULTURES.put("luzerne", "Légumineuses");

        // Céréales
        FAMILLES_CULTURES.put("blé", "Céréales");
        FAMILLES_CULTURES.put("orge", "Céréales");
        FAMILLES_CULTURES.put("maïs", "Céréales");
        FAMILLES_CULTURES.put("avoine", "Céréales");
        FAMILLES_CULTURES.put("seigle", "Céréales");

        // Solanacées
        FAMILLES_CULTURES.put("tomate", "Solanacées");
        FAMILLES_CULTURES.put("pomme de terre", "Solanacées");
        FAMILLES_CULTURES.put("poivron", "Solanacées");
        FAMILLES_CULTURES.put("aubergine", "Solanacées");
        FAMILLES_CULTURES.put("piment", "Solanacées");

        // Crucifères
        FAMILLES_CULTURES.put("chou", "Crucifères");
        FAMILLES_CULTURES.put("brocoli", "Crucifères");
        FAMILLES_CULTURES.put("navet", "Crucifères");
        FAMILLES_CULTURES.put("radis", "Crucifères");
        FAMILLES_CULTURES.put("colza", "Crucifères");

        // Cucurbitacées
        FAMILLES_CULTURES.put("courgette", "Cucurbitacées");
        FAMILLES_CULTURES.put("concombre", "Cucurbitacées");
        FAMILLES_CULTURES.put("melon", "Cucurbitacées");
        FAMILLES_CULTURES.put("pastèque", "Cucurbitacées");
        FAMILLES_CULTURES.put("courge", "Cucurbitacées");

        // Alliacées
        FAMILLES_CULTURES.put("oignon", "Alliacées");
        FAMILLES_CULTURES.put("ail", "Alliacées");
        FAMILLES_CULTURES.put("poireau", "Alliacées");
        FAMILLES_CULTURES.put("échalote", "Alliacées");

        // Ombellifères
        FAMILLES_CULTURES.put("carotte", "Ombellifères");
        FAMILLES_CULTURES.put("persil", "Ombellifères");
        FAMILLES_CULTURES.put("céleri", "Ombellifères");
        FAMILLES_CULTURES.put("fenouil", "Ombellifères");

        // Oléagineuses
        FAMILLES_CULTURES.put("tournesol", "Oléagineuses");
        FAMILLES_CULTURES.put("colza", "Oléagineuses");
        FAMILLES_CULTURES.put("lin", "Oléagineuses");
    }

    private static void initIncompatibilites() {
        // Ne jamais replanter la même famille
        INCOMPATIBILITES.put("tomate", Arrays.asList("tomate", "pomme de terre", "poivron", "aubergine"));
        INCOMPATIBILITES.put("pomme de terre", Arrays.asList("pomme de terre", "tomate", "poivron", "aubergine"));
        INCOMPATIBILITES.put("blé", Arrays.asList("blé", "orge")); // Éviter céréales sur céréales
        INCOMPATIBILITES.put("maïs", Arrays.asList("maïs"));
        INCOMPATIBILITES.put("chou", Arrays.asList("chou", "brocoli", "navet", "radis"));
        INCOMPATIBILITES.put("oignon", Arrays.asList("oignon", "ail", "poireau"));
        INCOMPATIBILITES.put("carotte", Arrays.asList("carotte", "persil", "céleri"));
        INCOMPATIBILITES.put("courgette", Arrays.asList("courgette", "concombre", "melon", "courge"));
    }

    private static void initBenefiques() {
        // Légumineuses avant tout (apport azote)
        BENEFIQUES.put("blé", Arrays.asList("haricot", "pois", "lentille", "trèfle", "luzerne"));
        BENEFIQUES.put("maïs", Arrays.asList("haricot", "pois", "soja", "trèfle"));
        BENEFIQUES.put("tomate", Arrays.asList("haricot", "pois", "oignon", "ail"));
        BENEFIQUES.put("pomme de terre", Arrays.asList("haricot", "pois", "fève"));
        BENEFIQUES.put("chou", Arrays.asList("haricot", "pois", "oignon"));

        // Après céréales (structure du sol)
        BENEFIQUES.put("haricot", Arrays.asList("blé", "orge", "maïs"));
        BENEFIQUES.put("pois", Arrays.asList("blé", "orge", "maïs"));

        // Après cultures nettoyantes
        BENEFIQUES.put("carotte", Arrays.asList("pomme de terre", "oignon", "poireau"));
        BENEFIQUES.put("oignon", Arrays.asList("chou", "pomme de terre", "carotte"));
    }

    private static void initBesoinsNutriments() {
        // [Azote, Phosphore, Potassium] - échelle 1-10
        BESOINS_NUTRIMENTS.put("blé", new int[]{7, 5, 5});
        BESOINS_NUTRIMENTS.put("maïs", new int[]{9, 6, 7});
        BESOINS_NUTRIMENTS.put("tomate", new int[]{7, 8, 9});
        BESOINS_NUTRIMENTS.put("pomme de terre", new int[]{6, 7, 9});
        BESOINS_NUTRIMENTS.put("chou", new int[]{8, 6, 7});
        BESOINS_NUTRIMENTS.put("carotte", new int[]{4, 6, 7});
        BESOINS_NUTRIMENTS.put("oignon", new int[]{5, 5, 6});
        BESOINS_NUTRIMENTS.put("haricot", new int[]{2, 5, 5}); // Fixe son azote
        BESOINS_NUTRIMENTS.put("pois", new int[]{2, 4, 4});
        BESOINS_NUTRIMENTS.put("courgette", new int[]{6, 5, 7});
        BESOINS_NUTRIMENTS.put("melon", new int[]{6, 5, 8});
        BESOINS_NUTRIMENTS.put("tournesol", new int[]{5, 6, 8});
    }

    private static void initApportsNutriments() {
        // Légumineuses enrichissent le sol en azote
        APPORTS_NUTRIMENTS.put("haricot", new int[]{3, 0, 0});
        APPORTS_NUTRIMENTS.put("pois", new int[]{4, 0, 0});
        APPORTS_NUTRIMENTS.put("lentille", new int[]{3, 0, 0});
        APPORTS_NUTRIMENTS.put("fève", new int[]{5, 0, 0});
        APPORTS_NUTRIMENTS.put("soja", new int[]{4, 0, 0});
        APPORTS_NUTRIMENTS.put("trèfle", new int[]{6, 1, 1});
        APPORTS_NUTRIMENTS.put("luzerne", new int[]{7, 1, 2});
    }

    private static void initPreferencesSol() {
        PREFERENCES_SOL.put("blé", Arrays.asList("limoneux", "argileux"));
        PREFERENCES_SOL.put("maïs", Arrays.asList("limoneux", "argileux"));
        PREFERENCES_SOL.put("pomme de terre", Arrays.asList("sableux", "limoneux"));
        PREFERENCES_SOL.put("carotte", Arrays.asList("sableux", "limoneux"));
        PREFERENCES_SOL.put("tomate", Arrays.asList("limoneux", "sableux"));
        PREFERENCES_SOL.put("oignon", Arrays.asList("limoneux", "sableux"));
        PREFERENCES_SOL.put("chou", Arrays.asList("argileux", "limoneux"));
        PREFERENCES_SOL.put("haricot", Arrays.asList("limoneux", "sableux"));
        PREFERENCES_SOL.put("tournesol", Arrays.asList("limoneux", "argileux"));
    }

    private static void initPreferencesPh() {
        PREFERENCES_PH.put("blé", new double[]{6.0, 7.5});
        PREFERENCES_PH.put("maïs", new double[]{5.8, 7.0});
        PREFERENCES_PH.put("pomme de terre", new double[]{5.0, 6.5});
        PREFERENCES_PH.put("tomate", new double[]{6.0, 7.0});
        PREFERENCES_PH.put("carotte", new double[]{6.0, 7.0});
        PREFERENCES_PH.put("oignon", new double[]{6.0, 7.0});
        PREFERENCES_PH.put("chou", new double[]{6.5, 7.5});
        PREFERENCES_PH.put("haricot", new double[]{6.0, 7.5});
        PREFERENCES_PH.put("pois", new double[]{6.0, 7.5});
    }

    private static void initPeriodesPlanation() {
        PERIODES_PLANTATION.put("blé", "Octobre - Novembre (hiver) / Mars (printemps)");
        PERIODES_PLANTATION.put("maïs", "Avril - Mai");
        PERIODES_PLANTATION.put("tomate", "Mars - Avril (sous abri) / Mai (plein air)");
        PERIODES_PLANTATION.put("pomme de terre", "Mars - Avril");
        PERIODES_PLANTATION.put("carotte", "Février - Juillet");
        PERIODES_PLANTATION.put("oignon", "Février - Mars / Août - Septembre");
        PERIODES_PLANTATION.put("haricot", "Mai - Juillet");
        PERIODES_PLANTATION.put("pois", "Février - Avril / Octobre");
        PERIODES_PLANTATION.put("chou", "Mars - Juillet");
        PERIODES_PLANTATION.put("courgette", "Mai - Juin");
        PERIODES_PLANTATION.put("melon", "Avril - Mai");
        PERIODES_PLANTATION.put("tournesol", "Avril - Mai");
    }

    /**
     * Génère les recommandations de rotation pour une parcelle
     */
    public List<RecommandationCulture> genererRecommandations(Parcelle parcelle) {
        List<RecommandationCulture> recommandations = new ArrayList<>();

        for (String culture : FAMILLES_CULTURES.keySet()) {
            RecommandationCulture reco = evaluerCulture(culture, parcelle);
            recommandations.add(reco);
        }

        // Trier par score décroissant
        return recommandations.stream()
                .sorted(Comparator.comparingDouble(RecommandationCulture::getScoreCompatibilite).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Évalue la compatibilité d'une culture avec une parcelle
     */
    private RecommandationCulture evaluerCulture(String culture, Parcelle parcelle) {
        RecommandationCulture reco = new RecommandationCulture();
        reco.setCulture(culture);
        reco.setFamille(FAMILLES_CULTURES.get(culture));
        reco.setPeriodeOptimale(PERIODES_PLANTATION.getOrDefault(culture, "Variable"));

        double score = 100.0;
        StringBuilder raisons = new StringBuilder();
        StringBuilder benefices = new StringBuilder();

        String derniereCulture = parcelle.getDerniereCulture() != null ?
                                  parcelle.getDerniereCulture().toLowerCase() : "";
        String avantDerniere = parcelle.getAvantDerniereCulture() != null ?
                               parcelle.getAvantDerniereCulture().toLowerCase() : "";

        // 1. Vérifier incompatibilités (-50 points si incompatible)
        List<String> incompatibles = INCOMPATIBILITES.get(culture);
        if (incompatibles != null && incompatibles.contains(derniereCulture)) {
            score -= 50;
            raisons.append("⚠️ Incompatible avec la culture précédente (").append(derniereCulture).append(")\n");
        }

        // 2. Vérifier si même famille (-30 points)
        String familleCulture = FAMILLES_CULTURES.get(culture);
        String famillePrecedente = FAMILLES_CULTURES.get(derniereCulture);
        if (familleCulture != null && familleCulture.equals(famillePrecedente)) {
            score -= 30;
            raisons.append("⚠️ Même famille que la culture précédente (").append(familleCulture).append(")\n");
        }

        // 3. Bonus si culture bénéfique après la précédente (+20 points)
        List<String> benefiquesApres = BENEFIQUES.get(culture);
        if (benefiquesApres != null && benefiquesApres.contains(derniereCulture)) {
            score += 20;
            raisons.append("✅ Excellente succession après ").append(derniereCulture).append("\n");
            benefices.append("Bénéficie des résidus de la culture précédente\n");
        }

        // 4. Évaluer compatibilité sol (-15 à +10 points)
        List<String> solsPreferees = PREFERENCES_SOL.get(culture);
        String typeSol = parcelle.getTypeSol() != null ? parcelle.getTypeSol().toLowerCase() : "";
        if (solsPreferees != null) {
            if (solsPreferees.get(0).equalsIgnoreCase(typeSol)) {
                score += 10;
                raisons.append("✅ Sol idéal (").append(typeSol).append(")\n");
            } else if (solsPreferees.contains(typeSol.toLowerCase())) {
                score += 5;
                raisons.append("✓ Sol compatible (").append(typeSol).append(")\n");
            } else {
                score -= 15;
                raisons.append("⚠️ Sol non optimal (préfère: ").append(String.join(", ", solsPreferees)).append(")\n");
            }
        }

        // 5. Évaluer pH (-10 à +5 points)
        double[] phPref = PREFERENCES_PH.get(culture);
        if (phPref != null && parcelle.getPh() > 0) {
            if (parcelle.getPh() >= phPref[0] && parcelle.getPh() <= phPref[1]) {
                score += 5;
                raisons.append("✅ pH optimal (").append(parcelle.getPh()).append(")\n");
            } else {
                score -= 10;
                raisons.append("⚠️ pH non optimal (recommandé: ").append(phPref[0]).append("-").append(phPref[1]).append(")\n");
            }
        }

        // 6. Évaluer nutriments (-20 à +10 points)
        int[] besoins = BESOINS_NUTRIMENTS.get(culture);
        if (besoins != null) {
            int diffN = parcelle.getNiveauAzote() - besoins[0];
            int diffP = parcelle.getNiveauPhosphore() - besoins[1];
            int diffK = parcelle.getNiveauPotassium() - besoins[2];

            if (diffN >= 0 && diffP >= 0 && diffK >= 0) {
                score += 10;
                raisons.append("✅ Nutriments suffisants\n");
            } else if (diffN < -3 || diffP < -3 || diffK < -3) {
                score -= 20;
                raisons.append("⚠️ Carences importantes en nutriments\n");
                if (diffN < -3) raisons.append("   - Azote insuffisant\n");
                if (diffP < -3) raisons.append("   - Phosphore insuffisant\n");
                if (diffK < -3) raisons.append("   - Potassium insuffisant\n");
            } else {
                score -= 5;
                raisons.append("⚠️ Fertilisation recommandée\n");
            }
        }

        // 7. Bonus si légumineuse et sol pauvre en azote (+15 points)
        int[] apports = APPORTS_NUTRIMENTS.get(culture);
        if (apports != null && apports[0] > 0) {
            benefices.append("🌱 Fixation d'azote atmosphérique (+").append(apports[0]).append(" N)\n");
            if (parcelle.getNiveauAzote() < 5) {
                score += 15;
                raisons.append("✅ Légumineuse idéale pour sol pauvre en azote\n");
            }
        }

        // 8. Bonus si jachère recommandée et parcelle épuisée
        if (parcelle.getAnneesDepuisJachere() > 4 && parcelle.getFertiliteGlobale() < 4) {
            if ("trèfle".equals(culture) || "luzerne".equals(culture)) {
                score += 15;
                raisons.append("✅ Engrais vert recommandé après longue exploitation\n");
                benefices.append("Régénération du sol après jachère\n");
            }
        }

        // Limiter le score entre 0 et 100
        score = Math.max(0, Math.min(100, score));

        reco.setScoreCompatibilite(score);
        reco.setRaisonRecommandation(raisons.length() > 0 ? raisons.toString() : "Aucune contrainte particulière");
        reco.setBeneficesSol(benefices.length() > 0 ? benefices.toString() : "Aucun bénéfice spécifique identifié");

        return reco;
    }

    /**
     * Génère un plan de rotation sur plusieurs années
     */
    public List<String> genererPlanRotation(Parcelle parcelle, int nombreAnnees) {

        List<String> meilleuresRotation = new ArrayList<>();
        double meilleurScoreGlobal = Double.NEGATIVE_INFINITY;

        List<String> culturesDisponibles = getCulturesDisponibles();
        Random random = new Random();

        int nombreSimulations = 200; // Ajustable

        for (int s = 0; s < nombreSimulations; s++) {

            Parcelle parcelleSimulee = copierParcelle(parcelle);
            List<String> planTemporaire = new ArrayList<>();
            double scoreGlobal = 0;

            for (int annee = 0; annee < nombreAnnees; annee++) {

                // On prend le top 5 des meilleures cultures
                List<RecommandationCulture> recos = genererRecommandations(parcelleSimulee);
                int limite = Math.min(5, recos.size());

                if (limite == 0) break;

                // Choix aléatoire parmi le top 5 (évite cycles fixes)
                RecommandationCulture choisie = recos.get(random.nextInt(limite));

                planTemporaire.add(choisie.getCulture());
                scoreGlobal += choisie.getScoreCompatibilite();

                // Mise à jour historique
                parcelleSimulee.setAvantDerniereCulture(parcelleSimulee.getDerniereCulture());
                parcelleSimulee.setDerniereCulture(choisie.getCulture());

                // Simulation impact nutriments
                simulerImpactNutriments(parcelleSimulee, choisie.getCulture());
            }

            // 🔹 Bonus diversité (nombre de familles différentes)
            Set<String> familles = new HashSet<>();
            for (String culture : planTemporaire) {
                familles.add(FAMILLES_CULTURES.get(culture));
            }
            double bonusDiversite = familles.size() * 5;
            scoreGlobal += bonusDiversite;

            // 🔹 Pénalité répétition
            for (int i = 1; i < planTemporaire.size(); i++) {
                if (planTemporaire.get(i).equals(planTemporaire.get(i - 1))) {
                    scoreGlobal -= 15;
                }
            }

            // 🔹 Pénalité si alternance fixe détectée (ex: A B A B)
            if (planTemporaire.size() >= 4) {
                if (planTemporaire.get(0).equals(planTemporaire.get(2)) &&
                        planTemporaire.get(1).equals(planTemporaire.get(3))) {
                    scoreGlobal -= 20;
                }
            }

            if (scoreGlobal > meilleurScoreGlobal) {
                meilleurScoreGlobal = scoreGlobal;
                meilleuresRotation = new ArrayList<>(planTemporaire);
            }
        }

        return meilleuresRotation;
    }

    private Parcelle copierParcelle(Parcelle original) {
        Parcelle copie = new Parcelle();
        copie.setSurfaceHectares(original.getSurfaceHectares());
        copie.setTypeSol(original.getTypeSol());
        copie.setDerniereCulture(original.getDerniereCulture());
        copie.setAvantDerniereCulture(original.getAvantDerniereCulture());
        copie.setNiveauAzote(original.getNiveauAzote());
        copie.setNiveauPhosphore(original.getNiveauPhosphore());
        copie.setNiveauPotassium(original.getNiveauPotassium());
        copie.setPh(original.getPh());
        copie.setAnneesDepuisJachere(original.getAnneesDepuisJachere());
        return copie;
    }

    private void simulerImpactNutriments(Parcelle parcelle, String culture) {
        // Consommation
        int[] besoins = BESOINS_NUTRIMENTS.get(culture);
        if (besoins != null) {
            parcelle.setNiveauAzote(Math.max(1, parcelle.getNiveauAzote() - besoins[0] / 3));
            parcelle.setNiveauPhosphore(Math.max(1, parcelle.getNiveauPhosphore() - besoins[1] / 3));
            parcelle.setNiveauPotassium(Math.max(1, parcelle.getNiveauPotassium() - besoins[2] / 3));
        }

        // Apports (légumineuses)
        int[] apports = APPORTS_NUTRIMENTS.get(culture);
        if (apports != null) {
            parcelle.setNiveauAzote(Math.min(10, parcelle.getNiveauAzote() + apports[0]));
        }
    }

    /**
     * Retourne la liste des cultures disponibles
     */
    public List<String> getCulturesDisponibles() {
        return new ArrayList<>(FAMILLES_CULTURES.keySet()).stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Retourne les types de sol disponibles
     */
    public List<String> getTypesSol() {
        return Arrays.asList("Argileux", "Sableux", "Limoneux", "Calcaire");
    }

    /**
     * Génère un rapport détaillé de rotation
     */
    public String genererRapport(Parcelle parcelle, List<RecommandationCulture> recommandations) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("📋 RAPPORT DE ROTATION DES CULTURES\n");
        sb.append("═══════════════════════════════════════════════════\n\n");

        sb.append("🌍 PARCELLE:\n");
        sb.append("   Surface: ").append(parcelle.getSurfaceHectares()).append(" hectares\n");
        sb.append("   Type de sol: ").append(parcelle.getTypeSol()).append("\n");
        sb.append("   pH: ").append(parcelle.getPh()).append("\n\n");

        sb.append("📊 ÉTAT NUTRITIONNEL:\n");
        sb.append("   Azote (N): ").append(parcelle.getNiveauAzote()).append("/10\n");
        sb.append("   Phosphore (P): ").append(parcelle.getNiveauPhosphore()).append("/10\n");
        sb.append("   Potassium (K): ").append(parcelle.getNiveauPotassium()).append("/10\n");
        sb.append("   Fertilité globale: ").append(String.format("%.1f", parcelle.getFertiliteGlobale())).append("/10\n\n");

        sb.append("🌱 HISTORIQUE:\n");
        sb.append("   Dernière culture: ").append(parcelle.getDerniereCulture() != null ? parcelle.getDerniereCulture() : "Aucune").append("\n");
        sb.append("   Avant-dernière: ").append(parcelle.getAvantDerniereCulture() != null ? parcelle.getAvantDerniereCulture() : "Aucune").append("\n\n");

        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("⭐ TOP 5 RECOMMANDATIONS\n");
        sb.append("═══════════════════════════════════════════════════\n\n");

        int count = 0;
        for (RecommandationCulture reco : recommandations) {
            if (count >= 5) break;
            count++;

            sb.append(count).append(". ").append(reco.getNiveauIcon()).append(" ")
              .append(reco.getCulture().toUpperCase()).append(" (")
              .append(String.format("%.0f%%", reco.getScoreCompatibilite())).append(")\n");
            sb.append("   Famille: ").append(reco.getFamilleIcon()).append(" ").append(reco.getFamille()).append("\n");
            sb.append("   Période: ").append(reco.getPeriodeOptimale()).append("\n");
            sb.append("   ").append(reco.getRaisonRecommandation().replace("\n", "\n   "));
            sb.append("\n");
        }

        return sb.toString();
    }
}

