package com.codekeys.conjoint;

import com.codekeys.fichier.FileUtils;
import com.codekeys.fichier.GestionnaireFichier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConjointMapper {
    private final GestionnaireFichier gestionnaireFichier;
    public Conjoint conjointRequestToConjoint(ConjointRequest conjointRequest){
        String slug = UUID.randomUUID().toString();
        return Conjoint.builder()
                .id(conjointRequest.getId())
                .slug(slug)
                .nom(conjointRequest.getNom())
                .prenom(conjointRequest.getPrenom())
                .telephonePrincipal(conjointRequest.getTelephonePrincipal())
                .telephoneSecondaire(conjointRequest.getTelephoneSecondaire())
                .dateNaissance(conjointRequest.getDateNaissance())
                .lieuNaissance(conjointRequest.getLieuNaissance())
                .adresse(conjointRequest.getAdresse())
                .genre(conjointRequest.getGenre())
                //.acteDeMariage(this.gestionnaireFichier.saveFile("conjoint", conjointRequest.getActeDeMariage(), slug))
                .build();
    }

    public ConjointReponse conjointToConjointReponse(Conjoint conjoint){
        return ConjointReponse.builder()
                .slug(conjoint.getSlug())
                .nom(conjoint.getNom())
                .prenom(conjoint.getPrenom())
                .telephonePrincipal(conjoint.getTelephonePrincipal())
                .telephoneSecondaire(conjoint.getTelephoneSecondaire())
                .dateNaissance(conjoint.getDateNaissance())
                .lieuNaissance(conjoint.getLieuNaissance())
                .adresse(conjoint.getAdresse())
                .genre(conjoint.getGenre())
                .nomActeMariage(conjoint.getActeDeMariage() != null ? conjoint.getActeDeMariage().getNom() : null)
                //.acteDeMariage(FileUtils.readFileFromLocation(conjoint.getActeDeMariage()))
                .nomCompletConjoint(conjoint.getUtilisateur().getNomComplet())
                //.nomCompletValidateur(conjoint.getValidateur().getNomComplet())
                //.dateValidation(conjoint.getDateValidation())
                .build();
    }

    public ConjointReponseDetaillee conjointToConjointReponseDetaillee(Conjoint conjoint) {
        ConjointReponseDetaillee conjointReponseDetaillee = (ConjointReponseDetaillee) this.conjointToConjointReponse(conjoint);
        conjointReponseDetaillee.setDateCreation(conjoint.getDateCreation());
        conjointReponseDetaillee.setDateModification(conjoint.getDateModification());
        conjointReponseDetaillee.setCreerPar(conjoint.getCreerPar());
        conjointReponseDetaillee.setModifierPar(conjoint.getModifierPar());
        return conjointReponseDetaillee;
    }
}
