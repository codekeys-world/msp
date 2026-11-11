package com.codekeys.enfant;

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
public class EnfantReponse {
    private String slug;
    private String nom;
    private String prenom;
    private String telephonePrincipal;
    private String telephoneSecondaire;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private Genre genre;
    private String adresse;
    private byte[] acteDeNaissance;
    private String nomCompletParent;
    private LocalDateTime dateValidation;
    private String nomCompletValidateur;
    private byte[] certificatVieCollectif;
    private String nomActeNaissance;
    private String nomCertificatVieCollectif;
}
