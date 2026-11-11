import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { finalize, Observable, shareReplay, tap, throwError } from 'rxjs';
import { AppResponse } from '../../app-response';
import { environment } from '../../../environments/environment';
import { AuthentificationRequest } from '../models/authentification-request';
import { AuthentificationResponse } from '../models/authentification-response';
import { TokenService } from './token-service';
import { Token } from '../models/token';

@Injectable({
  providedIn: 'root',
})
export class AuthentificationService {
  private httpClient = inject(HttpClient);
  private tokenService = inject(TokenService);

  private baseUrl = environment.apiUrl + 'authentification';
  private refreshRequest$: Observable<AppResponse<AuthentificationResponse>> | null = null;

  authenticatedUser = signal<Token | null>(this.getCurrentUser());

  login(authentificationRequest : AuthentificationRequest) : Observable<AppResponse<AuthentificationResponse>>{
    return this.httpClient.post<AppResponse<AuthentificationResponse>>(`${this.baseUrl}/connexion`, authentificationRequest)
      .pipe(
        tap((response : AppResponse<AuthentificationResponse>) =>{
          //Sauvegarde tu token
          this.tokenService.saveToken(response.donnees.resultat.token);
          this.tokenService.saveRefreshToken(response.donnees.resultat.refreshToken);
          this.authenticatedUser.set(this.tokenService.decodeToken());
        })
      );
  }

  demanderReinitialisation(email: string): Observable<AppResponse<boolean>> {
    return this.httpClient.post<AppResponse<boolean>>(`${environment.apiUrl}adherents/mot-de-passe-oublier`, {email});
  }

  reinitialiserMotDePasse(token: string, password: string, confirmationPassword: string): Observable<AppResponse<boolean>> {
    return this.httpClient.post<AppResponse<boolean>>(`${environment.apiUrl}adherents/reinitialiser-mot-de-passe`,
      {token, password, confirmationPassword});
  }

  logout() : Observable<AppResponse<boolean>>{
    return this.httpClient.post<AppResponse<boolean>>(`${this.baseUrl}/deconnexion`, {})
      .pipe(
        tap((response : AppResponse<boolean>) =>{
          this.tokenService.removeToken();
          this.authenticatedUser.set(null);
          this.refreshRequest$ = null;
        })
      );
  }

  refreshToken(): Observable<AppResponse<AuthentificationResponse>> {
    if (!this.refreshRequest$) {
      const refreshToken = this.tokenService.readRefreshToken();
      if (!refreshToken) return throwError(() => new Error('Aucun refresh token disponible.'));

      this.refreshRequest$ = this.httpClient.post<AppResponse<AuthentificationResponse>>(
        `${this.baseUrl}/refresh`, {refreshToken}
      ).pipe(
        tap(response => {
          this.tokenService.saveToken(response.donnees.resultat.token);
          this.tokenService.saveRefreshToken(response.donnees.resultat.refreshToken);
          this.authenticatedUser.set(this.tokenService.decodeToken());
        }),
        finalize(() => this.refreshRequest$ = null),
        shareReplay({bufferSize: 1, refCount: false})
      );
    }
    return this.refreshRequest$;
  }

  clearSession(): void {
    this.tokenService.removeToken();
    this.authenticatedUser.set(null);
    this.refreshRequest$ = null;
  }

  getCurrentUser() : Token | null{
    if(this.tokenService.decodeToken() && !this.tokenService.isExpired())
      return this.tokenService.decodeToken();
    return null;
  }

  updateCurrentUserProfile(nom: string, email: string): void {
    const current = this.authenticatedUser();
    if (!current) return;
    this.authenticatedUser.set({...current, nom, email});
  }

  getUsername() : string | undefined{
    return this.getCurrentUser()?.sub;
  }

  getEmail() : string | undefined{
    return this.getCurrentUser()?.email;
  }

  getFullName() : string | undefined{
    return this.getCurrentUser()?.nom;
  }

  getRoles() : string[] | undefined{
    return this.authenticatedUser()?.authorities;
  }

  hasRole(role : string) : boolean | undefined{
    return this.getRoles()?.includes(role);
  }

  canManageAdherents(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_SECRETAIRE_ADMINISTRATIF', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN']
      .some(role => roles.includes(role));
  }

  canManageUserAccess(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN'].some(role => roles.includes(role));
  }

  canListAdherents(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_SECRETAIRE_ADMINISTRATIF', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'ROLE_PRESIDENT', 'ROLE_COMMISSION_CONTROLE']
      .some(role => roles.includes(role));
  }

  canConfectionCard(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_SECRETAIRE_ADMINISTRATIF', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'CONFECTION_CARTE']
      .some(role => roles.includes(role));
  }

  canExportAdherents(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'EXPORTER_ADHERENT']
      .some(role => roles.includes(role));
  }

  canViewControle(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_COMMISSION_CONTROLE', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'LISTER_JOURNAL_ACTION']
      .some(role => roles.includes(role));
  }

  getRole(role : string) : string | undefined {
    const matchingRole = this.getRoles()?.find(authority => authority === role);
    return matchingRole?.replace(/^ROLE_/, '');
  }

  getRoleName(): string {
    const role = this.getRoles()?.find(authority => authority.startsWith('ROLE_'));
    return role?.replace(/^ROLE_/, '').replace(/_/g, ' ') ?? '';
  }

  getInitiale(fullName : string | undefined) : string{
    if(fullName)
      return fullName.trim()
                .split(' ')
                .map(name => name.charAt(0).toUpperCase())
                .join('');
    return "";
  }

  canEngagerDepense(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'ENGAGER_DEPENSE']
      .some(role => roles.includes(role));
  }

  canSupprimerDepense(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'SUPPRIMER_DEPENSE']
      .some(role => roles.includes(role));
  }

  canValiderDecaissement(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_PRESIDENT', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'VALIDER_DECAISSEMENT']
      .some(role => roles.includes(role));
  }

  canListerDepense(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_TRESORIER', 'ROLE_PRESIDENT', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'LISTER_DEPENSE']
      .some(role => roles.includes(role));
  }

  canViewAdherentDetail(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_SECRETAIRE_ADMINISTRATIF', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'ROLE_PRESIDENT', 'ROLE_COMMISSION_CONTROLE', 'ROLE_TRESORIER', 'ROLE_SECRETAIRE_ORGANISATION', 'ROLE_SECRETAIRE_AFFAIRE_SOCIALE']
      .some(role => roles.includes(role));
  }

  canManageMateriel(): boolean {
    const roles = this.getRoles() ?? [];
    return ['ROLE_TRESORIER', 'ROLE_ADMINISTRATEUR', 'ROLE_SYSADMIN', 'GERER_MATERIEL']
      .some(role => roles.includes(role));
  }
}
