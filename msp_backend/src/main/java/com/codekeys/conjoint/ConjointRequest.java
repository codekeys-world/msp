package com.codekeys.conjoint;

import com.codekeys.common.Genre;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConjointRequest {
    Long id;
    @NotBlank(message = "Le nom du conjoint est obligatoire")
    //@NotEmpty(message = "Le nom du conjoint est obligatoire")
    private String nom;
    @NotBlank(message = "Le prenom du conjoint est obligatoire")
    //@NotEmpty(message = "Le prenom du conjoint est obligatoire")
    private String prenom;
    @NotBlank(message = "Le numéro de telephone du conjoint est obligatoire")
    //@NotEmpty(message = "Le numéro de telephone du conjoint est obligatoire")
    private String telephonePrincipal;
    private String telephoneSecondaire;
    @NotNull(message = "La date de naissance du conjoint est obligatoire")
    @Past(message = "La date de naissance du conjoint doit être au passé")
    private LocalDate dateNaissance;
    @NotBlank(message = "Le lieu de naissance du conjoint est obligatoire")
    private String lieuNaissance;
    private String adresse;
    @NotNull(message = "Le genre du conjoint est obligatoire")
    //@NotEmpty(message = "Le genre du conjoint est obligatoire")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    //@NotBlank(message = "L'acte de naissance du conjoint est obligatoire")
    private MultipartFile acteDeMariage;
}
