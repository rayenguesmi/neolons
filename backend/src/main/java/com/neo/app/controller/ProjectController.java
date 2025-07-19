package com.neo.app.controller;

import com.neo.app.documents.ProjectEntity;
import com.neo.app.service.ProjectService;
import com.neo.app.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuditLogService auditLogService;

    // Création d'un projet (Admin général seulement)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> createProject(@RequestBody ProjectEntity project, HttpServletRequest request) {
        try {
            project.setCreatedBy(getCurrentUserId(request));
            ProjectEntity createdProject = projectService.createProject(project);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "CREATE",
                    "PROJECT",
                    createdProject.getId(),
                    "Project created: " + createdProject.getName(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la création du projet", e.getMessage()));
        }
    }

    // Mise à jour d'un projet
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or @projectService.isProjectAdmin(#projectId, authentication.name)")
    public ResponseEntity<?> updateProject(@PathVariable String projectId,
                                           @RequestBody ProjectEntity project,
                                           HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.updateProject(projectId, project);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "UPDATE",
                    "PROJECT",
                    updatedProject.getId(),
                    "Project updated: " + updatedProject.getName(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la mise à jour du projet", e.getMessage()));
        }
    }

    // Suppression d'un projet (Admin général seulement)
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> deleteProject(@PathVariable String projectId, HttpServletRequest request) {
        try {
            projectService.deleteProject(projectId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "DELETE",
                    "PROJECT",
                    projectId,
                    "Project deleted",
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(new SuccessResponse("Projet supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de la suppression du projet", e.getMessage()));
        }
    }

    // Récupération d'un projet par ID
    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or hasRole('CHEF_PROJET') or @projectService.isUserAssignedToProject(#projectId, authentication.name)")
    public ResponseEntity<?> getProjectById(@PathVariable String projectId) {
        try {
            Optional<ProjectEntity> project = projectService.getProjectById(projectId);
            if (project.isPresent()) {
                return ResponseEntity.ok(project.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Projet non trouvé", "Aucun projet trouvé avec l'ID: " + projectId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération du projet", e.getMessage()));
        }
    }

    // Récupération de tous les projets
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_GENERAL') or hasRole('CHEF_PROJET')")
    public ResponseEntity<?> getAllProjects() {
        try {
            List<ProjectEntity> projects = projectService.getAllProjects();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des projets", e.getMessage()));
        }
    }

    // Récupération des projets actifs
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or hasRole('CHEF_PROJET')")
    public ResponseEntity<?> getActiveProjects() {
        try {
            List<ProjectEntity> projects = projectService.getActiveProjects();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des projets actifs", e.getMessage()));
        }
    }

    // Récupération des projets par statut
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or hasRole('CHEF_PROJET')")
    public ResponseEntity<?> getProjectsByStatus(@PathVariable String status) {
        try {
            List<ProjectEntity> projects = projectService.getProjectsByStatus(status);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération des projets par statut", e.getMessage()));
        }
    }

    // Récupération des projets de l'utilisateur connecté
    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects(HttpServletRequest request) {
        try {
            String userId = getCurrentUserId(request);
            List<ProjectEntity> projects = projectService.getProjectsByUser(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la récupération de vos projets", e.getMessage()));
        }
    }

    // Recherche de projets par nom
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or hasRole('CHEF_PROJET')")
    public ResponseEntity<?> searchProjects(@RequestParam String name) {
        try {
            List<ProjectEntity> projects = projectService.searchProjectsByName(name);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la recherche de projets", e.getMessage()));
        }
    }

    // Assignation d'un administrateur à un projet
    @PostMapping("/{projectId}/admins/{adminId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> assignAdmin(@PathVariable String projectId,
                                         @PathVariable String adminId,
                                         HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.assignAdmin(projectId, adminId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "ASSIGN_ADMIN",
                    "PROJECT",
                    projectId,
                    "Admin assigned to project: " + adminId,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de l'assignation de l'administrateur", e.getMessage()));
        }
    }

    // Retrait d'un administrateur d'un projet
    @DeleteMapping("/{projectId}/admins/{adminId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> removeAdmin(@PathVariable String projectId,
                                         @PathVariable String adminId,
                                         HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.removeAdmin(projectId, adminId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "REMOVE_ADMIN",
                    "PROJECT",
                    projectId,
                    "Admin removed from project: " + adminId,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors du retrait de l'administrateur", e.getMessage()));
        }
    }

    // Assignation d'un utilisateur à un projet
    @PostMapping("/{projectId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or @projectService.isProjectAdmin(#projectId, authentication.name)")
    public ResponseEntity<?> assignUser(@PathVariable String projectId,
                                        @PathVariable String userId,
                                        HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.assignUser(projectId, userId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "ASSIGN_USER",
                    "PROJECT",
                    projectId,
                    "User assigned to project: " + userId,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de l'assignation de l'utilisateur", e.getMessage()));
        }
    }

    // Retrait d'un utilisateur d'un projet
    @DeleteMapping("/{projectId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or @projectService.isProjectAdmin(#projectId, authentication.name)")
    public ResponseEntity<?> removeUser(@PathVariable String projectId,
                                        @PathVariable String userId,
                                        HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.removeUser(projectId, userId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "REMOVE_USER",
                    "PROJECT",
                    projectId,
                    "User removed from project: " + userId,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors du retrait de l'utilisateur", e.getMessage()));
        }
    }

    // Changement de statut d'un projet
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasRole('ADMIN_GENERAL') or @projectService.isProjectAdmin(#projectId, authentication.name)")
    public ResponseEntity<?> changeProjectStatus(@PathVariable String projectId,
                                                  @RequestParam String status,
                                                  HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.changeProjectStatus(projectId, status);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "STATUS_CHANGE",
                    "PROJECT",
                    projectId,
                    "Project status changed to: " + status,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors du changement de statut", e.getMessage()));
        }
    }

    // Association d'une licence à un projet
    @PatchMapping("/{projectId}/license")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<?> assignLicense(@PathVariable String projectId,
                                           @RequestParam String licenseId,
                                           HttpServletRequest request) {
        try {
            ProjectEntity updatedProject = projectService.assignLicense(projectId, licenseId);

            auditLogService.logAction(
                    getCurrentUserId(request),
                    getCurrentUsername(request),
                    "ASSIGN_LICENSE",
                    "PROJECT",
                    projectId,
                    "License assigned to project: " + licenseId,
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession().getId()
            );

            return ResponseEntity.ok(updatedProject);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Erreur lors de l'assignation de la licence", e.getMessage()));
        }
    }

    // Méthodes utilitaires
    private String getCurrentUserId(HttpServletRequest request) {
        return request.getHeader("X-User-ID");
    }

    private String getCurrentUsername(HttpServletRequest request) {
        return request.getHeader("X-Username");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Classes pour les réponses
    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

