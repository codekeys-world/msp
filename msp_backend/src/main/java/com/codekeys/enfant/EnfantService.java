package com.codekeys.enfant;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
public class EnfantService {
    private final EnfantRepository enfantRepository;
    private final EnfantMapper enfantMapper;
    private final UtilisateurService utilisateurService;
    private final DocumentService documentService;


    public EnfantReponse enregister(EnfantRequest enfantRequest, String adherentSlug,
                                    MultipartHttpServletRequest multipartRequest) throws IOException {
        MultipartFile acte = multipartRequest.getFile("acteDeNaissance");
        MultipartFile certificat = multipartRequest.getFile("certificatVieCollectif");
        if (acte == null || acte.isEmpty()) {
            throw new IllegalArgumentException("L'acte de naissance du descendant est obligatoire.");
        }
        if (certificat == null || certificat.isEmpty()) {
            throw new IllegalArgumentException("Le certificat de vie collectif du descendant est obligatoire.");
        }
        Enfant enfant = this.enfantMapper.enfantRequestToEnfant(enfantRequest);
        Utilisateur parent = this.utilisateurService.rechercherUtilisateurParSlug(adherentSlug);
        enfant.setUtilisateur(parent);
        parent.ajouterEnfant(enfant);
        enfant.setActeDeNaissance(this.documentService.uploadFile(
                acte, TypeDocument.DESCENDANT, parent.getSlug() + "/" + enfant.getSlug()
        ));
        enfant.setCertificatVieCollectif(this.documentService.uploadFile(
                certificat, TypeDocument.CERTIFICAT_VIE_COLLECTIF,
                parent.getSlug() + "/" + enfant.getSlug()
        ));
        enfant = this.enfantRepository.save(enfant);
        return this.enfantMapper.enfantToEnfantReponse(enfant);
    }

    public EnfantReponse rechercherEnfantParSlug(String adherentSlug, String enfantSlug) {
        Enfant enfant = this.rechercherEnfant(adherentSlug, enfantSlug);
        return this.enfantMapper.enfantToEnfantReponse(enfant);
    }



    public PageReponse<EnfantReponse> listerEnfantParAdherent(String adherentSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,  Sort.by("id").descending());
        Page<Enfant> enfants = this.enfantRepository.findAllByUtilisateurSlug(adherentSlug, pageable);
        List<EnfantReponse> enfantReponse = enfants.stream().map(this.enfantMapper::enfantToEnfantReponse).toList();
        return new PageReponse<EnfantReponse>(
                enfantReponse,
                enfants.getNumber(),
                enfants.getSize(),
                enfants.getTotalElements(),
                enfants.getTotalPages(),
                enfants.isFirst(),
                enfants.isLast()
        );
    }

    public EnfantReponse modifierEnfant(@Valid EnfantRequest enfantRequest, String adherentSlug, String enfantSlug,
                                        MultipartHttpServletRequest multipartRequest) throws IOException {
        Enfant enfantBdd = this.rechercherEnfant(adherentSlug, enfantSlug);
        Enfant nouveauInfoEnfant = this.enfantMapper.enfantRequestToEnfant(enfantRequest);
        enfantBdd.setTelephonePrincipal(nouveauInfoEnfant.getTelephonePrincipal());
        enfantBdd.setPrenom(nouveauInfoEnfant.getPrenom());
        enfantBdd.setTelephoneSecondaire(nouveauInfoEnfant.getTelephoneSecondaire());
        enfantBdd.setDateNaissance(nouveauInfoEnfant.getDateNaissance());
        enfantBdd.setLieuNaissance(nouveauInfoEnfant.getLieuNaissance());
        enfantBdd.setAdresse(nouveauInfoEnfant.getAdresse());
        enfantBdd.setNom(nouveauInfoEnfant.getNom());
        enfantBdd.setGenre(nouveauInfoEnfant.getGenre());
        MultipartFile acte = multipartRequest.getFile("acteDeNaissance");
        if (acte != null && !acte.isEmpty()) {
            com.codekeys.fichier.Document ancienActe = enfantBdd.getActeDeNaissance();
            com.codekeys.fichier.Document nouvelActe = this.documentService.uploadFile(
                    acte, TypeDocument.DESCENDANT,
                    enfantBdd.getUtilisateur().getSlug() + "/" + enfantBdd.getSlug()
            );
            if (ancienActe != null && !ancienActe.getChemin().equals(nouvelActe.getChemin())) {
                this.documentService.supprimerFichier(ancienActe);
            }
            enfantBdd.setActeDeNaissance(nouvelActe);
        } else if (enfantBdd.getActeDeNaissance() == null) {
            throw new IllegalArgumentException("L'acte de naissance du descendant est obligatoire.");
        }
        MultipartFile certificat = multipartRequest.getFile("certificatVieCollectif");
        if (certificat != null && !certificat.isEmpty()) {
            com.codekeys.fichier.Document ancienCertificat = enfantBdd.getCertificatVieCollectif();
            com.codekeys.fichier.Document nouveauCertificat = this.documentService.uploadFile(
                    certificat, TypeDocument.CERTIFICAT_VIE_COLLECTIF,
                    enfantBdd.getUtilisateur().getSlug() + "/" + enfantBdd.getSlug()
            );
            if (ancienCertificat != null && !ancienCertificat.getChemin().equals(nouveauCertificat.getChemin())) {
                this.documentService.supprimerFichier(ancienCertificat);
            }
            enfantBdd.setCertificatVieCollectif(nouveauCertificat);
        } else if (enfantBdd.getCertificatVieCollectif() == null) {
            throw new IllegalArgumentException("Le certificat de vie collectif du descendant est obligatoire.");
        }
        enfantBdd = this.enfantRepository.save(enfantBdd);
        return this.enfantMapper.enfantToEnfantReponse(enfantBdd);
    }

    public boolean supprimerEnfant(String adherentSlug, String enfantSlug) throws IOException {
        Enfant enfant = this.rechercherEnfant(adherentSlug, enfantSlug);
        Utilisateur parent = enfant.getUtilisateur();
        this.documentService.supprimerFichier(enfant.getActeDeNaissance());
        this.documentService.supprimerFichier(enfant.getCertificatVieCollectif());
        parent.enleverEnfant(enfant);
        this.enfantRepository.delete(enfant);
        return true;
    }

    public PageReponse<EnfantReponseDetaillee> listerEnfant(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Enfant> enfants = this.enfantRepository.findAll(pageable);
        List<EnfantReponseDetaillee> enfantReponseDetaillees = enfants.stream()
                .map(enfantMapper::enfantToEnfantReponseDetaillee)
                .toList();
        return new PageReponse<EnfantReponseDetaillee>(
                enfantReponseDetaillees,
                enfants.getNumber(),
                enfants.getSize(),
                enfants.getTotalElements(),
                enfants.getTotalPages(),
                enfants.isFirst(),
                enfants.isLast()
        );
    }

    public EnfantReponseDetaillee rechercheDetaillee(String enfantSlug) {
        Enfant enfant = this.enfantRepository.findEnfantBySlug(enfantSlug).orElseThrow(
                () -> new EntityNotFoundException("Aucun enfant n'a été trouvé !")
        );
        return this.enfantMapper.enfantToEnfantReponseDetaillee(enfant);
    }

    private Enfant rechercherEnfant(String adherentSlug, String enfantSlug) {
        Enfant enfant = this.enfantRepository.findEnfantBySlugAndUtilisateurSlug(enfantSlug, adherentSlug).orElseThrow(
                () -> new EntityNotFoundException("Aucun n'a été trouvé !")
        );
        return enfant;
    }
}
