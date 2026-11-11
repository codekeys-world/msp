package com.codekeys.authorisation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorisationService {
    private final AuthorisationRepository  authorisationRepository;


    public Authorisation rechercherAuthorisationParId(Long authorisationId){
        return this.authorisationRepository.findById(authorisationId).orElseThrow(
                () -> new EntityNotFoundException("Aucune authorisation n'a été trouvée !")
        );
    }

    public Authorisation rechercherAuthorisationParNom(String authorisationNom){
        return this.authorisationRepository.findByNom(authorisationNom).orElseThrow(
                () -> new EntityNotFoundException("Aucune authorisation n'a été trouvée !")
        );
    }
}
