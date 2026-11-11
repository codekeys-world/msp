export interface Token {
    nom: string;
    email: string;
    sub: string;
    iat: number;
    exp: number;
    authorities : Array<string>;
}
