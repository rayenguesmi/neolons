package com.neo.app.controller;

import com.neo.app.documents.LicenseEntity;
import com.neo.app.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/licenses")
@CrossOrigin(origins = "*")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    /**
     * Récupère toutes les licences
     */
    @GetMapping
    public ResponseEntity<List<LicenseEntity>> getAllLicenses() {
        try {
            List<LicenseEntity> licenses = licenseService.getAllLicenses();
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une licence par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LicenseEntity> getLicenseById(@PathVariable String id) {
        try {
            Optional<LicenseEntity> license = licenseService.getLicenseById(id);
            if (license.isPresent()) {
                return ResponseEntity.ok(license.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crée une nouvelle licence
     */
    @PostMapping
    public ResponseEntity<LicenseEntity> createLicense(@RequestBody LicenseEntity license) {
        try {
            LicenseEntity createdLicense = licenseService.createLicense(license);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLicense);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour une licence existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<LicenseEntity> updateLicense(@PathVariable String id, @RequestBody LicenseEntity license) {
        try {
            LicenseEntity updatedLicense = licenseService.updateLicense(id, license);
            return ResponseEntity.ok(updatedLicense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une licence
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLicense(@PathVariable String id) {
        try {
            licenseService.deleteLicense(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Valide une licence par sa clé
     */
    @PostMapping("/validate")
    public ResponseEntity<LicenseService.LicenseValidationResult> validateLicense(@RequestBody ValidateLicenseRequest request) {
        try {
            LicenseService.LicenseValidationResult result = licenseService.validateLicense(request.getLicenseKey());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Active une licence par sa clé
     */
    @PostMapping("/activate")
    public ResponseEntity<LicenseEntity> activateLicense(@RequestBody ActivateLicenseRequest request) {
        try {
            LicenseEntity activatedLicense = licenseService.activateLicense(request.getLicenseKey());
            return ResponseEntity.ok(activatedLicense);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Désactive une licence
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<LicenseEntity> deactivateLicense(@PathVariable String id) {
        try {
            LicenseEntity deactivatedLicense = licenseService.deactivateLicense(id);
            return ResponseEntity.ok(deactivatedLicense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des licences
     */
    @GetMapping("/search")
    public ResponseEntity<List<LicenseEntity>> searchLicenses(@RequestParam(required = false) String q) {
        try {
            List<LicenseEntity> licenses = licenseService.searchLicenses(q);
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les licences par type
     */
    @GetMapping("/type/{licenseType}")
    public ResponseEntity<List<LicenseEntity>> getLicensesByType(@PathVariable String licenseType) {
        try {
            List<LicenseEntity> licenses = licenseService.getLicensesByType(licenseType);
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les licences actives
     */
    @GetMapping("/active")
    public ResponseEntity<List<LicenseEntity>> getActiveLicenses() {
        try {
            List<LicenseEntity> licenses = licenseService.getActiveLicenses();
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les licences expirées
     */
    @GetMapping("/expired")
    public ResponseEntity<List<LicenseEntity>> getExpiredLicenses() {
        try {
            List<LicenseEntity> licenses = licenseService.getExpiredLicenses();
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les licences qui expirent bientôt
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<LicenseEntity>> getLicensesExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        try {
            List<LicenseEntity> licenses = licenseService.getLicensesExpiringSoon(days);
            return ResponseEntity.ok(licenses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les statistiques des licences
     */
    @GetMapping("/statistics")
    public ResponseEntity<LicenseService.LicenseStatistics> getLicenseStatistics() {
        try {
            LicenseService.LicenseStatistics statistics = licenseService.getLicenseStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Initialise des licences par défaut pour les tests
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaultLicenses() {
        try {
            // Licence Standard
            LicenseEntity standardLicense = new LicenseEntity();
            standardLicense.setLicenseName("Licence Standard Neo.TM");
            standardLicense.setLicenseType("STANDARD");
            standardLicense.setProductName("Neo.TM");
            standardLicense.setProductVersion("1.0.0");
            standardLicense.setCustomerName("Entreprise Demo");
            standardLicense.setCustomerEmail("demo@entreprise.com");
            standardLicense.setOrganization("Demo Corp");
            standardLicense.setMaxUsers(10);
            standardLicense.setMaxProjects(5);
            standardLicense.setFeatures(List.of("USER_MANAGEMENT", "PROJECT_MANAGEMENT", "BASIC_REPORTING"));
            
            // Date d'expiration dans 1 an
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            standardLicense.setExpiryDate(cal.getTime());
            
            licenseService.createLicense(standardLicense);

            // Licence Premium
            LicenseEntity premiumLicense = new LicenseEntity();
            premiumLicense.setLicenseName("Licence Premium Neo.TM");
            premiumLicense.setLicenseType("PREMIUM");
            premiumLicense.setProductName("Neo.TM");
            premiumLicense.setProductVersion("1.0.0");
            premiumLicense.setCustomerName("Entreprise Premium");
            premiumLicense.setCustomerEmail("premium@entreprise.com");
            premiumLicense.setOrganization("Premium Corp");
            premiumLicense.setMaxUsers(50);
            premiumLicense.setMaxProjects(25);
            premiumLicense.setFeatures(List.of("USER_MANAGEMENT", "PROJECT_MANAGEMENT", "ADVANCED_REPORTING", "API_ACCESS", "CUSTOM_ROLES"));
            
            cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 2);
            premiumLicense.setExpiryDate(cal.getTime());
            
            licenseService.createLicense(premiumLicense);

            // Licence d'essai
            LicenseEntity trialLicense = new LicenseEntity();
            trialLicense.setLicenseName("Licence d'Essai Neo.TM");
            trialLicense.setLicenseType("TRIAL");
            trialLicense.setProductName("Neo.TM");
            trialLicense.setProductVersion("1.0.0");
            trialLicense.setCustomerName("Utilisateur Test");
            trialLicense.setCustomerEmail("test@example.com");
            trialLicense.setMaxUsers(3);
            trialLicense.setMaxProjects(2);
            trialLicense.setIsTrial(true);
            trialLicense.setMaxUsage(100);
            trialLicense.setFeatures(List.of("USER_MANAGEMENT", "PROJECT_MANAGEMENT"));
            
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            trialLicense.setExpiryDate(cal.getTime());
            
            licenseService.createLicense(trialLicense);

            return ResponseEntity.ok("Licences par défaut créées avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la création des licences par défaut: " + e.getMessage());
        }
    }

    // Classes pour les requêtes
    public static class ValidateLicenseRequest {
        private String licenseKey;

        public String getLicenseKey() { return licenseKey; }
        public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    }

    public static class ActivateLicenseRequest {
        private String licenseKey;

        public String getLicenseKey() { return licenseKey; }
        public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    }
}

