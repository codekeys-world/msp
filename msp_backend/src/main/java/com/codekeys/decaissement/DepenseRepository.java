package com.codekeys.decaissement;

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
public interface DepenseRepository extends JpaRepository<Depense, Long> {
    @EntityGraph(attributePaths = {"fraisDeGestion", "justificatif"})
    Optional<Depense> findBySlug(String slug);

    @EntityGraph(attributePaths = {"fraisDeGestion", "justificatif"})
    Page<Depense> findByFraisDeGestionSlug(String fraisDeGestionSlug, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"fraisDeGestion", "justificatif"})
    Page<Depense> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"fraisDeGestion", "justificatif"})
    @Query("""
            select d
            from Depense d
            left join d.justificatif document
            where lower(coalesce(d.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(d.libelle, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(d.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(d.statut as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(d.dateDepense as string)) like lower(concat('%', :terme, '%'))
               or lower(cast(d.montant as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<Depense> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @EntityGraph(attributePaths = {"fraisDeGestion", "justificatif"})
    @Query("""
            select d
            from Depense d
            left join d.justificatif document
            where d.fraisDeGestion.slug = :fraisDeGestionSlug
               and (lower(coalesce(d.slug, '')) like lower(concat('%', :terme, '%'))
                  or lower(coalesce(d.libelle, '')) like lower(concat('%', :terme, '%'))
                  or lower(coalesce(d.observation, '')) like lower(concat('%', :terme, '%'))
                  or lower(cast(d.statut as string)) like lower(concat('%', :terme, '%'))
                  or lower(cast(d.dateDepense as string)) like lower(concat('%', :terme, '%'))
                  or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%')))
            """)
    Page<Depense> rechercherParFraisDeGestion(@Param("fraisDeGestionSlug") String fraisDeGestionSlug, @Param("terme") String terme, Pageable pageable);

    long countByFraisDeGestionSlugAndStatut(String fraisDeGestionSlug, StatutDepense statut);

    @Query("""
            select coalesce(sum(d.montant), 0)
            from Depense d
            where d.fraisDeGestion.slug = :slug
              and d.statut = :statut
            """)
    long sommeValideeParFraisDeGestion(@Param("slug") String fraisDeGestionSlug,
                                       @Param("statut") StatutDepense statut);

    /**
     * Récupère les dépenses d'une année donnée pour le rapport de situation.
     * Retourne un tableau Object[] : { mois (Integer), libellé (String), statut (String), count (Long), sumMontant (Long) }
     */
    @Query("""
            select EXTRACT(MONTH FROM d.dateDepense) as mois,
                   d.libelle as libelle,
                   d.statut as statut,
                   COUNT(d.id) as count,
                   SUM(d.montant) as total
            from Depense d
            where d.dateDepense >= :debut and d.dateDepense < :fin
            group by EXTRACT(MONTH FROM d.dateDepense), d.libelle, d.statut
            order by mois, libelle, statut
            """)
    List<Object[]> situationParAnnee(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
