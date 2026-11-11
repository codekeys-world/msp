package com.codekeys.enfant;

import com.codekeys.fichier.FileUtils;
import com.codekeys.fichier.GestionnaireFichier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnfantMapper {
    private final GestionnaireFichier gestionnaireFichier;
    public Enfant enfantRequestToEnfant(EnfantRequest enfantRequest) {
        String slug = UUID.randomUUID().toString();
        return Enfant.builder()
                .id(enfantRequest.getId())
                .slug(slug)
                .nom(enfantRequest.getNom())
                .prenom(enfantRequest.getPrenom())
                .telephonePrincipal(enfantRequest.getTelephonePrincipal())
                .telephoneSecondaire(enfantRequest.getTelephoneSecondaire())
                .dateNaissance(enfantRequest.getDateNaissance())
                .lieuNaissance(enfantRequest.getLieuNaissance())
                .genre(enfantRequest.getGenre())
                .adresse(enfantRequest.getAdresse())
                //.acteDeNaissance(this.gestionnaireFichier.saveFile("enfant", enfantRequest.getActeDeNaissance(), slug))
                //.certificatVieCollectif(this.gestionnaireFichier.saveFile("enfant", enfantRequest.getCertificatVieCollectif(), slug))
                .build();
    }

    public EnfantReponse enfantToEnfantReponse(Enfant enfant) {
        return EnfantReponse.builder()
                .slug(enfant.getSlug())
                .nom(enfant.getNom())
                .prenom(enfant.getPrenom())
                .telephonePrincipal(enfant.getTelephonePrincipal())
                .telephoneSecondaire(enfant.getTelephoneSecondaire())
                .dateNaissance(enfant.getDateNaissance())
                .lieuNaissance(enfant.getLieuNaissance())
                .genre(enfant.getGenre())
                .adresse(enfant.getAdresse())
                .nomActeNaissance(enfant.getActeDeNaissance() != null ? enfant.getActeDeNaissance().getNom() : null)
                .nomCertificatVieCollectif(enfant.getCertificatVieCollectif() != null
                        ? enfant.getCertificatVieCollectif().getNom() : null)
                //.acteDeNaissance(FileUtils.readFileFromLocation(enfant.getActeDeNaissance()))
                //.certificatVieCollectif(FileUtils.readFileFromLocation(enfant.getCertificatVieCollectif()))
                .nomCompletParent(enfant.getUtilisateur().getNomComplet())
                //.nomCompletValidateur(enfant.getValidateur().getNomComplet())
                //.dateValidation(enfant.getDateValidation())
                .build();
    }

    public EnfantReponseDetaillee enfantToEnfantReponseDetaillee(Enfant  enfant) {
        EnfantReponseDetaillee enfantReponseDetaillee = (EnfantReponseDetaillee) this.enfantToEnfantReponse(enfant);
        enfantReponseDetaillee.setDateCreation(enfant.getDateCreation());
        enfantReponseDetaillee.setDateModification(enfant.getDateModification());
        enfantReponseDetaillee.setCreerPar(enfant.getCreerPar());
        enfantReponseDetaillee.setModifierPar(enfant.getModifierPar());
        return enfantReponseDetaillee;
    }
}
