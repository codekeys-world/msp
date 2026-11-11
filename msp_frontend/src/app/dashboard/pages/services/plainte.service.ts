import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {PlaintePage, PlainteResponse, PlainteStatistiques} from '../models/plainte';

@Injectable({providedIn: 'root'})
export class PlainteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}plaintes`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<PlaintePage>> {
    return this.http.get<AppResponse<PlaintePage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<PlainteResponse>> {
    return this.http.get<AppResponse<PlainteResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  create(request: FormData): Observable<AppResponse<PlainteResponse>> {
    return this.http.post<AppResponse<PlainteResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<PlainteResponse>> {
    return this.http.put<AppResponse<PlainteResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  getStatistiques(): Observable<AppResponse<PlainteStatistiques>> {
    return this.http.get<AppResponse<PlainteStatistiques>>(`${this.baseUrl}/statistiques`);
  }

  getStatistiquesPubliques(): Observable<AppResponse<PlainteStatistiques>> {
    return this.http.get<AppResponse<PlainteStatistiques>>(`${this.baseUrl}/statistiques-publiques`);
  }
}
