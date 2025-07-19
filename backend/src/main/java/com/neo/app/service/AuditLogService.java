package com.neo.app.service;

import com.neo.app.documents.AuditLogEntity;
import com.neo.app.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

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

