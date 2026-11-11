package com.codekeys.encaissement;

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
@RequestMapping("/encaissements")
@RequiredArgsConstructor
@Tag(name = "Encaissement", description = "Gestion des encaissements (versements, dons, legs, intérêts)")
public class EncaissementControleur {
    private final EncaissementService encaissementService;

    @PreAuthorize("hasAuthority('AJOUTER_ENCAISSEMENT')")
    @PostMapping
    @Operation(summary = "Créer un encaissement")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid EncaissementRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("L'encaissement a été enregistré.")
                .developerMessage("Création d'un encaissement.")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .donnees(Map.of("resultat", encaissementService.enregistrer(request)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_ENCAISSEMENT')")
    @GetMapping
    @Operation(summary = "Lister les encaissements")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des encaissements a été chargée.")
                .developerMessage("Liste paginée des encaissements.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", encaissementService.lister(page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('CONSULTER_ENCAISSEMENT')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un encaissement")
    public ResponseEntity<@NonNull ApiResponse> rechercher(@PathVariable String slug,
            @RequestParam(name = "inclureJustificatif", defaultValue = "true") boolean inclureJustificatif) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("L'encaissement a été chargé.")
                .developerMessage("Recherche d'un encaissement par son slug.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", encaissementService.rechercherParSlug(slug, inclureJustificatif)))
                .build());
    }

    @PreAuthorize("hasAuthority('MODIFIER_ENCAISSEMENT')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un encaissement")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug,
            @ModelAttribute @Valid EncaissementRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("L'encaissement a été modifié.")
                .developerMessage("Modification d'un encaissement.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", encaissementService.modifier(slug, request)))
                .build());
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_ENCAISSEMENT')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un encaissement")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("L'encaissement a été supprimé.")
                .developerMessage("Suppression d'un encaissement et de son justificatif.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", encaissementService.supprimer(slug)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_ENCAISSEMENT')")
    @GetMapping("/situation/{annee}")
    @Operation(summary = "Situation des encaissements par année")
    public ResponseEntity<@NonNull ApiResponse> situation(@PathVariable int annee) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La situation des encaissements a été chargée.")
                .developerMessage("Rapport de situation des encaissements pour l'année " + annee + ".")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", encaissementService.situationParAnnee(annee)))
                .build());
    }
}
