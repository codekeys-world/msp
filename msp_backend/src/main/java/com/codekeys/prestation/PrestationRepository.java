package com.codekeys.prestation;

import com.codekeys.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrestationRepository extends JpaRepository<Prestation, Long> {
    Optional<Prestation> findBySlug(String slug);

    @Query("""
            select distinct pr
            from Prestation pr
            join pr.adherent adherent
            left join pr.justificatif document
            where lower(coalesce(pr.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.nomDefunt, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.prenomDefunt, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.nomBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.prenomBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(pr.telephoneBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.numeroInscription, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(pr.typePrestation as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(pr.datePrestation as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<Prestation> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @Query("""
            select adherent
            from Utilisateur adherent
            where lower(adherent.role.nom) = 'adherent'
              and (
                :terme = ''
                or lower(concat(coalesce(adherent.prenom, ''), ' ', coalesce(adherent.nom, ''))) like lower(concat('%', :terme, '%'))
                or lower(coalesce(adherent.numeroInscription, '')) like lower(concat('%', :terme, '%'))
                or lower(coalesce(adherent.matricule, '')) like lower(concat('%', :terme, '%'))
                or lower(coalesce(adherent.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
              )
            """)
    Page<Utilisateur> rechercherAdherents(@Param("terme") String terme, Pageable pageable);
}
