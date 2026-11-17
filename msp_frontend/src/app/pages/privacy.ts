import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {CookieConsentService} from '../shared/services/cookie-consent.service';

@Component({
  imports: [RouterLink],
  template: `<main class="container py-5" style="max-width: 52rem">
    <a routerLink="/">Retour à l’accueil</a>
    <h1 class="mt-4">Confidentialité et cookies</h1>
    <p>La Mutuelle des Surveillants de Prison du Mali utilise le stockage local du navigateur pour assurer la connexion à l’espace adhérent et mémoriser certaines préférences.</p>
    <h2 class="h4">Stockage nécessaire</h2>
    <p>Les jetons de connexion permettent d’accéder à votre compte. Ils sont supprimés à la déconnexion. L’état du menu peut également être mémorisé sur cet appareil.</p>
    <h2 class="h4">Votre choix</h2>
    <p>Votre choix concernant les cookies facultatifs est conservé sur cet appareil pendant six mois. Vous pouvez le modifier à tout moment avec le bouton « Cookies ».</p>
    <p>Aucun outil publicitaire ou de mesure d’audience n’est installé actuellement. Accepter les cookies facultatifs n’active donc aucun suivi supplémentaire.</p>
    <button type="button" class="btn btn-primary" (click)="consent.open.set(true)">Modifier mes préférences</button>
  </main>`,
})
export class Privacy { readonly consent = inject(CookieConsentService); }
