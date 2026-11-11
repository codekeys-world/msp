package com.codekeys.encaissement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface EncaissementRepository extends JpaRepository<Encaissement, Long> {
    @EntityGraph(attributePaths = "justificatif")
    Optional<Encaissement> findBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = "justificatif")
    Page<Encaissement> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "justificatif")
    @Query("""
            select en
            from Encaissement en
            left join en.justificatif document
            where lower(coalesce(en.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(en.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(en.typeEncaissement as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(en.dateEncaissement as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<Encaissement> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    /**
     * Récupère les encaissements d'une année donnée pour le rapport de situation.
     * Retourne directement les lignes du rapport sous forme de DTO.
     */
    @Query("""
            select new com.codekeys.encaissement.EncaissementSituationResponse(
                   EXTRACT(MONTH FROM en.dateEncaissement),
                   en.typeEncaissement,
                   COUNT(en),
                   SUM(en.montant))
            from Encaissement en
            where en.dateEncaissement >= :debut and en.dateEncaissement < :fin
            group by EXTRACT(MONTH FROM en.dateEncaissement), en.typeEncaissement
            order by EXTRACT(MONTH FROM en.dateEncaissement), en.typeEncaissement
            """)
    List<EncaissementSituationResponse> situationParAnnee(
            @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);


}
