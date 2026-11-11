package com.codekeys.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findBySlug(String slug);
    Optional<Role> findByNom(String nom);
    boolean existsByNom(String nom);
}
