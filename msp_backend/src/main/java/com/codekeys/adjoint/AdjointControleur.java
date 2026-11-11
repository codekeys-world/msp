package com.codekeys.adjoint;

import com.codekeys.common.ApiResponse;
import com.codekeys.common.PageReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/adjoints")
@RequiredArgsConstructor
@Tag(name = "Adjoint", description = "Gestion des adjoints par rôle")
public class AdjointControleur {

    private final AdjointService adjointService;

    // ==================== Endpoints par rôle spécifique ====================
    // Chaque rôle ne peut accéder qu'à ses propres adjoints

    // --- Secrétaire Administratif ---
    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping("/SECRETAIRE_ADMINISTRATIF/{delegant-slug}")
    @Operation(summary = "Désigner un adjoint Secrétaire Administratif", description = "Le Secrétaire Administratif désigne un adjoint pour son rôle")
    public ResponseEntity<@NonNull ApiResponse> designerAdjointSecAdmin(
            @RequestPart("request") @Valid AdjointRequest request,
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désigné avec succès !")
                        .developerMessage("Désignation d'un adjoint pour le rôle SECRETAIRE_ADMINISTRATIF.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", adjointService.designerAdjoint(request, delegantSlug, "SECRETAIRE_ADMINISTRATIF")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_ADMINISTRATIF/{delegant-slug}")
    @Operation(summary = "Lister les adjoints du Secrétaire Administratif", description = "Liste les adjoints désignés par le Secrétaire Administratif")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsSecAdmin(
            @PathVariable("delegant-slug") String delegantSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints récupérée avec succès !")
                        .developerMessage("Liste des adjoints du Secrétaire Administratif.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsParDelegant(delegantSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_ADMINISTRATIF/{delegant-slug}/actif")
    @Operation(summary = "Obtenir l'adjoint actif du Secrétaire Administratif", description = "Retourne l'adjoint actif du Secrétaire Administratif")
    public ResponseEntity<@NonNull ApiResponse> getAdjointActifSecAdmin(
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint actif récupéré avec succès !")
                        .developerMessage("Adjoint actif du Secrétaire Administratif.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.getAdjointActif(delegantSlug, "SECRETAIRE_ADMINISTRATIF")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping("/{slug}/desactiver")
    @Operation(summary = "Désactiver un adjoint", description = "Désactive une délégation d'adjoint")
    public ResponseEntity<@NonNull ApiResponse> desactiverAdjoint(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désactivé avec succès !")
                        .developerMessage("Désactivation de l'adjoint " + slug + ".")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.desactiverAdjoint(slug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping("/{slug}/reactiver")
    @Operation(summary = "Réactiver un adjoint", description = "Réactive une délégation d'adjoint")
    public ResponseEntity<@NonNull ApiResponse> reactivierAdjoint(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint réactivé avec succès !")
                        .developerMessage("Réactivation de l'adjoint " + slug + ".")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.reactivierAdjoint(slug)))
                        .build()
        );
    }

    // --- Président ---
    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping("/PRESIDENT/{delegant-slug}")
    @Operation(summary = "Désigner un adjoint du Président", description = "Le Président désigne un adjoint pour son rôle")
    public ResponseEntity<@NonNull ApiResponse> designerAdjointPresident(
            @RequestPart("request") @Valid AdjointRequest request,
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désigné avec succès !")
                        .developerMessage("Désignation d'un adjoint pour le rôle PRESIDENT.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", adjointService.designerAdjoint(request, delegantSlug, "PRESIDENT")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/PRESIDENT/{delegant-slug}")
    @Operation(summary = "Lister les adjoints du Président", description = "Liste les adjoints désignés par le Président")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsPresident(
            @PathVariable("delegant-slug") String delegantSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints récupérée avec succès !")
                        .developerMessage("Liste des adjoints du Président.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsParDelegant(delegantSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('PRESIDENT', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/PRESIDENT/{delegant-slug}/actif")
    @Operation(summary = "Obtenir l'adjoint actif du Président", description = "Retourne l'adjoint actif du Président")
    public ResponseEntity<@NonNull ApiResponse> getAdjointActifPresident(
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint actif récupéré avec succès !")
                        .developerMessage("Adjoint actif du Président.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.getAdjointActif(delegantSlug, "PRESIDENT")))
                        .build()
        );
    }

    // --- Trésorier ---
    @PreAuthorize("hasAnyRole('TRESORIER', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping("/TRESORIER/{delegant-slug}")
    @Operation(summary = "Désigner un adjoint du Trésorier", description = "Le Trésorier désigne un adjoint pour son rôle")
    public ResponseEntity<@NonNull ApiResponse> designerAdjointTresorier(
            @RequestPart("request") @Valid AdjointRequest request,
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désigné avec succès !")
                        .developerMessage("Désignation d'un adjoint pour le rôle TRESORIER.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", adjointService.designerAdjoint(request, delegantSlug, "TRESORIER")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('TRESORIER', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/TRESORIER/{delegant-slug}")
    @Operation(summary = "Lister les adjoints du Trésorier", description = "Liste les adjoints désignés par le Trésorier")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsTresorier(
            @PathVariable("delegant-slug") String delegantSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints récupérée avec succès !")
                        .developerMessage("Liste des adjoints du Trésorier.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsParDelegant(delegantSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('TRESORIER', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/TRESORIER/{delegant-slug}/actif")
    @Operation(summary = "Obtenir l'adjoint actif du Trésorier", description = "Retourne l'adjoint actif du Trésorier")
    public ResponseEntity<@NonNull ApiResponse> getAdjointActifTresorier(
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint actif récupéré avec succès !")
                        .developerMessage("Adjoint actif du Trésorier.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.getAdjointActif(delegantSlug, "TRESORIER")))
                        .build()
        );
    }

    // --- Secrétaire Organisation ---
    @PreAuthorize("hasAnyRole('SECRETAIRE_ORGANISATION', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping("/SECRETAIRE_ORGANISATION/{delegant-slug}")
    @Operation(summary = "Désigner un adjoint du Secrétaire Organisation", description = "Le Secrétaire Organisation désigne un adjoint pour son rôle")
    public ResponseEntity<@NonNull ApiResponse> designerAdjointSecOrg(
            @RequestPart("request") @Valid AdjointRequest request,
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désigné avec succès !")
                        .developerMessage("Désignation d'un adjoint pour le rôle SECRETAIRE_ORGANISATION.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", adjointService.designerAdjoint(request, delegantSlug, "SECRETAIRE_ORGANISATION")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ORGANISATION', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_ORGANISATION/{delegant-slug}")
    @Operation(summary = "Lister les adjoints du Secrétaire Organisation", description = "Liste les adjoints désignés par le Secrétaire Organisation")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsSecOrg(
            @PathVariable("delegant-slug") String delegantSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints récupérée avec succès !")
                        .developerMessage("Liste des adjoints du Secrétaire Organisation.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsParDelegant(delegantSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ORGANISATION', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_ORGANISATION/{delegant-slug}/actif")
    @Operation(summary = "Obtenir l'adjoint actif du Secrétaire Organisation", description = "Retourne l'adjoint actif du Secrétaire Organisation")
    public ResponseEntity<@NonNull ApiResponse> getAdjointActifSecOrg(
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint actif récupéré avec succès !")
                        .developerMessage("Adjoint actif du Secrétaire Organisation.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.getAdjointActif(delegantSlug, "SECRETAIRE_ORGANISATION")))
                        .build()
        );
    }

    // --- Secrétaire Affaires Sociales ---
    @PreAuthorize("hasAnyRole('SECRETAIRE_AFFAIRE_SOCIALE', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping("/SECRETAIRE_AFFAIRE_SOCIALE/{delegant-slug}")
    @Operation(summary = "Désigner un adjoint du Secrétaire Affaires Sociales", description = "Le Secrétaire Affaires Sociales désigne un adjoint pour son rôle")
    public ResponseEntity<@NonNull ApiResponse> designerAdjointSecAffaireSociale(
            @RequestPart("request") @Valid AdjointRequest request,
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint désigné avec succès !")
                        .developerMessage("Désignation d'un adjoint pour le rôle SECRETAIRE_AFFAIRE_SOCIALE.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", adjointService.designerAdjoint(request, delegantSlug, "SECRETAIRE_AFFAIRE_SOCIALE")))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_AFFAIRE_SOCIALE', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_AFFAIRE_SOCIALE/{delegant-slug}")
    @Operation(summary = "Lister les adjoints du Secrétaire Affaires Sociales", description = "Liste les adjoints désignés par le Secrétaire Affaires Sociales")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsSecAffaireSociale(
            @PathVariable("delegant-slug") String delegantSlug,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints récupérée avec succès !")
                        .developerMessage("Liste des adjoints du Secrétaire Affaires Sociales.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsParDelegant(delegantSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_AFFAIRE_SOCIALE', 'ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/SECRETAIRE_AFFAIRE_SOCIALE/{delegant-slug}/actif")
    @Operation(summary = "Obtenir l'adjoint actif du Secrétaire Affaires Sociales", description = "Retourne l'adjoint actif du Secrétaire Affaires Sociales")
    public ResponseEntity<@NonNull ApiResponse> getAdjointActifSecAffaireSociale(
            @PathVariable("delegant-slug") String delegantSlug
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint actif récupéré avec succès !")
                        .developerMessage("Adjoint actif du Secrétaire Affaires Sociales.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.getAdjointActif(delegantSlug, "SECRETAIRE_AFFAIRE_SOCIALE")))
                        .build()
        );
    }

    // ==================== Endpoints Admin/Sysadmin uniquement ====================

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/actifs")
    @Operation(summary = "Lister les adjoints actifs", description = "Liste tous les adjoints actuellement actifs (Admin/Sysadmin uniquement)")
    public ResponseEntity<@NonNull ApiResponse> listerAdjointsActifs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des adjoints actifs récupérée avec succès !")
                        .developerMessage("Liste des adjoints actifs.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.listerAdjointsActifs(page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/{slug}")
    @Operation(summary = "Rechercher un adjoint", description = "Recherche un adjoint par son slug (Admin/Sysadmin uniquement)")
    public ResponseEntity<@NonNull ApiResponse> rechercherAdjoint(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Adjoint trouvé avec succès !")
                        .developerMessage("Recherche de l'adjoint " + slug + ".")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", adjointService.rechercherAdjoint(slug)))
                        .build()
        );
    }
}
