import {HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {PageResponse, UtilisateurDetail} from '../models/utilisateur-detail';

@Injectable({providedIn: 'root'})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}admin`;

  listerUtilisateurs(page = 0, size = 20): Observable<AppResponse<PageResponse<UtilisateurDetail>>> {
    return this.http.get<AppResponse<PageResponse<UtilisateurDetail>>>(
      `${this.baseUrl}/utilisateurs?page=${page}&size=${size}`
    );
  }

  modifierStatutUtilisateur(slug: string, enabled: boolean): Observable<AppResponse<UtilisateurDetail>> {
    const params = new HttpParams().set('enabled', String(enabled));
    return this.http.put<AppResponse<UtilisateurDetail>>(
      `${this.baseUrl}/utilisateurs/${encodeURIComponent(slug)}/statut`,
      {},
      {params}
    );
  }
}
