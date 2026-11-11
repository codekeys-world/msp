/**
 * Ligne d'un rapport de situation des encaissements.
 */
export interface EncaissementSituation {
  mois: number;
  libelleMois: string;
  libelle: string;       // "Versement" | "Dons, legs, intérêt"
  nombre: number;
  montantTotal: number;
}

/**
 * Formatage d'un montant en FCFA.
 */
export function formatMontantEncaissement(montant: number | null | undefined): string {
  if (montant == null) return '—';
  return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
}

/**
 * Libellé du mois à partir du numéro.
 */
const MOIS_FR = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
];
export function libelleMois(mois: number): string {
  return MOIS_FR[mois - 1] || '';
}
