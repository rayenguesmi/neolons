package com.neo.app.controller;

import com.neo.app.documents.LicenseEntity;
import com.neo.app.service.EmailService;
import com.neo.app.service.LicenseExpirationScheduler;
import com.neo.app.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/license-notifications")
@CrossOrigin(origins = "*")
public class LicenseNotificationController {

    @Autowired
    private LicenseExpirationScheduler expirationScheduler;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LicenseService licenseService;

    /**
     * Déclenche manuellement la vérification des licences expirées
     */
    @PostMapping("/check-expired")
    public ResponseEntity<Map<String, Object>> checkExpiredLicenses() {
        try {
            expirationScheduler.manualExpirationCheck();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vérification des licences expirées déclenchée avec succès");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Envoie une notification d'expiration pour une licence spécifique
     */
    @PostMapping("/send-expiration/{licenseId}")
    public ResponseEntity<Map<String, Object>> sendExpirationNotification(@PathVariable String licenseId) {
        try {
            LicenseEntity license = licenseService.getLicenseById(licenseId).orElse(null);
            
            if (license == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Licence non trouvée");
                return ResponseEntity.status(404).body(response);
            }

            if (license.getCustomerEmail() == null || license.getCustomerEmail().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Aucun email configuré pour cette licence");
                return ResponseEntity.status(400).body(response);
            }

            emailService.sendHtmlLicenseExpirationNotification(license);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification envoyée à " + license.getCustomerEmail());
            response.put("licenseId", licenseId);
            response.put("customerEmail", license.getCustomerEmail());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Envoie un rappel d'expiration pour une licence spécifique
     */
    @PostMapping("/send-reminder/{licenseId}")
    public ResponseEntity<Map<String, Object>> sendExpirationReminder(
            @PathVariable String licenseId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            LicenseEntity license = licenseService.getLicenseById(licenseId).orElse(null);
            
            if (license == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Licence non trouvée");
                return ResponseEntity.status(404).body(response);
            }

            if (license.getCustomerEmail() == null || license.getCustomerEmail().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Aucun email configuré pour cette licence");
                return ResponseEntity.status(400).body(response);
            }

            emailService.sendLicenseExpirationReminder(license, days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rappel envoyé à " + license.getCustomerEmail());
            response.put("licenseId", licenseId);
            response.put("customerEmail", license.getCustomerEmail());
            response.put("reminderDays", days);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi du rappel: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Teste la configuration email
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration() {
        try {
            boolean success = emailService.testEmailConfiguration();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Configuration email OK" : "Erreur de configuration email");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors du test: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtient les statistiques des notifications
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            String stats = expirationScheduler.getNotificationStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des statistiques: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Envoie un email de test à une adresse spécifique
     */
    @PostMapping("/test-email-to")
    public ResponseEntity<Map<String, Object>> testEmailTo(@RequestParam String email) {
        try {
            // Créer une licence fictive pour le test
            LicenseEntity testLicense = new LicenseEntity();
            testLicense.setCustomerName("Utilisateur Test");
            testLicense.setCustomerEmail(email);
            testLicense.setProductName("Produit Test");
            testLicense.setLicenseName("Licence Test");
            testLicense.setLicenseType("TEST");
            testLicense.setExpiryDate(new java.util.Date());
            
            emailService.sendHtmlLicenseExpirationNotification(testLicense);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email de test envoyé à " + email);
            response.put("email", email);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi du test: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtient la configuration actuelle des notifications
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getNotificationConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("notificationEnabled", true); // À récupérer depuis la configuration
            config.put("reminderDays", "7,3,1"); // À récupérer depuis la configuration
            config.put("checkInterval", "Toutes les heures");
            config.put("reminderTime", "9h00 quotidien");
            config.put("cleanupTime", "2h00 hebdomadaire (dimanche)");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("config", config);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération de la configuration: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

