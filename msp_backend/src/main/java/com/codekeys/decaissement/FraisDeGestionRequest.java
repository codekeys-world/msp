package com.codekeys.decaissement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FraisDeGestionRequest {
    @NotNull(message = "L'année est obligatoire.")
    private Integer annee;
    @NotNull(message = "Le montant total est obligatoire.")
    private Long montantTotal;
    @Size(max = 2000, message = "L'observation ne peut pas dépasser 2000 caractères.")
    private String observation;
    private MultipartFile justificatif;
}
