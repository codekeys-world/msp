package com.codekeys.convocation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {
    Optional<Convocation> findBySlug(String slug);

    @Query("""
            select distinct convocation
            from Convocation convocation
            left join convocation.documentConvocation document
            left join convocation.validationEntite validation
            left join validation.validateur validateur
            where lower(coalesce(convocation.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(convocation.description, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(convocation.creerPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(convocation.modifierPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validation.message, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(convocation.id as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(convocation.dateConvocation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(convocation.dateCreation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateValidation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateRefus as string)) like lower(concat('%', :terme, '%'))
            """)
    Page<Convocation> rechercherParInformation(@Param("terme") String terme, Pageable pageable);
}
