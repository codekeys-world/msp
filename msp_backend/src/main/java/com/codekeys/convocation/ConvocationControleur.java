package com.codekeys.convocation;

import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/convocations")
@RequiredArgsConstructor
@Tag(name = "Convocation", description = "Gestion des convocations")
public class ConvocationControleur {
    private final ConvocationService convocationService;

    @PreAuthorize("hasAuthority('AJOUTER_CONVOCATION')")
    @PostMapping
    @Operation(summary = "Créer une convocation")
    public ResponseEntity<@NonNull ApiResponse> creer(
            @ModelAttribute @Valid ConvocationRequest request) throws IOException {
        return reponse("La convocation a été enregistrée.", HttpStatus.CREATED,
                convocationService.enregistrer(request));
    }

    @PreAuthorize("hasAuthority('LISTER_CONVOCATION')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les convocations")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return reponse("Liste des convocations chargée.", HttpStatus.OK,
                convocationService.lister(page, size, terme));
    }

    @PreAuthorize("hasAuthority('RECHERCHER_CONVOCATION')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter une convocation")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return reponse("Convocation chargée.", HttpStatus.OK, convocationService.rechercherParSlug(slug));
    }

    @PreAuthorize("hasAuthority('MODIFIER_CONVOCATION')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier une convocation")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug, @ModelAttribute @Valid ConvocationRequest request) throws IOException {
        return reponse("La convocation a été modifiée.", HttpStatus.OK,
                convocationService.modifier(slug, request));
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_CONVOCATION')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer une convocation et son document éventuel")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return reponse("La convocation et son document éventuel ont été supprimés.",
                HttpStatus.OK, convocationService.supprimer(slug));
    }

    private ResponseEntity<@NonNull ApiResponse> reponse(String message, HttpStatus status, Object resultat) {
        return ResponseEntity.status(status).body(ApiResponse.builder()
                .message(message)
                .developerMessage(message)
                .status(status)
                .statusCode(status.value())
                .donnees(Map.of("resultat", resultat))
                .build());
    }
}
