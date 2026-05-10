package services;



import entities.ConsommationProduit;
import entities.StockProduit;

import javax.mail.MessagingException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private final ConsommationProduitService consommationService;
    private final StockProduitService stockService;
    private final UserService userService;
    private final EmailServices emailServices;

    public NotificationService() {
        this.consommationService = new ConsommationProduitService();
        this.stockService = new StockProduitService();
        this.userService = new UserService();
        this.emailServices = new EmailServices();
    }

    public void checkAndSendNotifications() {
        System.out.println("=== Exécution de checkAndSendNotifications à " + LocalDateTime.now() + " ===");
        LocalDate targetDate = LocalDate.now().plusDays(4);
        Date sqlTargetDate = Date.valueOf(targetDate);
        System.out.println("Date cible (J+4) : " + sqlTargetDate);

        try {
            List<ConsommationProduit> consommations = consommationService.getByDateUtilisation(sqlTargetDate);
            System.out.println("Nombre de consommations trouvées : " + consommations.size());

            for (ConsommationProduit conso : consommations) {
                System.out.println("Traitement de la consommation ID " + conso.getidProduit() + ", idStockProduit=" + conso.getIdStockProduit());
                try {
                    StockProduit stock = stockService.getById(conso.getIdStockProduit());
                    if (stock == null) {
                        System.out.println("Stock introuvable pour l'ID " + conso.getIdStockProduit());
                        continue;
                    }
                    System.out.println("Stock trouvé : typeProduit=" + stock.getTypeProduit() + ", idUser=" + stock.getIdUser());

                    String userEmail = userService.getEmailById(stock.getIdUser());
                    System.out.println("Email récupéré : " + userEmail);
                    if (userEmail == null || userEmail.isEmpty()) {
                        System.out.println("Email invalide pour l'utilisateur " + stock.getIdUser());
                        continue;
                    }

                    String htmlContent = buildEmailTemplate(stock.getTypeProduit(), conso.getDateUtilisation());
                    emailServices.sendHtmlEmail(userEmail, "FlahaSmart - Rappel d'utilisation produit", htmlContent);
                    System.out.println("Email envoyé avec succès à " + userEmail);

                } catch (SQLException | MessagingException e) {
                    System.err.println("Erreur pour la consommation ID " + conso.getidProduit() + " : ");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String buildEmailTemplate(String typeProduit, Date dateUtilisation) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; color: #333333; }" +
                ".content p { font-size: 16px; line-height: 1.5; }" +
                ".product-info { background-color: #e8f5e9; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0; }" +
                ".product-info strong { color: #2e7d32; }" +
                ".footer { background-color: #eeeeee; color: #666666; text-align: center; padding: 15px; font-size: 14px; }" +
                ".footer a { color: #4CAF50; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1>FlahaSmart</h1>" +
                "<p>Votre assistant agricole intelligent</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Bonjour,</p>" +
                "<p>Ceci est un rappel concernant l'utilisation de votre produit <strong>" + typeProduit + "</strong>.</p>" +
                "<div class=\"product-info\">" +
                "<p><strong>Produit :</strong> " + typeProduit + "</p>" +
                "<p><strong>Date d'utilisation prévue :</strong> " + dateUtilisation + "</p>" +
                "</div>" +
                "<p>Pensez à préparer votre matériel et à respecter les doses recommandées. N'hésitez pas à consulter l'application pour plus de détails.</p>" +
                "<p>Cordialement,<br>L'équipe FlahaSmart</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>© 2025 FlahaSmart. Tous droits réservés.</p>" +
                "<p><a href=\"#\">Se désabonner</a> | <a href=\"#\">Contact</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}