package com.neo.app.controller;

import com.neo.app.documents.ExecutionEntity;
import com.neo.app.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionController.class);
    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public ResponseEntity<List<ExecutionEntity>> getExecutions(@RequestParam(required = false) String userId) {
        List<ExecutionEntity> executions;

        if (userId != null) {
            executions = executionService.getExecutionsByUserId(userId);
        } else {
            executions = executionService.getAllExecutions();
        }

        if (executions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(executions);
    }

    @PostMapping
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<ExecutionEntity> addExecution(@RequestBody ExecutionEntity execution) {
        // Vérifier si l'ID est fourni par Angular
        if (execution.getId() != null) {
            Optional<ExecutionEntity> existingExecution = executionService.getExecutionById(execution.getId());
            if (existingExecution.isPresent()) {
                return ResponseEntity.badRequest().body(null); // Empêcher l'ajout d'un ID existant
            }
        }

        if (execution.getStatut() == null) {
            execution.setStatut("En cours");
        }
        if (execution.getResultat() == null) {
            execution.setResultat("En attente");
        }

        ExecutionEntity createdExecution = executionService.addExecution(execution);
        return ResponseEntity.ok(createdExecution);
    }

    @PutMapping("/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<ExecutionEntity> updateExecution(@PathVariable String id, @RequestBody ExecutionEntity execution) {
        return executionService.updateExecution(id, execution).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Void> deleteExecution(@PathVariable String id) {
        if (executionService.deleteExecution(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExecutions() throws IOException {
        byte[] data = executionService.exportExecutions();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=executions.csv");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
