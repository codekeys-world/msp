package com.codekeys.CasSocial;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CasSocialResponse {
    private String slug;
    private TypeCasSocial typeCas;
    private LocalDate dateCas;
    private Integer heuresHospitalisation;
    private LienDefunt lienDefunt;
    private String nomDefunt;
    private String prenomDefunt;
    private TypeBeneficiaire typeBeneficiaire;
    private String nomBeneficiaire;
    private String prenomBeneficiaire;
    private String telephoneBeneficiaire;
    private Long montantPrestation;
    private Integer dureeCotisationMois;
    private Long montantCotisationsVersees;
    private Long montantRemboursement;
    private StatutPrestation statutPrestation;
    private String observation;
    private String adherentSlug;
    private String adherentNomComplet;
    private String adherentNumeroInscription;
    private byte[] justificatif;
    private String nomFichierJustificatif;
    private String typeFichierJustificatif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
}
