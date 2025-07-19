package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "utilisateurs_validateurs")
public class ValidateurEntity {
    @Id
    private String id;
    private String documentId;
    private String utilisateurId;
    private String statutValidation; // "en attente", "validé", "refusé"
    private LocalDateTime dateAction;
}
