import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {EncaissementPage, EncaissementResponse} from '../models/encaissement';
import {EncaissementSituation} from '../models/encaissement-situation';

@Injectable({providedIn: 'root'})
export class EncaissementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}encaissements`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<EncaissementPage>> {
    return this.http.get<AppResponse<EncaissementPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string, inclureJustificatif = true): Observable<AppResponse<EncaissementResponse>> {
    return this.http.get<AppResponse<EncaissementResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, {
      params: {inclureJustificatif}
    });
  }

  create(request: FormData): Observable<AppResponse<EncaissementResponse>> {
    return this.http.post<AppResponse<EncaissementResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<EncaissementResponse>> {
    return this.http.put<AppResponse<EncaissementResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  situation(annee: number): Observable<AppResponse<EncaissementSituation[]>> {
    return this.http.get<AppResponse<EncaissementSituation[]>>(`${this.baseUrl}/situation/${annee}`);
  }
}
