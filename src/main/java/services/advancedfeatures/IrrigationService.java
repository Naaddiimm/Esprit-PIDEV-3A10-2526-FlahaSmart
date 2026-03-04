package services.advancedfeatures;

import entities.advancedfeatures.IrrigationPlan;
import entities.Operation;
import entities.advancedfeatures.Weather;
import services.OperationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service intelligent pour la gestion de l'irrigation
 * Calcule automatiquement les besoins en eau selon:
 * - Température (API météo)
 * - Humidité du sol (simulation)
 * - Précipitations prévues
 * - Type de culture
 */
public class IrrigationService {

    private WeatherService weatherService;
    private OperationService operationService;
    private Random random; // Pour simulation humidité sol

    // Besoins en eau de base par type de culture (L/m² par jour)
    private static final Map<String, Double> BESOINS_EAU_BASE = new HashMap<>() {{
        put("blé", 4.0);
        put("maïs", 6.0);
        put("tomate", 5.5);
        put("pomme de terre", 4.5);
        put("olivier", 3.0);
        put("vigne", 3.5);
        put("laitue", 5.0);
        put("carotte", 4.0);
        put("oignon", 3.5);
        put("courgette", 5.5);
        put("haricot", 4.5);
        put("piment", 5.0);
        put("aubergine", 5.5);
        put("melon", 6.0);
        put("pastèque", 6.5);
        put("fraise", 4.5);
        put("agrumes", 4.0);
        put("default", 4.5);
    }};

    // Coefficient de correction selon température
    private static final Map<String, Double> COEF_TEMPERATURE = new HashMap<>() {{
        put("froid", 0.6);      // < 15°C
        put("modéré", 1.0);    // 15-25°C
        put("chaud", 1.4);     // 25-35°C
        put("très chaud", 1.8); // > 35°C
    }};

    public IrrigationService() {
        this.weatherService = new WeatherService();
        this.operationService = new OperationService();
        this.random = new Random();
    }

    /**
     * Génère un plan d'irrigation intelligent pour une culture
     */
    public IrrigationPlan genererPlanIrrigation(String typeCulture, double surfaceHectares, String ville) {
        return genererPlanIrrigation(typeCulture, surfaceHectares, ville, null, 0);
    }

    /**
     * Génère un plan d'irrigation intelligent avec historique
     * @param typeCulture Type de culture
     * @param surfaceHectares Surface en hectares
     * @param ville Ville pour météo
     * @param derniereDateIrrigation Date de la dernière irrigation (peut être null)
     * @param quantiteEauPrecedente Quantité d'eau utilisée précédemment en litres
     */
    public IrrigationPlan genererPlanIrrigation(String typeCulture, double surfaceHectares, String ville,
                                                  java.time.LocalDate derniereDateIrrigation, double quantiteEauPrecedente) {
        IrrigationPlan plan = new IrrigationPlan(typeCulture, surfaceHectares);

        // Définir les nouveaux critères
        plan.setDerniereDateIrrigation(derniereDateIrrigation);
        plan.setQuantiteEauPrecedente(quantiteEauPrecedente);

        // Récupérer données météo
        Weather weather = weatherService.getWeather(ville);

        if (weather != null) {
            plan.setTemperature(weather.getTemperature());
            plan.setHumiditeAir(weather.getHumidity());
            plan.setConditionMeteo(weather.getDescription());

            // Simuler précipitations selon description météo
            plan.setPrecipitationsPrevues(estimerPrecipitations(weather.getDescription()));
        } else {
            // Valeurs par défaut si API non disponible
            plan.setTemperature(25);
            plan.setHumiditeAir(50);
            plan.setConditionMeteo("Données non disponibles");
            plan.setPrecipitationsPrevues(0);
        }

        // Simuler humidité du sol (30-80%)
        plan.setHumiditeSol(30 + random.nextInt(51));

        // Calculer besoin en eau
        double besoinEau = calculerBesoinEau(plan);
        plan.setBesoinEauLitres(besoinEau);

        // Déterminer heure optimale
        plan.setHeureOptimale(determinerHeureOptimale(plan.getTemperature()));

        // Calculer durée d'irrigation (débit moyen: 10L/min par hectare)
        int duree = (int) Math.ceil(besoinEau / (10 * surfaceHectares * 60));
        plan.setDureeMinutes(Math.max(15, Math.min(duree, 180))); // Min 15min, max 3h

        // Déterminer priorité
        plan.setPriorite(determinerPriorite(plan));

        // Générer justification
        plan.setJustification(genererJustification(plan));

        // Date d'irrigation
        plan.setDateIrrigation(determinerDateIrrigation(plan));

        return plan;
    }

