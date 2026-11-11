package com.codekeys.admin;

import com.codekeys.common.ApiResponse;
import com.codekeys.conjoint.ConjointService;
import com.codekeys.enfant.EnfantService;
import com.codekeys.utilisateur.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(
        name = "Administrateur",
        description = "Gérer toutes les ressources (enfants, etc...)"
)
public class AdminControleur {
    private final EnfantService enfantService;
    private final ConjointService conjointService;
    private final UtilisateurService utilisateurService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/enfants")
    @Operation(
            summary = "Affichage de la liste des enfants",
            description = "Affichage de la liste des enfants avec des informations détaillées"
    )
    public ResponseEntity<@NonNull ApiResponse> getAllEnfants(@RequestParam(name = "page", defaultValue = "0", required = true) int  page, @RequestParam(name = "size", defaultValue = "10", required = true) int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des enfants reussie")
                        .developerMessage("Affichage de la liste de tous les enfants")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.listerEnfant(page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/enfants/{enfant-slug}")
    @Operation(
            summary = "Rechercher un enfant",
            description = "Affichage d'un enfant avec des informations détaillées à partir de son slug"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherEnfants(@PathVariable("enfant-slug") String enfantSlug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage des informations détaillées de l'enfant reussie")
                        .developerMessage("Affichage des informations d'un enfant")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.enfantService.rechercheDetaillee(enfantSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/conjoints")
    @Operation(
            summary = "Affichage de la liste des conjoints",
            description = "Affichage de la liste des conjoints avec des informations détaillées"
    )
    public ResponseEntity<@NonNull ApiResponse> listerConjoint(@RequestParam(name = "page", defaultValue = "0", required = true) int  page, @RequestParam(name = "size", defaultValue = "10", required = true) int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des conjoints reussie !")
                        .developerMessage("Affichage de la liste des conjoints.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.conjointService.listerConjointDetaillee(page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/conjoints/{conjoint-slug}")
    @Operation(
            summary = "Rechercher un conjoint",
            description = "Affichage d'un conjoint avec des informations détaillées à partir de son slug"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherConjoint(@PathVariable("conjoint-slug")  String conjointSlug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage des informations du conjoints reussi !")
                        .developerMessage("Affichage des informations du conjoints.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.conjointService.afficherConjoint(conjointSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR')")
    @GetMapping("/adherents/{adherent-slug}/role/{role-slug}")
    @Operation(
            summary = "Assigne un role à l'utilisateur",
            description = "Assigner un role à un utilisateur du système"
    )
    public ResponseEntity<@NonNull ApiResponse> assignerRoleAdherent(@PathVariable("adherent-slug") String adherentSlug, @PathVariable("role-slug") String roleSlug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Role assigné à l'utilisateur avec succès !")
                        .developerMessage("Assignation de rôle à l'utilisateur.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.assignerRoleAdherent(adherentSlug, roleSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/adherents/liste")
    @Operation(
            summary = "Affiche la liste des adhérents",
            description = "Affichage de la liste des adhérents"
    )
    public ResponseEntity<@NonNull ApiResponse> listerAdherent(@RequestParam(name = "page", defaultValue = "0", required = true) int  page, @RequestParam(name = "size", defaultValue = "10", required = true) int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des adhérents reussie.")
                        .developerMessage("Affichage de la liste des adhérents")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.listerAdherent(page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'PRESIDENT', 'COMMISSION_CONTROLE')")
    @GetMapping("/adherents/rechercher/{adherent-slug}")
    @Operation(
            summary = "Affiche les information d'un adhérents",
            description = "Affichage de la liste des informations des adhérents"
    )
    public ResponseEntity<@NonNull ApiResponse> listerInformationAdherent(@PathVariable("adherent-slug") String adherentSlug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des adhérents reussie.")
                        .developerMessage("Affichage de la liste des adhérents")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherDetailleeAdherent(adherentSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/utilisateurs")
    @Operation(
            summary = "Affiche la liste de tous les utilisateurs",
            description = "Affichage de la liste de tous les utilisateurs du système"
    )
    public ResponseEntity<@NonNull ApiResponse> listerUtilisateurs(@RequestParam(name = "page", defaultValue = "0", required = false) int page, @RequestParam(name = "size", defaultValue = "20", required = false) int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage de la liste des utilisateurs reussie.")
                        .developerMessage("Affichage de la liste de tous les utilisateurs")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.listerUtilisateur(page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping("/utilisateurs/{adherent-slug}/statut")
    @Operation(
            summary = "Active ou désactive un utilisateur",
            description = "Modifier le statut actif/désactivé d'un utilisateur"
    )
    public ResponseEntity<@NonNull ApiResponse> modifierStatutUtilisateur(
            @PathVariable("adherent-slug") String adherentSlug,
            @RequestParam("enabled") boolean enabled) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Statut de l'utilisateur modifié avec succès.")
                        .developerMessage("Modification du statut actif/désactivé d'un utilisateur")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.desactiverUtilisateur(adherentSlug, enabled)))
                        .build()
        );
    }
}
