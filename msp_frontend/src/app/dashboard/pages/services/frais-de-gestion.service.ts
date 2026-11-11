import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {FraisDeGestionPage, FraisDeGestionResponse} from '../models/frais-de-gestion';

@Injectable({providedIn: 'root'})
export class FraisDeGestionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}frais-de-gestion`;

  lister(page = 0, size = 8, terme = ''): Observable<AppResponse<FraisDeGestionPage>> {
    return this.http.get<AppResponse<FraisDeGestionPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(terme)}`
    );
  }

  listerOptions(): Observable<AppResponse<FraisDeGestionResponse[]>> {
    return this.http.get<AppResponse<FraisDeGestionResponse[]>>(`${this.baseUrl}/options`);
  }

  rechercher(slug: string): Observable<AppResponse<FraisDeGestionResponse>> {
    return this.http.get<AppResponse<FraisDeGestionResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  enregistrer(request: FormData): Observable<AppResponse<FraisDeGestionResponse>> {
    return this.http.post<AppResponse<FraisDeGestionResponse>>(this.baseUrl, request);
  }

  modifier(slug: string, request: FormData): Observable<AppResponse<FraisDeGestionResponse>> {
    return this.http.put<AppResponse<FraisDeGestionResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  supprimer(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }
}
