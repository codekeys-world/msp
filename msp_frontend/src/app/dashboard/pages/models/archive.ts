export interface ArchiveResponse {
  id: number;
  slug: string;
  nom: string;
  chemin: string;
  type: string | null;
  taille: number | null;
  typeDocument: string;
  contenuBase64: string | null;
  dateCreation: string | null;
}

export interface ArchivePage {
  content: ArchiveResponse[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const ARCHIVE_CATEGORIES = [
  { value: '', label: 'Tous les documents', icon: 'bi-archive' },
  { value: 'ORDRE_MISSION', label: 'Ordres de mission', icon: 'bi-file-earmark-text' },
  { value: 'PV', label: 'Procès-verbaux', icon: 'bi-clipboard-check' },
  { value: 'PLAINTE', label: 'Plaintes', icon: 'bi-exclamation-triangle' },
  { value: 'CAS_SOCIAUX', label: 'Cas sociaux', icon: 'bi-heart-pulse' },
  { value: 'CONVOCATION', label: 'Convocations', icon: 'bi-file-earmark-text' },
];

export function formatTaille(taille: number | null): string {
  if (!taille) return '—';
  if (taille < 1024) return taille + ' o';
  if (taille < 1024 * 1024) return (taille / 1024).toFixed(1) + ' Ko';
  return (taille / (1024 * 1024)).toFixed(1) + ' Mo';
}

export function libelleTypeDocument(typeDocument: string): string {
  const map: Record<string, string> = {
    ORDRE_MISSION: 'Ordre de mission',
    PV: 'Procès-verbal',
    PLAINTE: 'Plainte',
    CAS_SOCIAUX: 'Cas social',
    CONVOCATION: 'Convocation',
  };
  return map[typeDocument] || typeDocument;
}
