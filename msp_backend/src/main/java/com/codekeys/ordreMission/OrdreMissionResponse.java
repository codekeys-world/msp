package com.codekeys.ordreMission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrdreMissionResponse {
    private String slug;
    private String libelle;
    private String objet;
    private String imputationBudgetaire;
    private String moyenDeplacement;
    private String itineraire;
    private LocalDate dateDepart;
    private LocalDate dateRetour;
    private String observation;
    //Chemin de l'ordre de mission
    private byte[] ordreMission;
    private String nomFichierOrdreMission;
    private String typeFichierOrdreMission;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
}
