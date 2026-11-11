package com.codekeys.decaissement;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepenseService {
    private final DepenseRepository depenseRepository;
    private final FraisDeGestionRepository fraisDeGestionRepository;
    private final DepenseMapper depenseMapper;
    private final DocumentService documentService;

    @Transactional
    public DepenseResponse engager(@Valid DepenseRequest request) throws IOException {
        FraisDeGestion fraisDeGestion = fraisDeGestionRepository.findBySlug(request.getFraisDeGestionSlug())
                .orElseThrow(() -> new EntityNotFoundException("Frais de gestion non trouvé : " + request.getFraisDeGestionSlug()));

        long montantTotalDepensé = depenseRepository.sommeValideeParFraisDeGestion(
                fraisDeGestion.getSlug(), StatutDepense.VALIDEE);
        long solde = fraisDeGestion.getMontantTotal() - montantTotalDepensé;
        if (request.getMontant() > solde) {
            throw new IllegalArgumentException("Le montant de la dépense dépasse le solde disponible du frais de gestion.");
        }

        Depense depense = depenseMapper.requestToEntity(request);
        depense.setSlug(UUID.randomUUID().toString());
        depense.setFraisDeGestion(fraisDeGestion);
        depense.setStatut(StatutDepense.EN_ATTENTE);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            depense.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.DECAISSEMENT, depense.getSlug() + "/"));
        }
        return depenseMapper.entityToResponse(depenseRepository.save(depense), false);
    }

    @Transactional(readOnly = true)
    public PageReponse<DepenseResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des dépenses sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Depense> resultats = StringUtils.hasText(terme)
                ? depenseRepository.rechercherParInformation(terme.trim(), pageable)
                : depenseRepository.findAll(pageable);
        List<DepenseResponse> contenu = resultats.stream()
                .map(depense -> depenseMapper.entityToResponse(depense, false))
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional(readOnly = true)
    public PageReponse<DepenseResponse> listerParFraisDeGestion(String fraisDeGestionSlug, int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des dépenses sont invalides.");
        }
        if (!fraisDeGestionRepository.existsBySlug(fraisDeGestionSlug)) {
            throw new EntityNotFoundException("Frais de gestion non trouvé : " + fraisDeGestionSlug);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Depense> resultats = StringUtils.hasText(terme)
                ? depenseRepository.rechercherParFraisDeGestion(fraisDeGestionSlug, terme.trim(), pageable)
                : depenseRepository.findByFraisDeGestionSlug(fraisDeGestionSlug, pageable);
        List<DepenseResponse> contenu = resultats.stream()
                .map(depense -> depenseMapper.entityToResponse(depense, false))
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional(readOnly = true)
    public DepenseResponse rechercherParSlug(String slug) {
        return rechercherParSlug(slug, true);
    }

    @Transactional(readOnly = true)
    public DepenseResponse rechercherParSlug(String slug, boolean inclureJustificatif) {
        return depenseMapper.entityToResponse(rechercher(slug), inclureJustificatif);
    }

    @Transactional
    public DepenseResponse modifier(String slug, @Valid DepenseRequest request) throws IOException {
        Depense depense = rechercher(slug);
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les dépenses en attente peuvent être modifiées.");
        }

        // Si le frais de gestion change, vérifier le solde
        if (!depense.getFraisDeGestion().getSlug().equals(request.getFraisDeGestionSlug())) {
            FraisDeGestion nouveauFdg = fraisDeGestionRepository.findBySlug(request.getFraisDeGestionSlug())
                    .orElseThrow(() -> new EntityNotFoundException("Frais de gestion non trouvé : " + request.getFraisDeGestionSlug()));
            long montantTotalDepensé = depenseRepository.sommeValideeParFraisDeGestion(
                    nouveauFdg.getSlug(), StatutDepense.VALIDEE);
            long solde = nouveauFdg.getMontantTotal() - montantTotalDepensé;
            if (request.getMontant() > solde) {
                throw new IllegalArgumentException("Le montant de la dépense dépasse le solde disponible du frais de gestion.");
            }
            depense.setFraisDeGestion(nouveauFdg);
        }

        Document ancienJustificatif = depense.getJustificatif();
        depenseMapper.mettreAJourEntity(depense, request);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            depense.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.DECAISSEMENT,
                    depense.getSlug() + "/" + UUID.randomUUID()));
        }

        depense = depenseRepository.save(depense);
        if (depense.getJustificatif() != ancienJustificatif) {
            documentService.supprimerFichier(ancienJustificatif);
        }
        return depenseMapper.entityToResponse(depense, false);
    }

    @Transactional
    public DepenseResponse valider(String slug, String observationValidation) {
        Depense depense = rechercher(slug);
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les dépenses en attente peuvent être validées.");
        }
        depense.setStatut(StatutDepense.VALIDEE);
        depense.setObservationValidation(observationValidation);
        return depenseMapper.entityToResponse(depenseRepository.save(depense), false);
    }

    @Transactional
    public DepenseResponse rejeter(String slug, String observationValidation) {
        Depense depense = rechercher(slug);
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les dépenses en attente peuvent être rejetées.");
        }
        depense.setStatut(StatutDepense.REJETEE);
        depense.setObservationValidation(observationValidation);
        return depenseMapper.entityToResponse(depenseRepository.save(depense), false);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Depense depense = rechercher(slug);
        if (depense.getStatut() != StatutDepense.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les dépenses en attente peuvent être supprimées.");
        }
        documentService.supprimerFichier(depense.getJustificatif());
        depenseRepository.delete(depense);
        return true;
    }

    private Depense rechercher(String slug) {
        return depenseRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Dépense non trouvée : " + slug));
    }

    /**
     * Rapport de situation des dépenses pour une année donnée,
     * regroupé par mois, par libellé et par statut.
     */
    @Transactional(readOnly = true)
    public List<DepenseSituationResponse> situationParAnnee(int annee) {
        List<Object[]> rows = depenseRepository.situationParAnnee(
                java.time.LocalDate.of(annee, 1, 1), java.time.LocalDate.of(annee + 1, 1, 1));
        List<DepenseSituationResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            int mois = ((Number) row[0]).intValue();
            String libelle = (String) row[1];
            String statut = String.valueOf(row[2]);
            long count = ((Number) row[3]).longValue();
            long total = ((Number) row[4]).longValue();

            result.add(DepenseSituationResponse.builder()
                    .mois(mois)
                    .libelleMois(Month.of(mois).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRANCE))
                    .libelle(libelle)
                    .statut(statut)
                    .nombre(count)
                    .montantTotal(total)
                    .build());
        }
        return result;
    }
}
