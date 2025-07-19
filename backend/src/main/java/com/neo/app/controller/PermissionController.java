package com.neo.app.controller;

import com.neo.app.documents.PermissionEntity;
import com.neo.app.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * Récupère toutes les permissions
     */
    @GetMapping
    public ResponseEntity<List<PermissionEntity>> getAllPermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère toutes les permissions actives
     */
    @GetMapping("/active")
    public ResponseEntity<List<PermissionEntity>> getActivePermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getActivePermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une permission par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PermissionEntity> getPermissionById(@PathVariable String id) {
        try {
            Optional<PermissionEntity> permission = permissionService.getPermissionById(id);
            if (permission.isPresent()) {
                return ResponseEntity.ok(permission.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une permission par son nom
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<PermissionEntity> getPermissionByName(@PathVariable String name) {
        try {
            Optional<PermissionEntity> permission = permissionService.getPermissionByName(name);
            if (permission.isPresent()) {
                return ResponseEntity.ok(permission.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les permissions par module
     */
    @GetMapping("/module/{module}")
    public ResponseEntity<List<PermissionEntity>> getPermissionsByModule(@PathVariable String module) {
        try {
            List<PermissionEntity> permissions = permissionService.getPermissionsByModule(module);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les permissions par ressource
     */
    @GetMapping("/resource/{resource}")
    public ResponseEntity<List<PermissionEntity>> getPermissionsByResource(@PathVariable String resource) {
        try {
            List<PermissionEntity> permissions = permissionService.getPermissionsByResource(resource);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crée une nouvelle permission
     */
    @PostMapping
    public ResponseEntity<?> createPermission(@RequestBody PermissionEntity permission,
                                             @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            PermissionEntity createdPermission = permissionService.createPermission(permission, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création de la permission");
        }
    }

    /**
     * Met à jour une permission existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(@PathVariable String id,
                                             @RequestBody PermissionEntity permission,
                                             @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            PermissionEntity updatedPermission = permissionService.updatePermission(id, permission, userId);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour de la permission");
        }
    }

    /**
     * Active ou désactive une permission
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> togglePermissionStatus(@PathVariable String id,
                                                   @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            PermissionEntity updatedPermission = permissionService.togglePermissionStatus(id, userId);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du changement de statut");
        }
    }

    /**
     * Supprime une permission
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable String id,
                                             @RequestHeader(value = "X-User-ID", defaultValue = "SYSTEM") String userId) {
        try {
            permissionService.deletePermission(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de la permission");
        }
    }

    /**
     * Recherche des permissions par nom d'affichage
     */
    @GetMapping("/search")
    public ResponseEntity<List<PermissionEntity>> searchPermissions(@RequestParam String q) {
        try {
            List<PermissionEntity> permissions = permissionService.searchPermissionsByDisplayName(q);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les permissions système
     */
    @GetMapping("/system")
    public ResponseEntity<List<PermissionEntity>> getSystemPermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getSystemPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les permissions personnalisées (non-système)
     */
    @GetMapping("/custom")
    public ResponseEntity<List<PermissionEntity>> getCustomPermissions() {
        try {
            List<PermissionEntity> permissions = permissionService.getCustomPermissions();
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Initialise les permissions système par défaut
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<?> initializeDefaultPermissions() {
        try {
            permissionService.initializeDefaultPermissions();
            return ResponseEntity.ok("Permissions système initialisées avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'initialisation des permissions");
        }
    }
}

