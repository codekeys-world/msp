package com.codekeys.plainte;

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
public class PlainteService {
    private final PlainteRepository plainteRepository;
    private final PlainteMapper plainteMapper;
    private final DocumentService documentService;

    @Transactional
    public PlainteResponse enregistrer(@Valid PlainteRequest request) throws IOException {
        Plainte plainte = plainteMapper.plainteRequestToPlainte(request);
        plainte.setSlug(UUID.randomUUID().toString());
        MultipartFile fichier = request.getDocumentPlainte();
        if (fichier != null && !fichier.isEmpty()) {
            plainte.setDocumentPlainte(documentService.uploadFile(
                    fichier, TypeDocument.PLAINTE, plainte.getSlug() + "/"));
        }
        return plainteMapper.plainteToPlainteResponse(plainteRepository.save(plainte));
    }

    @Transactional
    public PageReponse<PlainteResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des plaintes sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Plainte> plaintes = StringUtils.hasText(terme)
                ? plainteRepository.rechercherParInformation(terme.trim(), pageable)
                : plainteRepository.findAll(pageable);
        List<PlainteResponse> contenu = plaintes.stream()
                .map(plainteMapper::plainteToPlainteResponse).toList();
        return new PageReponse<>(contenu, plaintes.getNumber(), plaintes.getSize(),
                plaintes.getTotalElements(), plaintes.getTotalPages(), plaintes.isFirst(), plaintes.isLast());
    }

    @Transactional
    public PlainteStatistiques obtenirStatistiques() {
        return new PlainteStatistiques(
                plainteRepository.count(),
                plainteRepository.countEnAttente(),
                plainteRepository.countTraitees(),
                plainteRepository.countRefusees()
        );
    }

    @Transactional
    public PlainteResponse rechercherParSlug(String slug) {
        return plainteMapper.plainteToPlainteResponse(rechercher(slug));
    }

    @Transactional
    public PlainteResponse modifier(String slug, @Valid PlainteRequest request) throws IOException {
        Plainte plainte = rechercher(slug);
        Document ancienDocument = plainte.getDocumentPlainte();
        Plainte donnees = plainteMapper.plainteRequestToPlainte(request);
        plainte.setDatePv(donnees.getDatePv());
        plainte.setObservation(donnees.getObservation());
        MultipartFile fichier = request.getDocumentPlainte();
        if (fichier != null && !fichier.isEmpty()) {
            plainte.setDocumentPlainte(documentService.uploadFile(fichier, TypeDocument.PLAINTE,
                    plainte.getSlug() + "/" + UUID.randomUUID()));
        }
        plainte = plainteRepository.save(plainte);
        if (plainte.getDocumentPlainte() != ancienDocument) {
            documentService.supprimerFichier(ancienDocument);
        }
        return plainteMapper.plainteToPlainteResponse(plainte);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Plainte plainte = rechercher(slug);
        documentService.supprimerFichier(plainte.getDocumentPlainte());
        plainteRepository.delete(plainte);
        return true;
    }

    private Plainte rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug de la plainte est obligatoire.");
        }
        return plainteRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucune plainte n'a été trouvée."));
    }
}
