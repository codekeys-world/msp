package com.codekeys.authentification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModifierMotDePasse {
    //@NotEmpty(message = "Le mot de passe ancien de l'utilisateur est obligatoire")
    @NotBlank(message = "Le mot de passe ancien de l'utilisateur est obligatoire")
    @Size(min = 8, message = "Le mot de passe ancien doit contenir au minimum 8 caractères")
    private String ancienMotDePasse;
    //@NotEmpty(message = "Le nouveau mot de passe de l'utilisateur est obligatoire")
    @NotBlank(message = "Le nouveau mot de passe de l'utilisateur est obligatoire")
    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au minimum 8 caractères")
    private String nouveauMotDePasse;
    //@NotEmpty(message = "Le mot de passe de confirmation de l'utilisateur est obligatoire")
    @NotBlank(message = "Le mot de passe de confirmation de l'utilisateur est obligatoire")
    @Size(min = 8, message = "Le mot de passe de confirmation doit contenir au minimum 8 caractères")
    private String confirmationPassword;
}
