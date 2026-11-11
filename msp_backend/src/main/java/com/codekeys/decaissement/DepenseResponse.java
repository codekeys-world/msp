package com.codekeys.decaissement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DepenseResponse {
    private String slug;
    private String libelle;
    private LocalDate dateDepense;
    private Long montant;
    private StatutDepense statut;
    private String observation;
    private String observationValidation;
    private String fraisDeGestionSlug;
    private Integer fraisDeGestionAnnee;
    private byte[] justificatif;
    private String nomFichierJustificatif;
    private String typeFichierJustificatif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
}
