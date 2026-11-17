import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';

@Component({
  imports: [RouterLink],
  template: `<main class="container text-center py-5" style="max-width: 44rem">
    <p class="display-1 fw-bold text-primary" aria-hidden="true">404</p>
    <h1>Page introuvable</h1>
    <p class="lead">Cette page n’existe pas ou a été déplacée. Vérifiez l’adresse ou poursuivez depuis l’accueil.</p>
    <div class="d-flex flex-wrap justify-content-center gap-3 mt-4">
      <a class="btn btn-primary" routerLink="/">Retour à l’accueil</a>
      <a class="btn btn-outline-primary" routerLink="/dashboard">Mon espace adhérent</a>
    </div>
  </main>`,
})
export class NotFound {}