    /**
     * Calcule le besoin en eau selon tous les paramètres
     */
    private double calculerBesoinEau(IrrigationPlan plan) {
        String typeLower = plan.getTypeCulture().toLowerCase();

        // Besoin de base
        double besoinBase = BESOINS_EAU_BASE.getOrDefault(typeLower, BESOINS_EAU_BASE.get("default"));

        // Surface en m²
        double surfaceM2 = plan.getSurfaceHectares() * 10000;

        // Coefficient température
        double coefTemp = getCoefTemperature(plan.getTemperature());

        // Coefficient humidité sol (inverse: moins c'est humide, plus il faut d'eau)
        double coefHumSol = 1.5 - (plan.getHumiditeSol() / 100.0);

        // Coefficient humidité air
        double coefHumAir = 1.3 - (plan.getHumiditeAir() / 100.0 * 0.5);

        // Réduction si précipitations prévues
        double coefPrecip = 1.0 - (plan.getPrecipitationsPrevues() / 20.0);
        coefPrecip = Math.max(0.2, coefPrecip); // Minimum 20% même avec pluie

        // Coefficient basé sur la dernière date d'irrigation
        double coefDerniereIrrigation = calculerCoefDerniereIrrigation(plan);

        // Coefficient basé sur la quantité d'eau précédente
        double coefQuantitePrecedente = calculerCoefQuantitePrecedente(plan);

        // Calcul final
        double besoinTotal = besoinBase * surfaceM2 * coefTemp * coefHumSol * coefHumAir * coefPrecip 
                            * coefDerniereIrrigation * coefQuantitePrecedente;

        return Math.round(besoinTotal * 100.0) / 100.0;
    }

    /**
     * Calcule le coefficient basé sur la dernière date d'irrigation
     * Plus le temps depuis la dernière irrigation est long, plus le besoin augmente
     */
    private double calculerCoefDerniereIrrigation(IrrigationPlan plan) {
        if (plan.getDerniereDateIrrigation() == null) {
            return 1.0; // Pas d'historique, coefficient neutre
        }

        int joursDepuis = plan.getJoursDepuisDerniereIrrigation();
        
        if (joursDepuis <= 1) {
            return 0.6; // Irrigué hier ou aujourd'hui, réduire fortement
        } else if (joursDepuis <= 3) {
            return 0.8; // Irrigué récemment
        } else if (joursDepuis <= 5) {
            return 1.0; // Normal
        } else if (joursDepuis <= 7) {
            return 1.2; // Un peu en retard
        } else if (joursDepuis <= 10) {
            return 1.4; // En retard
        } else {
            return 1.6; // Très en retard, urgence
        }
    }

    /**
     * Calcule le coefficient basé sur la quantité d'eau précédente
     * Ajuste le besoin en fonction de l'efficacité de la dernière irrigation
     */
    private double calculerCoefQuantitePrecedente(IrrigationPlan plan) {
        if (plan.getQuantiteEauPrecedente() <= 0) {
            return 1.0; // Pas d'historique
        }

        // Calculer le besoin théorique pour comparaison
        double besoinTheorique = BESOINS_EAU_BASE.getOrDefault(
            plan.getTypeCulture().toLowerCase(), 
            BESOINS_EAU_BASE.get("default")
        ) * plan.getSurfaceHectares() * 10000;

        double ratio = plan.getQuantiteEauPrecedente() / besoinTheorique;

        if (ratio < 0.5) {
            return 1.3; // Sous-irrigation précédente, augmenter
        } else if (ratio < 0.8) {
            return 1.1; // Légèrement sous-irrigué
        } else if (ratio <= 1.2) {
            return 1.0; // Quantité normale
        } else if (ratio <= 1.5) {
            return 0.9; // Légèrement sur-irrigué
        } else {
            return 0.8; // Sur-irrigation, réduire
        }
    }

