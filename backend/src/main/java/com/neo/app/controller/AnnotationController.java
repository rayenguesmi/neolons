package com.neo.app.controller;

import com.neo.app.documents.AnnotationEntity;
import com.neo.app.repository.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationController {

    @Autowired
    private AnnotationRepository annotationRepository;

    // Création d'une annotation avec validation minimale
    @PostMapping
    public ResponseEntity<?> createAnnotation(@RequestBody AnnotationEntity annotation) {
        if (annotation.getDocumentId() == null || annotation.getDocumentId().isEmpty() ||
                annotation.getTexte() == null || annotation.getTexte().isEmpty()) {
            return ResponseEntity.badRequest().body("documentId et texte sont obligatoires");
        }
        annotation.setDateCreation(LocalDateTime.now());
        AnnotationEntity savedAnnotation = annotationRepository.save(annotation);
        return ResponseEntity.ok(savedAnnotation);
    }

    // Récupérer les annotations par documentId
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<AnnotationEntity>> getAnnotationsByDocument(@PathVariable String documentId) {
        List<AnnotationEntity> annotations = annotationRepository.findByDocumentId(documentId);
        if (annotations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(annotations);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnotation(@PathVariable String id) {
        if (!annotationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        annotationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
