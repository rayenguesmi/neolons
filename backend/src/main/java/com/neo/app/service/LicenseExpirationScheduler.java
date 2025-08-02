package com.neo.app.service;

import com.neo.app.documents.LicenseEntity;
import com.neo.app.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LicenseExpirationScheduler {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private LicenseService licenseService;

    @Value("${app.license.notification.enabled:true}")
    private boolean notificationEnabled;

    @Value("${app.license.reminder.days:7,3,1}")
    private String reminderDaysConfig;

    /**
     * Vérifie les licences expirées toutes les heures
     * Cron: 0 0 * * * * (à chaque heure pile)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkExpiredLicenses() {
        if (!notificationEnabled) {
            return;
        }

        try {
            System.out.println("Début de la vérification des licences expirées...");
            
            // Récupérer toutes les licences actives
            List<LicenseEntity> activeLicenses = licenseRepository.findByIsActiveTrue();
            
            // Filtrer les licences expirées qui n'ont pas encore été notifiées aujourd'hui
            List<LicenseEntity> expiredLicenses = activeLicenses.stream()
                .filter(license -> license.isExpired() && shouldSendExpirationNotification(license))
                .collect(Collectors.toList());

            if (!expiredLicenses.isEmpty()) {
                System.out.println("Licences expirées trouvées: " + expiredLicenses.size());
                
                // Envoyer les notifications d'expiration
                for (LicenseEntity license : expiredLicenses) {
                    emailService.sendHtmlLicenseExpirationNotification(license);
                    
                    // Mettre à jour le statut de validation
                    license.setValidationStatus("EXPIRED");
                    license.setLastValidation(new Date());
                    licenseRepository.save(license);
                    
                    // Petite pause entre les envois
                    Thread.sleep(200);
                }
                
                System.out.println("Notifications d'expiration envoyées: " + expiredLicenses.size());
            } else {
                System.out.println("Aucune licence expirée nécessitant une notification");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des licences expirées: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifie les licences qui vont expirer bientôt
     * Cron: 0 0 9 * * * (tous les jours à 9h00)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkUpcomingExpirations() {
        if (!notificationEnabled) {
            return;
        }

        try {
            System.out.println("Début de la vérification des licences qui vont expirer...");
            
            // Récupérer les jours de rappel configurés
            int[] reminderDays = parseReminderDays();
            
            for (int days : reminderDays) {
                List<LicenseEntity> upcomingExpirations = findLicensesExpiringInDays(days);
                
                if (!upcomingExpirations.isEmpty()) {
                    System.out.println("Licences expirant dans " + days + " jour(s): " + upcomingExpirations.size());
                    
                    for (LicenseEntity license : upcomingExpirations) {
                        if (shouldSendReminderNotification(license, days)) {
                            emailService.sendLicenseExpirationReminder(license, days);
                            
                            // Marquer le rappel comme envoyé
                            markReminderSent(license, days);
                            
                            Thread.sleep(200);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des expirations à venir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nettoyage hebdomadaire des anciennes données
     * Cron: 0 0 2 * * SUN (tous les dimanches à 2h00)
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCleanup() {
        try {
            System.out.println("Début du nettoyage hebdomadaire...");
            
            // Désactiver les licences expirées depuis plus de 30 jours
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            Date thirtyDaysAgo = cal.getTime();
            
            List<LicenseEntity> oldExpiredLicenses = licenseRepository.findByIsActiveTrueAndExpiryDateBefore(thirtyDaysAgo);
            
            for (LicenseEntity license : oldExpiredLicenses) {
                license.setIsActive(false);
                license.setValidationStatus("SUSPENDED");
                license.setUpdatedAt(new Date());
                licenseRepository.save(license);
            }
            
            System.out.println("Nettoyage terminé: " + oldExpiredLicenses.size() + " licences suspendues");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérification manuelle des licences expirées
     */
    public void manualExpirationCheck() {
        System.out.println("Vérification manuelle déclenchée...");
        checkExpiredLicenses();
        checkUpcomingExpirations();
    }

    /**
     * Trouve les licences qui expirent dans un nombre de jours donné
     */
    private List<LicenseEntity> findLicensesExpiringInDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = cal.getTime();
        
        return licenseRepository.findByIsActiveTrueAndExpiryDateBetween(startDate, endDate);
    }

    /**
     * Vérifie si une notification d'expiration doit être envoyée
     */
    private boolean shouldSendExpirationNotification(LicenseEntity license) {
        // Vérifier si une notification a déjà été envoyée aujourd'hui
        if (license.getLastValidation() != null) {
            Calendar today = Calendar.getInstance();
            Calendar lastValidation = Calendar.getInstance();
            lastValidation.setTime(license.getLastValidation());
            
            return today.get(Calendar.DAY_OF_YEAR) != lastValidation.get(Calendar.DAY_OF_YEAR) ||
                   today.get(Calendar.YEAR) != lastValidation.get(Calendar.YEAR);
        }
        
        return true;
    }

    /**
     * Vérifie si un rappel doit être envoyé
     */
    private boolean shouldSendReminderNotification(LicenseEntity license, int days) {
        // Logique simple: envoyer le rappel si pas déjà envoyé aujourd'hui
        // Dans une implémentation plus avancée, on pourrait stocker les rappels envoyés
        return license.getLastValidation() == null || 
               !isSameDay(license.getLastValidation(), new Date());
    }

    /**
     * Marque un rappel comme envoyé
     */
    private void markReminderSent(LicenseEntity license, int days) {
        // Mettre à jour la date de dernière validation
        license.setLastValidation(new Date());
        
        // Ajouter une note sur le rappel envoyé
        String currentNotes = license.getNotes() != null ? license.getNotes() : "";
        String reminderNote = "Rappel " + days + " jour(s) envoyé le " + new Date();
        license.setNotes(currentNotes + "\n" + reminderNote);
        
        licenseRepository.save(license);
    }

    /**
     * Parse la configuration des jours de rappel
     */
    private int[] parseReminderDays() {
        try {
            String[] daysStr = reminderDaysConfig.split(",");
            int[] days = new int[daysStr.length];
            for (int i = 0; i < daysStr.length; i++) {
                days[i] = Integer.parseInt(daysStr[i].trim());
            }
            return days;
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing des jours de rappel, utilisation des valeurs par défaut");
            return new int[]{7, 3, 1}; // Valeurs par défaut
        }
    }

    /**
     * Vérifie si deux dates sont le même jour
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
               cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    /**
     * Obtient les statistiques des notifications
     */
    public String getNotificationStats() {
        try {
            List<LicenseEntity> allLicenses = licenseRepository.findAll();
            long totalLicenses = allLicenses.size();
            long activeLicenses = allLicenses.stream().filter(LicenseEntity::getIsActive).count();
            long expiredLicenses = allLicenses.stream().filter(LicenseEntity::isExpired).count();
            
            return String.format("Statistiques: Total=%d, Actives=%d, Expirées=%d", 
                                totalLicenses, activeLicenses, expiredLicenses);
        } catch (Exception e) {
            return "Erreur lors de la récupération des statistiques: " + e.getMessage();
        }
    }
}

