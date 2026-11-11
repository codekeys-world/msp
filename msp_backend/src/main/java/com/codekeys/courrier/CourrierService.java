package com.codekeys.courrier;

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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourrierService {
    private final CourrierRepository courrierRepository;
    private final CourrierMapper courrierMapper;
    private final DocumentService documentService;

    @Transactional
    public CourrierResponse enregistrer(@Valid CourrierRequest request) throws IOException {
        verifierChamps(request);
        verifierReferenceDisponible(request.getNom(), null);
        MultipartFile fichier = request.getDocumentCourrier();
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le document du courrier est obligatoire.");
        }
        Courrier courrier = courrierMapper.courrierRequestToCourrier(request);
        courrier.setSlug(UUID.randomUUID().toString());
        courrier.setDocumentCourrier(documentService.uploadFile(fichier, TypeDocument.COURRIER,
                courrier.getSlug() + "/"));
        return courrierMapper.courrierToCourrierResponse(courrierRepository.save(courrier));
    }

    @Transactional
    public PageReponse<CourrierResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des courriers sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Courrier> courriers = StringUtils.hasText(terme)
                ? courrierRepository.rechercherParInformation(terme.trim(), pageable)
                : courrierRepository.findAll(pageable);
        List<CourrierResponse> contenu = courriers.stream()
                .map(courrierMapper::courrierToCourrierResponse).toList();
        return new PageReponse<>(contenu, courriers.getNumber(), courriers.getSize(),
                courriers.getTotalElements(), courriers.getTotalPages(), courriers.isFirst(), courriers.isLast());
    }

    @Transactional
    public CourrierStatistiques obtenirStatistiques() {
        return new CourrierStatistiques(
                courrierRepository.count(),
                courrierRepository.countByTypeCourrier(TypeCourrier.ARRIVE),
                courrierRepository.countByTypeCourrier(TypeCourrier.DEPART),
                courrierRepository.countEnAttente(),
                courrierRepository.countTraites(),
                courrierRepository.countRefuses()
        );
    }

    @Transactional
    public CourrierResponse rechercherParSlug(String slug) {
        return courrierMapper.courrierToCourrierResponse(rechercher(slug));
    }

    @Transactional
    public CourrierResponse modifier(String slug, @Valid CourrierRequest request) throws IOException {
        verifierChamps(request);
        Courrier courrier = rechercher(slug);
        verifierReferenceDisponible(request.getNom(), slug);
        Document ancienDocument = courrier.getDocumentCourrier();
        Courrier donnees = courrierMapper.courrierRequestToCourrier(request);
        courrier.setNom(donnees.getNom());
        courrier.setObjet(donnees.getObjet());
        courrier.setTypeCourrier(donnees.getTypeCourrier());
        courrier.setDescription(donnees.getDescription());
        courrier.setDateArrivee(donnees.getDateArrivee());
        courrier.setDateDepart(donnees.getDateDepart());

        MultipartFile fichier = request.getDocumentCourrier();
        if (fichier != null && !fichier.isEmpty()) {
            courrier.setDocumentCourrier(documentService.uploadFile(fichier, TypeDocument.COURRIER,
                    courrier.getSlug() + "/" + UUID.randomUUID()));
        }
        courrier = courrierRepository.save(courrier);
        if (courrier.getDocumentCourrier() != ancienDocument) {
            documentService.supprimerFichier(ancienDocument);
        }
        return courrierMapper.courrierToCourrierResponse(courrier);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Courrier courrier = rechercher(slug);
        documentService.supprimerFichier(courrier.getDocumentCourrier());
        courrierRepository.delete(courrier);
        return true;
    }

    private Courrier rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug du courrier est obligatoire.");
        }
        return courrierRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucun courrier n'a été trouvé."));
    }

    private void verifierChamps(CourrierRequest request) {
        if (request.getTypeCourrier() == null) {
            throw new IllegalArgumentException("Le type du courrier est obligatoire.");
        }
        if (request.getTypeCourrier() == TypeCourrier.ARRIVE && request.getDateArrivee() == null) {
            throw new IllegalArgumentException("La date d'arrivée est obligatoire pour un courrier entrant.");
        }
        if (request.getTypeCourrier() == TypeCourrier.DEPART && request.getDateDepart() == null) {
            throw new IllegalArgumentException("La date de départ est obligatoire pour un courrier sortant.");
        }
    }

    private void verifierReferenceDisponible(String nom, String slugExclu) {
        if (!StringUtils.hasText(nom)) {
            throw new IllegalArgumentException("La référence du courrier est obligatoire.");
        }
        boolean existe = slugExclu == null
                ? courrierRepository.existsByNomIgnoreCase(nom.trim())
                : courrierRepository.existsByNomIgnoreCaseAndSlugNot(nom.trim(), slugExclu);
        if (existe) {
            throw new IllegalArgumentException("Cette référence de courrier est déjà utilisée.");
        }
    }
}
