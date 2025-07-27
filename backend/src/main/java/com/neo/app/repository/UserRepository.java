package com.neo.app.repository;

import com.neo.app.documents.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByKeycloakId(String keycloakId);

    List<UserEntity> findByIsActive(Boolean isActive);

    @Query("{'roles': ?0}")
    List<UserEntity> findByRole(String role);

    @Query("{'roles': {'$in': ?0}}")
    List<UserEntity> findByRoles(List<String> roles);

    @Query("{'projects': ?0}")
    List<UserEntity> findByProject(String projectId);

    @Query("{'projects': {'$in': ?0}}")
    List<UserEntity> findByProjects(List<String> projectIds);

    @Query("{'roles': 'ADMIN_GENERAL'}")
    List<UserEntity> findAllAdministrators();

    @Query("{'roles': 'CHEF_PROJET', 'projects': ?0}")
    List<UserEntity> findProjectManagers(String projectId);

    @Query("{'roles': 'EYA_EXECUTANTE', 'projects': ?0}")
    List<UserEntity> findExecutors(String projectId);

    @Query("{'$and': [{'isActive': true}, {'roles': {'$in': ?0}}]}")
    List<UserEntity> findActiveUsersByRoles(List<String> roles);

    @Query("{'$and': [{'isActive': true}, {'projects': ?0}]}")
    List<UserEntity> findActiveUsersByProject(String projectId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String keycloakId);

    @Query(value = "{'roles': 'ADMIN_GENERAL'}", count = true)
    long countAdministrators();

    // Méthodes de comptage pour les statistiques du dashboard
    long countByIsActive(Boolean isActive);
    
    @Query(value = "{'roles': ?0}", count = true)
    long countByRolesContaining(String role);
    
    @Query(value = "{'createdAt': {'$gte': ?0}}", count = true)
    long countByCreatedAtAfter(java.util.Date date);
    
    @Query(value = "{'lastLogin': {'$gte': ?0}}", count = true)
    long countByLastLoginAfter(java.util.Date date);
}

