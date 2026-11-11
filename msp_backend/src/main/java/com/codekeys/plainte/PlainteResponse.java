package com.codekeys.plainte;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PlainteResponse {
    private String slug;
    private LocalDate datePv;
    private String observation;
    private byte[] documentPlainte;
    private String nomFichierPlainte;
    private String typeFichierPlainte;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
    private LocalDateTime dateValidation;
    private LocalDateTime dateRefus;
    private String validateur;
    private String messageValidation;
}
