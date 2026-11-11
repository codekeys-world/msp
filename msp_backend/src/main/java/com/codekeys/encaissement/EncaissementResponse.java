package com.codekeys.encaissement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EncaissementResponse {
    private String slug;
    private TypeEncaissement typeEncaissement;
    private LocalDate dateEncaissement;
    private LocalDate dateDebutPeriode;
    private LocalDate dateFinPeriode;
    private Long montant;
    private String typeBudget;
    private String observation;
    private byte[] justificatif;
    private String nomFichierJustificatif;
    private String typeFichierJustificatif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
}
