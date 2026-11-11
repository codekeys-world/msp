package com.codekeys.CasSocial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasSocialRequest {
    @NotBlank(message = "L'adhérent concerné est obligatoire.")
    private String adherentSlug;
    @NotNull(message = "Le type du cas social est obligatoire.")
    private TypeCasSocial typeCas;
    @NotNull(message = "La date du cas social est obligatoire.")
    private LocalDate dateCas;
    private Integer heuresHospitalisation;
    private LienDefunt lienDefunt;
    @Size(max = 100, message = "Le nom du défunt ne peut pas dépasser 100 caractères.")
    private String nomDefunt;
    @Size(max = 100, message = "Le prénom du défunt ne peut pas dépasser 100 caractères.")
    private String prenomDefunt;
    @NotNull(message = "Le bénéficiaire de la prestation est obligatoire.")
    private TypeBeneficiaire typeBeneficiaire;
    @Size(max = 100, message = "Le nom du bénéficiaire ne peut pas dépasser 100 caractères.")
    private String nomBeneficiaire;
    @Size(max = 100, message = "Le prénom du bénéficiaire ne peut pas dépasser 100 caractères.")
    private String prenomBeneficiaire;
    @Size(max = 30, message = "Le numéro du bénéficiaire ne peut pas dépasser 30 caractères.")
    private String telephoneBeneficiaire;
    private Integer dureeCotisationMois;
    private Long montantCotisationsVersees;
    @Size(max = 2000, message = "L'observation ne peut pas dépasser 2000 caractères.")
    private String observation;
    private MultipartFile justificatif;
}
