package com.neo.app.repository;

import com.neo.app.documents.CampagneEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CampagneRepository extends MongoRepository<CampagneEntity, String> {
    List<CampagneEntity> findByUserId(String userId);
}