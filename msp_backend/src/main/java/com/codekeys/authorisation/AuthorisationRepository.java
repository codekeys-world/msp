package com.codekeys.authorisation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorisationRepository extends JpaRepository<Authorisation, Long> {
    Optional<Authorisation> findByNom(String nom);
    boolean existsByNom(String nom);
}
