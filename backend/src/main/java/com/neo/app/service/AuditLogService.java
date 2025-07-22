package com.neo.app.service;

import com.neo.app.documents.AuditLogEntity;
import com.neo.app.documents.AuditChangeEntity;
import com.neo.app.repository.AuditLogRepository;
import com.neo.app.repository.AuditChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditChangeRepository auditChangeRepository;

    // Enregistrement d'une action d'audit avec tracking des modifications
    public AuditLogEntity logActionWithChanges(String userId, String username, String action,
                                               String resourceType, String resourceId, String details,
                                               Map<String, Object> oldValues, Map<String, Object> newValues) {
        AuditLogEntity auditLog = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        
        // Calculer le niveau de risque basé sur les modifications
        if (oldValues != null && newValues != null) {
            auditLog.setChangesCount(calculateChangesCount(oldValues, newValues));
            auditLog.setRiskLevel(calculateRiskLevelFromChanges(action, resourceType, oldValues, newValues));
        }
        
        // Sauvegarder le log d'audit
        AuditLogEntity savedLog = auditLogRepository.save(auditLog);
        
        // Enregistrer les modifications détaillées
        if (oldValues != null && newValues != null && "UPDATE".equals(action)) {
            saveDetailedChanges(savedLog.getId(), oldValues, newValues);
        }
        
        return savedLog;
    }

    // Enregistrement d'une action d'audit (méthode simplifiée)
    public AuditLogEntity logAction(String userId, String action, String resourceType, 
                                    String resourceId, String oldValues, String details) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUserId(userId);
        auditLog.setUsername(userId); // TODO: Récupérer le vrai nom d'utilisateur
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(new Date());
        auditLog.setSuccess(true);
        
        return auditLogRepository.save(auditLog);
    }

    // Enregistrement d'une action d'audit avec informations de session
    public AuditLogEntity logAction(String userId, String username, String action,
                                    String resourceType, String resourceId, String details,
                                    String ipAddress, String userAgent, String sessionId) {
        AuditLogEntity auditLog = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setSessionId(sessionId);
        return auditLogRepository.save(auditLog);
    }

    // Enregistrement d'une action d'audit avec projet
    public AuditLogEntity logActionWithProject(String userId, String username, String action,
                                               String resourceType, String resourceId, String details,
                                               String projectId) {
        AuditLogEntity auditLog = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        auditLog.setProjectId(projectId);
        return auditLogRepository.save(auditLog);
    }

    // Enregistrement d'une action échouée
    public AuditLogEntity logFailedAction(String userId, String username, String action,
                                          String resourceType, String resourceId, String details,
                                          String errorMessage) {
        AuditLogEntity auditLog = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        auditLog.setSuccess(false);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setRiskLevel("MEDIUM"); // Les échecs sont considérés comme risque moyen
        return auditLogRepository.save(auditLog);
    }

    // Enregistrement de connexion
    public AuditLogEntity logLogin(String userId, String username, String ipAddress, String userAgent) {
        AuditLogEntity loginLog = AuditLogEntity.createLoginLog(userId, username, ipAddress, userAgent);
        return auditLogRepository.save(loginLog);
    }

    // Enregistrement de déconnexion
    public AuditLogEntity logLogout(String userId, String username, String sessionId) {
        AuditLogEntity logoutLog = AuditLogEntity.createLogoutLog(userId, username, sessionId);
        return auditLogRepository.save(logoutLog);
    }

    // Récupération des modifications pour un log d'audit
    public List<AuditChangeEntity> getLogChanges(String auditLogId) {
        return auditChangeRepository.findByAuditLogId(auditLogId);
    }

    // Récupération des logs par utilisateur
    public List<AuditLogEntity> getLogsByUser(String userId) {
        return auditLogRepository.findByUserId(userId);
    }

    // Récupération des logs par utilisateur avec pagination
    public Page<AuditLogEntity> getLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    // Récupération des logs par action
    public List<AuditLogEntity> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    // Récupération des logs par type de ressource
    public List<AuditLogEntity> getLogsByResourceType(String resourceType) {
        return auditLogRepository.findByResourceType(resourceType);
    }

    // Récupération des logs par ressource spécifique
    public List<AuditLogEntity> getLogsByResource(String resourceType, String resourceId) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
    }

    // Récupération des logs par projet
    public List<AuditLogEntity> getLogsByProject(String projectId) {
        return auditLogRepository.findByProjectId(projectId);
    }

    // Récupération des logs par projet avec pagination
    public Page<AuditLogEntity> getLogsByProject(String projectId, Pageable pageable) {
        return auditLogRepository.findByProjectId(projectId, pageable);
    }

    // Récupération des logs par période
    public List<AuditLogEntity> getLogsByPeriod(Date startDate, Date endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }

    // Récupération des logs par période avec pagination
    public Page<AuditLogEntity> getLogsByPeriod(Date startDate, Date endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    // Récupération des logs par utilisateur et période
    public List<AuditLogEntity> getLogsByUserAndPeriod(String userId, Date startDate, Date endDate) {
        return auditLogRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);
    }

    // Récupération des logs par action et période
    public List<AuditLogEntity> getLogsByActionAndPeriod(String action, Date startDate, Date endDate) {
        return auditLogRepository.findByActionAndTimestampBetween(action, startDate, endDate);
    }

    // Récupération des logs par projet et période
    public List<AuditLogEntity> getLogsByProjectAndPeriod(String projectId, Date startDate, Date endDate) {
        return auditLogRepository.findByProjectIdAndTimestampBetween(projectId, startDate, endDate);
    }

    // Récupération des actions échouées
    public List<AuditLogEntity> getFailedActions() {
        return auditLogRepository.findBySuccess(false);
    }

    // Récupération des actions échouées par période
    public List<AuditLogEntity> getFailedActionsByPeriod(Date startDate, Date endDate) {
        return auditLogRepository.findFailedActionsBetween(startDate, endDate);
    }

    // Récupération des logs de connexion par utilisateur
    public List<AuditLogEntity> getLoginLogsByUser(String userId) {
        return auditLogRepository.findLoginLogsByUserId(userId);
    }

    // Récupération des logs par adresse IP et période
    public List<AuditLogEntity> getLogsByIpAndPeriod(String ipAddress, Date startDate, Date endDate) {
        return auditLogRepository.findByIpAddressAndTimestampBetween(ipAddress, startDate, endDate);
    }

    // Récupération des logs par niveau de risque
    public List<AuditLogEntity> getLogsByRiskLevel(String riskLevel) {
        return auditLogRepository.findByRiskLevel(riskLevel);
    }

    // Statistiques - Nombre d'actions par utilisateur
    public long countActionsByUser(String userId) {
        return auditLogRepository.countByUserId(userId);
    }

    // Statistiques - Nombre d'actions par type et période
    public long countActionsByTypeAndPeriod(String action, Date startDate, Date endDate) {
        return auditLogRepository.countByActionAndTimestampBetween(action, startDate, endDate);
    }

    // Statistiques - Nombre d'actions échouées par période
    public long countFailedActionsByPeriod(Date startDate, Date endDate) {
        return auditLogRepository.countFailedActionsBetween(startDate, endDate);
    }

    // Statistiques - Nombre d'actions par projet et période
    public long countActionsByProjectAndPeriod(String projectId, Date startDate, Date endDate) {
        return auditLogRepository.countByProjectIdAndTimestampBetween(projectId, startDate, endDate);
    }

    // Récupération de tous les logs
    public List<AuditLogEntity> getAllLogs() {
        return auditLogRepository.findAll();
    }

    // Récupération d'un log par ID
    public Optional<AuditLogEntity> getLogById(String logId) {
        return auditLogRepository.findById(logId);
    }

    // Suppression des logs anciens (pour maintenance)
    public void deleteOldLogs(Date beforeDate) {
        List<AuditLogEntity> oldLogs = auditLogRepository.findByTimestampBetween(
                new Date(0), beforeDate);
        
        // Supprimer les modifications associées
        oldLogs.forEach(log -> auditChangeRepository.deleteByAuditLogId(log.getId()));
        
        // Supprimer les logs
        auditLogRepository.deleteAll(oldLogs);

        // Log de la maintenance
        logAction("system", "system", "MAINTENANCE", "AUDIT_LOG", null,
                "Deleted " + oldLogs.size() + " old audit logs before " + beforeDate.toString());
    }

    // Génération de rapport d'activité
    public AuditReport generateActivityReport(Date startDate, Date endDate) {
        List<AuditLogEntity> logs = getLogsByPeriod(startDate, endDate);

        AuditReport report = new AuditReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalActions(logs.size());

        // Compter les actions par type
        long loginCount = logs.stream().filter(log -> "LOGIN".equals(log.getAction())).count();
        long createCount = logs.stream().filter(log -> "CREATE".equals(log.getAction())).count();
        long updateCount = logs.stream().filter(log -> "UPDATE".equals(log.getAction())).count();
        long deleteCount = logs.stream().filter(log -> "DELETE".equals(log.getAction())).count();
        long failedCount = logs.stream().filter(log -> !log.getSuccess()).count();

        report.setLoginCount(loginCount);
        report.setCreateCount(createCount);
        report.setUpdateCount(updateCount);
        report.setDeleteCount(deleteCount);
        report.setFailedCount(failedCount);

        return report;
    }

    // Initialisation de données d'audit de test avec nouvelles fonctionnalités
    public void initializeTestAuditData() {
        // Supprimer les données existantes pour éviter les doublons
        auditLogRepository.deleteAll();
        auditChangeRepository.deleteAll();

        // Créer des données d'audit réalistes avec niveaux de risque
        String[] userIds = {"user1", "user2", "user3", "admin", "manager"};
        String[] usernames = {"Jean Dupont", "Marie Martin", "Pierre Durand", "Admin System", "Manager Test"};
        String[] actions = {"LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "VIEW"};
        String[] resourceTypes = {"USER", "DOCUMENT", "LICENCE", "ROLE", "PROJECT", "CAMPAGNE"};
        String[] ipAddresses = {"192.168.1.100", "192.168.1.101", "10.0.0.50", "172.16.0.10"};
        String[] riskLevels = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};

        // Générer des logs pour les 30 derniers jours
        Date now = new Date();
        long thirtyDaysAgo = now.getTime() - (30L * 24 * 60 * 60 * 1000);

        for (int i = 0; i < 150; i++) {
            AuditLogEntity log = new AuditLogEntity();
            
            // Sélection aléatoire des données
            int userIndex = (int) (Math.random() * userIds.length);
            log.setUserId(userIds[userIndex]);
            log.setUsername(usernames[userIndex]);
            
            String action = actions[(int) (Math.random() * actions.length)];
            log.setAction(action);
            
            String resourceType = resourceTypes[(int) (Math.random() * resourceTypes.length)];
            log.setResourceType(resourceType);
            log.setResourceId("resource_" + (int) (Math.random() * 100));
            log.setIpAddress(ipAddresses[(int) (Math.random() * ipAddresses.length)]);
            log.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            log.setSessionId("session_" + (int) (Math.random() * 1000));
            
            // Date aléatoire dans les 30 derniers jours
            long randomTime = thirtyDaysAgo + (long) (Math.random() * (now.getTime() - thirtyDaysAgo));
            log.setTimestamp(new Date(randomTime));
            
            // 90% de succès, 10% d'échec
            boolean success = Math.random() > 0.1;
            log.setSuccess(success);
            
            // Définir le niveau de risque
            if ("DELETE".equals(action)) {
                log.setRiskLevel("HIGH");
                log.addTag("deletion");
            } else if ("UPDATE".equals(action) && ("USER".equals(resourceType) || "ROLE".equals(resourceType))) {
                log.setRiskLevel("MEDIUM");
                log.addTag("sensitive-update");
            } else {
                log.setRiskLevel("LOW");
            }
            
            if (!success) {
                log.setErrorMessage("Erreur d'autorisation ou de validation");
                log.setDetails("Action échouée: " + log.getAction() + " sur " + log.getResourceType());
                log.setRiskLevel("MEDIUM"); // Les échecs augmentent le risque
                log.addTag("failed-action");
            } else {
                log.setDetails("Action réussie: " + log.getAction() + " sur " + log.getResourceType() + " " + log.getResourceId());
            }
            
            // Ajouter un projet pour certains logs
            if (Math.random() > 0.5) {
                log.setProjectId("project_" + (int) (Math.random() * 10));
            }
            
            // Ajouter des métadonnées
            log.addMetadata("browser", "Chrome");
            log.addMetadata("os", "Windows 10");
            
            // Ajouter des flags de conformité pour certaines actions
            if ("DELETE".equals(action) || ("UPDATE".equals(action) && "USER".equals(resourceType))) {
                log.addComplianceFlag("GDPR");
            }
            
            AuditLogEntity savedLog = auditLogRepository.save(log);
            
            // Créer des modifications détaillées pour les actions UPDATE
            if ("UPDATE".equals(action) && success) {
                createSampleChanges(savedLog.getId());
                log.setChangesCount((int) (Math.random() * 5) + 1);
                auditLogRepository.save(log);
            }
        }
        
        // Ajouter quelques logs récents pour l'activité en temps réel
        for (int i = 0; i < 10; i++) {
            AuditLogEntity recentLog = new AuditLogEntity();
            int userIndex = (int) (Math.random() * userIds.length);
            recentLog.setUserId(userIds[userIndex]);
            recentLog.setUsername(usernames[userIndex]);
            recentLog.setAction(actions[(int) (Math.random() * actions.length)]);
            recentLog.setResourceType(resourceTypes[(int) (Math.random() * resourceTypes.length)]);
            recentLog.setResourceId("resource_" + (int) (Math.random() * 100));
            recentLog.setIpAddress(ipAddresses[(int) (Math.random() * ipAddresses.length)]);
            recentLog.setSuccess(true);
            recentLog.setDetails("Action récente: " + recentLog.getAction() + " sur " + recentLog.getResourceType());
            recentLog.setRiskLevel(riskLevels[(int) (Math.random() * riskLevels.length)]);
            
            // Logs des dernières heures
            long recentTime = now.getTime() - (long) (Math.random() * 6 * 60 * 60 * 1000); // 6 dernières heures
            recentLog.setTimestamp(new Date(recentTime));
            
            auditLogRepository.save(recentLog);
        }
    }

    // Helper method to get most frequent actions as a map
    private java.util.Map<String, Long> getMostFrequentActionsMap() {
        List<AuditLogEntity> allLogs = auditLogRepository.findAll();
        return allLogs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        AuditLogEntity::getAction,
                        java.util.stream.Collectors.counting()
                ));
    }

    // Helper method to get most accessed resource types as a map
    private java.util.Map<String, Long> getMostAccessedResourceTypesMap() {
        List<AuditLogEntity> allLogs = auditLogRepository.findAll();
        return allLogs.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        AuditLogEntity::getResourceType,
                        java.util.stream.Collectors.counting()
                ));
    }

    // Obtenir les statistiques du tableau de bord
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        // Statistiques générales
        stats.setTotalLogs(auditLogRepository.count());
        
        // Logs des dernières 24h
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        stats.setLogsLast24h(auditLogRepository.countByTimestampAfter(yesterday));
        
        // Logs échoués des dernières 24h
        stats.setFailedLogsLast24h(auditLogRepository.countFailedActionsBetween(yesterday, new Date()));
        
        // Utilisateurs actifs des dernières 24h
        stats.setActiveUsersLast24h(auditLogRepository.countDistinctUsersByTimestampAfter(yesterday));
        
        // Actions les plus fréquentes
        stats.setMostFrequentActions(getMostFrequentActionsMap());
        
        // Types de ressources les plus accédés
        stats.setMostAccessedResourceTypes(getMostAccessedResourceTypesMap());
        
        return stats;
    }

    // Méthodes privées utilitaires
    private void saveDetailedChanges(String auditLogId, Map<String, Object> oldValues, Map<String, Object> newValues) {
        for (String fieldName : newValues.keySet()) {
            Object oldValue = oldValues.get(fieldName);
            Object newValue = newValues.get(fieldName);
            
            // Ne sauvegarder que si les valeurs sont différentes
            if (!java.util.Objects.equals(oldValue, newValue)) {
                AuditChangeEntity change = AuditChangeEntity.createFieldChange(auditLogId, fieldName, oldValue, newValue);
                auditChangeRepository.save(change);
            }
        }
    }

    private int calculateChangesCount(Map<String, Object> oldValues, Map<String, Object> newValues) {
        int count = 0;
        for (String fieldName : newValues.keySet()) {
            Object oldValue = oldValues.get(fieldName);
            Object newValue = newValues.get(fieldName);
            if (!java.util.Objects.equals(oldValue, newValue)) {
                count++;
            }
        }
        return count;
    }

    private String calculateRiskLevelFromChanges(String action, String resourceType, Map<String, Object> oldValues, Map<String, Object> newValues) {
        // Logique avancée de calcul du risque basée sur les modifications
        if ("DELETE".equals(action)) {
            return "HIGH";
        }
        
        if ("UPDATE".equals(action)) {
            // Vérifier si des champs sensibles ont été modifiés
            String[] sensitiveFields = {"password", "email", "role", "permissions", "status"};
            for (String field : sensitiveFields) {
                if (newValues.containsKey(field) && !java.util.Objects.equals(oldValues.get(field), newValues.get(field))) {
                    return "HIGH";
                }
            }
            
            // Si beaucoup de champs ont été modifiés
            int changesCount = calculateChangesCount(oldValues, newValues);
            if (changesCount > 5) {
                return "MEDIUM";
            }
        }
        
        return "LOW";
    }

    private void createSampleChanges(String auditLogId) {
        // Créer des exemples de modifications pour les tests
        String[] fields = {"name", "email", "status", "description", "category"};
        String[] oldValues = {"Ancien nom", "ancien@email.com", "INACTIVE", "Ancienne description", "Ancienne catégorie"};
        String[] newValues = {"Nouveau nom", "nouveau@email.com", "ACTIVE", "Nouvelle description", "Nouvelle catégorie"};
        
        int numChanges = (int) (Math.random() * 3) + 1; // 1 à 3 modifications
        for (int i = 0; i < numChanges; i++) {
            AuditChangeEntity change = new AuditChangeEntity(auditLogId, fields[i], oldValues[i], newValues[i], "string");
            auditChangeRepository.save(change);
        }
    }

    // Classe interne pour le rapport d'audit
    public static class AuditReport {
        private Date startDate;
        private Date endDate;
        private long totalActions;
        private long loginCount;
        private long createCount;
        private long updateCount;
        private long deleteCount;
        private long failedCount;

        // Getters et Setters
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }

        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }

        public long getTotalActions() { return totalActions; }
        public void setTotalActions(long totalActions) { this.totalActions = totalActions; }

        public long getLoginCount() { return loginCount; }
        public void setLoginCount(long loginCount) { this.loginCount = loginCount; }

        public long getCreateCount() { return createCount; }
        public void setCreateCount(long createCount) { this.createCount = createCount; }

        public long getUpdateCount() { return updateCount; }
        public void setUpdateCount(long updateCount) { this.updateCount = updateCount; }

        public long getDeleteCount() { return deleteCount; }
        public void setDeleteCount(long deleteCount) { this.deleteCount = deleteCount; }

        public long getFailedCount() { return failedCount; }
        public void setFailedCount(long failedCount) { this.failedCount = failedCount; }
    }
}