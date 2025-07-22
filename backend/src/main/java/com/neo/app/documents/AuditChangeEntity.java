package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "audit_changes")
public class AuditChangeEntity {

    @Id
    private String id;

    @Field("audit_log_id")
    private String auditLogId;

    @Field("field_name")
    private String fieldName;

    @Field("old_value")
    private String oldValue;

    @Field("new_value")
    private String newValue;

    @Field("data_type")
    private String dataType;

    @Field("timestamp")
    private Date timestamp;

    // Constructeurs
    public AuditChangeEntity() {
        this.timestamp = new Date();
    }

    public AuditChangeEntity(String auditLogId, String fieldName, String oldValue, String newValue, String dataType) {
        this();
        this.auditLogId = auditLogId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.dataType = dataType;
    }

    // Méthodes utilitaires
    public static AuditChangeEntity createFieldChange(String auditLogId, String fieldName, 
                                                      Object oldValue, Object newValue) {
        String oldStr = oldValue != null ? oldValue.toString() : null;
        String newStr = newValue != null ? newValue.toString() : null;
        String dataType = determineDataType(newValue != null ? newValue : oldValue);
        
        return new AuditChangeEntity(auditLogId, fieldName, oldStr, newStr, dataType);
    }

    public static AuditChangeEntity createJsonFieldChange(String auditLogId, String fieldName, 
                                                          String oldJson, String newJson) {
        return new AuditChangeEntity(auditLogId, fieldName, oldJson, newJson, "json");
    }

    private static String determineDataType(Object value) {
        if (value == null) return "string";
        
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Number) return "number";
        if (value instanceof Date) return "date";
        if (value instanceof java.util.Collection) return "array";
        if (value.toString().startsWith("{") || value.toString().startsWith("[")) return "json";
        
        return "string";
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(String auditLogId) {
        this.auditLogId = auditLogId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

