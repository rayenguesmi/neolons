package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "commentaires")
public class CommentaireEntity {

    @Id
    private String id;
    private String annotationId;
    private String auteurId;
    private String texte;
    private LocalDateTime dateCreation;

    // Getters & Setters + Constructeurs
}
