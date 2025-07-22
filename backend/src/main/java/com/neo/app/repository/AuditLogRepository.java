package com.neo.app.repository;

import com.neo.app.documents.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogEntity, String> {

    List<AuditLogEntity> findByUserId(String userId);

    List<AuditLogEntity> findByUsername(String username);

    List<AuditLogEntity> findByAction(String action);

    List<AuditLogEntity> findByResourceType(String resourceType);

    List<AuditLogEntity> findByResourceId(String resourceId);

    List<AuditLogEntity> findByProjectId(String projectId);

    List<AuditLogEntity> findBySuccess(Boolean success);

    @Query("{'timestamp': {'$gte': ?0, '$lte': ?1}}")
    List<AuditLogEntity> findByTimestampBetween(Date startDate, Date endDate);

    @Query("{'userId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}")
    List<AuditLogEntity> findByUserIdAndTimestampBetween(String userId, Date startDate, Date endDate);

    @Query("{'action': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}")
    List<AuditLogEntity> findByActionAndTimestampBetween(String action, Date startDate, Date endDate);

    @Query("{'resourceType': ?0, 'resourceId': ?1}")
    List<AuditLogEntity> findByResourceTypeAndResourceId(String resourceType, String resourceId);

    @Query("{'projectId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}")
    List<AuditLogEntity> findByProjectIdAndTimestampBetween(String projectId, Date startDate, Date endDate);

    @Query("{'success': false, 'timestamp': {'$gte': ?0, '$lte': ?1}}")
    List<AuditLogEntity> findFailedActionsBetween(Date startDate, Date endDate);

    @Query("{'action': {'$in': ['LOGIN', 'LOGOUT']}, 'userId': ?0}")
    List<AuditLogEntity> findLoginLogsByUserId(String userId);

    @Query("{'ipAddress': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}")
    List<AuditLogEntity> findByIpAddressAndTimestampBetween(String ipAddress, Date startDate, Date endDate);

    // Méthodes avec pagination
    Page<AuditLogEntity> findByUserId(String userId, Pageable pageable);

    Page<AuditLogEntity> findByProjectId(String projectId, Pageable pageable);

    @Query("{'timestamp': {'$gte': ?0, '$lte': ?1}}")
    Page<AuditLogEntity> findByTimestampBetween(Date startDate, Date endDate, Pageable pageable);

    // Méthodes de comptage
    @Query(value = "{'userId': ?0}", count = true)
    long countByUserId(String userId);

    @Query(value = "{'action': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}", count = true)
    long countByActionAndTimestampBetween(String action, Date startDate, Date endDate);

    @Query(value = "{'success': false, 'timestamp': {'$gte': ?0, '$lte': ?1}}", count = true)
    long countFailedActionsBetween(Date startDate, Date endDate);

    @Query(value = "{'projectId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}", count = true)
    long countByProjectIdAndTimestampBetween(String projectId, Date startDate, Date endDate);

    // Nouvelles méthodes pour les statistiques du tableau de bord
    @Query(value = "{'timestamp': {'$gte': ?0}}", count = true)
    long countByTimestampAfter(Date date);

    @Query(value = "{'timestamp': {'$gte': ?0}}", fields = "{'userId': 1}")
    long countDistinctUsersByTimestampAfter(Date date);

    // Méthodes pour obtenir les actions les plus fréquentes (simulation avec des données par défaut)
    @Query("{'action': {'$exists': true}}")
    List<AuditLogEntity> findMostFrequentActions();
    List<AuditLogEntity> findByRiskLevel(String riskLevel);

    // Méthodes pour obtenir les types de ressources les plus accédés
    @Query("{'resourceType': {'$exists': true}}")
    List<AuditLogEntity> findMostAccessedResourceTypes();
}

