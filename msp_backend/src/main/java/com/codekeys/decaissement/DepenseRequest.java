package com.codekeys.decaissement;

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
public class DepenseRequest {
    @NotNull(message = "Le libellé est obligatoire.")
    private String libelle;
    @NotNull(message = "La date de dépense est obligatoire.")
    private LocalDate dateDepense;
    @NotNull(message = "Le montant est obligatoire.")
    private Long montant;
    @NotNull(message = "Le frais de gestion est obligatoire.")
    private String fraisDeGestionSlug;
    @Size(max = 2000, message = "L'observation ne peut pas dépasser 2000 caractères.")
    private String observation;
    private MultipartFile justificatif;
}
