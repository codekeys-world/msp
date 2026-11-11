import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {PlainteService} from '../services/plainte.service';
import {PlaintePage, PlainteResponse} from '../models/plainte';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-plaintes',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './plaintes.html',
  styleUrl: './plaintes.scss',
})
export class Plaintes implements OnInit {
  private readonly service = inject(PlainteService);
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
  readonly pageResponse = signal<AppResponse<PlaintePage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_PLAINTE') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_PLAINTE') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_PLAINTE') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_PLAINTE') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get plaintes(): PlainteResponse[] {
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
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les plaintes.');
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
          console.error('Erreur lors du chargement des plaintes.', error);
          this.errorMessage.set('Impossible de charger les plaintes.');
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
    void this.router.navigate(['dashboard', 'nouvelle-plainte']);
  }

  modifier(plainte: PlainteResponse): void {
    void this.router.navigate(['dashboard', 'modifier-plainte', plainte.slug], {state: {plainte}});
  }

  async supprimer(plainte: PlainteResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'la plainte', this.reference(plainte), plainte.nomFichierPlainte
        ? 'Le document joint sera également supprimé.' : 'Cette action est irréversible.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(plainte.slug);
    this.service.delete(plainte.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'La plainte a été supprimée.');
        if (this.plaintes.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression de la plainte.', error);
        this.sweetAlert.showError('Impossible de supprimer la plainte.');
      }
    });
  }

  reference(plainte: PlainteResponse): string {
    return `PL-${plainte.slug.slice(0, 8).toUpperCase()}`;
  }

  statut(plainte: PlainteResponse): string {
    if (plainte.dateValidation) return 'Traité';
    if (plainte.dateRefus) return 'Refusé';
    return 'En attente';
  }
}
