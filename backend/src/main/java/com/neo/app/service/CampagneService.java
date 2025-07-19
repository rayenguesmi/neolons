package com.neo.app.service;

import com.neo.app.documents.CampagneEntity;
import com.neo.app.repository.CampagneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CampagneService {

    @Autowired
    private CampagneRepository campagneRepository;

    // Ajouter une nouvelle campagne
    public CampagneEntity createCampaign(CampagneEntity campaign) {
        campaign.setCreatedAt(LocalDate.now());
        campaign.setUpdatedAt(LocalDate.now());
        return campagneRepository.save(campaign);
    }

    // Récupérer toutes les campagnes
    public List<CampagneEntity> getAllCampaigns() {
        return campagneRepository.findAll();
    }
    public List<CampagneEntity> getCampaignsByUserId(String userId) {
        return campagneRepository.findByUserId(userId);
    }
    public Optional<CampagneEntity> updateCampaignByUserId(String userId, String id, CampagneEntity campaign) {
        // Rechercher la campagne par l'id et vérifier si elle appartient à l'utilisateur
        Optional<CampagneEntity> existingCampaign = campagneRepository.findById(id);

        if (existingCampaign.isPresent() && existingCampaign.get().getUserId().equals(userId)) {
            // Si la campagne existe et appartient à l'utilisateur, mettre à jour les champs nécessaires
            CampagneEntity updatedCampaign = existingCampaign.get();
            updatedCampaign.setTitre(campaign.getTitre());
            updatedCampaign.setVersion(campaign.getVersion());
            updatedCampaign.setNbRapports(campaign.getNbRapports());
            updatedCampaign.setDate(campaign.getDate());
            updatedCampaign.setNbExecution(campaign.getNbExecution());

            // Sauvegarder la campagne mise à jour dans la base de données
            return Optional.of(campagneRepository.save(updatedCampaign));
        }

        return Optional.empty(); // Si la campagne n'existe pas ou n'appartient pas à l'utilisateur
    }

    // Récupérer une campagne par ID
    public Optional<CampagneEntity> getCampaignById(String id) {
        return campagneRepository.findById(id);
    }

    // Mettre à jour une campagne existante
    public Optional<CampagneEntity> updateCampaign(String id, CampagneEntity updatedCampaign) {
        return campagneRepository.findById(id).map(existingCampaign -> {
            updatedCampaign.setId(existingCampaign.getId());
            updatedCampaign.setCreatedAt(existingCampaign.getCreatedAt());
            updatedCampaign.setUpdatedAt(LocalDate.now());
            return campagneRepository.save(updatedCampaign);
        });
    }

    // Supprimer une campagne
    public boolean deleteCampaign(String id) {
        if (campagneRepository.existsById(id)) {
            campagneRepository.deleteById(id);
            return true;
        }
        return false;
    }


    // Dupliquer une campagne
    public Optional<CampagneEntity> duplicateCampaign(String id) {
        // Récupérer la campagne d'origine
        Optional<CampagneEntity> existingCampaign = campagneRepository.findById(id);
        if (existingCampaign.isPresent()) {
            CampagneEntity original = existingCampaign.get();

            // Trouver le dernier ID existant dans la base
            int lastId = campagneRepository.findAll()
                    .stream()
                    .mapToInt(campaign -> Integer.parseInt(campaign.getId()))
                    .max()
                    .orElse(0); // Par défaut, 0 si aucune campagne n'existe

            // Incrémenter l'ID
            int nextId = lastId + 1;

            // Créer une nouvelle campagne à partir de l'original
            CampagneEntity duplicate = new CampagneEntity();
            duplicate.setId(String.valueOf(nextId)); // Attribuer un nouvel ID
            duplicate.setTitre(original.getTitre());
            duplicate.setVersion(original.getVersion());
            duplicate.setDate(original.getDate());
            duplicate.setNbRapports(original.getNbRapports());
            duplicate.setNbExecution(original.getNbExecution());


            // Sauvegarder la nouvelle entité
            campagneRepository.save(duplicate);

            return Optional.of(duplicate);
        }
        return Optional.empty(); // Retourner vide si la campagne n'existe pas
    }
    public void assignDocumentsToCampaign(String campaignId, List<String> documentIds) {
        // Récupérer la campagne
        CampagneEntity campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        // Mettre à jour la liste des documents
        campagne.setListedoc(documentIds);
        campagne.setUpdatedAt(LocalDate.now()); // Mise à jour de la date de modification

        // Sauvegarder la campagne
        campagneRepository.save(campagne);

    }
    public List<String> getAssignedDocuments(String campaignId) {
        // Récupérer la campagne de la base de données par son ID
        CampagneEntity campagne = campagneRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne avec l'ID " + campaignId + " non trouvée"));

        return campagne.getListedoc();  // assuming 'listedoc' contains the filenames
    }



}