    private double getCoefTemperature(int temperature) {
        if (temperature < 15) return COEF_TEMPERATURE.get("froid");
        if (temperature <= 25) return COEF_TEMPERATURE.get("modéré");
        if (temperature <= 35) return COEF_TEMPERATURE.get("chaud");
        return COEF_TEMPERATURE.get("très chaud");
    }

    /**
     * Détermine l'heure optimale d'irrigation
     */
    private String determinerHeureOptimale(int temperature) {
        if (temperature > 35) {
            return "05:00 - 06:30"; // Très tôt si très chaud
        } else if (temperature > 28) {
            return "06:00 - 08:00"; // Tôt le matin
        } else if (temperature > 20) {
            return "07:00 - 09:00"; // Matin
        } else {
            return "08:00 - 10:00"; // Plus tard si frais
        }
    }

    /**
     * Détermine la priorité d'irrigation
     */
    private String determinerPriorite(IrrigationPlan plan) {
        int score = 0;

        // Humidité sol critique
        if (plan.getHumiditeSol() < 35) score += 3;
        else if (plan.getHumiditeSol() < 45) score += 2;
        else if (plan.getHumiditeSol() < 55) score += 1;

        // Température élevée
        if (plan.getTemperature() > 35) score += 2;
        else if (plan.getTemperature() > 30) score += 1;

        // Pas de pluie prévue
        if (plan.getPrecipitationsPrevues() < 2) score += 1;

        // Faible humidité air
        if (plan.getHumiditeAir() < 40) score += 1;

        // Nouveau critère: Jours depuis dernière irrigation
        int joursDepuis = plan.getJoursDepuisDerniereIrrigation();
        if (joursDepuis > 10) score += 3;
        else if (joursDepuis > 7) score += 2;
        else if (joursDepuis > 5) score += 1;

        // Nouveau critère: Sous-irrigation précédente
        if (plan.getQuantiteEauPrecedente() > 0) {
            double besoinTheorique = BESOINS_EAU_BASE.getOrDefault(
                plan.getTypeCulture().toLowerCase(), 
                BESOINS_EAU_BASE.get("default")
            ) * plan.getSurfaceHectares() * 10000;
            double ratio = plan.getQuantiteEauPrecedente() / besoinTheorique;
            if (ratio < 0.5) score += 2;
            else if (ratio < 0.7) score += 1;
        }

        if (score >= 6) return "URGENT";
        if (score >= 3) return "NORMAL";
        return "FAIBLE";
    }

    /**
     * Estime les précipitations selon la description météo
     */
    private double estimerPrecipitations(String description) {
        String descLower = description.toLowerCase();

        if (descLower.contains("heavy rain") || descLower.contains("forte pluie")) return 15.0;
        if (descLower.contains("rain") || descLower.contains("pluie")) return 8.0;
        if (descLower.contains("drizzle") || descLower.contains("bruine")) return 3.0;
        if (descLower.contains("shower") || descLower.contains("averse")) return 6.0;
        if (descLower.contains("thunderstorm") || descLower.contains("orage")) return 12.0;
        if (descLower.contains("cloudy") || descLower.contains("nuageux")) return 1.0;

        return 0.0;
    }

