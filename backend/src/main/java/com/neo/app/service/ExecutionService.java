package com.neo.app.service;

import com.neo.app.documents.ExecutionEntity;
import com.neo.app.repository.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExecutionService {

    @Autowired
    private ExecutionRepository executionRepository;

    public ExecutionEntity addExecution(ExecutionEntity execution) {
       // execution.setDateMiseAJour(LocalDate.now());
        return executionRepository.save(execution);
    }

    public Optional<ExecutionEntity> updateExecution(String id, ExecutionEntity updatedExecution) {
        return executionRepository.findById(id).map(existingExecution -> {
            // Mettre à jour les valeurs de l'exécution existante avec celles de l'exécution mise à jour
            updatedExecution.setId(existingExecution.getId());
            if (updatedExecution.getNomDeCampagne() != null && !updatedExecution.getNomDeCampagne().isEmpty()) {
                existingExecution.setNomDeCampagne(updatedExecution.getNomDeCampagne());
            }
            // Si 'nomDeCampagne' est null, conserver la valeur existante
            if (updatedExecution.getNomDeCampagne() == null) {
                updatedExecution.setNomDeCampagne(existingExecution.getNomDeCampagne());
            }

            // Si 'dateMiseAJour' est null, conserver la valeur existante
            if (updatedExecution.getDateMiseAJour() == null) {
                updatedExecution.setDateMiseAJour(existingExecution.getDateMiseAJour());
            }

            // Si 'statut' est null, conserver la valeur existante
            if (updatedExecution.getStatut() == null) {
                updatedExecution.setStatut(existingExecution.getStatut());
            }

            // Si 'demandePar' est null, conserver la valeur existante
            if (updatedExecution.getDemandePar() == null) {
                updatedExecution.setDemandePar(existingExecution.getDemandePar());
            }

            // Si 'resultat' est null, conserver la valeur existante
            if (updatedExecution.getResultat() == null) {
                updatedExecution.setResultat(existingExecution.getResultat());
            }

            // Si 'userId' est null, conserver la valeur existante
            if (updatedExecution.getUserId() == null) {
                updatedExecution.setUserId(existingExecution.getUserId());
            }

            // Mettre à jour la date de mise à jour avec la date actuelle
            updatedExecution.setDateMiseAJour(LocalDate.now());

            // Sauvegarder l'entité mise à jour
            return executionRepository.save(updatedExecution);
        });
    }


    public boolean deleteExecution(String id) {
        if (executionRepository.existsById(id)) {
            executionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<ExecutionEntity> getAllExecutions() {
        return executionRepository.findAll();
    }
    public Optional<ExecutionEntity> getExecutionById(String id) {
        return executionRepository.findById(id);
    }
    public List<ExecutionEntity> getExecutionsByUserId(String userId) {
        return executionRepository.findByUserId(userId);
    }

    public byte[] exportExecutions() {
        // Générer un export CSV
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Nom de campagne,Date Mise à Jour,Statut,Demande Par,Resultat\n");

        for (ExecutionEntity execution : getAllExecutions()) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s,%s\n",
                    execution.getId(),
                    execution.getNomDeCampagne(),
                    execution.getDateMiseAJour(),
                    execution.getStatut(),
                    execution.getDemandePar(),
                    execution.getResultat()));
        }

        return csvBuilder.toString().getBytes();
    }


 
}
