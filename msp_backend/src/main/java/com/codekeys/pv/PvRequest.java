package com.codekeys.pv;

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
public class PvRequest {
    @NotBlank(message = "Le numéro du procès-verbal est obligatoire.")
    private String numero;
    @NotBlank(message = "L'objet du procès-verbal est obligatoire.")
    private String objet;
    @NotBlank(message = "Le type de procès-verbal est obligatoire.")
    private String type;
    @NotNull(message = "La date du procès-verbal est obligatoire.")
    private LocalDate datePv;
    private String observation;
    private MultipartFile documentPv;
}
