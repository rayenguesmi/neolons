package com.neo.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.neo.app.documents.DocumentEntity;
import java.util.List;
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    List<DocumentEntity> findByUserId(String userId);
}
