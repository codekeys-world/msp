package com.codekeys.plainte;

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
@RequestMapping("/plaintes")
@RequiredArgsConstructor
@Tag(name = "Plainte", description = "Gestion des plaintes")
public class PlainteControleur {
    private final PlainteService plainteService;

    @PreAuthorize("hasAuthority('AJOUTER_PLAINTE')")
    @PostMapping
    @Operation(summary = "Créer une plainte")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid PlainteRequest request) throws IOException {
        return reponse("La plainte a été enregistrée.", HttpStatus.CREATED, plainteService.enregistrer(request));
    }

    @PreAuthorize("hasAuthority('LISTER_PLAINTE')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les plaintes")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return reponse("Liste des plaintes chargée.", HttpStatus.OK, plainteService.lister(page, size, terme));
    }

    @PreAuthorize("hasAuthority('LISTER_PLAINTE')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des plaintes")
    public ResponseEntity<@NonNull ApiResponse> statistiques() {
        return reponse("Statistiques des plaintes chargées.", HttpStatus.OK, plainteService.obtenirStatistiques());
    }

    @GetMapping("/statistiques-publiques")
    @Operation(summary = "Statistiques publiques des plaintes")
    public ResponseEntity<@NonNull ApiResponse> statistiquesPubliques() {
        return reponse("Statistiques publiques des plaintes chargées.", HttpStatus.OK, plainteService.obtenirStatistiques());
    }

    @PreAuthorize("hasAuthority('RECHERCHER_PLAINTE')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter une plainte")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return reponse("Plainte chargée.", HttpStatus.OK, plainteService.rechercherParSlug(slug));
    }

    @PreAuthorize("hasAuthority('MODIFIER_PLAINTE')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier une plainte")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug, @ModelAttribute @Valid PlainteRequest request) throws IOException {
        return reponse("La plainte a été modifiée.", HttpStatus.OK, plainteService.modifier(slug, request));
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_PLAINTE')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer une plainte et son document éventuel")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return reponse("La plainte et son document éventuel ont été supprimés.",
                HttpStatus.OK, plainteService.supprimer(slug));
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
