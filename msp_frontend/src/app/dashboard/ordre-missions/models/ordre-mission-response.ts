export interface OrdreMissionResponse {
  slug : string;
  libelle : string;
  objet : string;
  imputationBudgetaire : string;
  moyenDeplacement : string;
  itineraire : string;
  dateDepart : string;
  dateRetour : string;
  observation : string;
  ordreMission : string | null;
  nomFichierOrdreMission : string | null;
  typeFichierOrdreMission?: string | null;

  dateCreation : string;
  dateModification : string;
  creerPar : string;
  modifierPar : string;
}