    /**
     * Génère une justification détaillée
     */
    private String genererJustification(IrrigationPlan plan) {
        StringBuilder sb = new StringBuilder();

        sb.append("📊 Analyse pour ").append(plan.getTypeCulture()).append(":\n\n");

        // Température
        sb.append("🌡 Température: ").append(plan.getTemperature()).append("°C");
        if (plan.getTemperature() > 30) {
            sb.append(" → Stress thermique, augmentation des besoins");
        } else if (plan.getTemperature() < 15) {
            sb.append(" → Faible évaporation, réduction des besoins");
        }
        sb.append("\n");

        // Humidité sol
        sb.append("💧 Humidité sol: ").append(plan.getHumiditeSol()).append("%");
        if (plan.getHumiditeSol() < 40) {
            sb.append(" → Sol sec, irrigation urgente");
        } else if (plan.getHumiditeSol() > 70) {
            sb.append(" → Sol bien hydraté");
        }
        sb.append("\n");

        // Humidité air
        sb.append("💨 Humidité air: ").append(plan.getHumiditeAir()).append("%\n");

        // Précipitations
        if (plan.getPrecipitationsPrevues() > 5) {
            sb.append("🌧 Pluie prévue: ").append(plan.getPrecipitationsPrevues()).append("mm → Réduction irrigation\n");
        } else if (plan.getPrecipitationsPrevues() > 0) {
            sb.append("🌤 Légères précipitations possibles\n");
        } else {
            sb.append("☀️ Pas de pluie prévue\n");
        }

        // Nouveau critère: Dernière date d'irrigation
        int joursDepuis = plan.getJoursDepuisDerniereIrrigation();
        if (joursDepuis >= 0) {
            sb.append("\n📅 Dernière irrigation: il y a ").append(joursDepuis).append(" jour(s)");
            if (joursDepuis > 7) {
                sb.append(" → ⚠️ Irrigation en retard!");
            } else if (joursDepuis <= 2) {
                sb.append(" → Irrigation récente");
            }
            sb.append("\n");
        }

        // Nouveau critère: Quantité d'eau précédente
        if (plan.getQuantiteEauPrecedente() > 0) {
            sb.append("💦 Dernière quantité: ").append(plan.getQuantiteEauPrecedenteFormate());
            double besoinTheorique = BESOINS_EAU_BASE.getOrDefault(
                plan.getTypeCulture().toLowerCase(), 
                BESOINS_EAU_BASE.get("default")
            ) * plan.getSurfaceHectares() * 10000;
            double ratio = plan.getQuantiteEauPrecedente() / besoinTheorique;
            if (ratio < 0.7) {
                sb.append(" → Sous-irrigation, compensation nécessaire");
            } else if (ratio > 1.3) {
                sb.append(" → Sur-irrigation, réduction appliquée");
            }
            sb.append("\n");
        }

        sb.append("\n💡 Recommandation: Irriguer ").append(plan.getBesoinEauFormate());
        sb.append(" pendant ").append(plan.getDureeMinutes()).append(" minutes");

        return sb.toString();
    }

    /**
     * Détermine la date d'irrigation optimale
     */
    private LocalDate determinerDateIrrigation(IrrigationPlan plan) {
        if (plan.getPriorite().equals("URGENT")) {
            return LocalDate.now(); // Aujourd'hui
        } else if (plan.getPriorite().equals("NORMAL")) {
            return LocalDate.now().plusDays(1); // Demain
        } else {
            return LocalDate.now().plusDays(2); // Dans 2 jours
        }
    }

    /**
     * Génère un planning d'irrigation pour plusieurs cultures
     */
    public List<IrrigationPlan> genererPlanningHebdomadaire(List<String> cultures,
                                                             Map<String, Double> surfaces,
                                                             String ville) {
        List<IrrigationPlan> planning = new ArrayList<>();

        for (String culture : cultures) {
            double surface = surfaces.getOrDefault(culture, 1.0);
            IrrigationPlan plan = genererPlanIrrigation(culture, surface, ville);
            planning.add(plan);
        }

        // Trier par priorité puis par date
        return planning.stream()
                .sorted(Comparator
                        .comparing(IrrigationPlan::getPriorite)
                        .thenComparing(IrrigationPlan::getDateIrrigation))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les opérations en cours de type irrigation
     */
    public List<Operation> getOperationsIrrigationEnCours() throws SQLException {
        return operationService.afficher().stream()
                .filter(op -> op.getStatut() != null && op.getStatut().equalsIgnoreCase("en cours"))
                .filter(op -> {
                    String type = op.getType_operation();
                    if (type == null) return false;
                    String typeLower = type.toLowerCase();
                    return typeLower.contains("irrig") ||
                           typeLower.contains("arros") ||
                           typeLower.contains("water");
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les opérations en cours
     */
    public List<Operation> getAllOperationsEnCours() throws SQLException {
        return operationService.afficher().stream()
                .filter(op -> op.getStatut() != null && op.getStatut().equalsIgnoreCase("en cours"))
                .collect(Collectors.toList());
    }

    /**
     * Retourne les types de cultures disponibles
     */
    public List<String> getTypesCultures() {
        return new ArrayList<>(BESOINS_EAU_BASE.keySet()).stream()
                .filter(k -> !k.equals("default"))
                .sorted()
                .collect(Collectors.toList());
    }
}

