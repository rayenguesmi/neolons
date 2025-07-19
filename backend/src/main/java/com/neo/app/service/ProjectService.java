package com.neo.app.service;

import com.neo.app.documents.ProjectEntity;
import com.neo.app.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LicenseService licenseService;

    // Création d'un projet
    public ProjectEntity createProject(ProjectEntity project) {
        // Vérifier que le code est unique
        if (projectRepository.existsByCode(project.getCode())) {
            throw new RuntimeException("Un projet avec ce code existe déjà");
        }

        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        return projectRepository.save(project);
    }

    // Mise à jour d'un projet
    public ProjectEntity updateProject(String projectId, ProjectEntity updatedProject) {
        Optional<ProjectEntity> existingProject = projectRepository.findById(projectId);
        
        if (existingProject.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = existingProject.get();
        
        // Vérifier l'unicité du code si il a changé
        if (!project.getCode().equals(updatedProject.getCode()) && 
            projectRepository.existsByCode(updatedProject.getCode())) {
            throw new RuntimeException("Un projet avec ce code existe déjà");
        }

        // Mettre à jour les champs
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setCode(updatedProject.getCode());
        project.setStatus(updatedProject.getStatus());
        project.setStartDate(updatedProject.getStartDate());
        project.setEndDate(updatedProject.getEndDate());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    // Suppression d'un projet
    public void deleteProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Projet non trouvé");
        }
        
        // Vérifier s'il y a des dépendances (campagnes, documents, etc.)
        // TODO: Ajouter les vérifications de dépendances
        
        projectRepository.deleteById(projectId);
    }

    // Récupération d'un projet par ID
    public Optional<ProjectEntity> getProjectById(String projectId) {
        return projectRepository.findById(projectId);
    }

    // Récupération d'un projet par code
    public Optional<ProjectEntity> getProjectByCode(String code) {
        return projectRepository.findByCode(code);
    }

    // Récupération de tous les projets
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    // Récupération des projets actifs
    public List<ProjectEntity> getActiveProjects() {
        return projectRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    // Récupération des projets par statut
    public List<ProjectEntity> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status);
    }

    // Récupération des projets par administrateur
    public List<ProjectEntity> getProjectsByAdmin(String adminId) {
        return projectRepository.findByAdminId(adminId);
    }

    // Récupération des projets par utilisateur
    public List<ProjectEntity> getProjectsByUser(String userId) {
        return projectRepository.findByUserId(userId);
    }

    // Recherche de projets par nom
    public List<ProjectEntity> searchProjectsByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    // Assignation d'un administrateur à un projet
    public ProjectEntity assignAdmin(String projectId, String adminId) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        
        if (project.getAdminIds() == null) {
            project.setAdminIds(List.of(adminId));
        } else if (!project.getAdminIds().contains(adminId)) {
            project.getAdminIds().add(adminId);
        }
        
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    // Retrait d'un administrateur d'un projet
    public ProjectEntity removeAdmin(String projectId, String adminId) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        
        if (project.getAdminIds() != null) {
            project.getAdminIds().remove(adminId);
        }
        
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    // Assignation d'un utilisateur à un projet
    public ProjectEntity assignUser(String projectId, String userId) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        
        if (project.getUserIds() == null) {
            project.setUserIds(List.of(userId));
        } else if (!project.getUserIds().contains(userId)) {
            project.getUserIds().add(userId);
        }
        
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    // Retrait d'un utilisateur d'un projet
    public ProjectEntity removeUser(String projectId, String userId) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        
        if (project.getUserIds() != null) {
            project.getUserIds().remove(userId);
        }
        
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    // Changement de statut d'un projet
    public ProjectEntity changeProjectStatus(String projectId, String newStatus) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        project.setStatus(newStatus);
        project.setUpdatedAt(LocalDateTime.now());
        
        return projectRepository.save(project);
    }

    // Vérifier si un utilisateur est administrateur d'un projet
    public boolean isProjectAdmin(String projectId, String userId) {
        Optional<ProjectEntity> project = projectRepository.findById(projectId);
        return project.isPresent() && 
               project.get().getAdminIds() != null && 
               project.get().getAdminIds().contains(userId);
    }

    // Vérifier si un utilisateur est assigné à un projet
    public boolean isUserAssignedToProject(String projectId, String userId) {
        Optional<ProjectEntity> project = projectRepository.findById(projectId);
        return project.isPresent() && 
               project.get().getUserIds() != null && 
               project.get().getUserIds().contains(userId);
    }

    // Statistiques des projets
    public long countProjectsByStatus(String status) {
        return projectRepository.countByStatus(status);
    }

    // Associer une licence à un projet
    public ProjectEntity assignLicense(String projectId, String licenseId) {
        Optional<ProjectEntity> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Projet non trouvé");
        }

        ProjectEntity project = projectOpt.get();
        project.setLicenseId(licenseId);
        project.setUpdatedAt(LocalDateTime.now());
        
        return projectRepository.save(project);
    }
}

