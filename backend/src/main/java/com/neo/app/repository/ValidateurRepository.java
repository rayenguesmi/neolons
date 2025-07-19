package com.neo.app.repository;


import com.neo.app.documents.ValidateurEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ValidateurRepository extends MongoRepository<ValidateurEntity, String> {
    List<ValidateurEntity> findByDocumentId(String documentId);
    List<ValidateurEntity> findByUtilisateurId(String utilisateurId);
}
