package com.codekeys.CasSocial;

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
@RequestMapping("/cas-sociaux")
@RequiredArgsConstructor
@Tag(name = "Cas social", description = "Gestion des cas sociaux et des prestations")
public class CasSocialControleur {
    private final CasSocialService casSocialService;

    @PreAuthorize("hasAuthority('AJOUTER_CAS_SOCIAUX')")
    @PostMapping
    @Operation(summary = "Créer un cas social")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid CasSocialRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le cas social a été enregistré.")
                .developerMessage("Création d'un cas social.")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .donnees(Map.of("resultat", casSocialService.enregistrer(request)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_CAS_SOCIAUX')")
    @GetMapping
    @Operation(summary = "Lister les cas sociaux")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des cas sociaux a été chargée.")
                .developerMessage("Liste paginée des cas sociaux.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", casSocialService.lister(page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_CAS_SOCIAUX')")
    @GetMapping("/adherents")
    @Operation(summary = "Rechercher des adhérents pour un cas social")
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherents(
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Les adhérents ont été chargés.")
                .developerMessage("Recherche des adhérents à associer à un cas social.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", casSocialService.rechercherAdherents(terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('RECHERCHER_CAS_SOCIAUX')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un cas social")
    public ResponseEntity<@NonNull ApiResponse> rechercher(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le cas social a été chargé.")
                .developerMessage("Recherche d'un cas social par son slug.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", casSocialService.rechercherParSlug(slug)))
                .build());
    }

    @PreAuthorize("hasAuthority('MODIFIER_CAS_SOCIAUX')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un cas social")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug,
            @ModelAttribute @Valid CasSocialRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le cas social a été modifié.")
                .developerMessage("Modification d'un cas social.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", casSocialService.modifier(slug, request)))
                .build());
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_CAS_SOCIAUX')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un cas social")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le cas social a été supprimé.")
                .developerMessage("Suppression d'un cas social et de son justificatif.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", casSocialService.supprimer(slug)))
                .build());
    }
}
