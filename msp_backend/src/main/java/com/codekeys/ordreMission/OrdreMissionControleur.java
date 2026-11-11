package com.codekeys.ordreMission;

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
@RequestMapping("/ordre-missions")
@RequiredArgsConstructor
@Tag(name = "Ordre de mission", description = "Gestion des ordres de mission")
public class OrdreMissionControleur {
    private final OrdreMissionService ordreMissionService;

    @PreAuthorize("hasAuthority('AJOUTER_ORDRE_MISSION')")
    @PostMapping
    @Operation(
            summary = "Enregistre un nouveau ordre de mission",
            description = "Créer un enregistrement d'un nouveau ordre de mission à partir de ses informations"
    )
    public ResponseEntity<@NonNull ApiResponse> creerOrdreMission(@ModelAttribute @Valid OrdreMissionRequest ordreMissionRequest) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Enregistrement d'un nouveau ordre de mission reussi !")
                        .developerMessage("Enregistrement d'un nouveau ordre de mission.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.enregistrerOrdreMission(ordreMissionRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('LISTER_ORDRE_MISSION')")
    @GetMapping
    @Operation(
            summary = "Liste les ordres de missions",
            description = "Liste toutes les ordres de missions"
    )
    public ResponseEntity<@NonNull ApiResponse> listerOrdreMission(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                   @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                   @RequestParam(name = "q", defaultValue = "", required = false) String terme) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi de la liste des ordres de mission !")
                        .developerMessage("Affichage reussi de la liste des ordres de mission.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.listerOrdreMission(page, size, terme)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('LISTER_ORDRE_MISSION')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des ordres de mission", description = "Retourne les compteurs globaux par état de validation et période de mission")
    public ResponseEntity<@NonNull ApiResponse> statistiquesOrdreMission() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Statistiques des ordres de mission chargées.")
                        .developerMessage("Compteurs des ordres de mission.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.obtenirStatistiques()))
                        .build()
        );
    }

    @GetMapping("/statistiques-publiques")
    @Operation(summary = "Statistiques publiques des ordres de mission", description = "Retourne les compteurs d'ordres de mission affichés sur la page d'accueil.")
    public ResponseEntity<@NonNull ApiResponse> statistiquesPubliquesOrdreMission() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Statistiques publiques des ordres de mission chargées.")
                        .developerMessage("Compteurs des ordres de mission pour la page d'accueil.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.obtenirStatistiques()))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('RECHERCHER_ORDRE_MISSION')")
    @GetMapping("/{slug}")
    @Operation(summary = "Rechercher un ordre de mission", description = "Recherche un ordre de mission par son slug")
    public ResponseEntity<@NonNull ApiResponse> rechercherOrdreMission(@PathVariable String slug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de l'ordre de mission reussi.")
                        .developerMessage("Recherche d'un ordre de mission.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.rechercherParSlug(slug)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('MODIFIER_ORDRE_MISSION')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un ordre de mission", description = "Modifie les informations et le document joint si un nouveau fichier est fourni")
    public ResponseEntity<@NonNull ApiResponse> modifierOrdreMission(
            @PathVariable String slug,
            @ModelAttribute @Valid OrdreMissionRequest ordreMissionRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Modification de l'ordre de mission reussie.")
                        .developerMessage("Modification d'un ordre de mission.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.modifierOrdreMission(slug, ordreMissionRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('SUPPRIMER_ORDRE_MISSION')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un ordre de mission", description = "Supprime l'ordre de mission ainsi que son document joint")
    public ResponseEntity<@NonNull ApiResponse> supprimerOrdreMission(@PathVariable String slug) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Suppression de l'ordre de mission reussie.")
                        .developerMessage("Suppression d'un ordre de mission et de son document.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.ordreMissionService.supprimerOrdreMission(slug)))
                        .build()
        );
    }
}
