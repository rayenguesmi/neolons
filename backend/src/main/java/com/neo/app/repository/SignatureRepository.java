package com.neo.app.repository;


import com.neo.app.documents.SignatureEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SignatureRepository extends MongoRepository<SignatureEntity, String> {
    List<SignatureEntity> findByDocumentId(String documentId);
    List<SignatureEntity> findByUtilisateurId(String utilisateurId);
}
