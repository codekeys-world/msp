package com.codekeys.CasSocial;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CasSocialService {
    private static final long MONTANT_MALADIE = 50L;
    private static final long MONTANT_DECES_PARENT_OU_CONJOINT = 50_000L;
    private static final long MONTANT_DECES_ENFANT = 20_000L;
    private static final long MONTANT_DECES_ADHERENT = 1_500_000L;
    private static final int DUREE_DECES_ADHERENT_MOIS = 60;

    private final CasSocialRepository casSocialRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CasSocialMapper casSocialMapper;
    private final DocumentService documentService;

    @Transactional
    public CasSocialResponse enregistrer(@Valid CasSocialRequest request) throws IOException {
        validerReglesMetier(request);
        Utilisateur adherent = rechercherAdherent(request.getAdherentSlug());
        CasSocial casSocial = casSocialMapper.requestToEntity(request);
        casSocial.setSlug(UUID.randomUUID().toString());
        casSocial.setAdherent(adherent);
        appliquerMontants(casSocial, request);
        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            casSocial.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.CAS_SOCIAUX, casSocial.getSlug() + "/"));
        }
        return casSocialMapper.entityToResponse(casSocialRepository.save(casSocial));
    }

    @Transactional
    public PageReponse<CasSocialResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des cas sociaux sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull CasSocial> resultats = StringUtils.hasText(terme)
                ? casSocialRepository.rechercherParInformation(terme.trim(), pageable)
                : casSocialRepository.findAll(pageable);
        List<CasSocialResponse> contenu = resultats.stream()
                .map(casSocialMapper::entityToResponse)
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional
    public CasSocialResponse rechercherParSlug(String slug) {
        return casSocialMapper.entityToResponse(rechercher(slug));
    }

    @Transactional
    public CasSocialResponse modifier(String slug, @Valid CasSocialRequest request) throws IOException {
        validerReglesMetier(request);
        CasSocial casSocial = rechercher(slug);
        Document ancienJustificatif = casSocial.getJustificatif();
        Utilisateur adherent = rechercherAdherent(request.getAdherentSlug());
        casSocialMapper.mettreAJourEntity(casSocial, request);
        casSocial.setAdherent(adherent);
        appliquerMontants(casSocial, request);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            casSocial.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.CAS_SOCIAUX,
                    casSocial.getSlug() + "/" + UUID.randomUUID()));
        }

        casSocial = casSocialRepository.save(casSocial);
        if (casSocial.getJustificatif() != ancienJustificatif) {
            documentService.supprimerFichier(ancienJustificatif);
        }
        return casSocialMapper.entityToResponse(casSocial);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        CasSocial casSocial = rechercher(slug);
        documentService.supprimerFichier(casSocial.getJustificatif());
        casSocialRepository.delete(casSocial);
        return true;
    }

    @Transactional
    public List<CasSocialAdherentOption> rechercherAdherents(String terme) {
        String recherche = StringUtils.hasText(terme) ? terme.trim() : "";
        return casSocialRepository.rechercherAdherents(recherche, PageRequest.of(0, 20,
                        Sort.by("nom").ascending().and(Sort.by("prenom").ascending())))
                .stream()
                .map(adherent -> new CasSocialAdherentOption(adherent.getSlug(), adherent.getNomComplet(),
                        adherent.getNumeroInscription(), adherent.getTelephonePrincipal()))
                .toList();
    }

    private void validerReglesMetier(CasSocialRequest request) {
        if (request.getTypeCas() == null || request.getTypeBeneficiaire() == null) {
            throw new IllegalArgumentException("Le type de cas social et son bénéficiaire sont obligatoires.");
        }
        TypeCasSocial type = request.getTypeCas();
        if (request.getDureeCotisationMois() != null && request.getDureeCotisationMois() < 0) {
            throw new IllegalArgumentException("La durée de cotisation ne peut pas être négative.");
        }
        if (request.getMontantCotisationsVersees() != null && request.getMontantCotisationsVersees() < 0) {
            throw new IllegalArgumentException("Le total des cotisations versées ne peut pas être négatif.");
        }

        if (type == TypeCasSocial.RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE) {
            if (request.getTypeBeneficiaire() != TypeBeneficiaire.ADHERENT) {
                throw new IllegalArgumentException("Ce remboursement est attribué à l'adhérent.");
            }
            if (request.getLienDefunt() != null || StringUtils.hasText(request.getNomDefunt())
                    || StringUtils.hasText(request.getPrenomDefunt()) || request.getHeuresHospitalisation() != null) {
                throw new IllegalArgumentException("Les informations de décès et d'hospitalisation ne s'appliquent pas à ce type de dossier.");
            }
            if (request.getDureeCotisationMois() != null) {
                throw new IllegalArgumentException("La durée de cotisation ne s'applique pas à ce remboursement.");
            }
            if (request.getMontantCotisationsVersees() == null) {
                throw new IllegalArgumentException("Le montant des cotisations versées est obligatoire pour calculer le remboursement.");
            }
            validerCoordonneesBeneficiaire(request);
            return;
        }

        if (type == TypeCasSocial.MALADIE) {
            if (request.getHeuresHospitalisation() == null || request.getHeuresHospitalisation() < 72) {
                throw new IllegalArgumentException("Une hospitalisation d'au moins 72 heures est requise pour un cas de maladie.");
            }
            if (request.getLienDefunt() != null || StringUtils.hasText(request.getNomDefunt())
                    || StringUtils.hasText(request.getPrenomDefunt())) {
                throw new IllegalArgumentException("Les informations relatives au défunt ne s'appliquent pas à une maladie.");
            }
            if (request.getDureeCotisationMois() != null || request.getMontantCotisationsVersees() != null) {
                throw new IllegalArgumentException("Les informations de cotisation ne sont pas nécessaires pour un cas de maladie.");
            }
        } else if (type == TypeCasSocial.ACCIDENT_CIRCULATION) {
            if (request.getLienDefunt() != null || StringUtils.hasText(request.getNomDefunt())
                    || StringUtils.hasText(request.getPrenomDefunt())) {
                throw new IllegalArgumentException("Les informations relatives au défunt ne s'appliquent pas à un accident.");
            }
            if (request.getHeuresHospitalisation() != null) {
                throw new IllegalArgumentException("La durée d'hospitalisation ne s'applique qu'aux cas de maladie.");
            }
            if (request.getDureeCotisationMois() != null || request.getMontantCotisationsVersees() != null) {
                throw new IllegalArgumentException("Les informations de cotisation ne sont pas nécessaires pour un accident de la circulation.");
            }
        }

        validerCoordonneesBeneficiaire(request);
    }

    private void validerCoordonneesBeneficiaire(CasSocialRequest request) {
        if (request.getTypeBeneficiaire() == TypeBeneficiaire.PERSONNE_DESIGNEE
                && (!StringUtils.hasText(request.getNomBeneficiaire())
                || !StringUtils.hasText(request.getPrenomBeneficiaire())
                || !StringUtils.hasText(request.getTelephoneBeneficiaire()))) {
            throw new IllegalArgumentException("Le nom, le prénom et le téléphone de la personne désignée sont obligatoires.");
        }
        if (request.getTypeBeneficiaire() != TypeBeneficiaire.PERSONNE_DESIGNEE
                && (StringUtils.hasText(request.getNomBeneficiaire())
                || StringUtils.hasText(request.getPrenomBeneficiaire())
                || StringUtils.hasText(request.getTelephoneBeneficiaire()))) {
            throw new IllegalArgumentException("Les coordonnées du bénéficiaire ne sont à renseigner que pour une personne désignée.");
        }
    }

    private void appliquerMontants(CasSocial casSocial, CasSocialRequest request) {
        casSocial.setMontantPrestation(null);
        casSocial.setMontantRemboursement(null);
        casSocial.setStatutPrestation(StatutPrestation.PRESTATION);
        TypeCasSocial type = request.getTypeCas();

        if (type == TypeCasSocial.MALADIE) {
            casSocial.setMontantPrestation(MONTANT_MALADIE);
        } else if (type == TypeCasSocial.RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE) {
            casSocial.setMontantRemboursement(request.getMontantCotisationsVersees());
            casSocial.setStatutPrestation(StatutPrestation.REMBOURSEMENT_COTISATIONS);
        }
    }

    private Utilisateur rechercherAdherent(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("L'adhérent concerné est obligatoire.");
        }
        Utilisateur adherent = utilisateurRepository.findUtilisateurBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("L'adhérent concerné n'a pas été trouvé."));
        if (adherent.getRole() == null || !"ADHERENT".equalsIgnoreCase(adherent.getRole().getNom())) {
            throw new EntityNotFoundException("L'utilisateur sélectionné n'est pas un adhérent.");
        }
        return adherent;
    }

    private CasSocial rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug du cas social est obligatoire.");
        }
        return casSocialRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucun cas social n'a été trouvé."));
    }
}
