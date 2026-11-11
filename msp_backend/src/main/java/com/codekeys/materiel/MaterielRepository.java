package com.codekeys.materiel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MaterielRepository extends JpaRepository<Materiel, Long> {
    Optional<Materiel> findBySlug(String slug);
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndSlugNot(String nom, String slug);

    @Query("""
            select distinct materiel
            from Materiel materiel
            where lower(coalesce(materiel.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(materiel.categorie, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(materiel.fournisseur, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(materiel.emplacement, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(materiel.numeroSerie, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cast(materiel.statut as string), '')) like lower(concat('%', :terme, '%'))
            """)
    Page<Materiel> rechercherParInformation(@Param("terme") String terme, Pageable pageable);
}
