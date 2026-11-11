package com.codekeys.utilisateur;

import com.codekeys.common.Genre;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UtilisateurRequest {
    Long id;
    @NotBlank(message = "Le nom de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le nom de l'adhérent est obligatoire")
    private String nom;
    @NotBlank(message = "Le prenom de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le prenom de l'adhérent est obligatoire")
    private String prenom;
    @Email(message = "Veuillez preciser une adresse une email")
    @NotBlank(message = "L'email de l'adhérent est obligatoire")
    //@NotEmpty(message = "L'email de l'adhérent est obligatoire")
    private String email;
    @NotBlank(message = "Le numéro matricule de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le numéro matricule de l'adhérent est obligatoire")
    private String matricule;
    // Le mot de passe est généré automatiquement et envoyé par email à l'adhérent
    private String password;
    @NotBlank(message = "Le numéro de telephone principal de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le numéro de telephone principal de l'adhérent est obligatoire")
    private String telephonePrincipal;
    private String telephoneSecondaire;
    @NotNull(message = "La date de naissance de l'adhérent est obligatoire")
    @Past(message = "La date de naissance de l'adhérent doit être au passé")
    private LocalDate dateNaissance;
    @NotBlank(message = "Le numéro d'inscription de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le numéro d'inscription de l'adhérent est obligatoire")
    private String numeroInscription;
    private String numeroAdhesion;
    @NotBlank(message = "Le lieu de naissance de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le lieu de naissance de l'adhérent est obligatoire")
    private String lieuNaissance;
    @NotBlank(message = "Le nom du père de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le nom du père de l'adhérent est obligatoire")
    private String pere;
    @NotBlank(message = "Le prénom de la mère de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le prénom de la mère de l'adhérent est obligatoire")
    private String mere;
    @NotNull(message = "Le genre de l'adhérent est obligatoire")
    //@NotEmpty(message = "Le genre de l'adhérent est obligatoire")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private String adresse;
    //private MultipartFile profileImage;
    private String observations;
    private Long roleId;
    private List<Long> authorisationsId;
}
