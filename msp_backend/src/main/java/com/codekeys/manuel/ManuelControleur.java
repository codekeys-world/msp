package com.codekeys.manuel;

import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manuels")
@RequiredArgsConstructor
@Tag(
        name = "Manuels d'utilisateur",
        description = "Téléchargement des manuels PDF selon le rôle"
)
public class ManuelControleur {

    private static final Map<String, String> ROLE_MANUEL_MAP = Map.ofEntries(
            Map.entry("SYSADMIN", "manuel_sysadmin.md"),
            Map.entry("ADMINISTRATEUR", "manuel_administrateur.md"),
            Map.entry("SECRETAIRE_ADMINISTRATIF", "manuel_secretaire_administratif.md"),
            Map.entry("PRESIDENT", "manuel_president.md"),
            Map.entry("SECRETAIRE_ORGANISATION", "manuel_secretaire_organisation.md"),
            Map.entry("SECRETAIRE_AFFAIRE_SOCIALE", "manuel_secretaire_affaire_sociale.md"),
            Map.entry("TRESORIER", "manuel_tresorier.md"),
            Map.entry("COMMISSION_CONTROLE", "manuel_commission_controle.md"),
            Map.entry("ADHERENT", "manuel_adherent.md")
    );

    private static final Map<String, String> ROLE_MANUEL_TITRE = Map.ofEntries(
            Map.entry("SYSADMIN", "Manuel Administrateur système"),
            Map.entry("ADMINISTRATEUR", "Manuel Administrateur"),
            Map.entry("SECRETAIRE_ADMINISTRATIF", "Manuel Secrétaire Administratif"),
            Map.entry("PRESIDENT", "Manuel Président"),
            Map.entry("SECRETAIRE_ORGANISATION", "Manuel Secrétaire Organisation"),
            Map.entry("SECRETAIRE_AFFAIRE_SOCIALE", "Manuel Secrétaire Affaire Sociale"),
            Map.entry("TRESORIER", "Manuel Trésorier"),
            Map.entry("COMMISSION_CONTROLE", "Manuel Commission Contrôle"),
            Map.entry("ADHERENT", "Manuel Adhérent")
    );

    @GetMapping("/liste")
    @Operation(
            summary = "Liste des manuels disponibles",
            description = "Retourne la liste des manuels PDF disponibles pour le rôle de l'utilisateur connecté"
    )
    public ResponseEntity<ApiResponse> listerManuels(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("Aucun manuel disponible.")
                            .developerMessage("Utilisateur non authentifié")
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .donnees(Map.of("resultat", Collections.emptyList()))
                            .build()
            );
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .filter(ROLE_MANUEL_MAP::containsKey)
                .distinct()
                .collect(Collectors.toList());

        List<Map<String, String>> manuels = new ArrayList<>();
        for (String role : roles) {
            Map<String, String> manuel = new HashMap<>();
            manuel.put("role", role);
            manuel.put("titre", ROLE_MANUEL_TITRE.getOrDefault(role, "Manuel " + role));
            manuel.put("fichier", ROLE_MANUEL_MAP.get(role));
            manuels.add(manuel);
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Liste des manuels récupérée avec succès.")
                        .developerMessage("Liste des manuels disponibles pour les rôles de l'utilisateur")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", manuels))
                        .build()
        );
    }

    @GetMapping("/telecharger/{role}")
    @Operation(
            summary = "Télécharger un manuel PDF",
            description = "Génère et télécharge le manuel PDF correspondant au rôle spécifié"
    )
    public ResponseEntity<byte[]> telechargerManuel(@PathVariable String role, Authentication authentication) {
        // Vérifier que l'utilisateur a le rôle demandé
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (authentication.getAuthorities() != null) {
            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
            if (!hasRole) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }
        }

        String fichierMarkdown = ROLE_MANUEL_MAP.get(role);
        if (fichierMarkdown == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        try {
            // Lire le fichier Markdown depuis le classpath
            String markdownContent = lireMarkdown(fichierMarkdown);
            if (markdownContent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            byte[] pdfBytes = new ManuelPdfGenerator().generate(markdownContent, ROLE_MANUEL_TITRE.getOrDefault(role, "Manuel"));

            String fileName = "manuel_" + role.toLowerCase() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private String lireMarkdown(String fileName) {
        ClassPathResource resource = new ClassPathResource("documentation/" + fileName);
        try (InputStream input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

}
