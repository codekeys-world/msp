import {ChangeDetectorRef, Component, DestroyRef, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize, Subscription} from 'rxjs';
import {DepenseService} from '../services/depense.service';
import {DepenseSituation, formatMontantDepense, libelleStatutDepense, couleurStatutDepense} from '../models/depense-situation';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {downloadCsv} from '../../../shared/utils/download-csv';

@Component({
  selector: 'app-situation-depense',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './situation-depense.html',
  styleUrls: ['../../dashboard.shared.scss', './situation-depense.scss'],
})
export class SituationDepenseComponent implements OnInit {
  private readonly service = inject(DepenseService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private requestSubscription?: Subscription;
  readonly auth = inject(AuthentificationService);

  annee = new Date().getFullYear();
  loading = true;
  errorMessage = '';
  situation: DepenseSituation[] = [];

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
        console.error('Erreur lors du chargement de la situation des décaissements.', error);
        this.errorMessage = 'Impossible de charger la situation des décaissements. Veuillez réessayer.';
        this.changeDetector.markForCheck();
      },
    });
  }

  changerAnnee(): void {
    this.charger();
  }

  annuler(): void {
    this.router.navigate(['dashboard/depenses']);
  }

  /** Total général de toutes les dépenses de l'année */
  get totalGeneral(): number {
    return this.situation.reduce((sum, s) => sum + (s.montantTotal || 0), 0);
  }

  /** Totaux par statut */
  get totauxParStatut(): { statut: string; libelle: string; couleur: string; total: number; nombre: number }[] {
    const map = new Map<string, { total: number; nombre: number }>();
    for (const s of this.situation) {
      const entry = map.get(s.statut) || { total: 0, nombre: 0 };
      entry.total += (s.montantTotal || 0);
      entry.nombre += (s.nombre || 0);
      map.set(s.statut, entry);
    }
    return Array.from(map.entries()).map(([statut, data]) => ({
      statut,
      libelle: libelleStatutDepense(statut),
      couleur: couleurStatutDepense(statut),
      total: data.total,
      nombre: data.nombre,
    }));
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
    return formatMontantDepense(m);
  }

  libelleStatut(statut: string): string {
    return libelleStatutDepense(statut);
  }

  couleurStatut(statut: string): string {
    return couleurStatutDepense(statut);
  }

  telecharger(): void {
    if (this.loading || this.situation.length === 0) return;
    const rows: (string | number)[][] = [
      ['Situation des décaissements', this.annee],
      [],
      ['Mois', 'Libellé', 'Statut', 'Nombre', 'Montant total (FCFA)'],
      ...this.situation.map(item => [
        item.libelleMois,
        item.libelle,
        this.libelleStatut(item.statut),
        item.nombre,
        item.montantTotal
      ]),
      [],
      ['Récapitulatif par mois', 'Montant total (FCFA)'],
      ...this.totauxParMois.map(item => [item.libelleMois, item.total]),
      [],
      ['Récapitulatif par statut', 'Nombre de dépenses', 'Montant total (FCFA)'],
      ...this.totauxParStatut.map(item => [item.libelle, item.nombre, item.total]),
      [],
      ['Total général', this.totalGeneral]
    ];
    downloadCsv(`situation-decaissements-${this.annee}.csv`, rows);
  }
}
