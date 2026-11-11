package com.codekeys.courrier;

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
public class CourrierRequest {
    @NotBlank(message = "La référence du courrier est obligatoire.")
    private String nom;
    @NotBlank(message = "L'objet du courrier est obligatoire.")
    private String objet;
    @NotNull(message = "Le type du courrier est obligatoire.")
    private TypeCourrier typeCourrier;
    @NotBlank(message = "L'expéditeur ou le destinataire est obligatoire.")
    private String description;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private MultipartFile documentCourrier;
}
