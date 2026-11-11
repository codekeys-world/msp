package com.codekeys.decaissement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraisDeGestionRepository extends JpaRepository<FraisDeGestion, Long> {
    Optional<FraisDeGestion> findBySlug(String slug);
    Optional<FraisDeGestion> findByAnnee(Integer annee);
    boolean existsByAnnee(Integer annee);
    boolean existsBySlug(String slug);

    @Query("""
            select new com.codekeys.decaissement.FraisDeGestionOption(fg.slug, fg.annee, fg.montantTotal)
            from FraisDeGestion fg
            order by fg.annee desc
            """)
    java.util.List<FraisDeGestionOption> listerOptions();

    @Override
    @EntityGraph(attributePaths = "justificatif")
    Page<FraisDeGestion> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "justificatif")
    @Query("""
            select distinct fg
            from FraisDeGestion fg
            left join fg.justificatif document
            where lower(coalesce(fg.slug, '')) like lower(concat('%', :terme, '%'))
               or lower(cast(fg.annee as string)) like lower(concat('%', :terme, '%'))
               or lower(coalesce(fg.observation, '')) like lower(concat('%', :terme, '%'))
               or lower(coalesce(document.nom, '')) like lower(concat('%', :terme, '%'))
            """)
    Page<FraisDeGestion> rechercherParInformation(@Param("terme") String terme, Pageable pageable);

    @Query("""
            select fg.slug, count(d.id),
                   coalesce(sum(case when d.statut <> :statutRejete then d.montant else 0 end), 0)
            from FraisDeGestion fg
            left join fg.depenses d
            where fg.slug in :slugs
            group by fg.slug
            """)
    java.util.List<Object[]> totauxDepensesParSlug(@Param("slugs") java.util.List<String> slugs,
                                                   @Param("statutRejete") StatutDepense statutRejete);
}
