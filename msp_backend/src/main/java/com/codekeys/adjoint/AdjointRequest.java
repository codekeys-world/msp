package com.codekeys.adjoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjointRequest {

    @NotBlank(message = "Le slug de l'adjoint est obligatoire.")
    private String adjointSlug;

    @NotNull(message = "La date de début est obligatoire.")
    private LocalDate dateDebut;

    private LocalDate dateFin;

    private String motif;

}
