package com.codekeys.courrier;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CourrierResponse {
    private String slug;
    private String nom;
    private String objet;
    private TypeCourrier typeCourrier;
    private String description;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private byte[] documentCourrier;
    private String nomFichierCourrier;
    private String typeFichierCourrier;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
    private LocalDateTime dateValidation;
    private LocalDateTime dateRefus;
}
