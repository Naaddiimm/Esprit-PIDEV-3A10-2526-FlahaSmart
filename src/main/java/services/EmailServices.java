package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailServices {
    private final String host;
    private final String port;
    private final String username;
    private final String password;

    public EmailServices() {
        // Paramètres Gmail (à remplacer par vos identifiants)
        this.host = "smtp.gmail.com";
        this.port = "587";
        this.username = "mohamedbacharmhamed@gmail.com";
        this.password = "hjis ognn eyxc uyhm"; // Mot de passe d'application
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host); // Important pour Gmail
        props.put("mail.debug", "true"); // Affiche les échanges SMTP dans la console (à désactiver en production)

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}