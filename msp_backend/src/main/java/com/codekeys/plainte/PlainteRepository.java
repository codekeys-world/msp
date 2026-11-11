package com.codekeys.plainte;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlainteRepository extends JpaRepository<Plainte, Long> {
    Optional<Plainte> findBySlug(String slug);

    @Query("""
            select distinct plainte
            from Plainte plainte
            left join plainte.documentPlainte document
            left join plainte.validationEntite validation
            left join validation.validateur validateur
            where lower(coalesce(plainte.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(plainte.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(plainte.creerPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(plainte.modifierPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validation.message, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(plainte.id as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(plainte.datePv as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(plainte.dateCreation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateValidation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateRefus as string)) like lower(concat('%', :terme, '%'))
            """)
    Page<Plainte> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @Query("""
            select count(plainte)
            from Plainte plainte
            left join plainte.validationEntite validation
            where validation is null
               or (validation.dateValidation is null and validation.dateRefus is null)
            """)
    long countEnAttente();

    @Query("select count(plainte) from Plainte plainte where plainte.validationEntite.dateValidation is not null")
    long countTraitees();

    @Query("select count(plainte) from Plainte plainte where plainte.validationEntite.dateRefus is not null")
    long countRefusees();
}
