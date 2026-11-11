package com.codekeys.CasSocial;

import com.codekeys.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CasSocialRepository extends JpaRepository<CasSocial, Long> {
    Optional<CasSocial> findBySlug(String slug);

    @Query("""
            select distinct cas
            from CasSocial cas
            join cas.adherent adherent
            left join cas.justificatif document
            where lower(coalesce(cas.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.nomDefunt, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.prenomDefunt, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.nomBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.prenomBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(cas.telephoneBeneficiaire, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.nom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.prenom, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(adherent.numeroInscription, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(cas.typeCas as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(cas.dateCas as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<CasSocial> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

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
