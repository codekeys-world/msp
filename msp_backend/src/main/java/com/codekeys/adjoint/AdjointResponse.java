package com.codekeys.adjoint;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjointResponse {

    private Long id;
    private String slug;
    private String delegantNomComplet;
    private String delegantMatricule;
    private String delegantTelephone;
    private String adjointNomComplet;
    private String adjointMatricule;
    private String adjointTelephone;
    private String roleDelegue;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean estActif;
    private String motif;
    private LocalDate dateCreation;

}
