package com.codekeys.decaissement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FraisDeGestionResponse {
    private String slug;
    private Integer annee;
    private Long montantTotal;
    private Long montantDepense;
    private Long solde;
    private String observation;
    private byte[] justificatif;
    private String nomFichierJustificatif;
    private String typeFichierJustificatif;
    private Integer nombreDepenses;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
}
