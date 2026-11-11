package com.codekeys.journalAction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JournalActionRepository extends JpaRepository<JournalAction, Long>, JpaSpecificationExecutor<JournalAction> {

    Optional<JournalAction> findBySlug(String slug);

    long countByDateActionBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT j.matricule) FROM JournalAction j WHERE j.matricule IS NOT NULL AND j.matricule <> ''")
    long countDistinctUtilisateurs();

    long countByStatutIgnoreCase(String statut);
}
