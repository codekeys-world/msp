package com.codekeys.conjoint;

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
@RequestMapping("/adherent/{adherent-slug}/conjoints")
@RequiredArgsConstructor
@Tag(name = "Conjoint", description = "Gestion des conjoints")
public class ConjointControleur{
    private final ConjointService conjointService;


    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Enregistrement d'un conjoint",
            description = "Enregistre les informations d'un conjoint avec ses informations"
    )
    public ResponseEntity<@NonNull ApiResponse> ajouterConjoint(
            @RequestPart("request") @Valid ConjointRequest conjointRequest,
            @PathVariable("adherent-slug") String adherentSlug,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Enregistrement de conjoint reussi!")
                        .developerMessage("Enregistrement de conjoint.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", this.conjointService.ajouterConjoint(conjointRequest, adherentSlug, multipartRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN', 'PRESIDENT')")
    @GetMapping
    @Operation(
            summary = "Lister un conjoint",
            description = "Liste les informations d'un conjoint"
    )
    public ResponseEntity<@NonNull ApiResponse> listerConjoint(@PathVariable("adherent-slug") String adherentSlug, @RequestParam(name = "page", defaultValue = "0", required = true) int page, @RequestParam(name = "size", defaultValue = "10", required = true) int size){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage des informations des conjoints reussi !")
                        .developerMessage("Affichage des informations des conjoints.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.conjointService.listerConjoint(adherentSlug, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN', 'PRESIDENT')")
    @GetMapping("{conjoint-slug}")
    @Operation(
            summary = "Rechercher un conjoint",
            description = "Recherche un conjoint par slug et affiche ses informations"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherConjoint(@PathVariable("adherent-slug") String adherentSlug,  @PathVariable("conjoint-slug") String conjointSlug){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage des informations du conjoint reussi !")
                        .developerMessage("Affichage des informations du conjoints.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.conjointService.rechercherConjoint(adherentSlug, conjointSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping(value = "{conjoint-slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Modifier un conjoint",
            description = "Modifie les informations d'un conjoint"
    )
    public ResponseEntity<@NonNull ApiResponse> modifierConjoint(
            @RequestPart("request") @Valid ConjointRequest conjointRequest,
            @PathVariable("adherent-slug") String adherentSlug,
            @PathVariable("conjoint-slug") String conjointSlug,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Modification des informations du conjoint reussie !")
                        .developerMessage("Modification des informations du conjoints.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.conjointService.modifierConjoint(conjointRequest, adherentSlug, conjointSlug, multipartRequest)))
                        .build()
        );
    }


    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @DeleteMapping("{conjoint-slug}")
    @Operation(
            summary = "Supprimer un conjoint",
            description = "Supprimer les information d'un conjoint "
    )
    public ResponseEntity<@NonNull ApiResponse> supprimerConjoint(@PathVariable("adherent-slug") String adherentSlug, @PathVariable("conjoint-slug") String conjointSlug) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Suppression des informations du conjoint reussi !")
                        .developerMessage("Suppression des informations du conjoint.")
                        .statusCode(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .donnees(Map.of("resultat", this.conjointService.supprimerConjoint(adherentSlug, conjointSlug)))
                        .build()
        );
    }
}
