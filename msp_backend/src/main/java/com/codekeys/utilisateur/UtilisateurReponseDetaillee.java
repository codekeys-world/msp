package com.codekeys.utilisateur;


import lombok.*;
import lombok.experimental.SuperBuilder;
import com.codekeys.conjoint.ConjointReponse;
import com.codekeys.enfant.EnfantReponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UtilisateurReponseDetaillee extends UtilisateurReponse{
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creerPar;
    private String modifierPar;
    private List<String> authorisations;
    private List<ConjointReponse> conjoints;
    private List<EnfantReponse> descendants;
}
