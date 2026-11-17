import {describe, expect, it} from 'vitest';
import {routes} from '../../app.routes';
import {Routes} from '@angular/router';

describe('Métadonnées et navigation', () => {
  it('donne un titre et une description à chaque page accessible', () => {
    const inspect = (items: Routes) => {
      for (const route of items) {
        if (!route.redirectTo) {
          expect(route.title).toBeTruthy();
          expect(route.data?.['description']).toBeTruthy();
          expect(String(route.title)).not.toContain('undefined');
        }
        if (route.children) inspect(route.children);
      }
    };
    inspect(routes);
  });

  it('préserve une vraie page introuvable et empêche l’indexation des espaces privés', () => {
    const missing = routes.at(-1)!;
    expect(missing.path).toBe('**');
    expect(missing.redirectTo).toBeUndefined();
    expect(missing.loadComponent).toBeDefined();
    expect(missing.data?.['noindex']).toBe(true);
    expect(routes.find(route=>route.path==='dashboard')?.children?.every(route=>route.data?.['noindex'])).toBe(true);
    expect(routes.find(route=>route.path==='')?.data?.['noindex']).toBe(false);
  });
});
