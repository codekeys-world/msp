package com.codekeys.conjoint;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConjointService {
    private final ConjointRepository conjointRepository;
    private final ConjointMapper conjointMapper;
    private final UtilisateurService  utilisateurService;
    private final DocumentService documentService;

    public ConjointReponse ajouterConjoint(@Valid ConjointRequest conjointRequest, String adherentSlug,
                                           MultipartHttpServletRequest multipartRequest) throws IOException {
        MultipartFile acte = multipartRequest.getFile("acteDeMariage");
        if (acte == null || acte.isEmpty()) {
            throw new IllegalArgumentException("Le certificat de mariage du conjoint est obligatoire.");
        }
        Conjoint conjoint = this.conjointMapper.conjointRequestToConjoint(conjointRequest);
        Utilisateur utilisateur = this.utilisateurService.rechercherUtilisateurParSlug(adherentSlug);
        conjoint.setUtilisateur(utilisateur);
        utilisateur.ajouterConjoint(conjoint);
        conjoint.setActeDeMariage(this.documentService.uploadFile(
                acte, TypeDocument.CONJOINT, utilisateur.getSlug() + "/" + conjoint.getSlug()
        ));
        this.conjointRepository.save(conjoint);
        return conjointMapper.conjointToConjointReponse(conjoint);
    }

    public PageReponse<ConjointReponseDetaillee> listerConjointDetaillee(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<@NonNull Conjoint> conjoints = this.conjointRepository.findAll(pageable);
        List<ConjointReponseDetaillee> conjointReponseDetaillees = conjoints.stream()
                .map(this.conjointMapper::conjointToConjointReponseDetaillee)
                .toList();
        return new PageReponse<ConjointReponseDetaillee>(
                conjointReponseDetaillees,
                conjoints.getNumber(),
                conjoints.getSize(),
                conjoints.getTotalElements(),
                conjoints.getTotalPages(),
                conjoints.isFirst(),
                conjoints.isLast()
        );
    }

    public ConjointReponseDetaillee afficherConjoint(String conjointSlug) {
        Conjoint conjoint = this.rechercher(conjointSlug);
        return this.conjointMapper.conjointToConjointReponseDetaillee(conjoint);
    }



    public PageReponse<ConjointReponse> listerConjoint(String adherentSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<@NonNull Conjoint> conjoints = this.conjointRepository.findAllConjointByUtilisateurSlug(adherentSlug, pageable);
        List<ConjointReponse> conjointReponses = conjoints.stream()
                .map(this.conjointMapper::conjointToConjointReponse)
                .toList();
        return new PageReponse<ConjointReponse>(
                conjointReponses,
                conjoints.getNumber(),
                conjoints.getSize(),
                conjoints.getTotalElements(),
                conjoints.getTotalPages(),
                conjoints.isFirst(),
                conjoints.isLast()
        );
    }

    public ConjointReponse rechercherConjoint(String adherentSlug, String conjointSlug) {
        Conjoint conjoint = rechercherConjointParAdherent(adherentSlug, conjointSlug);
        return this.conjointMapper.conjointToConjointReponse(conjoint);
    }


    public boolean supprimerConjoint(String adherentSlug, String conjointSlug) throws IOException {
        Conjoint conjoint = rechercherConjointParAdherent(adherentSlug, conjointSlug);
        Utilisateur adherent = conjoint.getUtilisateur();
        this.documentService.supprimerFichier(conjoint.getActeDeMariage());
        adherent.enleverConjoint(conjoint);
        conjoint.setUtilisateur(null);
        this.conjointRepository.delete(conjoint);
        return true;
    }

    private Conjoint rechercher(String conjointSlug) {
        return this.conjointRepository.findConjointBySlug(conjointSlug).orElseThrow(
                ()-> new EntityNotFoundException("Aucun conjoint n'a été trouvé")
        );
    }

    private Conjoint rechercherConjointParAdherent(String adherentSlug, String conjointSlug) {
        Conjoint conjoint = this.conjointRepository.findConjointBySlugAndUtilisateurSlug(conjointSlug, adherentSlug).orElseThrow(
                ()-> new EntityNotFoundException("Aucun conjoint n'a été trouvé.")
        );
        return conjoint;
    }

    public ConjointReponse modifierConjoint(@Valid ConjointRequest conjointRequest, String adherentSlug, String conjointSlug,
                                            MultipartHttpServletRequest multipartRequest) throws IOException {
        Conjoint conjointBdd = this.rechercherConjointParAdherent(adherentSlug, conjointSlug);
        Conjoint nouveauInfoConjoint = this.conjointMapper.conjointRequestToConjoint(conjointRequest);
        com.codekeys.common.Genre genreAttendu = conjointBdd.getUtilisateur().getGenre() == com.codekeys.common.Genre.HOMME
                ? com.codekeys.common.Genre.FEMME : com.codekeys.common.Genre.HOMME;
        if (nouveauInfoConjoint.getGenre() != genreAttendu) {
            throw new IllegalArgumentException("Le genre du conjoint doit être opposé à celui de l'adhérent.");
        }
        conjointBdd.setTelephonePrincipal(nouveauInfoConjoint.getTelephonePrincipal());
        conjointBdd.setPrenom(nouveauInfoConjoint.getPrenom());
        conjointBdd.setNom(nouveauInfoConjoint.getNom());
        conjointBdd.setTelephoneSecondaire(nouveauInfoConjoint.getTelephoneSecondaire());
        conjointBdd.setDateNaissance(nouveauInfoConjoint.getDateNaissance());
        conjointBdd.setLieuNaissance(nouveauInfoConjoint.getLieuNaissance());
        conjointBdd.setAdresse(nouveauInfoConjoint.getAdresse());
        MultipartFile acte = multipartRequest.getFile("acteDeMariage");
        if (acte != null && !acte.isEmpty()) {
            com.codekeys.fichier.Document ancienActe = conjointBdd.getActeDeMariage();
            com.codekeys.fichier.Document nouvelActe = this.documentService.uploadFile(
                    acte, TypeDocument.CONJOINT,
                    conjointBdd.getUtilisateur().getSlug() + "/" + conjointBdd.getSlug()
            );
            if (ancienActe != null && !ancienActe.getChemin().equals(nouvelActe.getChemin())) {
                this.documentService.supprimerFichier(ancienActe);
            }
            conjointBdd.setActeDeMariage(nouvelActe);
        } else if (conjointBdd.getActeDeMariage() == null) {
            throw new IllegalArgumentException("Le certificat de mariage du conjoint est obligatoire.");
        }
        conjointBdd = this.conjointRepository.save(conjointBdd);
        return this.conjointMapper.conjointToConjointReponse(conjointBdd);
    }
}
