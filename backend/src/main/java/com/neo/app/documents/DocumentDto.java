package com.neo.app.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.catalina.User;

import java.time.LocalDateTime;

// Si Document et UserDto sont dans le même package, pas besoin d'import
// Sinon, importe correctement selon leur package réel

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private Long id;

    @NotBlank(message = "Le nom du document est obligatoire")
    @Size(max = 255, message = "Le nom du document ne peut pas dépasser 255 caractères")
    private String name;

    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private Integer version;

    //private documents.DocumentType type;
    //private documents.DocumentStatus status;

    private String description;
    private String keywords;

    private Boolean isLatestVersion;
    private Boolean isArchived;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Informations sur l'utilisateur qui a uploadé le document
    private User uploadedBy;

    // Informations sur le document parent (pour le versioning)
    private Long parentDocumentId;

    // Nombre d'annotations
    private Integer annotationCount;

    // Statut de signature
    private Boolean isSigned;
}
