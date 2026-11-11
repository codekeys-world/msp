import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {CourrierPage, CourrierResponse, CourrierStatistiques} from '../models/courrier';

@Injectable({providedIn: 'root'})
export class CourrierService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}courriers`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<CourrierPage>> {
    return this.http.get<AppResponse<CourrierPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<CourrierResponse>> {
    return this.http.get<AppResponse<CourrierResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  create(request: FormData): Observable<AppResponse<CourrierResponse>> {
    return this.http.post<AppResponse<CourrierResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<CourrierResponse>> {
    return this.http.put<AppResponse<CourrierResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  getStatistiques(): Observable<AppResponse<CourrierStatistiques>> {
    return this.http.get<AppResponse<CourrierStatistiques>>(`${this.baseUrl}/statistiques`);
  }

  getStatistiquesPubliques(): Observable<AppResponse<CourrierStatistiques>> {
    return this.http.get<AppResponse<CourrierStatistiques>>(`${this.baseUrl}/statistiques-publiques`);
  }
}
