package com.codekeys.journalAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalActionResponse {
    private String slug;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String role;
    private String matricule;
    private String adresseIp;
    private String client;
    private String action;
    private String module;
    private String details;
    private String statut;
    private LocalDateTime dateAction;
    private LocalDateTime dateCreation;
}
