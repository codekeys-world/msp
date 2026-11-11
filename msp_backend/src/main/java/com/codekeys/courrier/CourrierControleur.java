package com.codekeys.courrier;

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
@RequestMapping("/courriers")
@RequiredArgsConstructor
@Tag(name = "Courrier", description = "Gestion des courriers")
public class CourrierControleur {
    private final CourrierService courrierService;

    @PreAuthorize("hasAuthority('AJOUTER_COURRIER')")
    @PostMapping
    @Operation(summary = "Créer un courrier")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid CourrierRequest request) throws IOException {
        return reponse("Le courrier a été enregistré.", HttpStatus.CREATED, courrierService.enregistrer(request));
    }

    @PreAuthorize("hasAuthority('LISTER_COURRIER')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les courriers")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return reponse("Liste des courriers chargée.", HttpStatus.OK, courrierService.lister(page, size, terme));
    }

    @PreAuthorize("hasAuthority('LISTER_COURRIER')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des courriers")
    public ResponseEntity<@NonNull ApiResponse> statistiques() {
        return reponse("Statistiques des courriers chargées.", HttpStatus.OK, courrierService.obtenirStatistiques());
    }

    @GetMapping("/statistiques-publiques")
    @Operation(summary = "Statistiques publiques des courriers")
    public ResponseEntity<@NonNull ApiResponse> statistiquesPubliques() {
        return reponse("Statistiques publiques des courriers chargées.", HttpStatus.OK, courrierService.obtenirStatistiques());
    }

    @PreAuthorize("hasAuthority('RECHERCHER_COURRIER')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un courrier")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return reponse("Courrier chargé.", HttpStatus.OK, courrierService.rechercherParSlug(slug));
    }

    @PreAuthorize("hasAuthority('MODIFIER_COURRIER')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un courrier")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug, @ModelAttribute @Valid CourrierRequest request) throws IOException {
        return reponse("Le courrier a été modifié.", HttpStatus.OK, courrierService.modifier(slug, request));
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_COURRIER')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un courrier et son document")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return reponse("Le courrier et son document ont été supprimés.", HttpStatus.OK, courrierService.supprimer(slug));
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
