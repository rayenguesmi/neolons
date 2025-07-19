package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "electronic_signatures")
public class ElectronicSignature {

    @Id
    private String id;

    @NotBlank(message = "L'identifiant de signature est obligatoire")
    private String signatureId;

    private SignatureStatus status = SignatureStatus.PENDING;

    private String signatureData;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime signedAt;

    // Relations avec d'autres entités Mongo
    @DBRef
    private DocumentEntity document;

    @DBRef
    private UserEntity signer;

    public enum SignatureStatus {
        PENDING, SIGNED, REJECTED, EXPIRED
    }

    // ----- Constructeurs -----
    public ElectronicSignature() {}

    public ElectronicSignature(String signatureId, SignatureStatus status, String signatureData,
                               LocalDateTime requestedAt, LocalDateTime signedAt,
                               DocumentEntity document, UserEntity signer) {
        this.signatureId = signatureId;
        this.status = status;
        this.signatureData = signatureData;
        this.requestedAt = requestedAt;
        this.signedAt = signedAt;
        this.document = document;
        this.signer = signer;
    }

    // ----- Getters et Setters -----
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getSignatureId() { return signatureId; }

    public void setSignatureId(String signatureId) { this.signatureId = signatureId; }

    public SignatureStatus getStatus() { return status; }

    public void setStatus(SignatureStatus status) { this.status = status; }

    public String getSignatureData() { return signatureData; }

    public void setSignatureData(String signatureData) { this.signatureData = signatureData; }

    public LocalDateTime getRequestedAt() { return requestedAt; }

    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getSignedAt() { return signedAt; }

    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public DocumentEntity getDocument() { return document; }

    public void setDocument(DocumentEntity document) { this.document = document; }

    public UserEntity getSigner() { return signer; }

    public void setSigner(UserEntity signer) { this.signer = signer; }
}
