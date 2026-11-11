package com.codekeys.fichier;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArchiveResponse {
    private Long id;
    private String slug;
    private String nom;
    private String chemin;
    private String type;
    private Long taille;
    private String typeDocument;
    private String contenuBase64;
    private String dateCreation;
}
