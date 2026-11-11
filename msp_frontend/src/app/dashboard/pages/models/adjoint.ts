export interface AdjointResponse {
  id: number;
  slug: string;
  delegantNomComplet: string;
  delegantMatricule: string;
  delegantTelephone: string;
  adjointNomComplet: string;
  adjointMatricule: string;
  adjointTelephone: string;
  roleDelegue: string;
  dateDebut: string;
  dateFin: string | null;
  estActif: boolean;
  motif: string | null;
  dateCreation: string | null;
}

export interface AdjointPage {
  content: AdjointResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AdjointRequest {
  adjointSlug: string;
  dateDebut: string;
  dateFin: string | null;
  motif: string | null;
}

export const formatRoleLabel = (role: string): string => {
  const labels: Record<string, string> = {
    'SECRETAIRE_ADMINISTRATIF': 'Secrétaire administratif',
    'PRESIDENT': 'Président',
    'TRESORIER': 'Trésorier',
    'SECRETAIRE_ORGANISATION': 'Secrétaire à l\'organisation',
    'SECRETAIRE_AFFAIRE_SOCIALE': 'Secrétaire aux affaires sociales'
  };
  return labels[role] || role.replace(/_/g, ' ').toLowerCase();
};

export const formatRoleSlug = (label: string): string => {
  const slugs: Record<string, string> = {
    'Secrétaire administratif': 'SECRETAIRE_ADMINISTRATIF',
    'Président': 'PRESIDENT',
    'Trésorier': 'TRESORIER',
    'Secrétaire à l\'organisation': 'SECRETAIRE_ORGANISATION',
    'Secrétaire aux affaires sociales': 'SECRETAIRE_AFFAIRE_SOCIALE'
  };
  return slugs[label] || label.toUpperCase().replace(/ /g, '_');
};
