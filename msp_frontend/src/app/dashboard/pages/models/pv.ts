export interface PvResponse {
  slug: string;
  numero: string;
  objet: string;
  type: string;
  datePv: string;
  observation?: string | null;
  documentPv?: string | null;
  nomFichierPv?: string | null;
  typeFichierPv?: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
  dateValidation?: string | null;
  dateRefus?: string | null;
  validateur?: string | null;
  messageValidation?: string | null;
}

export interface PvPage {
  content: PvResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PvStatistiques {
  total: number;
  enAttenteValidation: number;
  approuves: number;
  refuses: number;
}
