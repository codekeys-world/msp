export type TypeCasSocial =
  | 'MALADIE'
  | 'ACCIDENT_CIRCULATION'
  | 'RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE';
export type LienDefunt = 'ADHERENT' | 'PERE' | 'MERE' | 'EPOUX' | 'EPOUSE' | 'ENFANT';
export type TypeBeneficiaire = 'ADHERENT' | 'PARENTS' | 'PERSONNE_DESIGNEE';
export type StatutPrestation = 'PRESTATION' | 'REMBOURSEMENT_COTISATIONS' | 'NON_ELIGIBLE';

export interface CasSocialResponse {
  slug: string;
  typeCas: TypeCasSocial;
  dateCas: string;
  heuresHospitalisation: number | null;
  lienDefunt: LienDefunt | null;
  nomDefunt: string | null;
  prenomDefunt: string | null;
  typeBeneficiaire: TypeBeneficiaire;
  nomBeneficiaire: string | null;
  prenomBeneficiaire: string | null;
  telephoneBeneficiaire: string | null;
  montantPrestation: number | null;
  dureeCotisationMois: number | null;
  montantCotisationsVersees: number | null;
  montantRemboursement: number | null;
  statutPrestation: StatutPrestation | null;
  observation: string | null;
  adherentSlug: string;
  adherentNomComplet: string;
  adherentNumeroInscription: string;
  justificatif: string | null;
  nomFichierJustificatif: string | null;
  typeFichierJustificatif: string | null;
  dateCreation?: string | null;
  dateModification?: string | null;
  creerPar?: string | null;
  modifierPar?: string | null;
}

export interface CasSocialPage {
  content: CasSocialResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CasSocialAdherentOption {
  slug: string;
  nomComplet: string;
  numeroInscription: string;
  telephonePrincipal: string;
}
