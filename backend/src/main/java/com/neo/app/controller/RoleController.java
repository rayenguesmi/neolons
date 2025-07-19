package com.neo.app.controller;

import com.neo.app.documents.RoleEntity;
import com.neo.app.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Récupère tous les rôles
     */
    @GetMapping
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        try {
            List<RoleEntity> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les rôles actifs
     */
    @GetMapping("/active")
    public ResponseEntity<List<RoleEntity>> getActiveRoles() {
        try {
            List<RoleEntity> roles = roleService.getActiveRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un rôle par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable String id) {
        try {
            Optional<RoleEntity> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                return ResponseEntity.ok(role.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un rôle par son nom
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<RoleEntity> getRoleByName(@PathVariable String name) {
        try {
            Optional<RoleEntity> role = roleService.getRoleByName(name);
            if (role.isPresent()) {
                return ResponseEntity.ok(role.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crée un nouveau rôle
     */
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody RoleEntity role, 
                                       @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            RoleEntity createdRole = roleService.createRole(role, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création du rôle");
        }
    }

    /**
     * Met à jour un rôle existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable String id, 
                                       @RequestBody RoleEntity role,
                                       @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            RoleEntity updatedRole = roleService.updateRole(id, role, userId);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du rôle");
        }
    }

    /**
     * Active ou désactive un rôle
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleRoleStatus(@PathVariable String id,
                                             @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            RoleEntity updatedRole = roleService.toggleRoleStatus(id, userId);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du changement de statut");
        }
    }

    /**
     * Supprime un rôle
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable String id,
                                       @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            roleService.deleteRole(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression du rôle");
        }
    }

    /**
     * Recherche des rôles par nom d'affichage
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoleEntity>> searchRoles(@RequestParam String q) {
        try {
            List<RoleEntity> roles = roleService.searchRolesByDisplayName(q);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les rôles contenant une permission spécifique
     */
    @GetMapping("/with-permission/{permission}")
    public ResponseEntity<List<RoleEntity>> getRolesWithPermission(@PathVariable String permission) {
        try {
            List<RoleEntity> roles = roleService.getRolesWithPermission(permission);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les rôles système
     */
    @GetMapping("/system")
    public ResponseEntity<List<RoleEntity>> getSystemRoles() {
        try {
            List<RoleEntity> roles = roleService.getSystemRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les rôles personnalisés (non-système)
     */
    @GetMapping("/custom")
    public ResponseEntity<List<RoleEntity>> getCustomRoles() {
        try {
            List<RoleEntity> roles = roleService.getCustomRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Initialise les rôles système par défaut
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<?> initializeDefaultRoles() {
        try {
            roleService.initializeDefaultRoles();
            return ResponseEntity.ok("Rôles système initialisés avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'initialisation des rôles");
        }
    }
}

