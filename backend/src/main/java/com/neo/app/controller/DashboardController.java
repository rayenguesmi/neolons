package com.neo.app.controller;

import com.neo.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Récupère toutes les statistiques du tableau de bord
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getAllStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des utilisateurs
     */
    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> userStats = dashboardService.getUserStats();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des licences
     */
    @GetMapping("/stats/licenses")
    public ResponseEntity<Map<String, Object>> getLicenseStats() {
        try {
            Map<String, Object> licenseStats = dashboardService.getLicenseStats();
            return ResponseEntity.ok(licenseStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques d'audit
     */
    @GetMapping("/stats/audit")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        try {
            Map<String, Object> auditStats = dashboardService.getAuditStats();
            return ResponseEntity.ok(auditStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des documents
     */
    @GetMapping("/stats/documents")
    public ResponseEntity<Map<String, Object>> getDocumentStats() {
        try {
            Map<String, Object> documentStats = dashboardService.getDocumentStats();
            return ResponseEntity.ok(documentStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des campagnes
     */
    @GetMapping("/stats/campaigns")
    public ResponseEntity<Map<String, Object>> getCampaignStats() {
        try {
            Map<String, Object> campaignStats = dashboardService.getCampaignStats();
            return ResponseEntity.ok(campaignStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des rôles
     */
    @GetMapping("/stats/roles")
    public ResponseEntity<Map<String, Object>> getRoleStats() {
        try {
            Map<String, Object> roleStats = dashboardService.getRoleStats();
            return ResponseEntity.ok(roleStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les activités récentes
     */
    @GetMapping("/activities/recent")
    public ResponseEntity<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> activities = dashboardService.getRecentActivities(limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les métriques système (simulées)
     */
    @GetMapping("/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> systemHealth = dashboardService.getSystemHealth();
            return ResponseEntity.ok(systemHealth);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

