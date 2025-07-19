package com.neo.app.repository;

import com.neo.app.documents.ExecutionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ExecutionRepository extends MongoRepository<ExecutionEntity, String> {
    List<ExecutionEntity> findByUserId(String userId);
}
