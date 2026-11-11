package com.codekeys.utilisateur;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class AccesUtilisateurRequest {
    @NotBlank private String roleSlug;
    @NotNull private List<Long> authorisationIds;
}
