package com.neo.app.service;

import com.neo.app.documents.RoleEntity;
import com.neo.app.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Récupère tous les rôles
     */
    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Récupère tous les rôles actifs
     */
    public List<RoleEntity> getActiveRoles() {
        return roleRepository.findByIsActiveTrue();
    }

    /**
     * Récupère un rôle par son ID
     */
    public Optional<RoleEntity> getRoleById(String id) {
        return roleRepository.findById(id);
    }

    /**
     * Récupère un rôle par son nom
     */
    public Optional<RoleEntity> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * Crée un nouveau rôle
     */
    public RoleEntity createRole(RoleEntity role, String createdBy) {
        // Vérifier si le nom du rôle existe déjà
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Un rôle avec ce nom existe déjà: " + role.getName());
        }

        role.setCreatedBy(createdBy);
        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());

        RoleEntity savedRole = roleRepository.save(role);

        // Log d'audit
        auditLogService.logAction(
            createdBy,
            "CREATE_ROLE",
            "roles",
            savedRole.getId(),
            null,
            "Création du rôle: " + savedRole.getName()
        );

        return savedRole;
    }

    /**
     * Met à jour un rôle existant
     */
    public RoleEntity updateRole(String id, RoleEntity updatedRole, String updatedBy) {
        Optional<RoleEntity> existingRoleOpt = roleRepository.findById(id);
        if (!existingRoleOpt.isPresent()) {
            throw new RuntimeException("Rôle non trouvé avec l'ID: " + id);
        }

        RoleEntity existingRole = existingRoleOpt.get();

        // Vérifier si c'est un rôle système (non modifiable)
        if (existingRole.getIsSystemRole()) {
            throw new RuntimeException("Impossible de modifier un rôle système: " + existingRole.getName());
        }

        // Vérifier si le nouveau nom existe déjà (sauf pour le rôle actuel)
        if (!existingRole.getName().equals(updatedRole.getName()) && 
            roleRepository.existsByName(updatedRole.getName())) {
            throw new RuntimeException("Un rôle avec ce nom existe déjà: " + updatedRole.getName());
        }

        String oldValues = existingRole.toString();

        // Mettre à jour les champs
        existingRole.setName(updatedRole.getName());
        existingRole.setDisplayName(updatedRole.getDisplayName());
        existingRole.setDescription(updatedRole.getDescription());
        existingRole.setPermissions(updatedRole.getPermissions());
        existingRole.setIsActive(updatedRole.getIsActive());
        existingRole.setUpdatedAt(new Date());

        RoleEntity savedRole = roleRepository.save(existingRole);

        // Log d'audit
        auditLogService.logAction(
            updatedBy,
            "UPDATE_ROLE",
            "roles",
            savedRole.getId(),
            oldValues,
            "Modification du rôle: " + savedRole.getName()
        );

        return savedRole;
    }

    /**
     * Active ou désactive un rôle
     */
    public RoleEntity toggleRoleStatus(String id, String updatedBy) {
        Optional<RoleEntity> roleOpt = roleRepository.findById(id);
        if (!roleOpt.isPresent()) {
            throw new RuntimeException("Rôle non trouvé avec l'ID: " + id);
        }

        RoleEntity role = roleOpt.get();

        // Vérifier si c'est un rôle système (non modifiable)
        if (role.getIsSystemRole()) {
            throw new RuntimeException("Impossible de modifier le statut d'un rôle système: " + role.getName());
        }

        boolean oldStatus = role.getIsActive();
        role.setIsActive(!oldStatus);
        role.setUpdatedAt(new Date());

        RoleEntity savedRole = roleRepository.save(role);

        // Log d'audit
        String action = savedRole.getIsActive() ? "ACTIVATE_ROLE" : "DEACTIVATE_ROLE";
        auditLogService.logAction(
            updatedBy,
            action,
            "roles",
            savedRole.getId(),
            "isActive: " + oldStatus,
            "Changement de statut du rôle: " + savedRole.getName() + " -> " + savedRole.getIsActive()
        );

        return savedRole;
    }

    /**
     * Supprime un rôle
     */
    public void deleteRole(String id, String deletedBy) {
        Optional<RoleEntity> roleOpt = roleRepository.findById(id);
        if (!roleOpt.isPresent()) {
            throw new RuntimeException("Rôle non trouvé avec l'ID: " + id);
        }

        RoleEntity role = roleOpt.get();

        // Vérifier si c'est un rôle système (non supprimable)
        if (role.getIsSystemRole()) {
            throw new RuntimeException("Impossible de supprimer un rôle système: " + role.getName());
        }

        // TODO: Vérifier si le rôle est utilisé par des utilisateurs
        // Si oui, empêcher la suppression ou proposer une migration

        roleRepository.deleteById(id);

        // Log d'audit
        auditLogService.logAction(
            deletedBy,
            "DELETE_ROLE",
            "roles",
            id,
            role.toString(),
            "Suppression du rôle: " + role.getName()
        );
    }

    /**
     * Recherche des rôles par nom d'affichage
     */
    public List<RoleEntity> searchRolesByDisplayName(String searchTerm) {
        return roleRepository.findByDisplayNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Récupère les rôles contenant une permission spécifique
     */
    public List<RoleEntity> getRolesWithPermission(String permission) {
        return roleRepository.findByPermissionsContaining(permission);
    }

    /**
     * Récupère les rôles système
     */
    public List<RoleEntity> getSystemRoles() {
        return roleRepository.findByIsSystemRoleTrue();
    }

    /**
     * Récupère les rôles non-système (modifiables)
     */
    public List<RoleEntity> getCustomRoles() {
        return roleRepository.findByIsSystemRoleFalse();
    }

    /**
     * Initialise les rôles système par défaut
     */
    public void initializeDefaultRoles() {
        // Créer les rôles système s'ils n'existent pas
        createSystemRoleIfNotExists("ADMIN_GENERAL", "Administrateur Général", 
            "Accès complet à toutes les fonctionnalités du système",
            List.of("*")); // Toutes les permissions

        createSystemRoleIfNotExists("CHEF_PROJET", "Chef de Projet", 
            "Gestion des projets et des équipes",
            List.of("VIEW_USERS", "CREATE_PROJECT", "UPDATE_PROJECT", "DELETE_PROJECT", "VIEW_PROJECTS", "ASSIGN_USERS"));

        createSystemRoleIfNotExists("EYA_EXECUTANTE", "EYA Exécutante", 
            "Exécution des tâches et consultation des projets",
            List.of("VIEW_PROJECTS", "UPDATE_EXECUTION", "VIEW_DOCUMENTS"));
    }

    private void createSystemRoleIfNotExists(String name, String displayName, String description, List<String> permissions) {
        if (!roleRepository.existsByName(name)) {
            RoleEntity systemRole = new RoleEntity(name, displayName, description, permissions);
            systemRole.setIsSystemRole(true);
            systemRole.setCreatedBy("SYSTEM");
            roleRepository.save(systemRole);
        }
    }
}

