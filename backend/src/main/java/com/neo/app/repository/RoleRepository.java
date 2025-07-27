package com.neo.app.repository;

import com.neo.app.documents.RoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<RoleEntity, String> {

    /**
     * Trouve un rôle par son nom
     */
    Optional<RoleEntity> findByName(String name);

    /**
     * Trouve tous les rôles actifs
     */
    List<RoleEntity> findByIsActiveTrue();

    /**
     * Trouve tous les rôles système
     */
    List<RoleEntity> findByIsSystemRoleTrue();

    /**
     * Trouve tous les rôles non-système (modifiables)
     */
    List<RoleEntity> findByIsSystemRoleFalse();

    /**
     * Trouve les rôles par nom d'affichage (recherche insensible à la casse)
     */
    @Query("{'displayName': {$regex: ?0, $options: 'i'}}")
    List<RoleEntity> findByDisplayNameContainingIgnoreCase(String displayName);

    /**
     * Trouve les rôles contenant une permission spécifique
     */
    @Query("{'permissions': ?0}")
    List<RoleEntity> findByPermissionsContaining(String permission);

    /**
     * Vérifie si un rôle avec ce nom existe déjà
     */
    boolean existsByName(String name);

    /**
     * Compte le nombre de rôles actifs
     */
    long countByIsActiveTrue();

    /**
     * Trouve les rôles créés par un utilisateur spécifique
     */
    List<RoleEntity> findByCreatedBy(String createdBy);

    // Méthodes de comptage pour les statistiques du dashboard
    long countByIsActive(Boolean isActive);
    
    long countByIsSystemRole(Boolean isSystemRole);
}

