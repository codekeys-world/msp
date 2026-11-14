package com.codekeys.assemblee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssembleeRepository extends JpaRepository<Assemblee, Long> {
    Optional<Assemblee> findBySlug(String slug);
    Page<Assemblee> findByNomContainingIgnoreCaseOrLieuContainingIgnoreCase(String nom, String lieu, Pageable pageable);
}
