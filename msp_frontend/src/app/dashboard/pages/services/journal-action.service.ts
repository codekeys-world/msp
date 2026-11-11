import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {JournalActionPage, JournalActionResponse, JournalActionStatistiques} from '../models/journal-action';

@Injectable({providedIn: 'root'})
export class JournalActionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}journal-actions`;

  list(
    page = 0,
    size = 10,
    searchTerm = '',
    module = '',
    statut = '',
    dateDebut = '',
    dateFin = ''
  ): Observable<AppResponse<JournalActionPage>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (searchTerm) params = params.set('q', searchTerm);
    if (module) params = params.set('module', module);
    if (statut) params = params.set('statut', statut);
    if (dateDebut) params = params.set('dateDebut', dateDebut);
    if (dateFin) params = params.set('dateFin', dateFin);

    return this.http.get<AppResponse<JournalActionPage>>(this.baseUrl, {params});
  }

  get(slug: string): Observable<AppResponse<JournalActionResponse>> {
    return this.http.get<AppResponse<JournalActionResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  getStatistiques(): Observable<AppResponse<JournalActionStatistiques>> {
    return this.http.get<AppResponse<JournalActionStatistiques>>(`${this.baseUrl}/statistiques`);
  }
}
