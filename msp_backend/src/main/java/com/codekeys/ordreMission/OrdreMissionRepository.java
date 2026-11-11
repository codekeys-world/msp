package com.codekeys.ordreMission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OrdreMissionRepository extends JpaRepository<OrdreMission,Long> {
    Optional<OrdreMission> findBySlug(String slug);

    @Query("""
            select distinct mission
            from OrdreMission mission
            left join mission.ordreMission document
            left join mission.validationEntite validation
            left join validation.validateur validateur
            where lower(coalesce(mission.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.libelle, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.objet, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.imputationBudgetaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.moyenDeplacement, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.itineraire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.creerPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(mission.modifierPar, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.type, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validation.message, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.email, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.matricule, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.numeroInscription, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(validateur.telephoneSecondaire, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(mission.id as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(mission.dateDepart as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(mission.dateRetour as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(mission.dateCreation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(mission.dateModification as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateValidation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(validation.dateRefus as string)) like lower(concat('%', :terme, '%'))
            """)
    Page<OrdreMission> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @Query("""
            select count(ordreMission)
            from OrdreMission ordreMission
            left join ordreMission.validationEntite validation
            where validation is null
               or (validation.dateValidation is null and validation.dateRefus is null)
            """)
    long countEnAttenteValidation();

    @Query("""
            select count(ordreMission)
            from OrdreMission ordreMission
            where ordreMission.validationEntite.dateValidation is not null
            """)
    long countApprouvees();

    @Query("""
            select count(ordreMission)
            from OrdreMission ordreMission
            where ordreMission.validationEntite.dateRefus is not null
            """)
    long countRefusees();

    @Query("""
            select count(ordreMission)
            from OrdreMission ordreMission
            join ordreMission.validationEntite validation
            where ordreMission.dateDepart <= :date
              and ordreMission.dateRetour >= :date
              and validation.dateValidation is not null
            """)
    long countEnCours(@Param("date") LocalDate date);

    @Query("""
            select count(ordreMission)
            from OrdreMission ordreMission
            join ordreMission.validationEntite validation
            where ordreMission.dateRetour < :date
              and validation.dateValidation is not null
            """)
    long countTerminees(@Param("date") LocalDate date);
}
