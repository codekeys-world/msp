export type MaterielStatut = 'ACTIF' | 'EN_MAINTENANCE' | 'HORS_SERVICE';

export interface MaterielResponse {
  slug: string;
  nom: string;
  categorie: string;
  fournisseur: string;
  quantite: number;
  prixUnitaire: number;
  dateAcquisition: string | null;
  emplacement: string;
  numeroSerie?: string | null;
  observation?: string | null;
  statut: MaterielStatut;
}

export interface MaterielPage {
  content: MaterielResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
