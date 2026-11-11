package com.codekeys.prestation;

import com.codekeys.CasSocial.LienDefunt;
import com.codekeys.CasSocial.StatutPrestation;
import com.codekeys.CasSocial.TypeBeneficiaire;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PrestationResponse {
    private String slug;
    private TypePrestation typePrestation;
    private LocalDate datePrestation;
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
