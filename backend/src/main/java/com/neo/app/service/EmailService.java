package com.neo.app.service;

import com.neo.app.documents.LicenseEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Neolons}")
    private String companyName;

    @Value("${app.company.support-email:support@neolons.com}")
    private String supportEmail;

    /**
     * Envoie un email simple de notification d'expiration de licence
     */
    public void sendLicenseExpirationNotification(LicenseEntity license) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(license.getCustomerEmail());
            message.setSubject("Notification d'expiration de licence - " + license.getProductName());
            
            String emailBody = buildSimpleExpirationMessage(license);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            System.out.println("Email d'expiration envoyé à: " + license.getCustomerEmail());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email à " + license.getCustomerEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoie un email HTML de notification d'expiration de licence
     */
    public void sendHtmlLicenseExpirationNotification(LicenseEntity license) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(license.getCustomerEmail());
            helper.setSubject("Notification d'expiration de licence - " + license.getProductName());
            
            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("customerName", license.getCustomerName());
            context.setVariable("productName", license.getProductName());
            context.setVariable("licenseName", license.getLicenseName());
            context.setVariable("licenseType", license.getLicenseType());
            context.setVariable("expiryDate", formatDate(license.getExpiryDate()));
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("currentYear", new SimpleDateFormat("yyyy").format(new Date()));
            
            // Générer le contenu HTML
            String htmlContent = templateEngine.process("license-expiration-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
            System.out.println("Email HTML d'expiration envoyé à: " + license.getCustomerEmail());
            
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email HTML à " + license.getCustomerEmail() + ": " + e.getMessage());
            e.printStackTrace();
            // Fallback vers email simple
            sendLicenseExpirationNotification(license);
        }
    }

    /**
     * Envoie un email de rappel avant expiration
     */
    public void sendLicenseExpirationReminder(LicenseEntity license, int daysBeforeExpiry) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(license.getCustomerEmail());
            message.setSubject("Rappel: Votre licence expire bientôt - " + license.getProductName());
            
            String emailBody = buildReminderMessage(license, daysBeforeExpiry);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            System.out.println("Email de rappel envoyé à: " + license.getCustomerEmail() + " (" + daysBeforeExpiry + " jours avant expiration)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du rappel à " + license.getCustomerEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoie des notifications en lot pour plusieurs licences
     */
    public void sendBulkExpirationNotifications(List<LicenseEntity> expiredLicenses) {
        for (LicenseEntity license : expiredLicenses) {
            sendHtmlLicenseExpirationNotification(license);
            
            // Petite pause pour éviter de surcharger le serveur mail
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Envoi en lot terminé: " + expiredLicenses.size() + " notifications envoyées");
    }

    /**
     * Construit le message simple d'expiration
     */
    private String buildSimpleExpirationMessage(LicenseEntity license) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(license.getCustomerName()).append(",\n\n");
        sb.append("Nous vous informons que votre licence pour le produit \"").append(license.getProductName()).append("\" ");
        
        if (license.isExpired()) {
            sb.append("a expiré le ").append(formatDate(license.getExpiryDate())).append(".\n\n");
            sb.append("Votre accès au produit pourrait être limité ou suspendu.\n\n");
        } else {
            long daysLeft = license.getDaysUntilExpiry();
            sb.append("expirera dans ").append(daysLeft).append(" jour(s) le ").append(formatDate(license.getExpiryDate())).append(".\n\n");
        }
        
        sb.append("Détails de la licence:\n");
        sb.append("- Nom de la licence: ").append(license.getLicenseName()).append("\n");
        sb.append("- Type: ").append(license.getLicenseType()).append("\n");
        sb.append("- Produit: ").append(license.getProductName()).append("\n");
        sb.append("- Date d'expiration: ").append(formatDate(license.getExpiryDate())).append("\n\n");
        
        sb.append("Pour renouveler votre licence ou obtenir de l'aide, veuillez contacter notre équipe support à l'adresse: ").append(supportEmail).append("\n\n");
        sb.append("Cordialement,\n");
        sb.append("L'équipe ").append(companyName).append("\n");
        
        return sb.toString();
    }

    /**
     * Construit le message de rappel
     */
    private String buildReminderMessage(LicenseEntity license, int daysBeforeExpiry) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(license.getCustomerName()).append(",\n\n");
        sb.append("Ceci est un rappel que votre licence pour le produit \"").append(license.getProductName()).append("\" ");
        sb.append("expirera dans ").append(daysBeforeExpiry).append(" jour(s) le ").append(formatDate(license.getExpiryDate())).append(".\n\n");
        
        sb.append("Nous vous recommandons de renouveler votre licence avant cette date pour éviter toute interruption de service.\n\n");
        
        sb.append("Détails de la licence:\n");
        sb.append("- Nom de la licence: ").append(license.getLicenseName()).append("\n");
        sb.append("- Type: ").append(license.getLicenseType()).append("\n");
        sb.append("- Produit: ").append(license.getProductName()).append("\n");
        sb.append("- Date d'expiration: ").append(formatDate(license.getExpiryDate())).append("\n\n");
        
        sb.append("Pour renouveler votre licence, veuillez contacter notre équipe support à l'adresse: ").append(supportEmail).append("\n\n");
        sb.append("Cordialement,\n");
        sb.append("L'équipe ").append(companyName).append("\n");
        
        return sb.toString();
    }

    /**
     * Formate une date en string lisible
     */
    private String formatDate(Date date) {
        if (date == null) return "Non définie";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    /**
     * Teste la configuration email
     */
    public boolean testEmailConfiguration() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Envoie à soi-même pour tester
            message.setSubject("Test de configuration email - Neolons");
            message.setText("Ceci est un email de test pour vérifier la configuration du service email.");
            
            mailSender.send(message);
            System.out.println("Test email envoyé avec succès");
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test email: " + e.getMessage());
            return false;
        }
    }
}

