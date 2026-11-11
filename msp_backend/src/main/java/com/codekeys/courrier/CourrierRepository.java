package com.codekeys.courrier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourrierRepository extends JpaRepository<Courrier, Long> {
    Optional<Courrier> findBySlug(String slug);
    boolean existsByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCaseAndSlugNot(String nom, String slug);

    long countByTypeCourrier(TypeCourrier typeCourrier);

    @Query("""
            select count(courrier)
            from Courrier courrier
            left join courrier.validationEntite validation
            where validation is null
               or (validation.dateValidation is null and validation.dateRefus is null)
            """)
    long countEnAttente();

    @Query("select count(courrier) from Courrier courrier where courrier.validationEntite.dateValidation is not null")
    long countTraites();

    @Query("select count(courrier) from Courrier courrier where courrier.validationEntite.dateRefus is not null")
    long countRefuses();

    @Query("""
            select distinct courrier
            from Courrier courrier
            left join courrier.documentCourrier document
            left join courrier.validationEntite validation
            where lower(coalesce(courrier.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(courrier.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(courrier.objet, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(courrier.description, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cast(courrier.typeCourrier as string), '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(courrier.creerPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(courrier.modifierPar, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(courrier.dateArrivee as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(courrier.dateDepart as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(courrier.dateCreation as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validation.message, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<Courrier> rechercherParInformation(@Param("terme") String terme, Pageable pageable);
}
