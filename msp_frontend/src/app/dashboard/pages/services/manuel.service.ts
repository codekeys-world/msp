import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
export interface Manuel { role: string; titre: string; fichier: string; }
@Injectable({providedIn: 'root'})
export class ManuelService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl + 'manuels';
  list() { return this.http.get<AppResponse<Manuel[]>>(this.baseUrl + '/liste'); }
  download(role: string) { return this.http.get(this.baseUrl + '/telecharger/' + encodeURIComponent(role), {responseType: 'blob'}); }
}
