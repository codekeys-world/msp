import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {AppResponse} from '../../../app-response';
import {UtilisateurDetail} from '../models/utilisateur-detail';

export interface ModifierMotDePasseRequest {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
  confirmationPassword: string;
}

@Injectable({providedIn: 'root'})
export class ProfilService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}adherents`;

  getProfil(): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.get<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/me`);
  }

  updateProfil(request: Omit<UtilisateurDetail, 'slug' | 'role' | 'authorisations' | 'conjoints' | 'descendants' | 'profileImage' | 'profileImageType' | 'nomCertificatVieCollectif'>): Observable<AppResponse<UtilisateurDetail>> {
    return this.http.put<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/me`, request);
  }

  updatePhoto(photo: File): Observable<AppResponse<UtilisateurDetail>> {
    const formData = new FormData();
    formData.append('profileImage', photo);
    return this.http.post<AppResponse<UtilisateurDetail>>(`${this.baseUrl}/me/photo`, formData);
  }

  updatePassword(request: ModifierMotDePasseRequest): Observable<AppResponse<boolean>> {
    return this.http.post<AppResponse<boolean>>(`${this.baseUrl}/changer-mot-de-passe`, request);
  }
}
