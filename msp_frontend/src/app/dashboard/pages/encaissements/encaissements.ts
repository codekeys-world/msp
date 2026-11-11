import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {EncaissementService} from '../services/encaissement.service';
import {EncaissementPage, EncaissementResponse, libelleTypeEncaissement, formatMontant} from '../models/encaissement';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-encaissements',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './encaissements.html',
  styleUrl: './encaissements.scss',
})
export class Encaissements implements OnInit {
  private readonly service = inject(EncaissementService);
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
  readonly pageResponse = signal<AppResponse<EncaissementPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly justificatifs = signal<Record<string, string | null>>({});
  readonly loadingJustificatifSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_ENCAISSEMENT') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_ENCAISSEMENT') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_ENCAISSEMENT') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_ENCAISSEMENT') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get encaissements(): EncaissementResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  get pageNumbers(): number[] {
    return Array.from({length: this.totalPages}, (_, index) => index);
  }

  get isLastPage(): boolean {
    return this.totalPages === 0 || this.page() >= this.totalPages - 1;
  }

  load(): void {
    if (!this.canList) {
      this.isLoading.set(false);
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les encaissements.');
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
          console.error('Erreur lors du chargement des encaissements.', error);
          this.errorMessage.set('Impossible de charger les encaissements.');
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
    void this.router.navigate(['dashboard', 'nouvel-encaissement']);
  }

  modifier(encaissement: EncaissementResponse): void {
    void this.router.navigate(['dashboard', 'modifier-encaissement', encaissement.slug]);
  }

  async supprimer(encaissement: EncaissementResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'l\'encaissement', this.reference(encaissement),
      encaissement.nomFichierJustificatif ? 'Le justificatif joint sera également supprimé.' : 'Cette action est irréversible.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(encaissement.slug);
    this.service.delete(encaissement.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'L\'encaissement a été supprimé.');
        if (this.encaissements.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression de l\'encaissement.', error);
        this.sweetAlert.showError('Impossible de supprimer l\'encaissement.');
      }
    });
  }

  reference(encaissement: EncaissementResponse): string {
    return `ENC-${encaissement.slug.slice(0, 8).toUpperCase()}`;
  }

  typeLabel(type: EncaissementResponse['typeEncaissement']): string {
    return libelleTypeEncaissement(type);
  }

  montantLabel(encaissement: EncaissementResponse): string {
    return formatMontant(encaissement.montant);
  }

  chargerJustificatif(encaissement: EncaissementResponse): void {
    if (this.loadingJustificatifSlug() || !encaissement.nomFichierJustificatif) return;
    this.loadingJustificatifSlug.set(encaissement.slug);
    this.service.get(encaissement.slug).pipe(finalize(() => this.loadingJustificatifSlug.set(null))).subscribe({
      next: response => this.justificatifs.update(items => ({
        ...items,
        [encaissement.slug]: response.donnees.resultat.justificatif
      })),
      error: error => {
        console.error('Erreur lors du chargement du justificatif de l’encaissement.', error);
        this.sweetAlert.showError('Impossible de charger le justificatif. Veuillez réessayer.');
      }
    });
  }

  periodeLabel(encaissement: EncaissementResponse): string {
    if (encaissement.dateDebutPeriode && encaissement.dateFinPeriode) {
      const debut = new Date(encaissement.dateDebutPeriode).toLocaleDateString('fr-FR');
      const fin = new Date(encaissement.dateFinPeriode).toLocaleDateString('fr-FR');
      return `${debut} → ${fin}`;
    }
    return '—';
  }
}
