package com.codekeys.pv;

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
@RequestMapping("/pvs")
@RequiredArgsConstructor
@Tag(name = "Procès-verbal", description = "Gestion des procès-verbaux")
public class PvControleur {
    private final PvService pvService;

    @PreAuthorize("hasAuthority('AJOUTER_PV')")
    @PostMapping
    @Operation(summary = "Créer un procès-verbal")
    public ResponseEntity<@NonNull ApiResponse> creer(@ModelAttribute @Valid PvRequest request) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse(
                "Le procès-verbal a été enregistré.", HttpStatus.CREATED, pvService.enregistrer(request)));
    }

    @PreAuthorize("hasAuthority('LISTER_PV')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les procès-verbaux")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return ResponseEntity.ok(reponse("Liste des procès-verbaux chargée.", HttpStatus.OK,
                pvService.lister(page, size, terme)));
    }

    @PreAuthorize("hasAuthority('LISTER_PV')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des procès-verbaux")
    public ResponseEntity<@NonNull ApiResponse> statistiques() {
        return ResponseEntity.ok(reponse("Statistiques des procès-verbaux chargées.", HttpStatus.OK,
                pvService.obtenirStatistiques()));
    }

    @GetMapping("/statistiques-publiques")
    @Operation(summary = "Statistiques publiques des procès-verbaux")
    public ResponseEntity<@NonNull ApiResponse> statistiquesPubliques() {
        return ResponseEntity.ok(reponse("Statistiques publiques des procès-verbaux chargées.", HttpStatus.OK,
                pvService.obtenirStatistiques()));
    }

    @PreAuthorize("hasAuthority('RECHERCHER_PV')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un procès-verbal")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return ResponseEntity.ok(reponse("Procès-verbal chargé.", HttpStatus.OK, pvService.rechercherParSlug(slug)));
    }

    @PreAuthorize("hasAuthority('MODIFIER_PV')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un procès-verbal")
    public ResponseEntity<@NonNull ApiResponse> modifier(
            @PathVariable String slug, @ModelAttribute @Valid PvRequest request) throws IOException {
        return ResponseEntity.ok(reponse("Le procès-verbal a été modifié.", HttpStatus.OK,
                pvService.modifier(slug, request)));
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_PV')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un procès-verbal et son document")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(reponse("Le procès-verbal et son document ont été supprimés.", HttpStatus.OK,
                pvService.supprimer(slug)));
    }

    private ApiResponse reponse(String message, HttpStatus status, Object resultat) {
        return ApiResponse.builder()
                .message(message)
                .developerMessage(message)
                .status(status)
                .statusCode(status.value())
                .donnees(Map.of("resultat", resultat))
                .build();
    }
}
