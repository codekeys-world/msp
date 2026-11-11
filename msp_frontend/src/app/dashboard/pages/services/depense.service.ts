import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {DepensePage, DepenseResponse} from '../models/depense';
import {DepenseSituation} from '../models/depense-situation';

@Injectable({providedIn: 'root'})
export class DepenseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}depenses`;

  lister(page = 0, size = 8, terme = ''): Observable<AppResponse<DepensePage>> {
    return this.http.get<AppResponse<DepensePage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(terme)}`
    );
  }

  listerParFraisDeGestion(fraisDeGestionSlug: string, page = 0, size = 8, terme = ''): Observable<AppResponse<DepensePage>> {
    return this.http.get<AppResponse<DepensePage>>(
      `${this.baseUrl}/frais-de-gestion/${encodeURIComponent(fraisDeGestionSlug)}?page=${page}&size=${size}&q=${encodeURIComponent(terme)}`
    );
  }

  rechercher(slug: string, inclureJustificatif = true): Observable<AppResponse<DepenseResponse>> {
    return this.http.get<AppResponse<DepenseResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, {
      params: {inclureJustificatif}
    });
  }

  engager(request: FormData): Observable<AppResponse<DepenseResponse>> {
    return this.http.post<AppResponse<DepenseResponse>>(this.baseUrl, request);
  }

  modifier(slug: string, request: FormData): Observable<AppResponse<DepenseResponse>> {
    return this.http.put<AppResponse<DepenseResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  valider(slug: string, observationValidation?: string): Observable<AppResponse<DepenseResponse>> {
    return this.http.patch<AppResponse<DepenseResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}/valider`, { observationValidation });
  }

  rejeter(slug: string, observationValidation?: string): Observable<AppResponse<DepenseResponse>> {
    return this.http.patch<AppResponse<DepenseResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}/rejeter`, { observationValidation });
  }

  supprimer(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  situation(annee: number): Observable<AppResponse<DepenseSituation[]>> {
    return this.http.get<AppResponse<DepenseSituation[]>>(`${this.baseUrl}/situation/${annee}`);
  }
}
