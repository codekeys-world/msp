package com.codekeys.pv;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PvRepository extends JpaRepository<Pv, Long> {
    Optional<Pv> findBySlug(String slug);
    boolean existsByNumeroIgnoreCase(String numero);
    boolean existsByNumeroIgnoreCaseAndSlugNot(String numero, String slug);

    @Query("""
            select distinct pv
            from Pv pv
            left join pv.documentPv document
            left join pv.validationEntite validation
            left join validation.validateur validateur
            where lower(coalesce(pv.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.numero, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.objet, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.type, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.creerPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pv.modifierPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validation.message, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(pv.id as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(pv.datePv as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(pv.dateCreation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateValidation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateRefus as string)) like lower(concat('%', :terme, '%'))
            """)
    Page<Pv> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @Query("""
            select count(pv)
            from Pv pv
            left join pv.validationEntite validation
            where validation is null
               or (validation.dateValidation is null and validation.dateRefus is null)
            """)
    long countEnAttenteValidation();

    @Query("select count(pv) from Pv pv where pv.validationEntite.dateValidation is not null")
    long countApprouves();

    @Query("select count(pv) from Pv pv where pv.validationEntite.dateRefus is not null")
    long countRefuses();
}
