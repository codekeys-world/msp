package com.codekeys.role;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role findRoleById(Long id) {
        return this.roleRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("Aucun rôle n'a été trouvé !")
        );
    }

    public Role findRoleBySlug(String slug) {
        return this.roleRepository.findBySlug(slug).orElseThrow(
                ()-> new EntityNotFoundException("Aucun rôle n'a été trouvé !")
        );
    }

    public Role findRoleByNom(String nom) {
        return this.roleRepository.findByNom(nom).orElseThrow(
                ()-> new EntityNotFoundException("Aucun rôle n'a été trouvé !")
        );
    }
}
