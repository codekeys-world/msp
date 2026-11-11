import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authentificationInterceptor } from './authentification/interceptor/authentification-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    //provideAppInitializer(()),
    //provideAppInitializer(() => inject(AuthentificationService).getAuthenticationToken()),
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withInMemoryScrolling({
      anchorScrolling: 'enabled', // <-- Active les ancres (#)
      scrollPositionRestoration: 'enabled' // Ouvre la page en haut lors d'un changement de route
    })),
    provideHttpClient(withInterceptors([authentificationInterceptor]))
  ]
};
