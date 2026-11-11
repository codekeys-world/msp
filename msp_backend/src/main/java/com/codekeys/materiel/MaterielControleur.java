package com.codekeys.materiel;

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

import java.util.Map;

@RestController
@RequestMapping("/materiels")
@RequiredArgsConstructor
@Tag(name = "Matériel", description = "Gestion du matériel")
public class MaterielControleur {
    private final MaterielService materielService;

    @PreAuthorize("hasAnyAuthority('ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL', 'AJOUTER_MATERIEL')")
    @PostMapping
    @Operation(summary = "Créer un matériel")
    public ResponseEntity<@NonNull ApiResponse> creer(@RequestBody @Valid MaterielRequest request) {
        return reponse("Le matériel a été enregistré.", HttpStatus.CREATED, materielService.enregistrer(request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL', 'LISTER_MATERIEL')")
    @GetMapping
    @Operation(summary = "Lister et rechercher les matériels")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "8") int size,
            @RequestParam(name = "q", defaultValue = "") String terme) {
        return reponse("Liste du matériel chargée.", HttpStatus.OK, materielService.lister(page, size, terme));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL', 'LISTER_MATERIEL')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter un matériel")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return reponse("Matériel chargé.", HttpStatus.OK, materielService.rechercherParSlug(slug));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL', 'MODIFIER_MATERIEL')")
    @PutMapping("/{slug}")
    @Operation(summary = "Modifier un matériel")
    public ResponseEntity<@NonNull ApiResponse> modifier(@PathVariable String slug, @RequestBody @Valid MaterielRequest request) {
        return reponse("Le matériel a été modifié.", HttpStatus.OK, materielService.modifier(slug, request));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL', 'SUPPRIMER_MATERIEL')")
    @DeleteMapping("/{slug}")
    @Operation(summary = "Supprimer un matériel")
    public ResponseEntity<@NonNull ApiResponse> supprimer(@PathVariable String slug) {
        return reponse("Le matériel a été supprimé.", HttpStatus.OK, materielService.supprimer(slug));
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
