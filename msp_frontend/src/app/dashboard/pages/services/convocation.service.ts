import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {ConvocationPage, ConvocationResponse} from '../models/convocation';

@Injectable({providedIn: 'root'})
export class ConvocationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}convocations`;

  list(page = 0, size = 8, searchTerm = ''): Observable<AppResponse<ConvocationPage>> {
    return this.http.get<AppResponse<ConvocationPage>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  get(slug: string): Observable<AppResponse<ConvocationResponse>> {
    return this.http.get<AppResponse<ConvocationResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  create(request: FormData): Observable<AppResponse<ConvocationResponse>> {
    return this.http.post<AppResponse<ConvocationResponse>>(this.baseUrl, request);
  }

  update(slug: string, request: FormData): Observable<AppResponse<ConvocationResponse>> {
    return this.http.put<AppResponse<ConvocationResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`, request);
  }

  delete(slug: string): Observable<AppResponse<boolean>> {
    return this.http.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }
}
