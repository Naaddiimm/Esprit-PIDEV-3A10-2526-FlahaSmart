package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * Service d'envoi d'emails via Gmail SMTP.
 *
 * 🔑 Configuration :
 *   1. Activez la validation en 2 étapes : https://myaccount.google.com/security
 *   2. Générez un mot de passe d'application : https://myaccount.google.com/apppasswords
 *      → Nom : FlahaSmart → Créer → Copier le code 16 caractères
 *   3. Remplacez GMAIL_USER par votre adresse Gmail
 *   4. Remplacez GMAIL_APP_PASSWORD par le code généré (sans espaces)
 */
public class EmailService {

    // ─── Configuration ────────────────────────────────────────────────────────
    private static final String GMAIL_USER         = "dhaouadi.eya@esprit.tn";
    private static final String GMAIL_APP_PASSWORD = "ypwxsuxogbcbsipp"; // 16 caractères sans espaces
    private static final String SENDER_NAME        = "FlahaSmart";

    // ─── Stockage des tokens de réinitialisation (en mémoire) ─────────────────
    // Clé = email, Valeur = [token, timestamp]
    private static final Map<String, long[]> resetTokens = new HashMap<>();
    private static final long TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes

    // ─── Envoi générique ──────────────────────────────────────────────────────

    /**
     * Envoie un email via Gmail SMTP.
     * @return true si envoyé avec succès, false sinon
     */
    private static boolean sendEmail(String toEmail, String toName,
                                     String subject, String htmlContent) {
        try {
            // Configuration SMTP Gmail
            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            "smtp.gmail.com");
            props.put("mail.smtp.port",            "587");
            props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

            // Authentification
            javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASSWORD);
                }
            });

            // Construire le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_USER, SENDER_NAME));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            javax.mail.Transport.send(message);

            System.out.println("📧 Gmail SMTP envoyé → " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ─── Email de bienvenue ───────────────────────────────────────────────────

    /**
     * Envoie un email de bienvenue après inscription.
     * Appelé de façon asynchrone pour ne pas bloquer l'UI.
     */
    public static void sendWelcomeEmail(String toEmail, String prenom, String role) {
        new Thread(() -> {
            String subject = "Bienvenue sur FlahaSmart, " + prenom + " ! 🌿";
            String html = buildWelcomeHtml(prenom, toEmail, role);
            boolean sent = sendEmail(toEmail, prenom, subject, html);
            System.out.println(sent
                    ? "✅ Email de bienvenue envoyé à " + toEmail
                    : "⚠️ Échec envoi email de bienvenue à " + toEmail);
        }).start();
    }

    private static String buildWelcomeHtml(String prenom, String email, String role) {
        String roleLabel = "AGRICULTEUR".equals(role) ? "Agriculteur" : "Client";
        String roleEmoji = "AGRICULTEUR".equals(role) ? "🌾" : "🛒";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8"/>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f1f5f9; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: white;
                                 border-radius: 16px; overflow: hidden;
                                 box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #064e3b, #047857);
                              padding: 40px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 28px; }
                    .header p  { color: #6ee7b7; margin: 8px 0 0; font-size: 16px; }
                    .body { padding: 40px; }
                    .greeting { font-size: 22px; color: #1e293b; font-weight: bold; }
                    .message  { color: #64748b; font-size: 15px; line-height: 1.7; margin: 16px 0; }
                    .badge { display: inline-block; background: #ecfdf5; color: #047857;
                             padding: 8px 20px; border-radius: 50px; font-weight: bold;
                             font-size: 14px; margin: 10px 0; border: 1px solid #6ee7b7; }
                    .features { background: #f8fafc; border-radius: 12px;
                                padding: 24px; margin: 24px 0; }
                    .features h3 { color: #1e293b; margin: 0 0 16px; }
                    .feature-item { display: flex; align-items: center; margin: 10px 0;
                                    color: #475569; font-size: 14px; }
                    .btn { display: block; width: fit-content; margin: 24px auto 0;
                           background: linear-gradient(to right, #047857, #065f46);
                           color: white; text-decoration: none; padding: 14px 36px;
                           border-radius: 10px; font-weight: bold; font-size: 15px; }
                    .footer { background: #f8fafc; padding: 24px; text-align: center;
                              color: #94a3b8; font-size: 13px; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
            <div class="container">
                <div class="header">
                    <h1>🌿 FlahaSmart</h1>
                    <p>Plateforme agricole intelligente</p>
                </div>
                <div class="body">
                    <p class="greeting">Bonjour %s ! 👋</p>
                    <p class="message">
                        Votre compte FlahaSmart a été créé avec succès.<br/>
                        Vous êtes maintenant inscrit en tant que :
                    </p>
                    <div class="badge">%s %s</div>
                    <div class="features">
                        <h3>Ce que vous pouvez faire maintenant :</h3>
                        <div class="feature-item">🌱&nbsp;&nbsp; Accéder au marketplace agricole</div>
                        <div class="feature-item">🤝&nbsp;&nbsp; Participer au forum communautaire</div>
                        <div class="feature-item">📊&nbsp;&nbsp; Suivre vos activités en temps réel</div>
                        <div class="feature-item">🚀&nbsp;&nbsp; Profiter du support prioritaire</div>
                    </div>
                    <p class="message">
                        Email associé à votre compte : <strong>%s</strong>
                    </p>
                </div>
                <div class="footer">
                    © 2025 FlahaSmart — Tous droits réservés<br/>
                    Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.
                </div>
            </div>
            </body>
            </html>
            """.formatted(prenom, roleEmoji, roleLabel, email);
    }

    // ─── Reset mot de passe ───────────────────────────────────────────────────

    /**
     * Génère un token à 6 chiffres, le stocke, et envoie l'email de reset.
     * @return le token généré (pour vérification côté controller), ou null si échec
     */
    public static String sendPasswordResetEmail(String toEmail, String prenom) {
        // Générer un token à 6 chiffres
        String token = String.format("%06d", new Random().nextInt(1_000_000));
        long expiry = System.currentTimeMillis() + TOKEN_EXPIRY_MS;

        // Stocker le token (email → [token en long, timestamp expiry])
        resetTokens.put(toEmail.toLowerCase(), new long[]{Long.parseLong(token), expiry});

        new Thread(() -> {
            String subject = "🔐 Réinitialisation de votre mot de passe FlahaSmart";
            String html = buildResetHtml(prenom, token);
            boolean sent = sendEmail(toEmail, prenom, subject, html);
            System.out.println(sent
                    ? "✅ Email reset envoyé à " + toEmail
                    : "⚠️ Échec email reset à " + toEmail);
        }).start();

        return token;
    }

    /**
     * Vérifie si le token entré par l'utilisateur est valide et non expiré.
     */
    public static boolean verifyResetToken(String email, String enteredToken) {
        long[] stored = resetTokens.get(email.toLowerCase());
        if (stored == null) return false;

        boolean tokenMatch  = String.valueOf((long) stored[0]).equals(enteredToken.trim());
        boolean notExpired  = System.currentTimeMillis() < stored[1];

        if (tokenMatch && notExpired) {
            // Invalider le token après usage
            resetTokens.remove(email.toLowerCase());
            return true;
        }

        if (!notExpired) {
            resetTokens.remove(email.toLowerCase());
            System.out.println("⏰ Token expiré pour " + email);
        }

        return false;
    }

    private static String buildResetHtml(String prenom, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8"/>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f1f5f9; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: white;
                                 border-radius: 16px; overflow: hidden;
                                 box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #1e293b, #334155);
                              padding: 40px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 26px; }
                    .header p  { color: #94a3b8; margin: 8px 0 0; }
                    .body { padding: 40px; }
                    .greeting { font-size: 20px; color: #1e293b; font-weight: bold; }
                    .message  { color: #64748b; font-size: 15px; line-height: 1.7; }
                    .token-box { background: #f8fafc; border: 2px dashed #047857;
                                 border-radius: 12px; padding: 28px; text-align: center;
                                 margin: 24px 0; }
                    .token { font-size: 42px; font-weight: bold; color: #047857;
                             letter-spacing: 10px; font-family: monospace; }
                    .token-label { color: #94a3b8; font-size: 13px; margin-top: 8px; }
                    .warning { background: #fff7ed; border-left: 4px solid #f97316;
                               padding: 14px 18px; border-radius: 8px;
                               color: #9a3412; font-size: 14px; margin: 20px 0; }
                    .footer { background: #f8fafc; padding: 24px; text-align: center;
                              color: #94a3b8; font-size: 13px; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
            <div class="container">
                <div class="header">
                    <h1>🌿 FlahaSmart</h1>
                    <p>Réinitialisation du mot de passe</p>
                </div>
                <div class="body">
                    <p class="greeting">Bonjour %s,</p>
                    <p class="message">
                        Vous avez demandé la réinitialisation de votre mot de passe.<br/>
                        Entrez ce code dans l'application :
                    </p>
                    <div class="token-box">
                        <div class="token">%s</div>
                        <div class="token-label">⏱ Ce code expire dans <strong>15 minutes</strong></div>
                    </div>
                    <div class="warning">
                        ⚠️ Si vous n'avez pas demandé cette réinitialisation,
                        ignorez cet email. Votre mot de passe reste inchangé.
                    </div>
                </div>
                <div class="footer">
                    © 2025 FlahaSmart — Tous droits réservés
                </div>
            </div>
            </body>
            </html>
            """.formatted(prenom, token);
    }
}
