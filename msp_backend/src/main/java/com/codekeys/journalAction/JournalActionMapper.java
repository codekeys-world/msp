package com.codekeys.journalAction;

import org.springframework.stereotype.Service;

@Service
public class JournalActionMapper {

    public JournalActionResponse toResponse(JournalAction journalAction) {
        if (journalAction == null) return null;
        return JournalActionResponse.builder()
                .slug(journalAction.getSlug())
                .nom(journalAction.getNom())
                .prenom(journalAction.getPrenom())
                .nomComplet(journalAction.getNomComplet())
                .role(journalAction.getRole())
                .matricule(journalAction.getMatricule())
                .adresseIp(journalAction.getAdresseIp())
                .client(journalAction.getClient())
                .action(journalAction.getAction())
                .module(journalAction.getModule())
                .details(journalAction.getDetails())
                .statut(journalAction.getStatut())
                .dateAction(journalAction.getDateAction() != null ? journalAction.getDateAction() : journalAction.getDateCreation())
                .dateCreation(journalAction.getDateCreation())
                .build();
    }
}
