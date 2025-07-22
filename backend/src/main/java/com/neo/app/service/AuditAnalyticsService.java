package com.neo.app.service;

import com.neo.app.documents.AuditLogEntity;
import com.neo.app.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditAnalyticsService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Classe pour les données de série temporelle
    public static class TimeSeriesPoint {
        private Date timestamp;
        private long count;
        private long successCount;
        private long failureCount;

        public TimeSeriesPoint(Date timestamp, long count, long successCount, long failureCount) {
            this.timestamp = timestamp;
            this.count = count;
            this.successCount = successCount;
            this.failureCount = failureCount;
        }

        // Getters et setters
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        public long getFailureCount() { return failureCount; }
        public void setFailureCount(long failureCount) { this.failureCount = failureCount; }
    }

    // Classe pour l'activité des utilisateurs
    public static class UserActivity {
        private String userId;
        private String username;
        private long actionCount;
        private Date lastActivity;
        private double riskScore;

        public UserActivity(String userId, String username, long actionCount, Date lastActivity, double riskScore) {
            this.userId = userId;
            this.username = username;
            this.actionCount = actionCount;
            this.lastActivity = lastActivity;
            this.riskScore = riskScore;
        }

        // Getters et setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getActionCount() { return actionCount; }
        public void setActionCount(long actionCount) { this.actionCount = actionCount; }
        public Date getLastActivity() { return lastActivity; }
        public void setLastActivity(Date lastActivity) { this.lastActivity = lastActivity; }
        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    }

    // Classe pour les anomalies
    public static class AuditAnomaly {
        private String id;
        private String type;
        private String description;
        private String severity;
        private Date timestamp;
        private String userId;
        private String resourceType;
        private Map<String, Object> details;

        public AuditAnomaly(String type, String description, String severity, Date timestamp) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.timestamp = timestamp;
            this.details = new HashMap<>();
        }

        // Getters et setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    // Génération de données de série temporelle
    public List<TimeSeriesPoint> getTimeSeriesData(Date startDate, Date endDate, String granularity) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        
        // Grouper par période selon la granularité
        Map<Date, List<AuditLogEntity>> groupedLogs = groupLogsByPeriod(logs, granularity);
        
        return groupedLogs.entrySet().stream()
                .map(entry -> {
                    List<AuditLogEntity> periodLogs = entry.getValue();
                    long total = periodLogs.size();
                    long success = periodLogs.stream().mapToLong(log -> log.getSuccess() ? 1 : 0).sum();
                    long failure = total - success;
                    return new TimeSeriesPoint(entry.getKey(), total, success, failure);
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::getTimestamp))
                .collect(Collectors.toList());
    }

    // Obtenir les utilisateurs les plus actifs
    public List<UserActivity> getTopUsers(int limit, Date startDate, Date endDate) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        
        Map<String, List<AuditLogEntity>> userLogs = logs.stream()
                .collect(Collectors.groupingBy(AuditLogEntity::getUserId));
        
        return userLogs.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    List<AuditLogEntity> userLogList = entry.getValue();
                    String username = userLogList.get(0).getUsername();
                    long actionCount = userLogList.size();
                    Date lastActivity = userLogList.stream()
                            .map(AuditLogEntity::getTimestamp)
                            .max(Date::compareTo)
                            .orElse(new Date());
                    double riskScore = calculateUserRiskScore(userLogList);
                    
                    return new UserActivity(userId, username, actionCount, lastActivity, riskScore);
                })
                .sorted(Comparator.comparing(UserActivity::getActionCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Détecter les anomalies
    public List<AuditAnomaly> detectAnomalies(Date startDate, Date endDate) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        
        // Détecter les tentatives de connexion échouées multiples
        anomalies.addAll(detectFailedLoginAttempts(logs));
        
        // Détecter l'activité inhabituelle (volume élevé)
        anomalies.addAll(detectUnusualActivity(logs));
        
        // Détecter les actions suspectes
        anomalies.addAll(detectSuspiciousPatterns(logs));
        
        // Détecter les actions à haut risque
        anomalies.addAll(detectHighRiskActions(logs));
        
        return anomalies.stream()
                .sorted(Comparator.comparing(AuditAnomaly::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    // Obtenir la distribution des actions
    public Map<String, Long> getActionDistribution(Date startDate, Date endDate) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        return logs.stream()
                .collect(Collectors.groupingBy(AuditLogEntity::getAction, Collectors.counting()));
    }

    // Obtenir la distribution des types de ressources
    public Map<String, Long> getResourceTypeDistribution(Date startDate, Date endDate) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        return logs.stream()
                .collect(Collectors.groupingBy(AuditLogEntity::getResourceType, Collectors.counting()));
    }

    // Obtenir la distribution des niveaux de risque
    public Map<String, Long> getRiskLevelDistribution(Date startDate, Date endDate) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        return logs.stream()
                .filter(log -> log.getRiskLevel() != null)
                .collect(Collectors.groupingBy(AuditLogEntity::getRiskLevel, Collectors.counting()));
    }

    // Obtenir l'activité par heure
    public Map<Integer, Long> getHourlyActivity(Date startDate, Date endDate) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        
        return logs.stream()
                .collect(Collectors.groupingBy(
                        log -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(log.getTimestamp());
                            return cal.get(Calendar.HOUR_OF_DAY);
                        },
                        Collectors.counting()
                ));
    }

    // Méthodes privées utilitaires
    private Map<Date, List<AuditLogEntity>> groupLogsByPeriod(List<AuditLogEntity> logs, String granularity) {
        return logs.stream().collect(Collectors.groupingBy(log -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(log.getTimestamp());
            
            switch (granularity.toUpperCase()) {
                case "HOUR":
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    break;
                case "DAY":
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    break;
                case "WEEK":
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    break;
                default:
                    break;
            }
            return cal.getTime();
        }));
    }

    private double calculateUserRiskScore(List<AuditLogEntity> userLogs) {
        double score = 0.0;
        
        // Facteurs de risque
        long failedActions = userLogs.stream().mapToLong(log -> log.getSuccess() ? 0 : 1).sum();
        long deleteActions = userLogs.stream().mapToLong(log -> "DELETE".equals(log.getAction()) ? 1 : 0).sum();
        long highRiskActions = userLogs.stream().mapToLong(log -> "HIGH".equals(log.getRiskLevel()) || "CRITICAL".equals(log.getRiskLevel()) ? 1 : 0).sum();
        
        // Calcul du score (0-100)
        score += (failedActions * 10.0);
        score += (deleteActions * 15.0);
        score += (highRiskActions * 20.0);
        
        // Normaliser le score
        return Math.min(100.0, score);
    }

    private List<AuditAnomaly> detectFailedLoginAttempts(List<AuditLogEntity> logs) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        Map<String, List<AuditLogEntity>> loginsByUser = logs.stream()
                .filter(log -> "LOGIN".equals(log.getAction()) && !log.getSuccess())
                .collect(Collectors.groupingBy(AuditLogEntity::getUserId));
        
        loginsByUser.forEach((userId, failedLogins) -> {
            if (failedLogins.size() >= 5) { // Seuil configurable
                AuditAnomaly anomaly = new AuditAnomaly(
                        "FAILED_ATTEMPTS",
                        String.format("Utilisateur %s: %d tentatives de connexion échouées", userId, failedLogins.size()),
                        failedLogins.size() >= 10 ? "CRITICAL" : "HIGH",
                        failedLogins.get(failedLogins.size() - 1).getTimestamp()
                );
                anomaly.setUserId(userId);
                anomaly.getDetails().put("attemptCount", failedLogins.size());
                anomalies.add(anomaly);
            }
        });
        
        return anomalies;
    }

    private List<AuditAnomaly> detectUnusualActivity(List<AuditLogEntity> logs) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        Map<String, List<AuditLogEntity>> logsByUser = logs.stream()
                .collect(Collectors.groupingBy(AuditLogEntity::getUserId));
        
        // Calculer la moyenne d'activité
        double avgActivity = logsByUser.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0.0);
        
        logsByUser.forEach((userId, userLogs) -> {
            if (userLogs.size() > avgActivity * 3) { // 3x la moyenne
                AuditAnomaly anomaly = new AuditAnomaly(
                        "UNUSUAL_ACTIVITY",
                        String.format("Activité inhabituelle pour l'utilisateur %s: %d actions (moyenne: %.1f)", 
                                userId, userLogs.size(), avgActivity),
                        userLogs.size() > avgActivity * 5 ? "HIGH" : "MEDIUM",
                        userLogs.get(userLogs.size() - 1).getTimestamp()
                );
                anomaly.setUserId(userId);
                anomaly.getDetails().put("actionCount", userLogs.size());
                anomaly.getDetails().put("averageActivity", avgActivity);
                anomalies.add(anomaly);
            }
        });
        
        return anomalies;
    }

    private List<AuditAnomaly> detectSuspiciousPatterns(List<AuditLogEntity> logs) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        // Détecter les suppressions multiples
        Map<String, List<AuditLogEntity>> deletesByUser = logs.stream()
                .filter(log -> "DELETE".equals(log.getAction()))
                .collect(Collectors.groupingBy(AuditLogEntity::getUserId));
        
        deletesByUser.forEach((userId, deleteLogs) -> {
            if (deleteLogs.size() >= 10) { // Seuil configurable
                AuditAnomaly anomaly = new AuditAnomaly(
                        "SUSPICIOUS_PATTERN",
                        String.format("Pattern suspect: %d suppressions par l'utilisateur %s", deleteLogs.size(), userId),
                        "HIGH",
                        deleteLogs.get(deleteLogs.size() - 1).getTimestamp()
                );
                anomaly.setUserId(userId);
                anomaly.getDetails().put("deleteCount", deleteLogs.size());
                anomalies.add(anomaly);
            }
        });
        
        return anomalies;
    }

    private List<AuditAnomaly> detectHighRiskActions(List<AuditLogEntity> logs) {
        List<AuditAnomaly> anomalies = new ArrayList<>();
        
        List<AuditLogEntity> criticalLogs = logs.stream()
                .filter(log -> "CRITICAL".equals(log.getRiskLevel()))
                .collect(Collectors.toList());
        
        if (criticalLogs.size() > 5) { // Seuil configurable
            AuditAnomaly anomaly = new AuditAnomaly(
                    "HIGH_VOLUME",
                    String.format("Volume élevé d'actions critiques: %d actions", criticalLogs.size()),
                    "CRITICAL",
                    new Date()
            );
            anomaly.getDetails().put("criticalActionCount", criticalLogs.size());
            anomalies.add(anomaly);
        }
        
        return anomalies;
    }
}

