import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {PvPage, PvResponse, PvStatistiques} from '../models/pv';

@Injectable({providedIn: 'root'})
export class PvService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}pvs`;

  list(page = 0, size = 5, searchTerm = ''): Observable<AppResponse<PvPage>> {
    return this.http.get<AppResponse<PvPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<PvResponse>> {
    return this.http.get<AppResponse<PvResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  create(request: FormData): Observable<AppResponse<PvResponse>> {
    return this.http.post<AppResponse<PvResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<PvResponse>> {
    return this.http.put<AppResponse<PvResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  getStatistiques(): Observable<AppResponse<PvStatistiques>> {
    return this.http.get<AppResponse<PvStatistiques>>(`${this.baseUrl}/statistiques`);
  }

  getStatistiquesPubliques(): Observable<AppResponse<PvStatistiques>> {
    return this.http.get<AppResponse<PvStatistiques>>(`${this.baseUrl}/statistiques-publiques`);
  }
}
