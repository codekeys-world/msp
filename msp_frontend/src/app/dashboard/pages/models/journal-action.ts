export interface JournalActionResponse {
  slug: string;
  nom: string;
  prenom: string;
  nomComplet: string;
  role: string;
  matricule?: string | null;
  adresseIp: string;
  client: string;
  action: string;
  module?: string | null;
  details?: string | null;
  statut: string;
  dateAction: string;
  dateCreation?: string;
}

export interface JournalActionPage {
  content: JournalActionResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface JournalActionStatistiques {
  totalActions: number;
  actionsAujourdhui: number;
  totalUtilisateursDistincts: number;
  totalSucces: number;
  totalEchecs: number;
}
