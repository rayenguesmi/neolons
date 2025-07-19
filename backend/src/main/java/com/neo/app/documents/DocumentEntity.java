package com.neo.app.documents;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

// Annotation MongoDB pour cette entité
@Document(collection = "documents")
public class DocumentEntity {

    // L'identifiant unique pour le document, généré automatiquement par MongoDB
    @Id
    private String id;

    private String filename; // Nom du fichier
    private String filetype; // Type du fichier (pdf, csv, xml, etc.)
    private Date uploadDate; // Date d'upload du fichier
    private byte[] data; // Données du fichier

    private String userId;  // Champ pour associer chaque document à un utilisateur

    // Constructeurs
    public DocumentEntity() {}

    public DocumentEntity(String filename, String filetype, Date uploadDate, byte[] data, String userId) {
        this.filename = filename;
        this.filetype = filetype;
        this.uploadDate = uploadDate;
        this.data = data;
        this.userId = userId;
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    // Méthode pour obtenir la date sans l'heure
    public String getFormattedUploadDate() {
        if (this.uploadDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // Format de date sans l'heure
            return sdf.format(this.uploadDate);
        }
        return null;  // Retourner null si uploadDate est null
    }
}
