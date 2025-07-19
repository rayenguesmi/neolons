package com.neo.app.service;

import com.neo.app.documents.PermissionEntity;
import com.neo.app.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Récupère toutes les permissions
     */
    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Récupère toutes les permissions actives
     */
    public List<PermissionEntity> getActivePermissions() {
        return permissionRepository.findByIsActiveTrue();
    }

    /**
     * Récupère une permission par son ID
     */
    public Optional<PermissionEntity> getPermissionById(String id) {
        return permissionRepository.findById(id);
    }

    /**
     * Récupère une permission par son nom
     */
    public Optional<PermissionEntity> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    /**
     * Récupère les permissions par module
     */
    public List<PermissionEntity> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    /**
     * Récupère les permissions par ressource
     */
    public List<PermissionEntity> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    /**
     * Crée une nouvelle permission
     */
    public PermissionEntity createPermission(PermissionEntity permission, String createdBy) {
        // Vérifier si le nom de la permission existe déjà
        if (permissionRepository.existsByName(permission.getName())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà: " + permission.getName());
        }

        // Vérifier si une permission existe déjà pour cette combinaison module/ressource/action
        if (permissionRepository.existsByModuleAndResourceAndAction(
                permission.getModule(), permission.getResource(), permission.getAction())) {
            throw new RuntimeException("Une permission existe déjà pour cette combinaison: " + 
                permission.getModule() + "/" + permission.getResource() + "/" + permission.getAction());
        }

        permission.setCreatedAt(new Date());
        permission.setUpdatedAt(new Date());

        PermissionEntity savedPermission = permissionRepository.save(permission);

        // Log d'audit
        auditLogService.logAction(
            createdBy,
            "CREATE_PERMISSION",
            "permissions",
            savedPermission.getId(),
            null,
            "Création de la permission: " + savedPermission.getName()
        );

        return savedPermission;
    }

    /**
     * Met à jour une permission existante
     */
    public PermissionEntity updatePermission(String id, PermissionEntity updatedPermission, String updatedBy) {
        Optional<PermissionEntity> existingPermissionOpt = permissionRepository.findById(id);
        if (!existingPermissionOpt.isPresent()) {
            throw new RuntimeException("Permission non trouvée avec l'ID: " + id);
        }

        PermissionEntity existingPermission = existingPermissionOpt.get();

        // Vérifier si c'est une permission système (non modifiable)
        if (existingPermission.getIsSystemPermission()) {
            throw new RuntimeException("Impossible de modifier une permission système: " + existingPermission.getName());
        }

        // Vérifier si le nouveau nom existe déjà (sauf pour la permission actuelle)
        if (!existingPermission.getName().equals(updatedPermission.getName()) && 
            permissionRepository.existsByName(updatedPermission.getName())) {
            throw new RuntimeException("Une permission avec ce nom existe déjà: " + updatedPermission.getName());
        }

        String oldValues = existingPermission.toString();

        // Mettre à jour les champs
        existingPermission.setName(updatedPermission.getName());
        existingPermission.setDisplayName(updatedPermission.getDisplayName());
        existingPermission.setDescription(updatedPermission.getDescription());
        existingPermission.setModule(updatedPermission.getModule());
        existingPermission.setResource(updatedPermission.getResource());
        existingPermission.setAction(updatedPermission.getAction());
        existingPermission.setIsActive(updatedPermission.getIsActive());
        existingPermission.setUpdatedAt(new Date());

        PermissionEntity savedPermission = permissionRepository.save(existingPermission);

        // Log d'audit
        auditLogService.logAction(
            updatedBy,
            "UPDATE_PERMISSION",
            "permissions",
            savedPermission.getId(),
            oldValues,
            "Modification de la permission: " + savedPermission.getName()
        );

        return savedPermission;
    }

    /**
     * Active ou désactive une permission
     */
    public PermissionEntity togglePermissionStatus(String id, String updatedBy) {
        Optional<PermissionEntity> permissionOpt = permissionRepository.findById(id);
        if (!permissionOpt.isPresent()) {
            throw new RuntimeException("Permission non trouvée avec l'ID: " + id);
        }

        PermissionEntity permission = permissionOpt.get();

        // Vérifier si c'est une permission système (non modifiable)
        if (permission.getIsSystemPermission()) {
            throw new RuntimeException("Impossible de modifier le statut d'une permission système: " + permission.getName());
        }

        boolean oldStatus = permission.getIsActive();
        permission.setIsActive(!oldStatus);
        permission.setUpdatedAt(new Date());

        PermissionEntity savedPermission = permissionRepository.save(permission);

        // Log d'audit
        String action = savedPermission.getIsActive() ? "ACTIVATE_PERMISSION" : "DEACTIVATE_PERMISSION";
        auditLogService.logAction(
            updatedBy,
            action,
            "permissions",
            savedPermission.getId(),
            "isActive: " + oldStatus,
            "Changement de statut de la permission: " + savedPermission.getName() + " -> " + savedPermission.getIsActive()
        );

        return savedPermission;
    }

    /**
     * Supprime une permission
     */
    public void deletePermission(String id, String deletedBy) {
        Optional<PermissionEntity> permissionOpt = permissionRepository.findById(id);
        if (!permissionOpt.isPresent()) {
            throw new RuntimeException("Permission non trouvée avec l'ID: " + id);
        }

        PermissionEntity permission = permissionOpt.get();

        // Vérifier si c'est une permission système (non supprimable)
        if (permission.getIsSystemPermission()) {
            throw new RuntimeException("Impossible de supprimer une permission système: " + permission.getName());
        }

        // TODO: Vérifier si la permission est utilisée par des rôles
        // Si oui, empêcher la suppression ou proposer une migration

        permissionRepository.deleteById(id);

        // Log d'audit
        auditLogService.logAction(
            deletedBy,
            "DELETE_PERMISSION",
            "permissions",
            id,
            permission.toString(),
            "Suppression de la permission: " + permission.getName()
        );
    }

    /**
     * Recherche des permissions par nom d'affichage
     */
    public List<PermissionEntity> searchPermissionsByDisplayName(String searchTerm) {
        return permissionRepository.findByDisplayNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Récupère les permissions système
     */
    public List<PermissionEntity> getSystemPermissions() {
        return permissionRepository.findByIsSystemPermissionTrue();
    }

    /**
     * Récupère les permissions non-système (modifiables)
     */
    public List<PermissionEntity> getCustomPermissions() {
        return permissionRepository.findByIsSystemPermissionFalse();
    }

    /**
     * Initialise les permissions système par défaut
     */
    public void initializeDefaultPermissions() {
        // Permissions de gestion des utilisateurs
        createSystemPermissionIfNotExists("VIEW_USERS", "Voir les utilisateurs", 
            "Consulter la liste des utilisateurs", "USER_MANAGEMENT", "users", "read");
        createSystemPermissionIfNotExists("CREATE_USER", "Créer un utilisateur", 
            "Créer de nouveaux utilisateurs", "USER_MANAGEMENT", "users", "create");
        createSystemPermissionIfNotExists("UPDATE_USER", "Modifier un utilisateur", 
            "Modifier les informations des utilisateurs", "USER_MANAGEMENT", "users", "update");
        createSystemPermissionIfNotExists("DELETE_USER", "Supprimer un utilisateur", 
            "Supprimer des utilisateurs", "USER_MANAGEMENT", "users", "delete");
        createSystemPermissionIfNotExists("ASSIGN_ROLES", "Assigner des rôles", 
            "Assigner des rôles aux utilisateurs", "USER_MANAGEMENT", "users", "assign");

        // Permissions de gestion des projets
        createSystemPermissionIfNotExists("VIEW_PROJECTS", "Voir les projets", 
            "Consulter la liste des projets", "PROJECT_MANAGEMENT", "projects", "read");
        createSystemPermissionIfNotExists("CREATE_PROJECT", "Créer un projet", 
            "Créer de nouveaux projets", "PROJECT_MANAGEMENT", "projects", "create");
        createSystemPermissionIfNotExists("UPDATE_PROJECT", "Modifier un projet", 
            "Modifier les informations des projets", "PROJECT_MANAGEMENT", "projects", "update");
        createSystemPermissionIfNotExists("DELETE_PROJECT", "Supprimer un projet", 
            "Supprimer des projets", "PROJECT_MANAGEMENT", "projects", "delete");
        createSystemPermissionIfNotExists("ASSIGN_USERS", "Assigner des utilisateurs", 
            "Assigner des utilisateurs aux projets", "PROJECT_MANAGEMENT", "projects", "assign");

        // Permissions de gestion des licences
        createSystemPermissionIfNotExists("VIEW_LICENSES", "Voir les licences", 
            "Consulter la liste des licences", "LICENSE_MANAGEMENT", "licenses", "read");
        createSystemPermissionIfNotExists("CREATE_LICENSE", "Créer une licence", 
            "Créer de nouvelles licences", "LICENSE_MANAGEMENT", "licenses", "create");
        createSystemPermissionIfNotExists("UPDATE_LICENSE", "Modifier une licence", 
            "Modifier les informations des licences", "LICENSE_MANAGEMENT", "licenses", "update");
        createSystemPermissionIfNotExists("DELETE_LICENSE", "Supprimer une licence", 
            "Supprimer des licences", "LICENSE_MANAGEMENT", "licenses", "delete");

        // Permissions de gestion des documents
        createSystemPermissionIfNotExists("VIEW_DOCUMENTS", "Voir les documents", 
            "Consulter la liste des documents", "DOCUMENT_MANAGEMENT", "documents", "read");
        createSystemPermissionIfNotExists("UPLOAD_DOCUMENT", "Télécharger un document", 
            "Télécharger de nouveaux documents", "DOCUMENT_MANAGEMENT", "documents", "create");
        createSystemPermissionIfNotExists("UPDATE_DOCUMENT", "Modifier un document", 
            "Modifier les informations des documents", "DOCUMENT_MANAGEMENT", "documents", "update");
        createSystemPermissionIfNotExists("DELETE_DOCUMENT", "Supprimer un document", 
            "Supprimer des documents", "DOCUMENT_MANAGEMENT", "documents", "delete");

        // Permissions d'exécution
        createSystemPermissionIfNotExists("UPDATE_EXECUTION", "Mettre à jour l'exécution", 
            "Mettre à jour le statut d'exécution des tâches", "EXECUTION_MANAGEMENT", "executions", "update");
        createSystemPermissionIfNotExists("VIEW_EXECUTION", "Voir l'exécution", 
            "Consulter le statut d'exécution des tâches", "EXECUTION_MANAGEMENT", "executions", "read");

        // Permissions d'administration
        createSystemPermissionIfNotExists("VIEW_AUDIT_LOGS", "Voir les logs d'audit", 
            "Consulter les logs d'audit du système", "SYSTEM_ADMINISTRATION", "audit_logs", "read");
        createSystemPermissionIfNotExists("MANAGE_ROLES", "Gérer les rôles", 
            "Créer, modifier et supprimer des rôles", "SYSTEM_ADMINISTRATION", "roles", "manage");
        createSystemPermissionIfNotExists("MANAGE_PERMISSIONS", "Gérer les permissions", 
            "Créer, modifier et supprimer des permissions", "SYSTEM_ADMINISTRATION", "permissions", "manage");
    }

    private void createSystemPermissionIfNotExists(String name, String displayName, String description, 
                                                 String module, String resource, String action) {
        if (!permissionRepository.existsByName(name)) {
            PermissionEntity systemPermission = new PermissionEntity(name, displayName, description, module, resource, action);
            systemPermission.setIsSystemPermission(true);
            permissionRepository.save(systemPermission);
        }
    }
}

