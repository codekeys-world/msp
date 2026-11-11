package com.codekeys.conjoint;

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
public class ConjointReponse {
    private String slug;
    private String nom;
    private String prenom;
    private String telephonePrincipal;
    private String telephoneSecondaire;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresse;
    private Genre genre;
    private byte[] acteDeMariage;
    private String nomActeMariage;
    private String nomCompletConjoint;
    private LocalDateTime dateValidation;
    private String nomCompletValidateur;
}
