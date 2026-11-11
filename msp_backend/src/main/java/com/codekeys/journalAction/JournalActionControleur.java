package com.codekeys.journalAction;

import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/journal-actions")
@RequiredArgsConstructor
@Tag(name = "Commission de Contrôle", description = "Contrôle des actions et traçabilité du système")
public class JournalActionControleur {

    private final JournalActionService journalActionService;

    @PreAuthorize("hasAnyAuthority('LISTER_JOURNAL_ACTION', 'CONSULTER_JOURNAL_ACTION', 'ROLE_COMMISSION_CONTROLE', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN')")
    @GetMapping
    @Operation(summary = "Lister les actions réalisées dans le système avec tous les détails d'audit")
    public ResponseEntity<@NonNull ApiResponse> lister(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "q", defaultValue = "") String terme,
            @RequestParam(name = "module", required = false) String module,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "dateDebut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(name = "dateFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) {
        return reponse(
                "Journal des actions chargé avec succès.",
                HttpStatus.OK,
                journalActionService.lister(page, size, terme, module, statut, dateDebut, dateFin)
        );
    }

    @PreAuthorize("hasAnyAuthority('LISTER_JOURNAL_ACTION', 'CONSULTER_JOURNAL_ACTION', 'ROLE_COMMISSION_CONTROLE', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN')")
    @GetMapping("/{slug}")
    @Operation(summary = "Consulter les détails d'une action spécifique")
    public ResponseEntity<@NonNull ApiResponse> consulter(@PathVariable String slug) {
        return reponse(
                "Détails de l'action chargés avec succès.",
                HttpStatus.OK,
                journalActionService.rechercherParSlug(slug)
        );
    }

    @PreAuthorize("hasAnyAuthority('LISTER_JOURNAL_ACTION', 'CONSULTER_JOURNAL_ACTION', 'ROLE_COMMISSION_CONTROLE', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques globales des actions du système")
    public ResponseEntity<@NonNull ApiResponse> statistiques() {
        return reponse(
                "Statistiques des actions chargées avec succès.",
                HttpStatus.OK,
                journalActionService.obtenirStatistiques()
        );
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
