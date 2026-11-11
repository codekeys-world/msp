import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Token } from '../models/token';

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly jwtHelper = new JwtHelperService();

  saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  readToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  saveRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  readRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  decodeToken(): Token | null {
    const token = this.readToken();
    if (!token)
      return null;
    return this.jwtHelper.decodeToken(token);
  }

  isExpired(): boolean {
    const token = this.readToken();
    if (!token)
      return true;
    return this.jwtHelper.isTokenExpired(token);
  }
}
