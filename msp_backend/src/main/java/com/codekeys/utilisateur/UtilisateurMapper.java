package com.codekeys.utilisateur;

import com.codekeys.authorisation.Authorisation;
import com.codekeys.authorisation.AuthorisationService;
import com.codekeys.conjoint.ConjointMapper;
import com.codekeys.enfant.EnfantMapper;
import com.codekeys.fichier.DocumentService;
import com.codekeys.role.Role;
import com.codekeys.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurMapper {

    private final BCryptPasswordEncoder encoder;
    //private final GestionnaireFichier gestionnaireFichier;
    private final RoleService roleService;
    private final AuthorisationService authorisationService;
    private final DocumentService documentService;
    private final ConjointMapper conjointMapper;
    private final EnfantMapper enfantMapper;



    public Utilisateur utilisateurRequestToUtilisateur(@Valid UtilisateurRequest utilisateurRequest) {
        Role role;
        List<Authorisation> authorisations = new ArrayList<>();
        if(utilisateurRequest.getRoleId() != null){
            role = this.roleService.findRoleById(utilisateurRequest.getRoleId());
            if (utilisateurRequest.getAuthorisationsId() != null) utilisateurRequest.getAuthorisationsId().forEach(id ->
                    authorisations.add(this.authorisationService.rechercherAuthorisationParId(id)));
        }else{
            role = this.roleService.findRoleByNom("ADHERENT");
        }

        String slug = UUID.randomUUID().toString();
        return Utilisateur.builder()
                .id(utilisateurRequest.getId())
                .slug(slug)
                .nom(utilisateurRequest.getNom().toUpperCase())
                .prenom(utilisateurRequest.getPrenom())
                .genre(utilisateurRequest.getGenre())
                .telephonePrincipal(utilisateurRequest.getTelephonePrincipal())
                .telephoneSecondaire(utilisateurRequest.getTelephoneSecondaire())
                .email(utilisateurRequest.getEmail())
                .password(utilisateurRequest.getPassword() == null || utilisateurRequest.getPassword().isBlank()
                        ? null : this.encoder.encode(utilisateurRequest.getPassword()))
                .matricule(utilisateurRequest.getMatricule())
                .dateNaissance(utilisateurRequest.getDateNaissance())
                .lieuNaissance(utilisateurRequest.getLieuNaissance())
                .pere(utilisateurRequest.getPere())
                .mere(utilisateurRequest.getMere())
                .adresse(utilisateurRequest.getAdresse())
                .numeroInscription(utilisateurRequest.getNumeroInscription())
                .numeroAdhesion(utilisateurRequest.getNumeroAdhesion())
                .observations(utilisateurRequest.getObservations())
                .role(role)
                .authorisations(new java.util.LinkedHashSet<>(authorisations))
                .build();
    }

    public UtilisateurReponse utilisateurToUtilisateurReponse(Utilisateur utilisateur) {
        String profileImageBase64 = null;
        String profileImageType = null;
        if (utilisateur.getProfileImage() != null) {
            profileImageType = utilisateur.getProfileImage().getType();
            try {
                profileImageBase64 = Base64.getEncoder().encodeToString(this.documentService.readFileFromLocation(utilisateur.getProfileImage().getChemin()));
            } catch (Exception e) {
                log.warn("Impossible de lire l'image de profil pour l'utilisateur {}", utilisateur.getSlug(), e);
            }
        }
        return UtilisateurReponse.builder()
                .slug(utilisateur.getSlug())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .genre(utilisateur.getGenre())
                .email(utilisateur.getEmail())
                .matricule(utilisateur.getMatricule())
                .telephonePrincipal(utilisateur.getTelephonePrincipal())
                .telephoneSecondaire(utilisateur.getTelephoneSecondaire())
                .dateNaissance(utilisateur.getDateNaissance())
                .lieuNaissance(utilisateur.getLieuNaissance())
                .adresse(utilisateur.getAdresse())
                .profileImage(profileImageBase64)
                .profileImageType(profileImageType)
                .pere(utilisateur.getPere())
                .mere(utilisateur.getMere())
                .numeroInscription(utilisateur.getNumeroInscription())
                .numeroAdhesion(utilisateur.getNumeroAdhesion())
                .observations(utilisateur.getObservations())
                .accountLocked(utilisateur.isAccountLocked())
                .accountNonExpired(utilisateur.isAccountNonExpired())
                .enabled(utilisateur.isEnabled())
                .role(utilisateur.getRole() != null ? utilisateur.getRole().getNom() : null)
                .dateValidation(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getDateValidation() : null)
                .dateRefus(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getDateRefus() : null)
                .validateur(utilisateur.getValidationEntite() != null && utilisateur.getValidationEntite().getValidateur() != null
                        ? utilisateur.getValidationEntite().getValidateur().getNomComplet() : null)
                .message(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getMessage() : null)
                //.authorisations(authorisations)
                .build();
    }

    public UtilisateurReponseDetaillee utilisateurToUtilisateurReponseDetaillee(Utilisateur utilisateur) {
        com.codekeys.fichier.Document profile = utilisateur.getProfileImage();
        String profileImageBase64 = null;
        String profileImageType = null;
        if (profile != null) {
            profileImageType = profile.getType();
            try {
                profileImageBase64 = Base64.getEncoder().encodeToString(this.documentService.readFileFromLocation(profile.getChemin()));
            } catch (Exception e) {
                log.warn("Impossible de lire l'image de profil pour l'utilisateur {}", utilisateur.getSlug(), e);
            }
        }
        return UtilisateurReponseDetaillee.builder()
                .slug(utilisateur.getSlug())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .genre(utilisateur.getGenre())
                .email(utilisateur.getEmail())
                .matricule(utilisateur.getMatricule())
                .telephonePrincipal(utilisateur.getTelephonePrincipal())
                .telephoneSecondaire(utilisateur.getTelephoneSecondaire())
                .dateNaissance(utilisateur.getDateNaissance())
                .lieuNaissance(utilisateur.getLieuNaissance())
                .adresse(utilisateur.getAdresse())
                .profileImage(profileImageBase64)
                .profileImageType(profileImageType)
                .numeroInscription(utilisateur.getNumeroInscription())
                .numeroAdhesion(utilisateur.getNumeroAdhesion())
                .pere(utilisateur.getPere())
                .mere(utilisateur.getMere())
                .observations(utilisateur.getObservations())
                .role(utilisateur.getRole() != null ? utilisateur.getRole().getNom() : null)
                .accountLocked(utilisateur.isAccountLocked())
                .accountNonExpired(utilisateur.isAccountNonExpired())
                .enabled(utilisateur.isEnabled())
                .dateValidation(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getDateValidation() : null)
                .dateRefus(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getDateRefus() : null)
                .validateur(utilisateur.getValidationEntite() != null && utilisateur.getValidationEntite().getValidateur() != null
                        ? utilisateur.getValidationEntite().getValidateur().getNomComplet() : null)
                .message(utilisateur.getValidationEntite() != null ? utilisateur.getValidationEntite().getMessage() : null)
                .dateCreation(utilisateur.getDateCreation())
                .dateModification(utilisateur.getDateModification())
                .creerPar(utilisateur.getCreerPar())
                .modifierPar(utilisateur.getModifierPar())
                .authorisations(java.util.stream.Stream.concat(
                                utilisateur.getRole() != null && utilisateur.getRole().getAuthorisations() != null
                                        ? utilisateur.getRole().getAuthorisations().stream()
                                        : java.util.stream.Stream.empty(),
                                utilisateur.getAuthorisations() == null ? java.util.stream.Stream.empty()
                                        : utilisateur.getAuthorisations().stream())
                        .map(Authorisation::getNom).distinct().toList())
                .conjoints(utilisateur.getConjoints().stream().map(this.conjointMapper::conjointToConjointReponse).toList())
                .descendants(utilisateur.getEnfants().stream().map(this.enfantMapper::enfantToEnfantReponse).toList())
                .build();
    }
}
