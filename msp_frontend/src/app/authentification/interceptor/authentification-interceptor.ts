import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {catchError, switchMap, throwError} from 'rxjs';
import {AuthentificationService} from '../services/authentification-service';
import {TokenService} from '../services/token-service';

export const authentificationInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const authentificationService = inject(AuthentificationService);
  const router = inject(Router);
  const isAuthenticationRequest = req.url.includes('/authentification/connexion')
    || req.url.includes('/authentification/refresh');
  const token = tokenService.readToken();

  if (!token || isAuthenticationRequest) return next(req);

  const authenticatedRequest = req.clone({
    setHeaders: {Authorization: `Bearer ${token}`}
  });
  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      return authentificationService.refreshToken().pipe(
        switchMap(response => next(req.clone({
          setHeaders: {Authorization: `Bearer ${response.donnees.resultat.token}`}
        }))),
        catchError(refreshError => {
          authentificationService.clearSession();
          void router.navigate(['/authentification']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};
