package com.neo.app.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "campaigns")
public class CampagneEntity {

    @Id
    private String id; // ID de la base de données (MongoDB)
    private String titre;
    private String version;
    private int nbRapports;
    private LocalDate date; // LocalDate pour la date
    private int nbExecution;
    private String status; // Statut de la campagne (active, désactivée, terminée)
    private LocalDate startDate; // LocalDate pour la startDate
    private LocalDate endDate; // LocalDate pour la endDate
    private LocalDate createdAt; // LocalDate pour createdAt
    private LocalDate updatedAt; // LocalDate pour updatedAt
    private String userId; // Associer chaque campagne à un utilisateur
    private String id2; // Ajout du champ id2 pour identifier l'entité côté frontend
    private List<String> listedoc; // Liste des documents affectés (par leurs IDs)
    // Getters et Setters
    public String getId2() {
        return id2;  // Getter pour id2
    }

    public void setId2(String id2) {
        this.id2 = id2;  // Setter pour id2
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNbRapports() {
        return nbRapports;
    }

    public void setNbRapports(int nbRapports) {
        this.nbRapports = nbRapports;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getNbExecution() {
        return nbExecution;
    }

    public void setNbExecution(int nbExecution) {
        this.nbExecution = nbExecution;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public List<String> getListedoc() {
        return listedoc;
    }

    public void setListedoc(List<String> listedoc) {
        this.listedoc = listedoc;
    }
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

