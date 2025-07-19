package com.neo.app.controller;
import com.neo.app.service.CampagneService;
import com.neo.app.documents.CampagneEntity;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;


@RestController
    @RequestMapping("/api/campaigns")
    public class CampagneController {
        private static final Logger logger = LoggerFactory.getLogger(CampagneController.class);
        private final CampagneService campagneService;

        public CampagneController(CampagneService campagneService) {
            this.campagneService = campagneService;
        }

        @GetMapping
        public List<CampagneEntity> getAllCampaigns() {
            return campagneService.getAllCampaigns();
        }
        @PostMapping
        public ResponseEntity<CampagneEntity> createCampaign(@RequestBody CampagneEntity campaign) {
            logger.info("Requête reçue pour créer la campagne: {}", campaign.getTitre());

            // Validation des champs si nécessaire
            if (campaign.getTitre() == null || campaign.getTitre().isEmpty()) {
                logger.error("Le titre de la campagne est manquant");
                return ResponseEntity.badRequest().body(null); // Titre manquant
            }

            if (campaign.getDate() == null) {
                logger.error("La date de la campagne est manquante");
                return ResponseEntity.badRequest().body(null); // Date manquante
            }

            if (campaign.getNbExecution() == 0) {
                logger.error("Le nombre d'exécutions est manquant");
                return ResponseEntity.badRequest().body(null); // Nb d'exécution manquant
            }


            try {
                logger.debug("Détails de la campagne: {}", campaign);

                // Enregistrement de la campagne dans la base de données avec id2
                CampagneEntity createdCampaign = campagneService.createCampaign(campaign);

                logger.info("Campagne créée avec succès: {}", createdCampaign.getTitre());
                return ResponseEntity.ok(createdCampaign); // Retourne la campagne créée avec un statut 200
            } catch (Exception e) {
                logger.error("Erreur lors de la création de la campagne: {}", campaign.getTitre(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Erreur serveur
            }
        }

        @PutMapping("/{id}")
        @CrossOrigin(origins = "http://localhost:4200")
        public ResponseEntity<CampagneEntity> updateCampaign(@PathVariable String id, @RequestBody CampagneEntity campaign) {
            return campagneService.updateCampaign(id, campaign)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/{id}")
        @CrossOrigin(origins = "http://localhost:4200")
        public ResponseEntity<Void> deleteCampaign(@PathVariable String id) {
            if (campagneService.deleteCampaign(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        }


    @PutMapping("/{id}/duplicate")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<CampagneEntity> duplicateCampaign(@PathVariable String id) {
        // Appel au service pour dupliquer la campagne
        Optional<CampagneEntity> duplicatedCampaign = campagneService.duplicateCampaign(id);

        // Vérifier si la duplication a réussi
        if (duplicatedCampaign.isPresent()) {
            return ResponseEntity.ok(duplicatedCampaign.get());  // Retourner la campagne dupliquée
        } else {
            return ResponseEntity.notFound().build();  // Retourner 404 si la campagne n'existe pas
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCampaigns() throws IOException {
        // Récupérer toutes les campagnes
        List<CampagneEntity> campagnes = campagneService.getAllCampaigns();

        // Créer un flux d'export en CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");

        // En-tête CSV
        writer.write("ID,Titre,Version,Date,NbRapports,NbExecution\n");

        // Données des campagnes
        for (CampagneEntity campagne : campagnes) {
            writer.write(String.format("%s,%s,%s,%s,%d,%d\n",
                    campagne.getId(),
                    campagne.getTitre(),
                    campagne.getVersion(),
                    campagne.getDate(),
                    campagne.getNbRapports(),
                    campagne.getNbExecution()
            ));
        }

        writer.flush();

        // Configuration de la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=campaigns.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }
    @PostMapping("/{campaignId}/assignDocs")
    public ResponseEntity<Map<String, String>> assignDocumentsToCampaign(
            @PathVariable String campaignId,
            @RequestBody List<String> files) {
        // Vérifie si la liste des fichiers est vide ou nulle
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("La liste des fichiers ne peut pas être vide.");
        }

        // Appelle le service pour affecter les documents à la campagne
        campagneService.assignDocumentsToCampaign(campaignId, files);

        // Retourne une réponse JSON avec un message de succès
        Map<String, String> response = new HashMap<>();
        response.put("message", "Documents affectés avec succès");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    @GetMapping("/{campaignId}/assignedDocs")
    public ResponseEntity<List<String>> getAssignedDocuments(@PathVariable String campaignId) {
        List<String> assignedDocs = campagneService.getAssignedDocuments(campaignId);
        return ResponseEntity.ok(assignedDocs);
    }




}
