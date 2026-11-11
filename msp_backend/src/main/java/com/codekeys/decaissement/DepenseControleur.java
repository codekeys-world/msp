package com.codekeys.decaissement;

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
@RequestMapping("/depenses")
@RequiredArgsConstructor
@Tag(name = "Dépense", description = "Gestion des dépenses liées aux frais de gestion")
public class DepenseControleur {
    private final DepenseService depenseService;

    @PreAuthorize("hasAuthority('ENGAGER_DEPENSE')")
    @PostMapping
    @Operation(summary = "Engager une dépense")
    public ResponseEntity<@NonNull ApiResponse> engager(@ModelAttribute @Valid DepenseRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été engagée.")
                .developerMessage("Engagement d'une dépense.")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .donnees(Map.of("resultat", depenseService.engager(request)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_DEPENSE')")
    @GetMapping
    @Operation(summary = "Lister les dépenses")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des dépenses a été chargée.")
                .developerMessage("Liste paginée des dépenses.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.lister(page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_DEPENSE')")
    @GetMapping("/frais-de-gestion/{fraisDeGestionSlug}")
    @Operation(summary = "Lister les dépenses d'un frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> listerParFraisDeGestion(
            @PathVariable String fraisDeGestionSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des dépenses a été chargée.")
                .developerMessage("Liste paginée des dépenses par frais de gestion.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.listerParFraisDeGestion(fraisDeGestionSlug, page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('CONSULTER_DEPENSE')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter une dépense")
    public ResponseEntity<@NonNull ApiResponse> rechercher(@PathVariable String slug,
            @RequestParam(name = "inclureJustificatif", defaultValue = "true") boolean inclureJustificatif) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été chargée.")
                .developerMessage("Recherche d'une dépense par son slug.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.rechercherParSlug(slug, inclureJustificatif)))
                .build());
    }

    @PreAuthorize("hasAuthority('MODIFIER_DEPENSE')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier une dépense")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug,
            @ModelAttribute @Valid DepenseRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été modifiée.")
                .developerMessage("Modification d'une dépense.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.modifier(slug, request)))
                .build());
    }

    @PreAuthorize("hasAuthority('VALIDER_DECAISSEMENT')")
    @PatchMapping("/{slug}/valider")
    @Operation(summary = "Valider une dépense")
    public ResponseEntity<@NonNull ApiResponse> valider(
            @PathVariable String slug,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = (body != null) ? body.get("observationValidation") : null;
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été validée.")
                .developerMessage("Validation d'une dépense par le président.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.valider(slug, observation)))
                .build());
    }

    @PreAuthorize("hasAuthority('VALIDER_DECAISSEMENT')")
    @PatchMapping("/{slug}/rejeter")
    @Operation(summary = "Rejeter une dépense")
    public ResponseEntity<@NonNull ApiResponse> rejeter(
            @PathVariable String slug,
            @RequestBody(required = false) Map<String, String> body) {
        String observation = (body != null) ? body.get("observationValidation") : null;
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été rejetée.")
                .developerMessage("Rejet d'une dépense par le président.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.rejeter(slug, observation)))
                .build());
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_DEPENSE')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer une dépense")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La dépense a été supprimée.")
                .developerMessage("Suppression d'une dépense et de son justificatif.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.supprimer(slug)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_DEPENSE')")
    @GetMapping("/situation/{annee}")
    @Operation(summary = "Situation des dépenses par année")
    public ResponseEntity<@NonNull ApiResponse> situation(@PathVariable int annee) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La situation des dépenses a été chargée.")
                .developerMessage("Rapport de situation des dépenses pour l'année " + annee + ".")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", depenseService.situationParAnnee(annee)))
                .build());
    }
}
