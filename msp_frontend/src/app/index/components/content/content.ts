import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {AdherentStatistiques} from '../../../dashboard/pages/models/utilisateur-detail';
import {AdherentService} from '../../../dashboard/pages/services/adherent.service';

@Component({
  selector: 'app-content',
  imports: [CommonModule],
  templateUrl: './content.html',
  styleUrl: './content.scss',
})
export class Content implements OnInit {
  private readonly router = inject(Router);
  private readonly adherentService = inject(AdherentService);
  readonly statistiques = signal<AdherentStatistiques | null>(null);
  readonly statistiquesLoading = signal(true);
  readonly statistiquesError = signal(false);

  ngOnInit(): void {
    this.adherentService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.statistiques.set(response.donnees.resultat);
        this.statistiquesLoading.set(false);
      },
      error: error => {
        console.error('Erreur lors du chargement des statistiques publiques.', error);
        this.statistiquesError.set(true);
        this.statistiquesLoading.set(false);
      },
    });
  }

  protected goToLogin() {
    void this.router.navigate(['authentification']);
  }
}
