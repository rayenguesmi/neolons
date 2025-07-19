package com.neo.app.repository;

import com.neo.app.documents.ProjectEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<ProjectEntity, String> {

    // Recherche par code unique
    Optional<ProjectEntity> findByCode(String code);

    // Recherche par nom
    List<ProjectEntity> findByNameContainingIgnoreCase(String name);

    // Recherche par statut
    List<ProjectEntity> findByStatus(String status);

    // Recherche des projets actifs
    List<ProjectEntity> findByStatusOrderByCreatedAtDesc(String status);

    // Recherche des projets par administrateur
    @Query("{ 'adminIds': ?0 }")
    List<ProjectEntity> findByAdminId(String adminId);

    // Recherche des projets par utilisateur assigné
    @Query("{ 'userIds': ?0 }")
    List<ProjectEntity> findByUserId(String userId);

    // Recherche des projets par licence
    List<ProjectEntity> findByLicenseId(String licenseId);

    // Recherche des projets créés par un utilisateur
    List<ProjectEntity> findByCreatedBy(String createdBy);

    // Vérifier si un code existe déjà
    boolean existsByCode(String code);

    // Compter les projets par statut
    long countByStatus(String status);

    // Recherche des projets avec une licence expirée (via jointure)
    @Query("{ 'licenseId': { $in: ?0 } }")
    List<ProjectEntity> findByLicenseIdIn(List<String> licenseIds);
}

