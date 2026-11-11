package com.codekeys.authentification;

import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("authentification")
@RequiredArgsConstructor
@Tag(
        name = "Authentification",
        description = "Gestion des opérations d'authentification et d'authorisation (connexion, deconnexion, activation, mot de passe oublié)"
)
public class AuthentificationControleur {

    private final AuthentificationService authentificationService;

    @PostMapping("/connexion")
    @Operation(
            summary = "Authentifie un utilisateur",
            description = "Authentifie un utilisateur à partir de son (email, telephone, matricule) et mot de passe"
    )
    public ResponseEntity<@NonNull ApiResponse> connexion(@RequestBody @Valid AuthentificationRequest authentificationRequest) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .developerMessage("Authentification de l'utilisateur")
                        .message("Authentification de l'utilisateur reussie")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.authentificationService.connecter(authentificationRequest)))
                        .build()
        );
    }

    @PostMapping("/deconnexion")
    @Operation(
            summary = "Deconnecte un utilisateur",
            description = "Deconnecte un utilisateur et invalide sa session"
    )
    public ResponseEntity<@NonNull ApiResponse> deconnexion() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .developerMessage("Deconnexion de l'utilisateur")
                        .message("Deconnexion de l'utilisateur reussie")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.authentificationService.deconnecter()))
                        .build()
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "Donne l'utilisateur connecté",
            description = "Retourne les informations de l'utilisateur connecté"
    )
    public ResponseEntity<@NonNull ApiResponse> me() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .developerMessage("Donne le token de l'utilisateur connecté")
                        .message("Accès au token de l'utilisateur reussie")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.authentificationService.getAuthenticatedUserToken()))
                        .build()
        );
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh le token",
            description = "Refresh le token expiré de l'utilisateur"
    )
    public ResponseEntity<@NonNull ApiResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .developerMessage("Refresh du token reussi")
                        .message("Mise à jour du token reussi")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.authentificationService.refreshToken(request.getRefreshToken())))
                        .build()
        );
    }


}
