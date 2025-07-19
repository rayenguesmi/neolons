package com.neo.app.service;

import com.neo.app.documents.DocumentEntity;
import com.neo.app.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private static final String AES = "AES";
    private static final String SECRET_KEY = "1234567890123456";

    // Sauvegarder un document avec l'ID utilisateur

    public void saveDocument(MultipartFile file, String userId) throws IOException {
        // Création du DocumentEntity avec le userId
        DocumentEntity document = new DocumentEntity(
                file.getOriginalFilename(),     // Nom du fichier
                file.getContentType(),          // Type du fichier (pdf, xml, etc.)
                new Date(),                     // Date de l'upload
                file.getBytes(),                // Données du fichier en byte[]
                userId                          // L'ID de l'utilisateur qui télécharge ce fichier
        );

        // Sauvegarde dans MongoDB
        documentRepository.save(document);
    }


    public List<DocumentEntity> getDocumentsByUserId(String userId) {
        // Log pour vérifier la requête et les données
        logger.debug("Recherche des documents pour l'utilisateur ID: {}", userId);
        // Exemple d'appel à la base de données pour récupérer les documents
        List<DocumentEntity> documents = documentRepository.findByUserId(userId);

        // Log des résultats
        if (documents.isEmpty()) {
            logger.warn("Aucun document trouvé pour l'utilisateur: {}", userId);
        } else {
            logger.debug("Documents trouvés pour l'utilisateur {}: {}", userId, documents);
        }

        return documents;
    }



    private byte[] encrypt(byte[] data) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}
