package com.codekeys.enfant;

import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;

import java.util.Map;

@RestController
@RequestMapping("/adherent/{adherent-slug}/enfants")
@RequiredArgsConstructor
@Tag(name = "Enfant", description = "Gestion des opérations sur les déscants d'un adherent")
public class EnfantControleur {
    private final EnfantService enfantService;

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Enregistrer l'enfant d'un adherent",
            description = "Enregistre les informations de l'enfant d'un adherent et retourne les informations enregistrées"
    )
    public ResponseEntity<@NonNull ApiResponse> enregisterEnfant(
            @RequestPart("request") @Valid EnfantRequest enfantRequest,
            @PathVariable("adherent-slug") String adherentSlug,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
          ApiResponse.builder()
                  .message("Enregistrement du descendant reussi.")
                  .developerMessage("Enregistrement de descendant")
                  .status(HttpStatus.CREATED)
                  .statusCode(HttpStatus.CREATED.value())
                  .donnees(Map.of("resultat", this.enfantService.enregister(enfantRequest, adherentSlug, multipartRequest)))
                  .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN', 'PRESIDENT')")
    @GetMapping("{enfant-slug}")
    @Operation(
            summary = "Rechercher l'enfant d'un adherent",
            description = "Recherche l'enfant d'un adherent à partir de son slug et retourne ses informations enregistrées"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherEnfant(@PathVariable("adherent-slug") String adherentSlug, @PathVariable("enfant-slug") String enfantSlug){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage des informations reussie.")
                        .developerMessage("Affichage des informations d'un enfant.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.rechercherEnfantParSlug(adherentSlug, enfantSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN', 'PRESIDENT')")
    @GetMapping
    @Operation(
            summary = "Lister les enfants d'un adherent",
            description = "Lister tous les enfants d'un adherent"
    )
    public ResponseEntity<@NonNull ApiResponse> listerEnfantsParAdherent(@PathVariable("adherent-slug") String adherentSlug, @RequestParam(name = "page", defaultValue = "0", required = true) int page, @RequestParam(name = "size", defaultValue = "10", required = true) int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des enfants reussie.")
                        .developerMessage("Affichage de la liste des enfants.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.listerEnfantParAdherent(adherentSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping(value = "{enfant-slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Modifier l'enfant d'un adherent",
            description = "Modifie les informations d'un enfant d'un adherent"
    )
    public ResponseEntity<@NonNull ApiResponse> modifierEnfant(
            @RequestPart("request") @Valid EnfantRequest enfantRequest,
            @PathVariable("adherent-slug") String adherentSlug,
            @PathVariable("enfant-slug") String enfantSlug,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Modification de l'enfant reussie.")
                        .developerMessage("Modification de l'enfant.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.modifierEnfant(enfantRequest, adherentSlug, enfantSlug, multipartRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @DeleteMapping("{enfant-slug}")
    @Operation(
            summary = "Supprime l'enfant d'un adherent",
            description = "Supprime les informations d'un enfant d'un adherent"
    )
    public ResponseEntity<@NonNull ApiResponse> supprimerEnfant(@PathVariable("adherent-slug") String adherentSlug, @PathVariable("enfant-slug") String enfantSlug) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Suppression de l'enfant reussie.")
                        .developerMessage("Suppression de l'enfant.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.supprimerEnfant(adherentSlug, enfantSlug)))
                        .build()
        );
    }
}
