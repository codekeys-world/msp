import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {ConvocationService} from '../services/convocation.service';
import {ConvocationPage, ConvocationResponse} from '../models/convocation';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-convocations',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './convocations.html',
  styleUrl: './convocations.scss',
})
export class Convocations implements OnInit {
  private readonly service = inject(ConvocationService);
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
  readonly pageResponse = signal<AppResponse<ConvocationPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_CONVOCATION') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_CONVOCATION') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_CONVOCATION') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_CONVOCATION') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get convocations(): ConvocationResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  get pageNumbers(): number[] {
    return Array.from({length: this.totalPages}, (_, index) => index);
  }

  get isFirstPage(): boolean { return this.page() === 0; }
  get isLastPage(): boolean { return this.totalPages === 0 || this.page() >= this.totalPages - 1; }

  load(): void {
    if (!this.canList) {
      this.isLoading.set(false);
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les convocations.');
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
          console.error('Erreur lors du chargement des convocations.', error);
          this.errorMessage.set('Impossible de charger les convocations.');
        }
      });
  }

  onSearchInput(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.searchTerm.set(event.target.value);
    this.searchChanges.next(event.target.value.trim());
  }

  submitSearch(): void { this.applySearch(this.searchTerm().trim()); }

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

  ajouter(): void { void this.router.navigate(['dashboard', 'nouvelle-convocation']); }

  modifier(convocation: ConvocationResponse): void {
    void this.router.navigate(['dashboard', 'modifier-convocation', convocation.slug], {state: {convocation}});
  }

  async supprimer(convocation: ConvocationResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'la convocation', this.reference(convocation),
      convocation.nomFichierConvocation ? 'Le document joint sera également supprimé.' : 'Cette action est irréversible.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(convocation.slug);
    this.service.delete(convocation.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'La convocation a été supprimée.');
        if (this.convocations.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression de la convocation.', error);
        this.sweetAlert.showError('Impossible de supprimer la convocation.');
      }
    });
  }

  reference(convocation: ConvocationResponse): string {
    return `CONV-${convocation.slug.slice(0, 8).toUpperCase()}`;
  }

  statut(convocation: ConvocationResponse): string {
    if (convocation.dateValidation) return 'Validée';
    if (convocation.dateRefus) return 'Refusée';
    return 'En attente';
  }
}
