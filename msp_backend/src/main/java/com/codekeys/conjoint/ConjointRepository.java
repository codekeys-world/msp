package com.codekeys.conjoint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConjointRepository extends JpaRepository<Conjoint, Long> {
    Optional<Conjoint> findConjointBySlug(String slug);

    Page<Conjoint> findAllConjointByUtilisateurSlug(String utilisateurSlug, Pageable pageable);

    Optional<Conjoint> findConjointBySlugAndUtilisateurSlug(String conjointSlug, String utilisateurSlug);
}
