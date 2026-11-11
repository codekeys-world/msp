import {ChangeDetectorRef, Component, DestroyRef, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize, Subscription} from 'rxjs';
import {EncaissementService} from '../services/encaissement.service';
import {EncaissementSituation, formatMontantEncaissement} from '../models/encaissement-situation';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {downloadCsv} from '../../../shared/utils/download-csv';

@Component({
  selector: 'app-situation-encaissement',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './situation-encaissement.html',
  styleUrls: ['../../dashboard.shared.scss', './situation-encaissement.scss'],
})
export class SituationEncaissementComponent implements OnInit {
  private readonly service = inject(EncaissementService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private requestSubscription?: Subscription;
  readonly auth = inject(AuthentificationService);

  annee = new Date().getFullYear();
  loading = true;
  errorMessage = '';
  situation: EncaissementSituation[] = [];

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.requestSubscription?.unsubscribe();
    this.loading = true;
    this.errorMessage = '';
    this.changeDetector.markForCheck();
    this.requestSubscription = this.service.situation(this.annee).pipe(
      finalize(() => {
        this.loading = false;
        this.changeDetector.markForCheck();
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (res) => {
        this.situation = res.donnees.resultat;
        this.changeDetector.markForCheck();
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement de la situation des encaissements.', error);
        this.errorMessage = 'Impossible de charger la situation des encaissements. Veuillez réessayer.';
        this.changeDetector.markForCheck();
      },
    });
  }

  changerAnnee(): void {
    this.charger();
  }

  annuler(): void {
    this.router.navigate(['dashboard/encaissements']);
  }

  /** Total général de tous les encaissements de l'année */
  get totalGeneral(): number {
    return this.situation.reduce((sum, s) => sum + (s.montantTotal || 0), 0);
  }

  /** Regrouper par mois pour les totaux de mois */
  get totauxParMois(): { mois: number; libelleMois: string; total: number }[] {
    const map = new Map<number, number>();
    const moisLabels = new Map<number, string>();
    for (const s of this.situation) {
      map.set(s.mois, (map.get(s.mois) || 0) + (s.montantTotal || 0));
      moisLabels.set(s.mois, s.libelleMois);
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => a - b)
      .map(([mois, total]) => ({ mois, libelleMois: moisLabels.get(mois) || '', total }));
  }

  formatMontant(m: number | null | undefined): string {
    return formatMontantEncaissement(m);
  }

  telecharger(): void {
    if (this.loading || this.situation.length === 0) return;
    const rows: (string | number)[][] = [
      ['Situation des encaissements', this.annee],
      [],
      ['Mois', 'Type', 'Nombre', 'Montant total (FCFA)'],
      ...this.situation.map(item => [
        item.libelleMois, item.libelle, item.nombre, item.montantTotal
      ]),
      [],
      ['Récapitulatif par mois', 'Montant total (FCFA)'],
      ...this.totauxParMois.map(item => [item.libelleMois, item.total]),
      [],
      ['Total général', this.totalGeneral]
    ];
    downloadCsv(`situation-encaissements-${this.annee}.csv`, rows);
  }
}
