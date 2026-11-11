package com.codekeys.pv;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PvResponse {
    private String slug;
    private String numero;
    private String objet;
    private String type;
    private LocalDate datePv;
    private String observation;
    private byte[] documentPv;
    private String nomFichierPv;
    private String typeFichierPv;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
    private LocalDateTime dateValidation;
    private LocalDateTime dateRefus;
    private String validateur;
    private String messageValidation;
}
