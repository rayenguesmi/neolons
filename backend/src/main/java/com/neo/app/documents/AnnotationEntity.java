package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "annotations")
public class AnnotationEntity {

    @Id
    private String id;
    private String documentId;
    private String auteurId;
    private int page; // page du document PDF
    private float positionX;
    private float positionY;
    private String texte; // commentaire ou remarque
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public AnnotationEntity() {
    }

    // Constructeur complet
    public AnnotationEntity(String id, String documentId, String auteurId, int page,
                            float positionX, float positionY, String texte, LocalDateTime dateCreation) {
        this.id = id;
        this.documentId = documentId;
        this.auteurId = auteurId;
        this.page = page;
        this.positionX = positionX;
        this.positionY = positionY;
        this.texte = texte;
        this.dateCreation = dateCreation;
    }

    // Getters et setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getAuteurId() {
        return auteurId;
    }

    public void setAuteurId(String auteurId) {
        this.auteurId = auteurId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
