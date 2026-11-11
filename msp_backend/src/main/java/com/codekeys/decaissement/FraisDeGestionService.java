package com.codekeys.decaissement;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraisDeGestionService {
    private final FraisDeGestionRepository fraisDeGestionRepository;
    private final FraisDeGestionMapper fraisDeGestionMapper;
    private final DocumentService documentService;

    @Transactional
    public FraisDeGestionResponse enregistrer(@Valid FraisDeGestionRequest request) throws IOException {
        if (fraisDeGestionRepository.existsByAnnee(request.getAnnee())) {
            throw new IllegalArgumentException("Un frais de gestion pour l'année " + request.getAnnee() + " existe déjà.");
        }
        FraisDeGestion fraisDeGestion = fraisDeGestionMapper.requestToEntity(request);
        fraisDeGestion.setSlug(UUID.randomUUID().toString());
        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            fraisDeGestion.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.DECAISSEMENT, fraisDeGestion.getSlug() + "/"));
        }
        return reponseRapide(fraisDeGestionRepository.save(fraisDeGestion), false);
    }

    @Transactional
    public PageReponse<FraisDeGestionResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des frais de gestion sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("annee").descending());
        Page<@NonNull FraisDeGestion> resultats = StringUtils.hasText(terme)
                ? fraisDeGestionRepository.rechercherParInformation(terme.trim(), pageable)
                : fraisDeGestionRepository.findAll(pageable);
        List<FraisDeGestion> frais = resultats.getContent();
        Map<String, long[]> totaux = chargerTotaux(frais);
        List<FraisDeGestionResponse> contenu = frais.stream()
                .map(item -> {
                    long[] total = totaux.getOrDefault(item.getSlug(), new long[]{0, 0});
                    return fraisDeGestionMapper.entityToResponse(item, total[0], total[1], false);
                })
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional
    public List<FraisDeGestionOption> listerOptions() {
        return fraisDeGestionRepository.listerOptions();
    }

    @Transactional
    public FraisDeGestionResponse rechercherParSlug(String slug) {
        return reponseRapide(rechercher(slug), true);
    }

    @Transactional
    public FraisDeGestionResponse modifier(String slug, @Valid FraisDeGestionRequest request) throws IOException {
        FraisDeGestion fraisDeGestion = rechercher(slug);
        if (!fraisDeGestion.getAnnee().equals(request.getAnnee())
                && fraisDeGestionRepository.existsByAnnee(request.getAnnee())) {
            throw new IllegalArgumentException("Un frais de gestion pour l'année " + request.getAnnee() + " existe déjà.");
        }
        Document ancienJustificatif = fraisDeGestion.getJustificatif();
        fraisDeGestionMapper.mettreAJourEntity(fraisDeGestion, request);

        MultipartFile justificatif = request.getJustificatif();
        if (justificatif != null && !justificatif.isEmpty()) {
            fraisDeGestion.setJustificatif(documentService.uploadFile(
                    justificatif, TypeDocument.DECAISSEMENT,
                    fraisDeGestion.getSlug() + "/" + UUID.randomUUID()));
        }

        fraisDeGestion = fraisDeGestionRepository.save(fraisDeGestion);
        if (fraisDeGestion.getJustificatif() != ancienJustificatif) {
            documentService.supprimerFichier(ancienJustificatif);
        }
        return reponseRapide(fraisDeGestion, false);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        FraisDeGestion fraisDeGestion = rechercher(slug);
        if (!fraisDeGestion.getDepenses().isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer un frais de gestion contenant des dépenses.");
        }
        documentService.supprimerFichier(fraisDeGestion.getJustificatif());
        fraisDeGestionRepository.delete(fraisDeGestion);
        return true;
    }

    private FraisDeGestion rechercher(String slug) {
        return fraisDeGestionRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Frais de gestion non trouvé : " + slug));
    }

    private FraisDeGestionResponse reponseRapide(FraisDeGestion fraisDeGestion, boolean inclureJustificatif) {
        Map<String, long[]> totaux = chargerTotaux(List.of(fraisDeGestion));
        long[] total = totaux.getOrDefault(fraisDeGestion.getSlug(), new long[]{0, 0});
        return fraisDeGestionMapper.entityToResponse(fraisDeGestion, total[0], total[1], inclureJustificatif);
    }

    private Map<String, long[]> chargerTotaux(List<FraisDeGestion> frais) {
        if (frais.isEmpty()) return Map.of();
        List<String> slugs = frais.stream().map(FraisDeGestion::getSlug).toList();
        return fraisDeGestionRepository.totauxDepensesParSlug(slugs, StatutDepense.REJETEE).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> new long[]{
                        ((Number) row[2]).longValue(), ((Number) row[1]).longValue()
                }));
    }
}
