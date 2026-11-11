package com.codekeys.encaissement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Ligne d'un rapport de situation des encaissements :
 * regroupée par mois et par type d'encaissement.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncaissementSituationResponse {
    private int mois;               // 1..12
    private String libelleMois;     // "Janvier", "Février", ...
    private String libelle;         // "Versement", "Dons, legs, intérêt"
    private Long nombre;            // nombre d'encaissements regroupés
    private Long montantTotal;      // somme des montants

    public EncaissementSituationResponse(
            Integer mois, TypeEncaissement typeEncaissement, Long nombre, Long montantTotal) {
        this.mois = mois;
        this.libelleMois = Month.of(mois).getDisplayName(TextStyle.FULL, Locale.FRANCE);
        this.libelle = switch (typeEncaissement) {
            case VERSEMENT -> "Versement";
            case DONS_LEG_INTERET -> "Dons, legs, intérêt";
        };
        this.nombre = nombre;
        this.montantTotal = montantTotal;
    }
}
