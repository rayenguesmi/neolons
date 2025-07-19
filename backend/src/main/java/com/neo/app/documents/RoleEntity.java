package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document(collection = "roles")
public class RoleEntity {

    @Id
    private String id;

    @Field("name")
    private String name; // ADMIN_GENERAL, CHEF_PROJET, EYA_EXECUTANTE, etc.

    @Field("display_name")
    private String displayName; // Nom d'affichage convivial

    @Field("description")
    private String description;

    @Field("permissions")
    private List<String> permissions; // Liste des permissions associées

    @Field("is_active")
    private Boolean isActive;

    @Field("is_system_role")
    private Boolean isSystemRole; // Rôle système non modifiable

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    @Field("created_by")
    private String createdBy; // ID de l'utilisateur qui a créé le rôle

    // Constructeurs
    public RoleEntity() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.isSystemRole = false;
    }

    public RoleEntity(String name, String displayName, String description, List<String> permissions) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.permissions = permissions;
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystemRole() {
        return isSystemRole;
    }

    public void setIsSystemRole(Boolean isSystemRole) {
        this.isSystemRole = isSystemRole;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "RoleEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", permissions=" + permissions +
                ", isActive=" + isActive +
                ", isSystemRole=" + isSystemRole +
                '}';
    }
}

