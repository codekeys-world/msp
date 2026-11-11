package com.codekeys.convocation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConvocationResponse {
    private String slug;
    private String description;
    private LocalDate dateConvocation;
    private byte[] documentConvocation;
    private String nomFichierConvocation;
    private String typeFichierConvocation;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
    private LocalDateTime dateValidation;
    private LocalDateTime dateRefus;
    private String validateur;
    private String messageValidation;
}
