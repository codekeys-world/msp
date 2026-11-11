/**
 * Ligne d'un rapport de situation des dépenses (décaissements).
 */
export interface DepenseSituation {
  mois: number;
  libelleMois: string;
  libelle: string;       // Libellé de la dépense
  statut: string;        // EN_ATTENTE, VALIDEE, REJETEE
  nombre: number;
  montantTotal: number;
}

/**
 * Formatage d'un montant en FCFA.
 */
export function formatMontantDepense(montant: number | null | undefined): string {
  if (montant == null) return '—';
  return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
}

/**
 * Libellé du statut.
 */
export function libelleStatutDepense(statut: string): string {
  switch (statut) {
    case 'EN_ATTENTE': return 'En attente';
    case 'VALIDEE': return 'Validée';
    case 'REJETEE': return 'Rejetée';
    default: return statut;
  }
}

/**
 * Couleur Bootstrap selon le statut.
 */
export function couleurStatutDepense(statut: string): string {
  switch (statut) {
    case 'EN_ATTENTE': return 'warning';
    case 'VALIDEE': return 'success';
    case 'REJETEE': return 'danger';
    default: return 'secondary';
  }
}

/**
 * Libellé du mois à partir du numéro.
 */
const MOIS_FR = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
];
export function libelleMoisDepense(mois: number): string {
  return MOIS_FR[mois - 1] || '';
}
