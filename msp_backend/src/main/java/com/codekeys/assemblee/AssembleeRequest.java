package com.codekeys.assemblee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AssembleeRequest(
        @NotBlank(message = "Le nom est obligatoire.") @Size(max = 200) String nom,
        @NotNull(message = "La date est obligatoire.") LocalDate date,
        @NotBlank(message = "Le lieu est obligatoire.") @Size(max = 255) String lieu) {
}
