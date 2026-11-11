package com.codekeys.fichier;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface DocumentRepository extends JpaRepository<@NonNull Document, @NonNull Long> {
    Optional<Document> findByChemin(String chemin);

    Page<Document> findByTypeDocument(TypeDocument typeDocument, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.typeDocument = :typeDocument AND (LOWER(d.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR LOWER(d.chemin) LIKE LOWER(CONCAT('%', :terme, '%')))")
    Page<Document> findByTypeDocumentAndSearch(@Param("typeDocument") TypeDocument typeDocument, @Param("terme") String terme, Pageable pageable);

    Page<Document> findByTypeDocumentIn(List<TypeDocument> types, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.typeDocument IN :types AND (LOWER(d.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR LOWER(d.chemin) LIKE LOWER(CONCAT('%', :terme, '%')))")
    Page<Document> findByTypeDocumentInAndSearch(@Param("types") List<TypeDocument> types, @Param("terme") String terme, Pageable pageable);
}
