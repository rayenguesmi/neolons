package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

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

    // Constructeurs
    public AuditLogEntity() {
        this.timestamp = new Date();
        this.success = true;
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
    }

    // Méthodes utilitaires
    public static AuditLogEntity createLoginLog(String userId, String username, String ipAddress, String userAgent) {
        AuditLogEntity log = new AuditLogEntity(userId, username, "LOGIN", "USER", userId, "User logged in");
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return log;
    }

    public static AuditLogEntity createLogoutLog(String userId, String username, String sessionId) {
        AuditLogEntity log = new AuditLogEntity(userId, username, "LOGOUT", "USER", userId, "User logged out");
        log.setSessionId(sessionId);
        return log;
    }

    public static AuditLogEntity createResourceLog(String userId, String username, String action,
                                                   String resourceType, String resourceId, String details) {
        return new AuditLogEntity(userId, username, action, resourceType, resourceId, details);
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
}

