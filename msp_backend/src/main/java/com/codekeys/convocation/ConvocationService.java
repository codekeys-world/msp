package com.codekeys.convocation;

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
public class ConvocationService {
    private final ConvocationRepository convocationRepository;
    private final ConvocationMapper convocationMapper;
    private final DocumentService documentService;

    @Transactional
    public ConvocationResponse enregistrer(@Valid ConvocationRequest request) throws IOException {
        Convocation convocation = convocationMapper.convocationRequestToConvocation(request);
        convocation.setSlug(UUID.randomUUID().toString());
        MultipartFile fichier = request.getDocumentConvocation();
        if (fichier != null && !fichier.isEmpty()) {
            convocation.setDocumentConvocation(documentService.uploadFile(
                    fichier, TypeDocument.CONVOCATION, convocation.getSlug() + "/"));
        }
        return convocationMapper.convocationToConvocationResponse(convocationRepository.save(convocation));
    }

    @Transactional
    public PageReponse<ConvocationResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des convocations sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Convocation> convocations = StringUtils.hasText(terme)
                ? convocationRepository.rechercherParInformation(terme.trim(), pageable)
                : convocationRepository.findAll(pageable);
        List<ConvocationResponse> contenu = convocations.stream()
                .map(convocationMapper::convocationToConvocationResponse).toList();
        return new PageReponse<>(contenu, convocations.getNumber(), convocations.getSize(),
                convocations.getTotalElements(), convocations.getTotalPages(),
                convocations.isFirst(), convocations.isLast());
    }

    @Transactional
    public ConvocationResponse rechercherParSlug(String slug) {
        return convocationMapper.convocationToConvocationResponse(rechercher(slug));
    }

    @Transactional
    public ConvocationResponse modifier(String slug, @Valid ConvocationRequest request) throws IOException {
        Convocation convocation = rechercher(slug);
        Document ancienDocument = convocation.getDocumentConvocation();
        Convocation donnees = convocationMapper.convocationRequestToConvocation(request);
        convocation.setDescription(donnees.getDescription());
        convocation.setDateConvocation(donnees.getDateConvocation());
        MultipartFile fichier = request.getDocumentConvocation();
        if (fichier != null && !fichier.isEmpty()) {
            convocation.setDocumentConvocation(documentService.uploadFile(fichier, TypeDocument.CONVOCATION,
                    convocation.getSlug() + "/" + UUID.randomUUID()));
        }
        convocation = convocationRepository.save(convocation);
        if (convocation.getDocumentConvocation() != ancienDocument) {
            documentService.supprimerFichier(ancienDocument);
        }
        return convocationMapper.convocationToConvocationResponse(convocation);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Convocation convocation = rechercher(slug);
        documentService.supprimerFichier(convocation.getDocumentConvocation());
        convocationRepository.delete(convocation);
        return true;
    }

    private Convocation rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug de la convocation est obligatoire.");
        }
        return convocationRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucune convocation n'a été trouvée."));
    }
}
