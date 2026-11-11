package com.codekeys.materiel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MaterielRequest {
    @NotBlank(message = "Le nom du matériel est obligatoire.")
    private String nom;

    @NotBlank(message = "La catégorie du matériel est obligatoire.")
    private String categorie;

    @NotBlank(message = "Le fournisseur est obligatoire.")
    private String fournisseur;

    @NotNull(message = "La quantité est obligatoire.")
    @Min(value = 0, message = "La quantité ne peut pas être négative.")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Le prix unitaire ne peut pas être négatif.")
    private BigDecimal prixUnitaire;

    private LocalDate dateAcquisition;

    @NotBlank(message = "L'emplacement est obligatoire.")
    private String emplacement;

    private String numeroSerie;

    private String observation;

    @NotNull(message = "Le statut du matériel est obligatoire.")
    private StatutMateriel statut;
}
