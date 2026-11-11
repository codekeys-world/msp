package com.codekeys.materiel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterielResponse {
    private String slug;
    private String nom;
    private String categorie;
    private String fournisseur;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private LocalDate dateAcquisition;
    private String emplacement;
    private String numeroSerie;
    private String observation;
    private StatutMateriel statut;
}
