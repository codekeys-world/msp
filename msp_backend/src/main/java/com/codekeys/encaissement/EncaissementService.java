package com.codekeys.encaissement;

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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EncaissementService {
    private final EncaissementRepository encaissementRepository;
    private final EncaissementMapper encaissementMapper;
    private final DocumentService documentService;

    @Transactional
    public EncaissementResponse enregistrer(@Valid EncaissementRequest request) throws IOException {
        validerPeriode(request);
        Encaissement encaissement = encaissementMapper.requestToEntity(request);
        encaissement.setSlug(UUID.randomUUID().toString());
        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            encaissement.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.ENCAISSEMENT, encaissement.getSlug() + "/"));
        }
        return encaissementMapper.entityToResponse(encaissementRepository.save(encaissement), false);
    }

    @Transactional(readOnly = true)
    public PageReponse<EncaissementResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des encaissements sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Encaissement> resultats = StringUtils.hasText(terme)
                ? encaissementRepository.rechercherParInformation(terme.trim(), pageable)
                : encaissementRepository.findAll(pageable);
        List<EncaissementResponse> contenu = resultats.stream()
                .map(encaissement -> encaissementMapper.entityToResponse(encaissement, false))
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional(readOnly = true)
    public EncaissementResponse rechercherParSlug(String slug) {
        return rechercherParSlug(slug, true);
    }

    @Transactional(readOnly = true)
    public EncaissementResponse rechercherParSlug(String slug, boolean inclureJustificatif) {
        return encaissementMapper.entityToResponse(rechercher(slug), inclureJustificatif);
    }

    @Transactional
    public EncaissementResponse modifier(String slug, @Valid EncaissementRequest request) throws IOException {
        validerPeriode(request);
        Encaissement encaissement = rechercher(slug);
        Document ancienJustificatif = encaissement.getJustificatif();
        encaissementMapper.mettreAJourEntity(encaissement, request);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            encaissement.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.ENCAISSEMENT,
                    encaissement.getSlug() + "/" + UUID.randomUUID()));
        }

        encaissement = encaissementRepository.save(encaissement);
        if (encaissement.getJustificatif() != ancienJustificatif) {
            documentService.supprimerFichier(ancienJustificatif);
        }
        return encaissementMapper.entityToResponse(encaissement, false);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Encaissement encaissement = rechercher(slug);
        documentService.supprimerFichier(encaissement.getJustificatif());
        encaissementRepository.delete(encaissement);
        return true;
    }

    private Encaissement rechercher(String slug) {
        return encaissementRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Encaissement non trouvé : " + slug));
    }

    private void validerPeriode(EncaissementRequest request) {
        if (request.getDateDebutPeriode() != null && request.getDateFinPeriode() != null
                && request.getDateDebutPeriode().isAfter(request.getDateFinPeriode())) {
            throw new IllegalArgumentException("La date de début de période ne peut pas être postérieure à la date de fin.");
        }
    }

    /**
     * Rapport de situation des encaissements pour une année donnée,
     * regroupé par mois et par type d'encaissement.
     */
    @Transactional(readOnly = true)
    public List<EncaissementSituationResponse> situationParAnnee(int annee) {
        return encaissementRepository.situationParAnnee(
                LocalDate.of(annee, 1, 1), LocalDate.of(annee + 1, 1, 1));
    }
}
