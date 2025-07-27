package com.neo.app.repository;

import com.neo.app.documents.CampagneEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface CampagneRepository extends MongoRepository<CampagneEntity, String> {
    List<CampagneEntity> findByUserId(String userId);
    
    // Méthodes de comptage pour les statistiques du dashboard
    @Query(value = "{'statut': ?0}", count = true)
    long countByStatut(String statut);
}