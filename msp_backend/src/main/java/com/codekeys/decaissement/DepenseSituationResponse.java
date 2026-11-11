package com.codekeys.decaissement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ligne d'un rapport de situation des décaissements (dépenses) :
 * regroupée par mois et par libellé de dépense.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepenseSituationResponse {
    private int mois;               // 1..12
    private String libelleMois;     // "Janvier", "Février", ...
    private String libelle;         // Libellé de la dépense
    private String statut;          // EN_ATTENTE, VALIDEE, REJETEE
    private Long nombre;            // nombre de dépenses regroupées
    private Long montantTotal;      // somme des montants
}
