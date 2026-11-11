export interface AppResponse<T> {
    message : string;
    developerMessage : string;
    erreur ?: string;
    erreurs ?: Array<string>;
    donnees : {resultat : T}
    status : string;
    statusCode : number;
}
