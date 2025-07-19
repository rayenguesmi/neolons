package com.neo.app.repository;


import com.neo.app.documents.CommentaireEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentaireRepository extends MongoRepository<CommentaireEntity, String> {
    List<CommentaireEntity> findByAnnotationId(String annotationId);
}
