package com.neo.app.service;

import com.neo.app.documents.LicenseEntity;
import com.neo.app.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private AuditLogService auditLogService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    // Création d'une nouvelle licence
    public LicenseEntity createLicense(LicenseEntity license) {
        validateLicenseCreation(license);

        // Générer une clé de licence unique
        license.setLicenseKey(generateLicenseKey());
        
        // Définir les dates
        license.setCreatedAt(new Date());
        license.setUpdatedAt(new Date());
        license.setIssueDate(new Date());
        
        // Définir les valeurs par défaut
        if (license.getIsActive() == null) {
            license.setIsActive(true);
        }
        if (license.getIsTrial() == null) {
            license.setIsTrial(false);
        }
        if (license.getUsageCount() == null) {
            license.setUsageCount(0);
        }
        if (license.getValidationStatus() == null) {
            license.setValidationStatus("VALID");
        }

        LicenseEntity savedLicense = licenseRepository.save(license);

        // Audit log
        auditLogService.logAction(getCurrentUserId(),
                "CREATE_LICENSE", "licenses", savedLicense.getId(), null,
                "License created: " + savedLicense.getLicenseName());

        return savedLicense;
    }

    // Mise à jour d'une licence
    public LicenseEntity updateLicense(String licenseId, LicenseEntity updatedLicense) {
        Optional<LicenseEntity> existingLicenseOpt = licenseRepository.findById(licenseId);
        if (!existingLicenseOpt.isPresent()) {
            throw new RuntimeException("Licence non trouvée avec l'ID: " + licenseId);
        }

        LicenseEntity existingLicense = existingLicenseOpt.get();
        String oldValues = existingLicense.toString();

        // Mise à jour des champs modifiables
        if (updatedLicense.getLicenseName() != null) {
            existingLicense.setLicenseName(updatedLicense.getLicenseName());
        }
        if (updatedLicense.getLicenseType() != null) {
            existingLicense.setLicenseType(updatedLicense.getLicenseType());
        }
        if (updatedLicense.getProductName() != null) {
            existingLicense.setProductName(updatedLicense.getProductName());
        }
        if (updatedLicense.getProductVersion() != null) {
            existingLicense.setProductVersion(updatedLicense.getProductVersion());
        }
        if (updatedLicense.getCustomerName() != null) {
            existingLicense.setCustomerName(updatedLicense.getCustomerName());
        }
        if (updatedLicense.getCustomerEmail() != null) {
            existingLicense.setCustomerEmail(updatedLicense.getCustomerEmail());
        }
        if (updatedLicense.getOrganization() != null) {
            existingLicense.setOrganization(updatedLicense.getOrganization());
        }
        if (updatedLicense.getMaxUsers() != null) {
            existingLicense.setMaxUsers(updatedLicense.getMaxUsers());
        }
        if (updatedLicense.getMaxProjects() != null) {
            existingLicense.setMaxProjects(updatedLicense.getMaxProjects());
        }
        if (updatedLicense.getFeatures() != null) {
            existingLicense.setFeatures(updatedLicense.getFeatures());
        }
        if (updatedLicense.getExpiryDate() != null) {
            existingLicense.setExpiryDate(updatedLicense.getExpiryDate());
        }
        if (updatedLicense.getIsActive() != null) {
            existingLicense.setIsActive(updatedLicense.getIsActive());
        }
        if (updatedLicense.getMaxUsage() != null) {
            existingLicense.setMaxUsage(updatedLicense.getMaxUsage());
        }
        if (updatedLicense.getIpRestrictions() != null) {
            existingLicense.setIpRestrictions(updatedLicense.getIpRestrictions());
        }
        if (updatedLicense.getDomainRestrictions() != null) {
            existingLicense.setDomainRestrictions(updatedLicense.getDomainRestrictions());
        }
        if (updatedLicense.getNotes() != null) {
            existingLicense.setNotes(updatedLicense.getNotes());
        }

        existingLicense.setUpdatedAt(new Date());
        existingLicense.setUpdatedBy(getCurrentUserId());

        LicenseEntity savedLicense = licenseRepository.save(existingLicense);

        // Audit log
        auditLogService.logAction(getCurrentUserId(),
                "UPDATE_LICENSE", "licenses", savedLicense.getId(), oldValues,
                "License updated: " + savedLicense.getLicenseName());

        return savedLicense;
    }

    // Suppression d'une licence
    public void deleteLicense(String licenseId) {
        Optional<LicenseEntity> licenseOpt = licenseRepository.findById(licenseId);
        if (!licenseOpt.isPresent()) {
            throw new RuntimeException("Licence non trouvée avec l'ID: " + licenseId);
        }

        LicenseEntity license = licenseOpt.get();
        String oldValues = license.toString();

        licenseRepository.deleteById(licenseId);

        // Audit log
        auditLogService.logAction(getCurrentUserId(),
                "DELETE_LICENSE", "licenses", licenseId, oldValues,
                "License deleted: " + license.getLicenseName());
    }

    // Récupération d'une licence par ID
    public Optional<LicenseEntity> getLicenseById(String licenseId) {
        return licenseRepository.findById(licenseId);
    }

    // Récupération de toutes les licences
    public List<LicenseEntity> getAllLicenses() {
        return licenseRepository.findAll();
    }

    // Validation d'une licence par clé
    public LicenseValidationResult validateLicense(String licenseKey) {
        Optional<LicenseEntity> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
        
        if (!licenseOpt.isPresent()) {
            return new LicenseValidationResult(false, "Licence non trouvée", null);
        }

        LicenseEntity license = licenseOpt.get();
        
        // Mettre à jour la dernière validation
        license.setLastValidation(new Date());
        
        // Vérifications de validité
        if (!license.getIsActive()) {
            license.setValidationStatus("SUSPENDED");
            licenseRepository.save(license);
            return new LicenseValidationResult(false, "Licence suspendue", license);
        }

        if (license.isExpired()) {
            license.setValidationStatus("EXPIRED");
            licenseRepository.save(license);
            return new LicenseValidationResult(false, "Licence expirée", license);
        }

        if (!license.hasUsageLeft()) {
            license.setValidationStatus("USAGE_EXCEEDED");
            licenseRepository.save(license);
            return new LicenseValidationResult(false, "Limite d'utilisation dépassée", license);
        }

        // Licence valide
        license.setValidationStatus("VALID");
        license.incrementUsage();
        licenseRepository.save(license);

        // Audit log
        auditLogService.logAction("SYSTEM",
                "VALIDATE_LICENSE", "licenses", license.getId(), null,
                "License validated: " + license.getLicenseName());

        return new LicenseValidationResult(true, "Licence valide", license);
    }

    // Activation d'une licence
    public LicenseEntity activateLicense(String licenseKey) {
        Optional<LicenseEntity> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
        
        if (!licenseOpt.isPresent()) {
            throw new RuntimeException("Licence non trouvée avec la clé: " + licenseKey);
        }

        LicenseEntity license = licenseOpt.get();
        
        if (license.getActivationDate() != null) {
            throw new RuntimeException("Licence déjà activée");
        }

        license.setActivationDate(new Date());
        license.setIsActive(true);
        license.setValidationStatus("VALID");
        license.setUpdatedAt(new Date());

        LicenseEntity savedLicense = licenseRepository.save(license);

        // Audit log
        auditLogService.logAction(getCurrentUserId(),
                "ACTIVATE_LICENSE", "licenses", savedLicense.getId(), null,
                "License activated: " + savedLicense.getLicenseName());

        return savedLicense;
    }

    // Désactivation d'une licence
    public LicenseEntity deactivateLicense(String licenseId) {
        Optional<LicenseEntity> licenseOpt = licenseRepository.findById(licenseId);
        
        if (!licenseOpt.isPresent()) {
            throw new RuntimeException("Licence non trouvée avec l'ID: " + licenseId);
        }

        LicenseEntity license = licenseOpt.get();
        license.setIsActive(false);
        license.setValidationStatus("SUSPENDED");
        license.setUpdatedAt(new Date());

        LicenseEntity savedLicense = licenseRepository.save(license);

        // Audit log
        auditLogService.logAction(getCurrentUserId(),
                "DEACTIVATE_LICENSE", "licenses", savedLicense.getId(), null,
                "License deactivated: " + savedLicense.getLicenseName());

        return savedLicense;
    }

    // Recherche de licences
    public List<LicenseEntity> searchLicenses(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllLicenses();
        }

        List<LicenseEntity> results = new ArrayList<>();
        
        // Recherche par nom de licence
        results.addAll(licenseRepository.findByLicenseNameContainingIgnoreCase(searchTerm));
        
        // Recherche par nom de client
        results.addAll(licenseRepository.findByCustomerNameContainingIgnoreCase(searchTerm));
        
        // Recherche par organisation
        results.addAll(licenseRepository.findByOrganizationContainingIgnoreCase(searchTerm));
        
        // Supprimer les doublons
        return results.stream().distinct().collect(Collectors.toList());
    }

    // Récupération des licences par type
    public List<LicenseEntity> getLicensesByType(String licenseType) {
        return licenseRepository.findByLicenseType(licenseType);
    }

    // Récupération des licences actives
    public List<LicenseEntity> getActiveLicenses() {
        return licenseRepository.findByIsActive(true);
    }

    // Récupération des licences expirées
    public List<LicenseEntity> getExpiredLicenses() {
        return licenseRepository.findExpiredLicenses(new Date());
    }

    // Récupération des licences qui expirent bientôt
    public List<LicenseEntity> getLicensesExpiringSoon(int days) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, days);
        Date futureDate = cal.getTime();
        
        return licenseRepository.findLicensesExpiringBetween(now, futureDate);
    }

    // Statistiques des licences
    public LicenseStatistics getLicenseStatistics() {
        LicenseStatistics stats = new LicenseStatistics();
        
        stats.setTotalLicenses(licenseRepository.count());
        stats.setActiveLicenses(licenseRepository.countByIsActive(true));
        stats.setInactiveLicenses(licenseRepository.countByIsActive(false));
        stats.setTrialLicenses(licenseRepository.countByIsTrial(true));
        stats.setExpiredLicenses(licenseRepository.countExpiredLicenses(new Date()));
        
        // Compter par type
        Map<String, Long> byType = new HashMap<>();
        byType.put("STANDARD", licenseRepository.countByLicenseType("STANDARD"));
        byType.put("PREMIUM", licenseRepository.countByLicenseType("PREMIUM"));
        byType.put("ENTERPRISE", licenseRepository.countByLicenseType("ENTERPRISE"));
        byType.put("TRIAL", licenseRepository.countByLicenseType("TRIAL"));
        stats.setLicensesByType(byType);
        
        return stats;
    }

    // Génération d'une clé de licence unique
    private String generateLicenseKey() {
        String key;
        do {
            key = generateRandomKey();
        } while (licenseRepository.findByLicenseKey(key).isPresent());
        
        return key;
    }

    private String generateRandomKey() {
        StringBuilder sb = new StringBuilder();
        
        // Format: XXXX-XXXX-XXXX-XXXX
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append("-");
            for (int j = 0; j < 4; j++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
        }
        
        return sb.toString();
    }

    // Validation des données de création
    private void validateLicenseCreation(LicenseEntity license) {
        if (license.getLicenseName() == null || license.getLicenseName().trim().isEmpty()) {
            throw new RuntimeException("Le nom de la licence est obligatoire");
        }
        
        if (license.getLicenseType() == null || license.getLicenseType().trim().isEmpty()) {
            throw new RuntimeException("Le type de licence est obligatoire");
        }
        
        if (license.getCustomerName() == null || license.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("Le nom du client est obligatoire");
        }
        
        if (license.getCustomerEmail() == null || license.getCustomerEmail().trim().isEmpty()) {
            throw new RuntimeException("L'email du client est obligatoire");
        }
        
        if (license.getExpiryDate() != null && license.getExpiryDate().before(new Date())) {
            throw new RuntimeException("La date d'expiration ne peut pas être dans le passé");
        }
    }

    // Récupération de l'utilisateur actuel (à implémenter selon le système d'authentification)
    private String getCurrentUserId() {
        // TODO: Implémenter la récupération de l'utilisateur actuel
        return "ADMIN"; // Valeur par défaut pour les tests
    }

    // Classes internes pour les résultats
    public static class LicenseValidationResult {
        private boolean valid;
        private String message;
        private LicenseEntity license;

        public LicenseValidationResult(boolean valid, String message, LicenseEntity license) {
            this.valid = valid;
            this.message = message;
            this.license = license;
        }

        // Getters et setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LicenseEntity getLicense() { return license; }
        public void setLicense(LicenseEntity license) { this.license = license; }
    }

    public static class LicenseStatistics {
        private long totalLicenses;
        private long activeLicenses;
        private long inactiveLicenses;
        private long trialLicenses;
        private long expiredLicenses;
        private Map<String, Long> licensesByType;

        // Getters et setters
        public long getTotalLicenses() { return totalLicenses; }
        public void setTotalLicenses(long totalLicenses) { this.totalLicenses = totalLicenses; }
        
        public long getActiveLicenses() { return activeLicenses; }
        public void setActiveLicenses(long activeLicenses) { this.activeLicenses = activeLicenses; }
        
        public long getInactiveLicenses() { return inactiveLicenses; }
        public void setInactiveLicenses(long inactiveLicenses) { this.inactiveLicenses = inactiveLicenses; }
        
        public long getTrialLicenses() { return trialLicenses; }
        public void setTrialLicenses(long trialLicenses) { this.trialLicenses = trialLicenses; }
        
        public long getExpiredLicenses() { return expiredLicenses; }
        public void setExpiredLicenses(long expiredLicenses) { this.expiredLicenses = expiredLicenses; }
        
        public Map<String, Long> getLicensesByType() { return licensesByType; }
        public void setLicensesByType(Map<String, Long> licensesByType) { this.licensesByType = licensesByType; }
    }
}

