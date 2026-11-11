package com.codekeys.encaissement;

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
public class EncaissementRequest {
    @NotNull(message = "Le type d'encaissement est obligatoire.")
    private TypeEncaissement typeEncaissement;
    @NotNull(message = "La date d'encaissement est obligatoire.")
    private LocalDate dateEncaissement;
    private LocalDate dateDebutPeriode;
    private LocalDate dateFinPeriode;
    @NotNull(message = "Le montant est obligatoire.")
    private Long montant;
    @Size(max = 100, message = "Le type de budget ne peut pas dépasser 100 caractères.")
    private String typeBudget;
    @Size(max = 2000, message = "L'observation ne peut pas dépasser 2000 caractères.")
    private String observation;
    private MultipartFile justificatif;
}
