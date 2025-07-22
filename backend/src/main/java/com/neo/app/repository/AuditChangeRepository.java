package com.neo.app.repository;

import com.neo.app.documents.AuditChangeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AuditChangeRepository extends MongoRepository<AuditChangeEntity, String> {

    // Recherche par audit log ID
    List<AuditChangeEntity> findByAuditLogId(String auditLogId);

    // Recherche par nom de champ
    List<AuditChangeEntity> findByFieldName(String fieldName);

    // Recherche par nom de champ et période
    List<AuditChangeEntity> findByFieldNameAndTimestampBetween(String fieldName, Date startDate, Date endDate);

    // Recherche par type de données
    List<AuditChangeEntity> findByDataType(String dataType);

    // Recherche par période
    List<AuditChangeEntity> findByTimestampBetween(Date startDate, Date endDate);

    // Recherche des modifications avec anciennes valeurs spécifiques
    List<AuditChangeEntity> findByOldValue(String oldValue);

    // Recherche des modifications avec nouvelles valeurs spécifiques
    List<AuditChangeEntity> findByNewValue(String newValue);

    // Recherche des modifications où l'ancienne valeur contient un texte
    @Query("{ 'oldValue': { $regex: ?0, $options: 'i' } }")
    List<AuditChangeEntity> findByOldValueContaining(String text);

    // Recherche des modifications où la nouvelle valeur contient un texte
    @Query("{ 'newValue': { $regex: ?0, $options: 'i' } }")
    List<AuditChangeEntity> findByNewValueContaining(String text);

    // Compter les modifications par champ
    @Query(value = "{ 'fieldName': ?0 }", count = true)
    long countByFieldName(String fieldName);

    // Compter les modifications par audit log
    @Query(value = "{ 'auditLogId': ?0 }", count = true)
    long countByAuditLogId(String auditLogId);

    // Recherche des modifications récentes pour un champ
    @Query("{ 'fieldName': ?0, 'timestamp': { $gte: ?1 } }")
    List<AuditChangeEntity> findRecentChangesByField(String fieldName, Date since);

    // Recherche des modifications par multiple audit logs
    List<AuditChangeEntity> findByAuditLogIdIn(List<String> auditLogIds);

    // Suppression par audit log ID
    void deleteByAuditLogId(String auditLogId);

    // Suppression par période
    void deleteByTimestampBefore(Date beforeDate);
}

