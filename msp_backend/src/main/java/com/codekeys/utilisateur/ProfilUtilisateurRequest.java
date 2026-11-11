package com.codekeys.utilisateur;

import com.codekeys.common.Genre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProfilUtilisateurRequest {
    @NotBlank(message = "Le nom est obligatoire.")
    private String nom;
    @NotBlank(message = "Le prénom est obligatoire.")
    private String prenom;
    @NotBlank(message = "L'adresse e-mail est obligatoire.")
    @Email(message = "L'adresse e-mail n'est pas valide.")
    private String email;
    @NotBlank(message = "Le matricule est obligatoire.")
    private String matricule;
    @NotBlank(message = "Le numéro de téléphone principal est obligatoire.")
    private String telephonePrincipal;
    private String telephoneSecondaire;
    @NotNull(message = "La date de naissance est obligatoire.")
    @Past(message = "La date de naissance doit être passée.")
    private LocalDate dateNaissance;
    @NotBlank(message = "Le lieu de naissance est obligatoire.")
    private String lieuNaissance;
    private String adresse;
    @NotBlank(message = "Le numéro d'inscription est obligatoire.")
    private String numeroInscription;
    @NotBlank(message = "Le nom du père est obligatoire.")
    private String pere;
    @NotBlank(message = "Le nom de la mère est obligatoire.")
    private String mere;
    @NotNull(message = "Le genre est obligatoire.")
    private Genre genre;
    private String observations;
}
