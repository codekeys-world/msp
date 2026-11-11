export interface FraisDeGestionResponse {
    slug?: string;
    annee?: number;
    montantTotal?: number;
    montantDepense?: number;
    solde?: number;
    observation?: string;
    justificatif?: string | null;
    nomFichierJustificatif?: string;
    typeFichierJustificatif?: string;
    nombreDepenses?: number;
    dateCreation?: string | null;
    dateModification?: string | null;
    creerPar?: string;
    modifierPar?: string;
}

export interface FraisDeGestionPage {
    content: FraisDeGestionResponse[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export function formatMontant(montant: number | undefined | null): string {
    if (montant == null) return '0 FCFA';
    return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
}
