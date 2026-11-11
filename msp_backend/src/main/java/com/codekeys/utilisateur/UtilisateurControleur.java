package com.codekeys.utilisateur;

import com.codekeys.authentification.ModifierMotDePasse;
import com.codekeys.authentification.MotDePasseOublierRequest;
import com.codekeys.authentification.ReinitialiserMotDePasseRequest;
import com.codekeys.conjoint.Conjoint;
import com.codekeys.enfant.Enfant;
import com.codekeys.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.TypeDocument;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/adherents")
@RequiredArgsConstructor
@Tag(
        name = "Utilisateur",
        description = "Gestion des opérations des utilisateurs (création, mise à jour, lecture, suppresion)"
)
public class UtilisateurControleur {
    private final UtilisateurService utilisateurService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @GetMapping("/acces/options")
    public ResponseEntity<@NonNull ApiResponse> optionsAccesUtilisateur() {
        return ResponseEntity.ok(ApiResponse.builder().message("Rôles et autorisations chargés.")
                .developerMessage("Options d'accès récupérées depuis la base de données.")
                .status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.optionsAccesUtilisateur())).build());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping("/{adherent-slug}/acces")
    public ResponseEntity<@NonNull ApiResponse> modifierAccesUtilisateur(
            @PathVariable("adherent-slug") String slug,
            @RequestBody @Valid AccesUtilisateurRequest request) {
        return ResponseEntity.ok(ApiResponse.builder().message("Le rôle et les autorisations ont été mis à jour.")
                .developerMessage("Droits spécifiques à cet utilisateur mis à jour.")
                .status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.modifierAccesUtilisateur(slug, request))).build());
    }

    @GetMapping("/me")
    @Operation(summary = "Afficher mon profil", description = "Retourne le profil de l'utilisateur connecté.")
    public ResponseEntity<@NonNull ApiResponse> obtenirMonProfil() {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Votre profil a été chargé.")
                .developerMessage("Lecture du profil de l'utilisateur connecté.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.obtenirProfilConnecte()))
                .build());
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modifier mon profil", description = "Met à jour les informations personnelles de l'utilisateur connecté.")
    public ResponseEntity<@NonNull ApiResponse> modifierMonProfil(@RequestBody @Valid ProfilUtilisateurRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Votre profil a été mis à jour.")
                .developerMessage("Modification du profil de l'utilisateur connecté.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.modifierProfilConnecte(request)))
                .build());
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Modifier ma photo de profil")
    public ResponseEntity<@NonNull ApiResponse> modifierMaPhoto(@RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Votre photo de profil a été mise à jour.")
                .developerMessage("Modification de la photo de profil de l'utilisateur connecté.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.modifierPhotoProfilConnecte(profileImage)))
                .build());
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Enregistre un nouveau adhérent",
            description = "Créer un enregistrement d'un nouveau adhérent à partir de ses informations"
    )
    public ResponseEntity<@NonNull ApiResponse> creerUtilisateur(
            @RequestPart("request") @Valid AdherentEnregistrementRequest request,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Enregistrement d'un nouveau adhérent reussi !")
                        .developerMessage("Enregistrement d'un nouveau adhérent.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", this.utilisateurService.enregistrerAdherent(request, multipartRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enregistre un adhérent sans informations familiales")
    public ResponseEntity<@NonNull ApiResponse> creerUtilisateurJson(@RequestBody @Valid UtilisateurRequest request) {
        throw new IllegalArgumentException("L'enregistrement d'un adhérent nécessite son certificat de vie collectif via le formulaire multipart.");
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PostMapping(value = "{adherent-slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Mise à jour la photo de profil d'un adhérent",
            description = "Mettre à jour la photo de profil d'un adhérent"
    )
    public ResponseEntity<@NonNull ApiResponse> updateUtilisateurProfile(@Valid MultipartFile profileImage, @PathVariable("adherent-slug") String adherentSlug) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Mise à jour de la photo de profil de l'adhérent reussi !")
                        .developerMessage("Mise à jour de la photo de profil de l'adhérent.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .donnees(Map.of("resultat", this.utilisateurService.updateUtilisateurImageProfile(profileImage, adherentSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('LISTER_ADHERENT')")
    @GetMapping
    @Operation(
            summary = "Lister les adhérents",
            description = "Affiche la liste complète des adhérents "
    )
    public ResponseEntity<@NonNull ApiResponse> listerUtilisateur(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                  @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                  @RequestParam(name = "q", defaultValue = "", required = false) String terme) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affiche reussi de la liste des adhérents")
                        .developerMessage("Affichage de la liste des adhérents")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.listerAdherent(page, size, terme)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('EXPORTER_ADHERENT')")
    @GetMapping("/exporter")
    @Operation(summary = "Exporter la liste des adhérents", description = "Exporte la liste complète des adhérents au format CSV compatible Excel")
    public ResponseEntity<@NonNull ApiResponse> exporterAdherents() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Export de la liste des adhérents réussi.")
                        .developerMessage("Export CSV de tous les adhérents.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.exporterAdherents()))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('LISTER_ADHERENT')")
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques des adhérents", description = "Retourne le nombre total d'adhérents et la répartition par genre")
    public ResponseEntity<@NonNull ApiResponse> statistiquesAdherents() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Statistiques des adhérents chargées.")
                        .developerMessage("Compteurs des adhérents par genre.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.obtenirStatistiquesAdherents()))
                        .build()
        );
    }

    @GetMapping("/statistiques-publiques")
    @Operation(summary = "Statistiques publiques des adhérents", description = "Retourne les effectifs d'adhérents affichés sur la page d'accueil.")
    public ResponseEntity<@NonNull ApiResponse> statistiquesPubliquesAdherents() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Statistiques publiques chargées.")
                        .developerMessage("Effectifs d'adhérents pour la page d'accueil.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.obtenirStatistiquesAdherents()))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('LISTER_ADHERENT')")
    @GetMapping("/{adherent-slug}")
    @Operation(
            summary = "Rechercher un adhérent",
            description = "Rechercher un adhérent et affiche ses informations à partir de son slug"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherentParSlug(@PathVariable("adherent-slug") String adherentSlug){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi des informations de l'adhérent")
                        .developerMessage("Affichage des informations de l'adhérent par son slug")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherDetailleeAdherent(adherentSlug)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('RECHERCHER_ADHERENT')")
    @GetMapping("/rechercher/matricule/{matricule-adherent}")
    @Operation(
            summary = "Rechercher un adhérent",
            description = "Rechercher un adhérent et affiche ses informations à partir de son numéro matricule"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherentParMatricule(@PathVariable("matricule-adherent") String matricule){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi des informations de l'adhérent")
                        .developerMessage("Affichage des informations de l'adhérent par son numéro matricule")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherParMatricule(matricule)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('RECHERCHER_ADHERENT')")
    @GetMapping("/rechercher/telephone/{numero-telephone-adherent}")
    @Operation(
            summary = "Rechercher un adhérent",
            description = "Rechercher un adhérent et affiche ses informations à partir de son numéro de téléphone"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherentParTelephone(@PathVariable("numero-telephone-adherent") String numeroTelephone){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi des informations de l'adhérent")
                        .developerMessage("Affichage des informations de l'adhérent par son numéro de téléphone")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherParNumeroTelephone(numeroTelephone)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('RECHERCHER_ADHERENT')")
    @GetMapping("/rechercher/numeroInscription/{numero-inscription-adherent}")
    @Operation(
            summary = "Rechercher un adhérent",
            description = "Rechercher un adhérent et affiche ses informations à partir de son numéro d'inscription"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherAdherentParNumeroAdherent(@PathVariable("numero-inscription-adherent") String numeroInscription){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi des informations de l'adhérent")
                        .developerMessage("Affichage des informations de l'adhérent par son numéro d'inscription")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherParNumeroInscription(numeroInscription)))
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('RECHERCHER_ADHERENT')")
    @GetMapping("/rechercher/information/{information-rechercher}")
    @Operation(
            summary = "Rechercher un adhérent",
            description = "Rechercher un adhérent et affiche ses informations à partir de l'information saisie"
    )
    public ResponseEntity<@NonNull ApiResponse> rechercherInformation(@PathVariable("information-rechercher") String information,
                                                                      @RequestParam(name = "page", defaultValue = "0", required = true) int page,
                                                                      @RequestParam(name = "size", defaultValue = "10", required = true) int size)
    {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Affichage reussi des informations de l'adhérent.")
                        .developerMessage("Affichage des informations de l'adhérent.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.rechercherParInformation(information, page, size)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping(value = "{adherent-slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Modifie un adhérent",
            description = "Modifie les informations d'un adhérent"
    )
    public ResponseEntity<@NonNull ApiResponse> modifierAdherent(
            @PathVariable("adherent-slug") String adherentSlug,
            @RequestPart("request") @Valid AdherentEnregistrementRequest request,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Modification des informations de l'adhérent reussie.")
                        .developerMessage("Modification des informations de l'adhérent.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.modifierUtilisateur(adherentSlug, request, multipartRequest)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @PutMapping(value = "{adherent-slug}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull ApiResponse> modifierAdherentJson(
            @PathVariable("adherent-slug") String adherentSlug,
            @RequestBody @Valid UtilisateurRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Modification des informations de l'adhérent reussie.")
                        .developerMessage("Modification des informations de l'adhérent.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.modifierUtilisateur(adherentSlug, request)))
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE_ADMINISTRATIF', 'ADMINISTRATEUR', 'SYSADMIN')")
    @DeleteMapping("{adherent-slug}")
    @Operation(
            summary = "Supprime un adhérent",
            description = "Supprime les informations d'un adhérent"
    )
    public ResponseEntity<@NonNull ApiResponse> supprimerAdherent(@PathVariable("adherent-slug") String adherentSlug) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Suppression reussie de l'adhérent")
                        .developerMessage("Suppression de l'adhérent")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.supprimerAdherent(adherentSlug)))
                        .build()
        );
    }


    @PostMapping(path = "mot-de-passe-oublier")
    @Operation(
            summary = "Change le mot de passe",
            description = "Change le mot de passe oublié d'un utilisateur"
    )
    public ResponseEntity<@NonNull ApiResponse> motDePasseOublier(@RequestBody @Valid MotDePasseOublierRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Si cette adresse correspond à un compte, un lien de réinitialisation sera envoyé.")
                        .developerMessage("Demande de réinitialisation envoyée.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.motDePasseOublier(request)))
                        .build()
        );
    }

    @PostMapping(path = "reinitialiser-mot-de-passe")
    public ResponseEntity<@NonNull ApiResponse> reinitialiserMotDePasse(@RequestBody @Valid ReinitialiserMotDePasseRequest request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Votre mot de passe a été réinitialisé.")
                .developerMessage("Réinitialisation par lien à usage unique.")
                .status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
                .donnees(Map.of("resultat", this.utilisateurService.reinitialiserMotDePasse(request)))
                .build());
    }

    @PostMapping(path = "changer-mot-de-passe")
    @Operation(
            summary = "Change le mot de passe",
            description = "Change le mot de passe d'un utilisateur"
    )
    public ResponseEntity<@NonNull ApiResponse> changerMotDePasse(@RequestBody @Valid ModifierMotDePasse request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Changement du mot de passe de l'utilisateur reussi.")
                        .developerMessage("Change le mot de passe de l'utilisateur.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .donnees(Map.of("resultat", this.utilisateurService.modifierMotDePasse(request)))
                        .build()
        );
    }



}
