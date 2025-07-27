package com.neo.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.neo.app.documents.DocumentEntity;
import java.util.List;
import java.util.Date;

public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByUserId(String userId);
    
    // Méthodes de comptage pour les statistiques du dashboard
    @Query(value = "{'createdAt': {'$gte': ?0}}", count = true)
    long countByCreatedAtAfter(Date date);
}
