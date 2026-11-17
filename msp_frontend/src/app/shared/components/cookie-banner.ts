import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {CookieConsentService} from '../services/cookie-consent.service';

@Component({
  selector: 'app-cookie-banner', imports: [RouterLink],
  template: `
    @if (consent.open()) {
      <section class="cookie-banner" aria-labelledby="cookie-title" aria-describedby="cookie-description">
        <div>
          <h2 id="cookie-title">Vos préférences de confidentialité</h2>
          <p id="cookie-description">La MSP utilise le stockage du navigateur pour votre connexion et vos préférences.
            Aucun outil publicitaire ou de mesure d’audience n’est actuellement installé.
            Les cookies facultatifs restent désactivés sans votre accord.</p>
          <a routerLink="/confidentialite">Consulter les informations sur les cookies</a>
        </div>
        <div class="cookie-actions">
          <button type="button" class="btn btn-outline-primary" (click)="consent.save(false)">Continuer sans accepter</button>
          <button type="button" class="btn btn-primary" (click)="consent.save(true)">Tout accepter</button>
        </div>
      </section>
    } @else {
      <button type="button" class="cookie-settings" (click)="consent.open.set(true)" aria-label="Modifier mes préférences de cookies">Cookies</button>
    }
  `,
  styles: `
    .cookie-banner { position: fixed; inset: auto 1rem 1rem; z-index: 1300; max-height: 70dvh; overflow-y: auto;
      display: flex; align-items: center; gap: 1.5rem; padding: 1.25rem; background: #fff; color: #19352a;
      border: 1px solid #697b70; border-radius: 1rem; box-shadow: 0 4px 30px #0003; }
    h2 { font-size: 1.15rem; } p { margin-bottom: .5rem; max-width: 75ch; }
    .cookie-actions { display: flex; flex-wrap: wrap; gap: .75rem; flex-shrink: 0; }
    .cookie-settings { position: fixed; bottom: .5rem; right: .5rem; z-index: 1040; padding: .5rem .75rem;
      border: 1px solid #52675b; border-radius: .5rem; background: #fff; color: #19352a; font-size: .8rem; }
    @media (max-width: 900px) { .cookie-banner { flex-direction: column; align-items: stretch; gap: 1rem; }
      .cookie-actions > button { flex: 1; } }
    @media print { :host { display: none; } }
  `,
})
export class CookieBanner {
  readonly consent = inject(CookieConsentService);
}
