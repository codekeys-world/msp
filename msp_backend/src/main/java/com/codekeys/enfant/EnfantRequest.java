package com.codekeys.enfant;

import com.codekeys.common.Genre;
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
public class EnfantRequest {
    Long id;
    @NotBlank(message = "Le nom de l'enfant est obligatoire")
    //@NotEmpty(message = "Le nom de l'enfant est obligatoire")
    private String nom;
    @NotBlank(message = "Le prénom de l'enfant est obligatoire")
    //@NotEmpty(message = "Le prénom de l'enfant est obligatoire")
    private String prenom;
    private String telephonePrincipal;
    private String telephoneSecondaire;
    @NotNull(message = "La date de naissance de l'enfant est obligatoire")
    @Past(message = "La date de naissance de l'enfant doit être au passé")
    private LocalDate dateNaissance;
    @NotBlank(message = "Le lieu de naissance de l'enfant est obligatoire")
    private String lieuNaissance;
    private String adresse;
    @NotNull(message = "Le genre de l'enfant est obligatoire")
    private Genre genre;
    //@NotBlank(message = "L'acte de naissance de l'enfant est obligatoire")
    private MultipartFile acteDeNaissance;
    //@NotEmpty(message = "Le certificat de vie collectif de l'enfant est obligatoire")
    private MultipartFile certificatVieCollectif;
}
