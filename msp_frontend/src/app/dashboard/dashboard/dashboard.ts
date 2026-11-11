import { Component, DestroyRef, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { filter, tap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthentificationService } from '../../authentification/services/authentification-service';
import { Sidebar } from '../sidebar/sidebar';
import { Navbar } from "../navbar/navbar";

const PAGE_TITLES: Record<string, string> = {
  'home': 'Tableau de bord',
  'adherents': 'Adhérents',
  'adherent/create': 'Nouvel adhérent',
  'profil': 'Profil',
  'cartes-membres': 'Cartes membres',
  'proces-verbaux': 'Procès-verbaux',
  'nouveau-pv': 'Nouveau procès-verbal',
  'plaintes': 'Plaintes',
  'nouvelle-plainte': 'Nouvelle plainte',
  'cas-sociaux': 'Cas sociaux',
  'nouveau-cas-social': 'Nouveau cas social',
  'prestations': 'Prestations',
  'nouvelle-prestation': 'Nouvelle prestation',
  'convocations': 'Convocations',
  'nouvelle-convocation': 'Nouvelle convocation',
  'ordre-missions': 'Ordres de mission',
  'nouveau-ordre-missions': 'Nouvel ordre de mission',
  'courrier': 'Courrier',
  'nouveau-courrier': 'Nouveau courrier',
  'controle': 'Journal d\'actions',
  'journal-actions': 'Journal d\'actions',
  'archives': 'Archives',
  'encaissements': 'Encaissements',
  'nouvel-encaissement': 'Nouvel encaissement',
  'frais-de-gestion': 'Frais de Gestion',
  'depenses': 'Dépenses',
  'situation-encaissements': 'Situation des encaissements',
  'situation-depenses': 'Situation des décaissements',
  'adjoints': 'Désignation des adjoints',
  'utilisateurs': 'Gestion des utilisateurs',
};

@Component({
  selector: 'app-dashboard',
  imports: [Sidebar, RouterOutlet, Navbar],
  templateUrl: './dashboard.html',
  styleUrls: ['../dashboard.shared.scss','./dashboard.scss'],
  encapsulation : ViewEncapsulation.None
})
export class Dashboard implements OnInit {
  public authentificationService = inject(AuthentificationService);
  private readonly router = inject(Router);
  private readonly title = inject(Title);
  private readonly destroyRef = inject(DestroyRef);

  sidebarCollapsed = false;

  ngOnInit(): void {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      tap((event: NavigationEnd) => {
        const url = event.urlAfterRedirects.replace('/dashboard/', '');
        const path = url.split('/')[0];
        const pageTitle = PAGE_TITLES[path] || 'Tableau de bord';
        this.title.setTitle(`MSP - ${pageTitle}`);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
