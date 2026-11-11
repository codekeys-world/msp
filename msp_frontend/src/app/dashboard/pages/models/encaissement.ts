export type TypeEncaissement = 'VERSEMENT' | 'DONS_LEG_INTERET';

export interface EncaissementResponse {
  slug: string;
  typeEncaissement: TypeEncaissement;
  dateEncaissement: string;
  dateDebutPeriode: string | null;
  dateFinPeriode: string | null;
  montant: number;
  typeBudget: string | null;
  observation: string | null;
  justificatif: string | null;
  nomFichierJustificatif: string | null;
  typeFichierJustificatif: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
}

export interface EncaissementPage {
  content: EncaissementResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const TYPE_ENCAISSEMENT_LIBELLE: Record<TypeEncaissement, string> = {
  VERSEMENT: 'Versement',
  DONS_LEG_INTERET: 'Dons, legs, intérêts des placements'
};

export function libelleTypeEncaissement(type: TypeEncaissement): string {
  return TYPE_ENCAISSEMENT_LIBELLE[type] || type;
}

export function formatMontant(montant: number | null | undefined): string {
  if (montant == null) return '0 FCFA';
  return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
}
