import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {CasSocialAdherentOption, CasSocialPage, CasSocialResponse} from '../models/cas-social';

@Injectable({providedIn: 'root'})
export class CasSocialService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}cas-sociaux`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<CasSocialPage>> {
    return this.http.get<AppResponse<CasSocialPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<CasSocialResponse>> {
    return this.http.get<AppResponse<CasSocialResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  searchAdherents(term = ''): Observable<AppResponse<CasSocialAdherentOption[]>> {
    return this.http.get<AppResponse<CasSocialAdherentOption[]>>(
      `${this.baseUrl}/adherents?q=${encodeURIComponent(term)}`
    );
  }

  create(request: FormData): Observable<AppResponse<CasSocialResponse>> {
    return this.http.post<AppResponse<CasSocialResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<CasSocialResponse>> {
    return this.http.put<AppResponse<CasSocialResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }
}
