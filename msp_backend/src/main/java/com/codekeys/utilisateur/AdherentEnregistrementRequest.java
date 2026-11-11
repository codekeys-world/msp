package com.codekeys.utilisateur;

import com.codekeys.conjoint.ConjointRequest;
import com.codekeys.enfant.EnfantRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdherentEnregistrementRequest {
    @Valid
    @NotNull
    private UtilisateurRequest adherent;
    @Valid
    private List<ConjointRequest> conjoints = new ArrayList<>();
    @Valid
    private List<EnfantRequest> descendants = new ArrayList<>();
}
