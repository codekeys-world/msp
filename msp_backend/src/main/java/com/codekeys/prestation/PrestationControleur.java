package com.codekeys.prestation;

import com.codekeys.common.ApiResponse;
import com.codekeys.CasSocial.CasSocialAdherentOption;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prestations")
@RequiredArgsConstructor
@Tag(name = "Prestation", description = "Gestion des prestations (retraite, décès, invalidité)")
public class PrestationControleur {
    private final PrestationService prestationService;

    @PreAuthorize("hasAuthority('AJOUTER_PRESTATION')")
    @PostMapping
    @Operation(summary = "Créer une prestation")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid PrestationRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La prestation a été enregistrée.")
                .developerMessage("Création d'une prestation.")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .donnees(Map.of("resultat", prestationService.enregistrer(request)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_PRESTATION')")
    @GetMapping
    @Operation(summary = "Lister les prestations")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des prestations a été chargée.")
                .developerMessage("Liste paginée des prestations.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", prestationService.lister(page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_PRESTATION')")
    @GetMapping("/adherents")
    @Operation(summary = "Rechercher des adhérents pour une prestation")
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherents(
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Les adhérents ont été chargés.")
                .developerMessage("Recherche des adhérents à associer à une prestation.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", prestationService.rechercherAdherents(terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('RECHERCHER_PRESTATION')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter une prestation")
    public ResponseEntity<@NonNull ApiResponse> rechercher(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La prestation a été chargée.")
                .developerMessage("Recherche d'une prestation par son slug.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", prestationService.rechercherParSlug(slug)))
                .build());
    }

    @PreAuthorize("hasAuthority('MODIFIER_PRESTATION')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier une prestation")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug,
            @ModelAttribute @Valid PrestationRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La prestation a été modifiée.")
                .developerMessage("Modification d'une prestation.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", prestationService.modifier(slug, request)))
                .build());
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_PRESTATION')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer une prestation")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La prestation a été supprimée.")
                .developerMessage("Suppression d'une prestation et de son justificatif.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", prestationService.supprimer(slug)))
                .build());
    }
}
