import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {AdjointPage, AdjointRequest, AdjointResponse} from '../models/adjoint';

@Injectable({providedIn: 'root'})
export class AdjointService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}adjoints`;

  designer(request: FormData, roleDelegue: string, delegantSlug: string): Observable<AppResponse<AdjointResponse>> {
    return this.http.post<AppResponse<AdjointResponse>>(
      `${this.baseUrl}/${encodeURIComponent(roleDelegue)}/${encodeURIComponent(delegantSlug)}`,
      request
    );
  }

  listerParRole(roleDelegue: string, delegantSlug: string, page = 0, size = 10): Observable<AppResponse<AdjointPage>> {
    return this.http.get<AppResponse<AdjointPage>>(
      `${this.baseUrl}/${encodeURIComponent(roleDelegue)}/${encodeURIComponent(delegantSlug)}?page=${page}&size=${size}`
    );
  }

  listerActifs(page = 0, size = 10): Observable<AppResponse<AdjointPage>> {
    return this.http.get<AppResponse<AdjointPage>>(
      `${this.baseUrl}/actifs?page=${page}&size=${size}`
    );
  }

  getActif(roleDelegue: string, delegantSlug: string): Observable<AppResponse<AdjointResponse>> {
    return this.http.get<AppResponse<AdjointResponse>>(
      `${this.baseUrl}/${encodeURIComponent(roleDelegue)}/${encodeURIComponent(delegantSlug)}/actif`
    );
  }

  desactiver(slug: string): Observable<AppResponse<AdjointResponse>> {
    return this.http.put<AppResponse<AdjointResponse>>(
      `${this.baseUrl}/${encodeURIComponent(slug)}/desactiver`,
      {}
    );
  }

  reactivier(slug: string): Observable<AppResponse<AdjointResponse>> {
    return this.http.put<AppResponse<AdjointResponse>>(
      `${this.baseUrl}/${encodeURIComponent(slug)}/reactiver`,
      {}
    );
  }
}
