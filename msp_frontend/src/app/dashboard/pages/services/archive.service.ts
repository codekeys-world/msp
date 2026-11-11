import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {ArchivePage, ArchiveResponse} from '../models/archive';

@Injectable({providedIn: 'root'})
export class ArchiveService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}archives`;

  list(page = 0, size = 8, searchTerm = '', type = ''): Observable<AppResponse<ArchivePage>> {
    const params: string[] = [`page=${page}`, `size=${size}`, `q=${encodeURIComponent(searchTerm)}`];
    if (type) params.push(`type=${type}`);
    return this.http.get<AppResponse<ArchivePage>>(`${this.baseUrl}?${params.join('&')}`);
  }

  get(id: number): Observable<AppResponse<ArchiveResponse>> {
    return this.http.get<AppResponse<ArchiveResponse>>(`${this.baseUrl}/${id}`);
  }
}
