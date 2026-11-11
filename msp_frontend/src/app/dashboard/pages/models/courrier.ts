export type TypeCourrier = 'ARRIVE' | 'DEPART';

export interface CourrierResponse {
  slug: string;
  nom: string;
  objet: string;
  typeCourrier: TypeCourrier;
  description: string;
  dateArrivee?: string | null;
  dateDepart?: string | null;
  documentCourrier?: string | null;
  nomFichierCourrier?: string | null;
  typeFichierCourrier?: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
  dateValidation?: string | null;
  dateRefus?: string | null;
}

export interface CourrierPage {
  content: CourrierResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CourrierStatistiques {
  total: number;
  courriersArrives: number;
  courriersDepart: number;
  enAttente: number;
  traites: number;
  refuses: number;
}
