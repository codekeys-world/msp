package com.codekeys.fichier;

import com.codekeys.common.ApiResponse;
import com.codekeys.common.PageReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/archives")
@RequiredArgsConstructor
@Tag(name = "Archive", description = "Gestion des archives de documents")
public class ArchiveControleur {
    private final DocumentRepository documentRepository;

    private static final List<TypeDocument> ARCHIVE_TYPES = List.of(
            TypeDocument.ORDRE_MISSION,
            TypeDocument.PV,
            TypeDocument.PLAINTE,
            TypeDocument.CAS_SOCIAUX,
            TypeDocument.CONVOCATION
    );

    @PreAuthorize("hasAuthority('LISTER_ARCHIVE')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les archives")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme,
            @RequestParam(name = "type", required = false) String typeParam) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        Page<ArchiveResponse> result;

        if (typeParam != null && !typeParam.isEmpty()) {
            TypeDocument typeDoc = TypeDocument.valueOf(typeParam.toUpperCase());
            if (terme != null && !terme.isEmpty()) {
                result = documentRepository.findByTypeDocumentAndSearch(typeDoc, terme, pageable)
                        .map(this::toArchiveResponse);
            } else {
                result = documentRepository.findByTypeDocument(typeDoc, pageable)
                        .map(this::toArchiveResponse);
            }
        } else {
            if (terme != null && !terme.isEmpty()) {
                result = documentRepository.findByTypeDocumentInAndSearch(ARCHIVE_TYPES, terme, pageable)
                        .map(this::toArchiveResponse);
            } else {
                result = documentRepository.findByTypeDocumentIn(ARCHIVE_TYPES, pageable)
                        .map(this::toArchiveResponse);
            }
        }

        PageReponse<ArchiveResponse> pageReponse = new PageReponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .message("Liste des archives chargée.")
                .developerMessage("Liste des archives chargée.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", pageReponse))
                .build());
    }

    @PreAuthorize("hasAuthority('CONSULTER_ARCHIVE')")
    @GetMapping("/{id}")
    @Operation(summary = "Consulter un document d'archive")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        ArchiveResponse response = toArchiveResponse(document);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .message("Document chargé.")
                .developerMessage("Document chargé.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", response))
                .build());
    }

    private ArchiveResponse toArchiveResponse(Document document) {
        String contenuBase64 = null;
        if (document.getChemin() != null && !document.getChemin().isEmpty()) {
            try {
                byte[] bytes = Files.readAllBytes(Path.of(document.getChemin()));
                contenuBase64 = Base64.getEncoder().encodeToString(bytes);
            } catch (Exception e) {
                contenuBase64 = null;
            }
        }

        return ArchiveResponse.builder()
                .id(document.getId())
                .slug(document.getSlug())
                .nom(document.getNom())
                .chemin(document.getChemin())
                .type(document.getType())
                .taille(document.getTaille())
                .typeDocument(document.getTypeDocument().name())
                .contenuBase64(contenuBase64)
                .dateCreation(document.getDateCreation() != null ? document.getDateCreation().toString() : null)
                .build();
    }
}
