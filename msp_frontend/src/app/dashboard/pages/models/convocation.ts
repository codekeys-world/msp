export interface ConvocationResponse {
  slug: string;
  description: string;
  dateConvocation: string;
  documentConvocation: string | null;
  nomFichierConvocation: string | null;
  typeFichierConvocation: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
  dateValidation?: string | null;
  dateRefus?: string | null;
  validateur?: string | null;
  messageValidation?: string | null;
}

export interface ConvocationPage {
  content: ConvocationResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
