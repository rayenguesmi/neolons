package com.neo.app.controller;

import com.neo.app.documents.AuditLogEntity;
import com.neo.app.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Récupère tous les logs d'audit
     */
    @GetMapping
    public ResponseEntity<List<AuditLogEntity>> getAllLogs() {
        try {
            List<AuditLogEntity> logs = auditLogService.getAllLogs();
            // Trier les logs par timestamp desc
            logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un log d'audit par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogEntity> getLogById(@PathVariable String id) {
        try {
            Optional<AuditLogEntity> log = auditLogService.getLogById(id);
            if (log.isPresent()) {
                return ResponseEntity.ok(log.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les logs d'audit par utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogEntity>> getLogsByUser(@PathVariable String userId) {
        try {
            List<AuditLogEntity> logs = auditLogService.getLogsByUser(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les logs d'audit par action
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogEntity>> getLogsByAction(@PathVariable String action) {
        try {
            List<AuditLogEntity> logs = auditLogService.getLogsByAction(action);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les logs d'audit par type de ressource
     */
    @GetMapping("/resource-type/{resourceType}")
    public ResponseEntity<List<AuditLogEntity>> getLogsByResourceType(@PathVariable String resourceType) {
        try {
            List<AuditLogEntity> logs = auditLogService.getLogsByResourceType(resourceType);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les logs d'audit par ressource spécifique
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<List<AuditLogEntity>> getLogsByResource(
            @PathVariable String resourceType, 
            @PathVariable String resourceId) {
        try {
            List<AuditLogEntity> logs = auditLogService.getLogsByResource(resourceType, resourceId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les logs d'audit par période
     */
    @GetMapping("/period")
    public ResponseEntity<List<AuditLogEntity>> getLogsByPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<AuditLogEntity> logs = auditLogService.getLogsByPeriod(startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les actions échouées
     */
    @GetMapping("/failed")
    public ResponseEntity<List<AuditLogEntity>> getFailedActions() {
        try {
            List<AuditLogEntity> logs = auditLogService.getFailedActions();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Génère un rapport d'activité pour une période
     */
    @GetMapping("/report")
    public ResponseEntity<AuditLogService.AuditReport> generateActivityReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            AuditLogService.AuditReport report = auditLogService.generateActivityReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Statistiques - Nombre d'actions par utilisateur
     */
    @GetMapping("/stats/user/{userId}/count")
    public ResponseEntity<Long> countActionsByUser(@PathVariable String userId) {
        try {
            long count = auditLogService.countActionsByUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

