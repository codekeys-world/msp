import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {Observable} from 'rxjs';
import {AppResponse} from '../../../app-response';
import {OrdreMissionResponse} from '../models/ordre-mission-response';
import {PageResponseOrdreMissionResponse} from '../models/page-response-ordre-mission-response';
import {OrdreMissionStatistiques} from '../models/ordre-mission-statistiques';

@Injectable({
  providedIn: 'root',
})
export class OrdreMissionService {
  private httpClient = inject(HttpClient);

  private baseUrl = environment.apiUrl + 'ordre-missions';

  saveOrdreMission(ordreMissionRequest: FormData): Observable<AppResponse<OrdreMissionResponse>> {
    return this.httpClient.post<AppResponse<OrdreMissionResponse>>(this.baseUrl, ordreMissionRequest);
  }

  getOrdreMission(slug: string): Observable<AppResponse<OrdreMissionResponse>> {
    return this.httpClient.get<AppResponse<OrdreMissionResponse>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  updateOrdreMission(slug: string, ordreMissionRequest: FormData): Observable<AppResponse<OrdreMissionResponse>> {
    return this.httpClient.put<AppResponse<OrdreMissionResponse>>(
      `${this.baseUrl}/${encodeURIComponent(slug)}`,
      ordreMissionRequest
    );
  }

  deleteOrdreMission(slug: string): Observable<AppResponse<boolean>> {
    return this.httpClient.delete<AppResponse<boolean>>(`${this.baseUrl}/${encodeURIComponent(slug)}`);
  }

  listOrdreMission(
    page: number = 0,
    size: number = 5,
    searchTerm: string = ''
  ): Observable<AppResponse<PageResponseOrdreMissionResponse>> {
    return this.httpClient.get<AppResponse<PageResponseOrdreMissionResponse>>(
      `${this.baseUrl}?page=${page}&size=${size}&q=${encodeURIComponent(searchTerm)}`
    );
  }

  getStatistiques(): Observable<AppResponse<OrdreMissionStatistiques>> {
    return this.httpClient.get<AppResponse<OrdreMissionStatistiques>>(`${this.baseUrl}/statistiques`);
  }

  getStatistiquesPubliques(): Observable<AppResponse<OrdreMissionStatistiques>> {
    return this.httpClient.get<AppResponse<OrdreMissionStatistiques>>(`${this.baseUrl}/statistiques-publiques`);
  }
}
