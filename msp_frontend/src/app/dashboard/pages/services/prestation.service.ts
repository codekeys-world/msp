import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {PrestationAdherentOption, PrestationPage, PrestationResponse} from '../models/prestation';

@Injectable({providedIn: 'root'})
export class PrestationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}prestations`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<PrestationPage>> {
    return this.http.get<AppResponse<PrestationPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<PrestationResponse>> {
    return this.http.get<AppResponse<PrestationResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  searchAdherents(term = ''): Observable<AppResponse<PrestationAdherentOption[]>> {
    return this.http.get<AppResponse<PrestationAdherentOption[]>>(
      `${this.baseUrl}/adherents?q=${encodeURIComponent(term)}`
    );
  }

  create(request: FormData): Observable<AppResponse<PrestationResponse>> {
    return this.http.post<AppResponse<PrestationResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<PrestationResponse>> {
    return this.http.put<AppResponse<PrestationResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }
}
