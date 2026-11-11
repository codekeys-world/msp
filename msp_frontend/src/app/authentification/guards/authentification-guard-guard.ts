import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthentificationService } from '../services/authentification-service';

export const authentificationGuardGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthentificationService);
  const router = inject(Router);
  if (authService.authenticatedUser()) {
    return true;
  }
  return authService.refreshToken().pipe(
    map(() => true),
    catchError(() => {
      authService.clearSession();
      return of(router.createUrlTree(['/authentification']));
    })
  );
};
