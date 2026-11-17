import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';

export interface AssembleeRequest { nom: string; date: string; lieu: string; }
export interface Assemblee extends AssembleeRequest { slug: string; }
export interface AssembleePage {
  content: Assemblee[]; number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean;
}

@Injectable({providedIn: 'root'})
export class AssembleeService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}assemblees`;
  list(page = 0, q = '') {
    return this.http.get<AppResponse<AssembleePage>>(this.url, {params: {page, size: 8, q}});
  }
  create(request: AssembleeRequest) { return this.http.post<AppResponse<Assemblee>>(this.url, request); }
  update(slug: string, request: AssembleeRequest) {
    return this.http.put<AppResponse<Assemblee>>(`${this.url}/${encodeURIComponent(slug)}`, request);
  }
  delete(slug: string) { return this.http.delete<AppResponse<boolean>>(`${this.url}/${encodeURIComponent(slug)}`); }
  statistiques() { return this.http.get<AppResponse<{total: number}>>(`${this.url}/statistiques`); }
}
