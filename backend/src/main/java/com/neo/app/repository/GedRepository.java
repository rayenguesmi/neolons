package com.neo.app.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GedRepository {
    @Query("SELECT MAX(g.version) FROM GedDocument g WHERE g.nomDocument = :nomDocument")
    Optional<Integer> findLastVersion(@Param("nomDocument") String nomDocument);

}
