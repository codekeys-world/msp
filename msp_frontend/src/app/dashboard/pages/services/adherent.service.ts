import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {AdherentStatistiques, PageResponse, PersonneFamille, UtilisateurDetail} from '../models/utilisateur-detail';

@Injectable({providedIn: 'root'})
export class AdherentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}adherents`;

  getAccessOptions(): Observable<AppResponse<{roles: {slug: string; nom: string}[]; authorisations: {id: number; nom: string}[]}>> {
    return this.http.get<AppResponse<{roles: {slug: string; nom: string}[]; authorisations: {id: number; nom: string}[]}>>(`${this.baseUrl}/acces/options`);
  }

  updateUserAccess(slug: string, roleSlug: string, authorisationIds: number[]): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.put<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/${encodeURIComponent(slug)}/acces`, {roleSlug, authorisationIds});
  }

  listAdherents(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<PageResponse<UtilisateurDetail>>> {
    return this.http.get<AppResponse<PageResponse<UtilisateurDetail>>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  getStatistiques(): Observable<AppResponse<AdherentStatistiques>> {
    return this.http.get<AppResponse<AdherentStatistiques>>(`${this.baseUrl}/statistiques`);
  }

  getStatistiquesPubliques(): Observable<AppResponse<AdherentStatistiques>> {
    return this.http.get<AppResponse<AdherentStatistiques>>(`${this.baseUrl}/statistiques-publiques`);
  }

  createConjoint(adherentSlug: string, request: FormData): Observable<AppResponse<PersonneFamille>> {
    return this.http.post<AppResponse<PersonneFamille>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/conjoints`, request
    );
  }

  updateConjoint(adherentSlug: string, conjointSlug: string, request: FormData): Observable<AppResponse<PersonneFamille>> {
    return this.http.put<AppResponse<PersonneFamille>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/conjoints/${encodeURIComponent(conjointSlug)}`,
      request
    );
  }

  deleteConjoint(adherentSlug: string, conjointSlug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/conjoints/${encodeURIComponent(conjointSlug)}`
    );
  }

  createDescendant(adherentSlug: string, request: FormData): Observable<AppResponse<PersonneFamille>> {
    return this.http.post<AppResponse<PersonneFamille>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/enfants`, request
    );
  }

  updateDescendant(adherentSlug: string, descendantSlug: string, request: FormData): Observable<AppResponse<PersonneFamille>> {
    return this.http.put<AppResponse<PersonneFamille>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/enfants/${encodeURIComponent(descendantSlug)}`,
      request
    );
  }

  deleteDescendant(adherentSlug: string, descendantSlug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(
      `${environment.apiUrl}adherent/${encodeURIComponent(adherentSlug)}/enfants/${encodeURIComponent(descendantSlug)}`
    );
  }

  getAdherent(slug: string): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.get<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  createAdherent(request: FormData): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.post<AppResponse<UtilisateurDetail>>(this.baseUrl, request);
  }

  updateAdherent(slug: string, request: FormData): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.put<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  deleteAdherent(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  exportAdherents(): Observable<AppResponse<UtilisateurDetail[]>> {
    return this.http.get<AppResponse<UtilisateurDetail[]>>(`${this.baseUrl}/exporter`);
  }
}
