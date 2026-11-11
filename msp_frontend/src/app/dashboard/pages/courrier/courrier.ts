import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {CourrierService} from '../services/courrier.service';
import {CourrierPage, CourrierResponse} from '../models/courrier';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-courrier',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './courrier.html',
  styleUrl: './courrier.scss',
})
export class Courrier implements OnInit {
  private readonly service = inject(CourrierService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchChanges = new Subject<string>();
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly size = 8;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<CourrierPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_COURRIER') === true;
  readonly canSearch = this.canList;
  readonly canAdd = this.auth.hasRole('AJOUTER_COURRIER') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_COURRIER') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_COURRIER') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get courriers(): CourrierResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  get pageNumbers(): number[] {
    return Array.from({length: this.totalPages}, (_, index) => index);
  }

  get isFirstPage(): boolean {
    return this.page() === 0;
  }

  get isLastPage(): boolean {
    return this.totalPages === 0 || this.page() >= this.totalPages - 1;
  }

  load(): void {
    if (!this.canList) {
      this.isLoading.set(false);
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les courriers.');
      return;
    }
    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.list(this.page(), this.size, this.activeSearchTerm)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => this.pageResponse.set(response),
        error: error => {
          console.error('Erreur lors du chargement des courriers.', error);
          this.errorMessage.set('Impossible de charger les courriers.');
        }
      });
  }

  onSearchInput(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.searchTerm.set(event.target.value);
    this.searchChanges.next(event.target.value.trim());
  }

  submitSearch(): void {
    this.applySearch(this.searchTerm().trim());
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.searchChanges.next('');
    this.applySearch('');
  }

  private applySearch(term: string): void {
    if (term === this.activeSearchTerm) return;
    this.activeSearchTerm = term;
    this.page.set(0);
    this.load();
  }

  goToPage(page: number): void {
    if (this.isLoading() || !Number.isInteger(page) || page < 0 || page >= this.totalPages || page === this.page()) return;
    this.page.set(page);
    this.load();
  }

  ajouter(): void {
    void this.router.navigate(['dashboard', 'nouveau-courrier']);
  }

  modifier(courrier: CourrierResponse): void {
    void this.router.navigate(['dashboard', 'modifier-courrier', courrier.slug], {state: {courrier}});
  }

  async supprimer(courrier: CourrierResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'le courrier', courrier.nom, 'Le document joint sera également supprimé.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(courrier.slug);
    this.service.delete(courrier.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Le courrier a été supprimé.');
        if (this.courriers.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression du courrier.', error);
        this.sweetAlert.showError('Impossible de supprimer le courrier.');
      }
    });
  }

  typeLabel(type: string): string {
    return type === 'ARRIVE' ? 'Courrier entrant' : 'Courrier sortant';
  }

  dateCourrier(courrier: CourrierResponse): string | null {
    return courrier.typeCourrier === 'ARRIVE' ? courrier.dateArrivee ?? null : courrier.dateDepart ?? null;
  }

  statut(courrier: CourrierResponse): string {
    if (courrier.dateValidation) return 'Traité';
    if (courrier.dateRefus) return 'Refusé';
    return 'En attente';
  }
}
