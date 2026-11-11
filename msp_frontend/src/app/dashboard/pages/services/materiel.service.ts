import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppResponse} from '../../../app-response';
import {environment} from '../../../../environments/environment';
import {MaterielPage, MaterielResponse} from '../models/materiel';

@Injectable({providedIn: 'root'})
export class MaterielService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}materiels`;

  lister(page = 0, size = 8, terme = ''): Observable<AppResponse<MaterielPage>> {
    return this.http.get<AppResponse<MaterielPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(terme)}`
    );
  }

  rechercher(slug: string): Observable<AppResponse<MaterielResponse>> {
    return this.http.get<AppResponse<MaterielResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  enregistrer(request: Record<string, unknown>): Observable<AppResponse<MaterielResponse>> {
    return this.http.post<AppResponse<MaterielResponse>>(this.baseUrl, request);
  }

  modifier(slug: string, request: Record<string, unknown>): Observable<AppResponse<MaterielResponse>> {
    return this.http.put<AppResponse<MaterielResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  supprimer(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }
}
