package com.codekeys.adjoint;

import com.codekeys.utilisateur.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdjointRepository extends JpaRepository<Adjoint, Long> {

    Optional<Adjoint> findBySlug(String slug);

    @Query("SELECT a FROM Adjoint a WHERE a.delegant.slug = :delegantSlug")
    Page<Adjoint> findByDelegantSlug(@Param("delegantSlug") String delegantSlug, Pageable pageable);

    @Query("SELECT a FROM Adjoint a WHERE a.delegant.slug = :delegantSlug AND a.roleDelegue = :roleDelegue")
    List<Adjoint> findByDelegantSlugAndRoleDelegue(@Param("delegantSlug") String delegantSlug, @Param("roleDelegue") String roleDelegue);

    @Query("SELECT a FROM Adjoint a WHERE a.delegant.slug = :delegantSlug AND a.roleDelegue = :roleDelegue AND a.estActif = true")
    Optional<Adjoint> findActiveByDelegantSlugAndRoleDelegue(@Param("delegantSlug") String delegantSlug, @Param("roleDelegue") String roleDelegue);

    @Query("SELECT a FROM Adjoint a WHERE a.adjoint.slug = :adjointSlug")
    Page<Adjoint> findByAdjointSlug(@Param("adjointSlug") String adjointSlug, Pageable pageable);

    @Query("SELECT a FROM Adjoint a WHERE a.estActif = true AND a.dateDebut <= :date AND (a.dateFin IS NULL OR a.dateFin >= :date)")
    Page<Adjoint> findActifsParDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT a FROM Adjoint a WHERE a.estActif = true AND a.dateDebut <= :date AND (a.dateFin IS NULL OR a.dateFin >= :date) AND a.roleDelegue = :roleDelegue")
    List<Adjoint> findActifsParDateEtRole(@Param("date") LocalDate date, @Param("roleDelegue") String roleDelegue);

}
