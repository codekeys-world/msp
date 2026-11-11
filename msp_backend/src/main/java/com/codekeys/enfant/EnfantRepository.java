package com.codekeys.enfant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnfantRepository extends JpaRepository<Enfant, Long> {
    Optional<Enfant> findEnfantBySlugAndUtilisateurSlug(String slugEnfant, String slugUtilisateur);
    Page<Enfant> findAllByUtilisateurSlug(String slugUtilisateur, Pageable pageable);

    boolean deleteBySlugAndUtilisateurSlug(String slugEnfant, String slugUtilisateur);
    Optional<Enfant> findEnfantBySlug(String slugEnfant);


}
