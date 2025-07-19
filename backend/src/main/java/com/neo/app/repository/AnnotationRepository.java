package com.neo.app.repository;

import com.neo.app.documents.AnnotationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnnotationRepository extends MongoRepository<AnnotationEntity, String> {
    List<AnnotationEntity> findByDocumentId(String documentId);
}
