import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {PvService} from '../services/pv.service';
import {PvPage, PvResponse} from '../models/pv';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-proces-verbaux',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './proces-verbaux.html',
  styleUrl: './proces-verbaux.scss',
})
export class ProcesVerbaux implements OnInit {
  private readonly service = inject(PvService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchChanges = new Subject<string>();
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly size = 5;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<PvPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  activeSearchTerm = '';
  readonly canAdd = this.auth.hasRole('AJOUTER_PV') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_PV') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_PV') === true;
  readonly canSearch = this.auth.hasRole('RECHERCHER_PV') === true;

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
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

  get pvs(): PvResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  load(): void {
    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.list(this.page(), this.size, this.activeSearchTerm)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => this.pageResponse.set(response),
        error: error => {
          console.error('Erreur lors du chargement des procès-verbaux.', error);
          this.errorMessage.set('Impossible de charger les procès-verbaux.');
        }
      });
  }

  onSearchInput(event: Event): void {
    if (event.target instanceof HTMLInputElement) {
      this.searchTerm.set(event.target.value);
      this.searchChanges.next(event.target.value.trim());
    }
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
    void this.router.navigate(['dashboard', 'nouveau-pv']);
  }

  modifier(pv: PvResponse): void {
    void this.router.navigate(['dashboard', 'modifier-pv', pv.slug], {state: {pv}});
  }

  async supprimer(pv: PvResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'le procès-verbal', pv.numero, 'Le document joint sera également supprimé.'
    );
    if (!confirmed) return;

    this.deletingSlug.set(pv.slug);
    this.service.delete(pv.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Le procès-verbal a été supprimé.');
        if (this.pvs.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression du procès-verbal.', error);
        this.sweetAlert.showError('Impossible de supprimer le procès-verbal.');
      }
    });
  }

  statut(pv: PvResponse): string {
    if (pv.dateValidation) return 'Validé';
    if (pv.dateRefus) return 'Refusé';
    return 'En attente';
  }
}
