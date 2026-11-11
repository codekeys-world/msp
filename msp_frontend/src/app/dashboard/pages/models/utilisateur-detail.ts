export interface PersonneFamille {
  slug: string;
  nom: string;
  prenom: string;
  telephonePrincipal?: string | null;
  telephoneSecondaire?: string | null;
  dateNaissance?: string | null;
  lieuNaissance?: string | null;
  adresse?: string | null;
  genre?: string | null;
  nomActeMariage?: string | null;
  nomActeNaissance?: string | null;
  nomCertificatVieCollectif?: string | null;
}

export interface UtilisateurDetail {
  slug: string;
  nom: string;
  prenom: string;
  genre: string;
  email: string;
  matricule: string;
  telephonePrincipal: string;
  telephoneSecondaire?: string | null;
  dateNaissance: string;
  lieuNaissance: string;
  adresse?: string | null;
  numeroInscription: string;
  numeroAdhesion?: string | null;
  pere: string;
  mere: string;
  observations?: string | null;
  profileImage?: string | null;
  profileImageType?: string | null;
  role: string;
  authorisations: string[];
  enabled?: boolean;
  accountLocked?: boolean;
  conjoints: PersonneFamille[];
  descendants: PersonneFamille[];
}

export interface PageResponse<T> {
  content: T[];
  first: boolean;
  last: boolean;
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AdherentStatistiques {
  total: number;
  hommes: number;
  femmes: number;
}
