package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "audit_logs")
public class AuditLogEntity {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("username")
    private String username;

    @Field("action")
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW

    @Field("resource_type")
    private String resourceType; // USER, DOCUMENT, CAMPAGNE, LICENCE, etc.

    @Field("resource_id")
    private String resourceId;

    @Field("details")
    private String details; // JSON string avec les détails de l'action

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("timestamp")
    private Date timestamp;

    @Field("project_id")
    private String projectId;

    @Field("session_id")
    private String sessionId;

    @Field("success")
    private Boolean success;

    @Field("error_message")
    private String errorMessage;

    // Nouveaux champs pour les fonctionnalités avancées
    @Field("parent_log_id")
    private String parentLogId; // Pour chaîner les modifications

    @Field("metadata")
    private Map<String, Object> metadata; // Données contextuelles

    @Field("risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Field("tags")
    private List<String> tags; // Étiquettes pour catégorisation

    @Field("changes_count")
    private Integer changesCount; // Nombre de champs modifiés

    @Field("affected_users")
    private List<String> affectedUsers; // Utilisateurs impactés par l'action

    @Field("compliance_flags")
    private List<String> complianceFlags; // Flags de conformité (GDPR, SOX, etc.)

    // Constructeurs
    public AuditLogEntity() {
        this.timestamp = new Date();
        this.success = true;
        this.riskLevel = "LOW";
        this.changesCount = 0;
    }

    public AuditLogEntity(String userId, String username, String action,
                          String resourceType, String resourceId, String details) {
        this();
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.riskLevel = calculateRiskLevel(action, resourceType);
    }

    // Méthodes utilitaires
    public static AuditLogEntity createLoginLog(String userId, String username, String ipAddress, String userAgent) {
        AuditLogEntity log = new AuditLogEntity(userId, username, "LOGIN", "USER", userId, "User logged in");
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setRiskLevel("LOW");
        return log;
    }

    public static AuditLogEntity createLogoutLog(String userId, String username, String sessionId) {
        AuditLogEntity log = new AuditLogEntity(userId, username, "LOGOUT", "USER", userId, "User logged out");
        log.setSessionId(sessionId);
        log.setRiskLevel("LOW");
        return log;
    }

    public static AuditLogEntity createResourceLog(String userId, String username, String action,
                                                   String resourceType, String resourceId, String details) {
        AuditLogEntity log = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        log.setRiskLevel(calculateRiskLevel(action, resourceType));
        return log;
    }

    public static AuditLogEntity createHighRiskLog(String userId, String username, String action,
                                                   String resourceType, String resourceId, String details,
                                                   String reason) {
        AuditLogEntity log = new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
        log.setRiskLevel("HIGH");
        log.addTag("high-risk");
        log.addTag(reason);
        return log;
    }

    private static String calculateRiskLevel(String action, String resourceType) {
        // Logique de calcul du niveau de risque
        if ("DELETE".equals(action)) {
            return "HIGH";
        }
        if ("UPDATE".equals(action) && ("USER".equals(resourceType) || "ROLE".equals(resourceType))) {
            return "MEDIUM";
        }
        if ("CREATE".equals(action) && "USER".equals(resourceType)) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new java.util.ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public void addAffectedUser(String userId) {
        if (this.affectedUsers == null) {
            this.affectedUsers = new java.util.ArrayList<>();
        }
        if (!this.affectedUsers.contains(userId)) {
            this.affectedUsers.add(userId);
        }
    }

    public void addComplianceFlag(String flag) {
        if (this.complianceFlags == null) {
            this.complianceFlags = new java.util.ArrayList<>();
        }
        if (!this.complianceFlags.contains(flag)) {
            this.complianceFlags.add(flag);
        }
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getParentLogId() {
        return parentLogId;
    }

    public void setParentLogId(String parentLogId) {
        this.parentLogId = parentLogId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(Integer changesCount) {
        this.changesCount = changesCount;
    }

    public List<String> getAffectedUsers() {
        return affectedUsers;
    }

    public void setAffectedUsers(List<String> affectedUsers) {
        this.affectedUsers = affectedUsers;
    }

    public List<String> getComplianceFlags() {
        return complianceFlags;
    }

    public void setComplianceFlags(List<String> complianceFlags) {
        this.complianceFlags = complianceFlags;
    }
}

