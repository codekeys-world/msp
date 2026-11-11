import {DatePipe, DecimalPipe, NgClass} from '@angular/common';
import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {MaterielPage, MaterielResponse} from '../models/materiel';
import {MaterielService} from '../services/materiel.service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

@Component({
  selector: 'app-materiels',
  imports: [DatePipe, DecimalPipe, NgClass],
  templateUrl: './materiels.html',
  styleUrl: './materiels.scss',
})
export class Materiels implements OnInit {
  private readonly router = inject(Router);
  private readonly service = inject(MaterielService);
  private readonly auth = inject(AuthentificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly searchChanges = new Subject<string>();
  private readonly activeSearchTerm = signal('');
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly searchTerm = signal('');
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly deletingSlug = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<MaterielPage> | null>(null);

  readonly canList = this.auth.hasRole('LISTER_MATERIEL') === true || this.auth.canManageMateriel();
  readonly canAdd = this.auth.hasRole('AJOUTER_MATERIEL') === true || this.auth.canManageMateriel();
  readonly canEdit = this.auth.hasRole('MODIFIER_MATERIEL') === true || this.auth.canManageMateriel();
  readonly canDelete = this.auth.hasRole('SUPPRIMER_MATERIEL') === true || this.auth.canManageMateriel();

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get materiels(): MaterielResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  get premierePage(): boolean {
    return this.pageResponse()?.donnees.resultat.first ?? true;
  }

  get dernierePage(): boolean {
    return this.pageResponse()?.donnees.resultat.last ?? true;
  }

  load(): void {
    if (!this.canList) {
      this.pageResponse.set(null);
      this.isLoading.set(false);
      return;
    }

    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.lister(this.page(), 8, this.activeSearchTerm())
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.pageResponse.set(response),
      error: error => {
        console.error('Erreur lors du chargement du matériel.', error);
        this.errorMessage.set('Impossible de charger l’inventaire. Veuillez réessayer.');
      }
    });
  }

  onSearchInput(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.searchTerm.set(event.target.value);
    this.searchChanges.next(event.target.value.trim());
  }

  applySearch(term: string): void {
    if (this.activeSearchTerm() === term) return;
    this.searchTerm.set(term);
    this.activeSearchTerm.set(term);
    this.page.set(0);
    this.load();
  }

  effacerRecherche(): void {
    this.searchTerm.set('');
    this.searchChanges.next('');
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || this.page() === page) return;
    this.page.set(page);
    this.load();
  }

  statutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'text-bg-success';
      case 'EN_MAINTENANCE': return 'text-bg-warning';
      case 'HORS_SERVICE': return 'text-bg-secondary';
      default: return 'text-bg-light';
    }
  }

  ouvrirCreation(): void {
    void this.router.navigate(['dashboard', 'nouveau-materiel']);
  }

  modifier(materiel: MaterielResponse): void {
    void this.router.navigate(['dashboard', 'modifier-materiel', materiel.slug]);
  }

  async supprimer(materiel: MaterielResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion('le matériel', materiel.nom);
    if (!confirmed) return;

    this.deletingSlug.set(materiel.slug);
    this.service.supprimer(materiel.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Le matériel a été supprimé.');
        if (this.materiels.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression du matériel.', error);
        this.sweetAlert.showError(error?.error?.message || 'Impossible de supprimer le matériel.');
      }
    });
  }

  statutLabel(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'Actif';
      case 'EN_MAINTENANCE': return 'En maintenance';
      case 'HORS_SERVICE': return 'Hors service';
      default: return statut;
    }
  }
}
