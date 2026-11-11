export enum StatutDepense {
    EN_ATTENTE = 'EN_ATTENTE',
    VALIDEE = 'VALIDEE',
    REJETEE = 'REJETEE'
}

export interface DepenseResponse {
    slug?: string;
    libelle?: string;
    dateDepense?: string;
    montant?: number;
    statut?: StatutDepense;
    observation?: string;
    observationValidation?: string;
    fraisDeGestionSlug?: string;
    fraisDeGestionAnnee?: number;
    justificatif?: string | null;
    nomFichierJustificatif?: string;
    typeFichierJustificatif?: string;
    dateCreation?: string | null;
    dateModification?: string | null;
    creerPar?: string;
    modifierPar?: string;
}

export interface DepensePage {
    content: DepenseResponse[];
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

export function libelleStatut(statut: StatutDepense | undefined | null): string {
    switch (statut) {
        case StatutDepense.EN_ATTENTE: return 'En attente';
        case StatutDepense.VALIDEE: return 'Validée';
        case StatutDepense.REJETEE: return 'Rejetée';
        default: return 'Inconnu';
    }
}

export function couleurStatut(statut: StatutDepense | undefined | null): string {
    switch (statut) {
        case StatutDepense.EN_ATTENTE: return 'warning';
        case StatutDepense.VALIDEE: return 'success';
        case StatutDepense.REJETEE: return 'danger';
        default: return 'secondary';
    }
}
