package com.codekeys.ordreMission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OrdreMissionRequest {
    Long id;
    @NotBlank(message = "Le libellé de l'ordre de mission est obligatoire")
    private String libelle;
    @NotBlank(message = "L'objet de l'ordre de mission est obligatoire")
    private String objet;
    @NotBlank(message = "L'imputation de l'ordre de mission est obligatoire")
    private String imputationBudgetaire;
    @NotBlank(message = "Le moyen de déplacement de l'ordre de mission est obligatoire")
    private String moyenDeplacement;
    @NotBlank(message = "L'itinéraire de l'ordre de mission est obligatoire")
    private String itineraire;
    @NotNull(message = "La date de départ de l'ordre de mission est obligatoire")
    private LocalDate dateDepart;
    @NotNull(message = "Le date de retour de l'ordre de mission est obligatoire")
    private LocalDate dateRetour;
    private String observation;
    private MultipartFile ordreMission;
}
