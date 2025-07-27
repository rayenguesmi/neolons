package com.neo.app.service;

import com.neo.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CampagneRepository campagneRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Récupère toutes les statistiques du tableau de bord
     */
    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        
        allStats.put("userStats", getUserStats());
        allStats.put("licenseStats", getLicenseStats());
        allStats.put("auditStats", getAuditStats());
        allStats.put("documentStats", getDocumentStats());
        allStats.put("campaignStats", getCampaignStats());
        allStats.put("roleStats", getRoleStats());
        allStats.put("recentActivities", getRecentActivities(10));
        allStats.put("systemHealth", getSystemHealth());
        
        return allStats;
    }

    /**
     * Récupère les statistiques des utilisateurs
     */
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Nombre total d'utilisateurs
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // Utilisateurs actifs
        long activeUsers = userRepository.countByIsActive(true);
        stats.put("activeUsers", activeUsers);
        
        // Statistiques par rôle
        long administrators = userRepository.countByRolesContaining("ADMIN_GENERAL");
        long managers = userRepository.countByRolesContaining("CHEF_PROJET");
        long standardUsers = userRepository.countByRolesContaining("EYA_EXECUTANTE");
        
        stats.put("administrators", administrators);
        stats.put("managers", managers);
        stats.put("standardUsers", standardUsers);
        
        // Nouveaux utilisateurs ce mois
        Date startOfMonth = Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        stats.put("newUsersThisMonth", newUsersThisMonth);
        
        // Sessions actives aujourd'hui (basé sur lastLogin)
        Date startOfDay = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        long activeSessionsToday = userRepository.countByLastLoginAfter(startOfDay);
        stats.put("activeSessionsToday", activeSessionsToday);
        
        return stats;
    }

    /**
     * Récupère les statistiques des licences
     */
    public Map<String, Object> getLicenseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Nombre total de licences
        long totalLicenses = licenseRepository.count();
        stats.put("totalLicenses", totalLicenses);
        
        // Licences actives
        long activeLicenses = licenseRepository.countByIsActive(true);
        stats.put("activeLicenses", activeLicenses);
        
        // Licences expirant bientôt (dans les 30 prochains jours)
        Date thirtyDaysFromNow = Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date now = new Date();
        long expiringSoon = licenseRepository.countByExpiryDateBetweenAndIsActive(now, thirtyDaysFromNow, true);
        stats.put("expiringSoon", expiringSoon);
        
        // Licences expirées
        long expired = licenseRepository.countByExpiryDateBeforeAndIsActive(now, true);
        stats.put("expired", expired);
        
        // Taux d'utilisation
        double utilizationRate = totalLicenses > 0 ? (double) activeLicenses / totalLicenses * 100 : 0;
        stats.put("utilizationRate", Math.round(utilizationRate));
        
        return stats;
    }

    /**
     * Récupère les statistiques d'audit
     */
    public Map<String, Object> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Actions totales aujourd'hui
        Date startOfDay = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        long totalActionsToday = auditLogRepository.countByTimestampBetween(startOfDay, endOfDay);
        stats.put("totalActionsToday", totalActionsToday);
        
        // Actions par type
        long creations = auditLogRepository.countByActionAndTimestampBetween("CREATE", startOfDay, endOfDay);
        long modifications = auditLogRepository.countByActionAndTimestampBetween("UPDATE", startOfDay, endOfDay);
        long deletions = auditLogRepository.countByActionAndTimestampBetween("DELETE", startOfDay, endOfDay);
        long consultations = auditLogRepository.countByActionAndTimestampBetween("VIEW", startOfDay, endOfDay);
        
        stats.put("creations", creations);
        stats.put("modifications", modifications);
        stats.put("deletions", deletions);
        stats.put("consultations", consultations);
        
        // Alertes de sécurité (actions échouées)
        long securityAlerts = auditLogRepository.countBySuccessAndTimestampBetween(false, startOfDay, endOfDay);
        stats.put("securityAlerts", securityAlerts);
        
        // Tentatives de connexion échouées
        long failedLogins = auditLogRepository.countByActionAndSuccessAndTimestampBetween("LOGIN", false, startOfDay, endOfDay);
        stats.put("failedLogins", failedLogins);
        
        return stats;
    }

    /**
     * Récupère les statistiques des documents
     */
    public Map<String, Object> getDocumentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Nombre total de documents
        long totalDocuments = documentRepository.count();
        stats.put("totalDocuments", totalDocuments);
        
        // Documents créés ce mois
        Date startOfMonth = Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        long documentsThisMonth = documentRepository.countByCreatedAtAfter(startOfMonth);
        stats.put("documentsThisMonth", documentsThisMonth);
        
        return stats;
    }

    /**
     * Récupère les statistiques des campagnes
     */
    public Map<String, Object> getCampaignStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Nombre total de campagnes
        long totalCampaigns = campagneRepository.count();
        stats.put("totalCampaigns", totalCampaigns);
        
        // Campagnes actives
        long activeCampaigns = campagneRepository.countByStatut("ACTIVE");
        stats.put("activeCampaigns", activeCampaigns);
        
        return stats;
    }

    /**
     * Récupère les statistiques des rôles
     */
    public Map<String, Object> getRoleStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Nombre total de rôles
        long totalRoles = roleRepository.count();
        stats.put("totalRoles", totalRoles);
        
        // Rôles actifs
        long activeRoles = roleRepository.countByIsActive(true);
        stats.put("activeRoles", activeRoles);
        
        // Rôles personnalisés (non système)
        long customRoles = roleRepository.countByIsSystemRole(false);
        stats.put("customRoles", customRoles);
        
        return stats;
    }

    /**
     * Récupère les activités récentes
     */
    public Map<String, Object> getRecentActivities(int limit) {
        Map<String, Object> result = new HashMap<>();
        
        // Récupérer les logs d'audit récents
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<Map<String, Object>> activities = new ArrayList<>();
        
        auditLogRepository.findAll(pageRequest).forEach(log -> {
            Map<String, Object> activity = new HashMap<>();
            activity.put("user", log.getUsername() != null ? log.getUsername() : "Système");
            activity.put("action", getActionDescription(log.getAction(), log.getResourceType()));
            activity.put("resource", log.getResourceType());
            activity.put("time", log.getTimestamp());
            activity.put("status", log.getSuccess() ? "success" : "failed");
            activities.add(activity);
        });
        
        result.put("activities", activities);
        return result;
    }

    /**
     * Récupère les métriques système (simulées)
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Simulation des métriques système
        Random random = new Random();
        health.put("cpu", 30 + random.nextInt(30)); // 30-60%
        health.put("memory", 50 + random.nextInt(40)); // 50-90%
        health.put("storage", 20 + random.nextInt(20)); // 20-40%
        health.put("network", 85 + random.nextInt(15)); // 85-100%
        
        return health;
    }

    /**
     * Convertit l'action en description lisible
     */
    private String getActionDescription(String action, String resourceType) {
        switch (action) {
            case "CREATE":
                return "Création de " + resourceType.toLowerCase();
            case "UPDATE":
                return "Modification de " + resourceType.toLowerCase();
            case "DELETE":
                return "Suppression de " + resourceType.toLowerCase();
            case "VIEW":
                return "Consultation de " + resourceType.toLowerCase();
            case "LOGIN":
                return "Connexion";
            case "LOGOUT":
                return "Déconnexion";
            default:
                return action + " " + resourceType.toLowerCase();
        }
    }
}

