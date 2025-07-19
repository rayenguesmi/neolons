package com.neo.app.repository;

import com.neo.app.documents.PermissionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends MongoRepository<PermissionEntity, String> {

    /**
     * Trouve une permission par son nom
     */
    Optional<PermissionEntity> findByName(String name);

    /**
     * Trouve toutes les permissions actives
     */
    List<PermissionEntity> findByIsActiveTrue();

    /**
     * Trouve toutes les permissions système
     */
    List<PermissionEntity> findByIsSystemPermissionTrue();

    /**
     * Trouve toutes les permissions non-système (modifiables)
     */
    List<PermissionEntity> findByIsSystemPermissionFalse();

    /**
     * Trouve les permissions par module
     */
    List<PermissionEntity> findByModule(String module);

    /**
     * Trouve les permissions par ressource
     */
    List<PermissionEntity> findByResource(String resource);

    /**
     * Trouve les permissions par action
     */
    List<PermissionEntity> findByAction(String action);

    /**
     * Trouve les permissions par module et ressource
     */
    List<PermissionEntity> findByModuleAndResource(String module, String resource);

    /**
     * Trouve les permissions par ressource et action
     */
    List<PermissionEntity> findByResourceAndAction(String resource, String action);

    /**
     * Recherche dans le nom d'affichage (insensible à la casse)
     */
    @Query("{'displayName': {$regex: ?0, $options: 'i'}}")
    List<PermissionEntity> findByDisplayNameContainingIgnoreCase(String displayName);

    /**
     * Vérifie si une permission avec ce nom existe déjà
     */
    boolean existsByName(String name);

    /**
     * Vérifie si une permission existe pour une combinaison module/ressource/action
     */
    boolean existsByModuleAndResourceAndAction(String module, String resource, String action);

    /**
     * Compte le nombre de permissions actives
     */
    long countByIsActiveTrue();

    /**
     * Trouve toutes les permissions distinctes par module
     */
    @Query(value = "{}", fields = "{'module': 1}")
    List<String> findDistinctModules();

    /**
     * Trouve toutes les ressources distinctes
     */
    @Query(value = "{}", fields = "{'resource': 1}")
    List<String> findDistinctResources();

    /**
     * Trouve toutes les actions distinctes
     */
    @Query(value = "{}", fields = "{'action': 1}")
    List<String> findDistinctActions();
}

