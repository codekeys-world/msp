package com.codekeys.utilisateur;

import com.codekeys.common.Genre;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UtilisateurReponse {
    private String slug;
    private String nom;
    private String prenom;
    private Genre genre;
    private String email;
    private String matricule;
    private String telephonePrincipal;
    private String telephoneSecondaire;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresse;
    private String numeroInscription;
    private String numeroAdhesion;
    private String pere;
    private String mere;
    private String profileImage;
    private String profileImageType;
    private String observations;
    private String role;
    private boolean accountLocked;
    private boolean accountNonExpired;
    private boolean enabled;
    private LocalDateTime dateValidation;
    private LocalDateTime dateRefus;
    private String validateur;
    private String message;
}
