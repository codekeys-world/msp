export interface PlainteResponse {
  slug: string;
  datePv: string;
  observation: string | null;
  documentPlainte: string | null;
  nomFichierPlainte: string | null;
  typeFichierPlainte: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
  dateValidation?: string | null;
  dateRefus?: string | null;
  validateur?: string | null;
  messageValidation?: string | null;
}

export interface PlaintePage {
  content: PlainteResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PlainteStatistiques {
  total: number;
  enAttente: number;
  traitees: number;
  refusees: number;
}
