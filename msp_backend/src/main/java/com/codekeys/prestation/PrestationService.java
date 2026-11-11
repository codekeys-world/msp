package com.codekeys.prestation;

import com.codekeys.CasSocial.CasSocialAdherentOption;
import com.codekeys.CasSocial.LienDefunt;
import com.codekeys.CasSocial.StatutPrestation;
import com.codekeys.CasSocial.TypeBeneficiaire;
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
public class PrestationService {
    private static final long MONTANT_DECES_PARENT_OU_CONJOINT = 50_000L;
    private static final long MONTANT_DECES_ENFANT = 20_000L;
    private static final long MONTANT_DECES_ADHERENT = 1_500_000L;
    private static final long MONTANT_REFORME_INVALIDITE = 1_500_000L;
    private static final long MONTANT_RETRAITE = 2_000_000L;
    private static final int DUREE_DECES_ADHERENT_MOIS = 60;
    private static final int DUREE_RETRAITE_MOIS = 180;

    private final PrestationRepository prestationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PrestationMapper prestationMapper;
    private final DocumentService documentService;

    @Transactional
    public PrestationResponse enregistrer(@Valid PrestationRequest request) throws IOException {
        validerReglesMetier(request);
        Utilisateur adherent = rechercherAdherent(request.getAdherentSlug());
        Prestation prestation = prestationMapper.requestToEntity(request);
        prestation.setSlug(UUID.randomUUID().toString());
        prestation.setAdherent(adherent);
        appliquerMontants(prestation, request);
        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            prestation.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.CAS_SOCIAUX, prestation.getSlug() + "/"));
        }
        return prestationMapper.entityToResponse(prestationRepository.save(prestation));
    }

    @Transactional
    public PageReponse<PrestationResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des prestations sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Prestation> resultats = StringUtils.hasText(terme)
                ? prestationRepository.rechercherParInformation(terme.trim(), pageable)
                : prestationRepository.findAll(pageable);
        List<PrestationResponse> contenu = resultats.stream()
                .map(prestationMapper::entityToResponse)
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional
    public PrestationResponse rechercherParSlug(String slug) {
        return prestationMapper.entityToResponse(rechercher(slug));
    }

    @Transactional
    public PrestationResponse modifier(String slug, @Valid PrestationRequest request) throws IOException {
        validerReglesMetier(request);
        Prestation prestation = rechercher(slug);
        Document ancienJustificatif = prestation.getJustificatif();
        Utilisateur adherent = rechercherAdherent(request.getAdherentSlug());
        prestationMapper.mettreAJourEntity(prestation, request);
        prestation.setAdherent(adherent);
        appliquerMontants(prestation, request);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            prestation.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.CAS_SOCIAUX,
                    prestation.getSlug() + "/" + UUID.randomUUID()));
        }

        prestation = prestationRepository.save(prestation);
        if (prestation.getJustificatif() != ancienJustificatif) {
            documentService.supprimerFichier(ancienJustificatif);
        }
        return prestationMapper.entityToResponse(prestation);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Prestation prestation = rechercher(slug);
        documentService.supprimerFichier(prestation.getJustificatif());
        prestationRepository.delete(prestation);
        return true;
    }

    @Transactional
    public List<CasSocialAdherentOption> rechercherAdherents(String terme) {
        String recherche = StringUtils.hasText(terme) ? terme.trim() : "";
        return prestationRepository.rechercherAdherents(recherche, PageRequest.of(0, 20,
                        Sort.by("nom").ascending().and(Sort.by("prenom").ascending())))
                .stream()
                .map(adherent -> new CasSocialAdherentOption(adherent.getSlug(), adherent.getNomComplet(),
                        adherent.getNumeroInscription(), adherent.getTelephonePrincipal()))
                .toList();
    }

    private void validerReglesMetier(PrestationRequest request) {
        if (request.getTypePrestation() == null || request.getTypeBeneficiaire() == null) {
            throw new IllegalArgumentException("Le type de prestation et son bénéficiaire sont obligatoires.");
        }
        TypePrestation type = request.getTypePrestation();
        if (request.getDureeCotisationMois() != null && request.getDureeCotisationMois() < 0) {
            throw new IllegalArgumentException("La durée de cotisation ne peut pas être négative.");
        }
        if (request.getMontantCotisationsVersees() != null && request.getMontantCotisationsVersees() < 0) {
            throw new IllegalArgumentException("Le total des cotisations versées ne peut pas être négatif.");
        }

        if (type == TypePrestation.DECES_ADHERENT) {
            if (request.getTypeBeneficiaire() == TypeBeneficiaire.ADHERENT) {
                throw new IllegalArgumentException("En cas de décès de l'adhérent, la prestation est remise aux parents ou à une personne désignée.");
            }
            if (request.getTypeBeneficiaire() != TypeBeneficiaire.PARENTS
                    && request.getTypeBeneficiaire() != TypeBeneficiaire.PERSONNE_DESIGNEE) {
                throw new IllegalArgumentException("Le bénéficiaire doit être les parents ou une personne désignée.");
            }
            if (request.getDureeCotisationMois() == null) {
                throw new IllegalArgumentException("La durée totale de cotisation est obligatoire pour cette prestation.");
            }
            boolean remboursementPossible = request.getDureeCotisationMois() < DUREE_DECES_ADHERENT_MOIS;
            if (remboursementPossible && request.getMontantCotisationsVersees() == null) {
                throw new IllegalArgumentException("Le montant des cotisations versées est obligatoire pour calculer le remboursement.");
            }
        } else if (type == TypePrestation.RETRAITE) {
            if (request.getTypeBeneficiaire() != TypeBeneficiaire.ADHERENT) {
                throw new IllegalArgumentException("Cette prestation est attribuée à l'adhérent.");
            }
            if (request.getDureeCotisationMois() == null) {
                throw new IllegalArgumentException("La durée totale de cotisation est obligatoire pour cette prestation.");
            }
            if (request.getMontantCotisationsVersees() != null) {
                throw new IllegalArgumentException("Le montant des cotisations versées ne s'applique pas à la prestation de retraite.");
            }
        } else if (type == TypePrestation.REFORME_INVALIDITE) {
            if (request.getTypeBeneficiaire() != TypeBeneficiaire.ADHERENT) {
                throw new IllegalArgumentException("Cette prestation est attribuée à l'adhérent.");
            }
            if (request.getDureeCotisationMois() != null || request.getMontantCotisationsVersees() != null) {
                throw new IllegalArgumentException("Les informations de cotisation ne sont pas nécessaires pour la réforme pour invalidité.");
            }
        } else if (type == TypePrestation.DECES) {
            if (request.getLienDefunt() == null) {
                throw new IllegalArgumentException("Le lien entre l'adhérent et la personne décédée est obligatoire.");
            }
            if (request.getLienDefunt() == LienDefunt.ADHERENT) {
                if (request.getTypeBeneficiaire() == TypeBeneficiaire.ADHERENT) {
                    throw new IllegalArgumentException("En cas de décès de l'adhérent, la prestation est remise aux parents ou à une personne désignée.");
                }
                if (request.getTypeBeneficiaire() != TypeBeneficiaire.PARENTS
                        && request.getTypeBeneficiaire() != TypeBeneficiaire.PERSONNE_DESIGNEE) {
                    throw new IllegalArgumentException("Le bénéficiaire doit être les parents ou une personne désignée.");
                }
                if (request.getDureeCotisationMois() == null) {
                    throw new IllegalArgumentException("La durée totale de cotisation est obligatoire pour le décès de l'adhérent.");
                }
                if (request.getDureeCotisationMois() < DUREE_DECES_ADHERENT_MOIS
                        && request.getMontantCotisationsVersees() == null) {
                    throw new IllegalArgumentException("Le total des cotisations versées est obligatoire pour calculer le remboursement.");
                }
            } else if (request.getTypeBeneficiaire() == TypeBeneficiaire.PARENTS) {
                throw new IllegalArgumentException("Pour le décès d'un proche, la prestation est remise à l'adhérent ou à une personne désignée.");
            }
            if (request.getLienDefunt() != LienDefunt.ADHERENT
                    && (!StringUtils.hasText(request.getNomDefunt()) || !StringUtils.hasText(request.getPrenomDefunt()))) {
                throw new IllegalArgumentException("Le nom et le prénom de la personne décédée sont obligatoires.");
            }
        }

        validerCoordonneesBeneficiaire(request);
    }

    private void validerCoordonneesBeneficiaire(PrestationRequest request) {
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

    private void appliquerMontants(Prestation prestation, PrestationRequest request) {
        prestation.setMontantPrestation(null);
        prestation.setMontantRemboursement(null);
        prestation.setStatutPrestation(StatutPrestation.PRESTATION);
        TypePrestation type = request.getTypePrestation();

        if (type == TypePrestation.DECES_ADHERENT) {
            if (request.getDureeCotisationMois() >= DUREE_DECES_ADHERENT_MOIS) {
                prestation.setMontantPrestation(MONTANT_DECES_ADHERENT);
            } else {
                prestation.setMontantRemboursement(request.getMontantCotisationsVersees());
                prestation.setStatutPrestation(StatutPrestation.REMBOURSEMENT_COTISATIONS);
            }
        } else if (type == TypePrestation.DECES) {
            if (request.getLienDefunt() == LienDefunt.ADHERENT) {
                if (request.getDureeCotisationMois() >= DUREE_DECES_ADHERENT_MOIS) {
                    prestation.setMontantPrestation(MONTANT_DECES_ADHERENT);
                } else {
                    prestation.setMontantRemboursement(request.getMontantCotisationsVersees());
                    prestation.setStatutPrestation(StatutPrestation.REMBOURSEMENT_COTISATIONS);
                }
            } else {
                prestation.setMontantPrestation(switch (request.getLienDefunt()) {
                    case ADHERENT -> MONTANT_DECES_ADHERENT;
                    case PERE, MERE, EPOUX, EPOUSE -> MONTANT_DECES_PARENT_OU_CONJOINT;
                    case ENFANT -> MONTANT_DECES_ENFANT;
                });
            }
        } else if (type == TypePrestation.REFORME_INVALIDITE) {
            prestation.setMontantPrestation(MONTANT_REFORME_INVALIDITE);
        } else if (type == TypePrestation.RETRAITE) {
            if (request.getDureeCotisationMois() >= DUREE_RETRAITE_MOIS) {
                prestation.setMontantPrestation(MONTANT_RETRAITE);
            } else {
                prestation.setStatutPrestation(StatutPrestation.NON_ELIGIBLE);
            }
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

    private Prestation rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug de la prestation est obligatoire.");
        }
        return prestationRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucune prestation n'a été trouvée."));
    }
}
