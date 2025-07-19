package com.neo.app.documents;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "executions")
public class ExecutionEntity {

    @Id
    private String id; // ID unique
    @Field("nomDeCampagne")
    private String nomDeCampagne;
    @Field("dateMiseAJour") // Correspondance avec MongoDB
    @JsonProperty("dateMiseAjour") // Correspondance avec JSON reçu
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateMiseAJour;

    @Field("statut")
    private String statut = "En cours";
    @Field("demandePar")
    private String demandePar;
    private  String resultat = "En attente";
    private String userId;
    private List<String> historique;

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomDeCampagne() {
        return nomDeCampagne;
    }

    public void setNomDeCampagne(String nomDeCampagne) {
        this.nomDeCampagne = nomDeCampagne;
    }

    public LocalDate getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(LocalDate dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDemandePar() {
        return demandePar;
    }

    public void setDemandePar(String demandePar) {
        this.demandePar = demandePar;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getHistorique() {
        return historique;
    }

    public void setHistorique(List<String> historique) {
        this.historique = historique;
    }
}