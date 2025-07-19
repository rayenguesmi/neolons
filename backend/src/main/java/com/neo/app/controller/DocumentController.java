package com.neo.app.controller;

import com.neo.app.documents.DocumentEntity;
import com.neo.app.service.DocumentService;
import com.neo.app.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    // Méthode pour gérer l'upload d'un document
    @PostMapping("/upload")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                            @RequestParam("userId") String userId) {
        logger.debug("Début du téléchargement du fichier: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            logger.error("Le fichier est vide.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Le fichier ne peut pas être vide."));
        }

        // Limite de taille pour les fichiers
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(new ErrorResponse("Le fichier est trop volumineux. Taille maximale autorisée : 10MB."));
        }

        try {
            // Validation du type de fichier
            if (!isValidFileType(file)) {
                logger.error("Type de fichier non autorisé: {}", file.getContentType());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Type de fichier non autorisé. Veuillez télécharger un fichier PDF, CSV ou XML."));
            }

            // Sauvegarde du fichier dans la base de données en associant l'ID utilisateur
            documentService.saveDocument(file, userId);

            logger.debug("Téléchargement du fichier réussi.");
            return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("Document téléchargé avec succès."));
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement du fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Échec du téléchargement du document."));
        }
    }
    @GetMapping("/files/{userId}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<List<DocumentEntity>> getUserFiles(@PathVariable String userId) {
        List<DocumentEntity> documents = documentService.getDocumentsByUserId(userId);

        if (documents.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }


        return ResponseEntity.ok(documents);
    }

    // Méthode pour vérifier si le type de fichier est valide
    private boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("application/pdf")
                || contentType.equals("text/csv")
                || contentType.equals("application/xml"));
    }

    public static class SuccessResponse {
        private String message;
        public SuccessResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
