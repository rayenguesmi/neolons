package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "permissions")
public class PermissionEntity {

    @Id
    private String id;

    @Field("name")
    private String name; // CREATE_USER, UPDATE_USER, DELETE_USER, VIEW_USERS, etc.

    @Field("display_name")
    private String displayName; // Nom d'affichage convivial

    @Field("description")
    private String description;

    @Field("module")
    private String module; // USER_MANAGEMENT, PROJECT_MANAGEMENT, LICENSE_MANAGEMENT, etc.

    @Field("resource")
    private String resource; // users, projects, licenses, etc.

    @Field("action")
    private String action; // create, read, update, delete, execute, etc.

    @Field("is_active")
    private Boolean isActive;

    @Field("is_system_permission")
    private Boolean isSystemPermission; // Permission système non modifiable

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    // Constructeurs
    public PermissionEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.isSystemPermission = false;
    }

    public PermissionEntity(String name, String displayName, String description, String module, String resource, String action) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.module = module;
        this.resource = resource;
        this.action = action;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystemPermission() {
        return isSystemPermission;
    }

    public void setIsSystemPermission(Boolean isSystemPermission) {
        this.isSystemPermission = isSystemPermission;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PermissionEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", module='" + module + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

