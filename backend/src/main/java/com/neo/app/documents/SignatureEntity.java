package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "signatures")
public class SignatureEntity {

    @Id
    private String id;
    private String documentId;
    private String utilisateurId;
    private String typeSignature; // "électronique"
    private LocalDateTime dateSignature;

    // Getters & Setters + Constructeurs
}
