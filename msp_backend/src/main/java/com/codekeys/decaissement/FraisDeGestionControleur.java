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
@RequestMapping("/frais-de-gestion")
@RequiredArgsConstructor
@Tag(name = "Frais de Gestion", description = "Gestion des frais de gestion annuels")
public class FraisDeGestionControleur {
    private final FraisDeGestionService fraisDeGestionService;

    @PreAuthorize("hasAnyAuthority('ENGAGER_DEPENSE', 'MODIFIER_DEPENSE')")
    @PostMapping
    @Operation(summary = "Créer un frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid FraisDeGestionRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le frais de gestion a été enregistré.")
                .developerMessage("Création d'un frais de gestion.")
                .status(HttpStatus.CREATED)
                .statusCode(HttpStatus.CREATED.value())
                .donnees(Map.of("resultat", fraisDeGestionService.enregistrer(request)))
                .build());
    }

    @PreAuthorize("hasAuthority('LISTER_DEPENSE')")
    @GetMapping
    @Operation(summary = "Lister les frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("La liste des frais de gestion a été chargée.")
                .developerMessage("Liste paginée des frais de gestion.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", fraisDeGestionService.lister(page, size, terme)))
                .build());
    }

    @PreAuthorize("hasAuthority('ENGAGER_DEPENSE')")
    @GetMapping("/options")
    @Operation(summary = "Lister les frais de gestion pour le formulaire de dépense")
    public ResponseEntity<@NonNull ApiResponse> listerOptions() {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Les frais de gestion disponibles ont été chargés.")
                .developerMessage("Options minimales pour le formulaire de dépense.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", fraisDeGestionService.listerOptions()))
                .build());
    }

    @PreAuthorize("hasAuthority('CONSULTER_DEPENSE')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> rechercher(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le frais de gestion a été chargé.")
                .developerMessage("Recherche d'un frais de gestion par son slug.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", fraisDeGestionService.rechercherParSlug(slug)))
                .build());
    }

    @PreAuthorize("hasAuthority('MODIFIER_DEPENSE')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug,
            @ModelAttribute @Valid FraisDeGestionRequest request) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le frais de gestion a été modifié.")
                .developerMessage("Modification d'un frais de gestion.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", fraisDeGestionService.modifier(slug, request)))
                .build());
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_DEPENSE')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un frais de gestion")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Le frais de gestion a été supprimé.")
                .developerMessage("Suppression d'un frais de gestion et de son justificatif.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", fraisDeGestionService.supprimer(slug)))
                .build());
    }
}
