import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {CasSocialService} from '../services/cas-social.service';
import {CasSocialPage, CasSocialResponse} from '../models/cas-social';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-cas-sociaux',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './cas-sociaux.html',
  styleUrl: './cas-sociaux.scss',
})
export class CasSociaux implements OnInit {
  private readonly service = inject(CasSocialService);
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
  readonly pageResponse = signal<AppResponse<CasSocialPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_CAS_SOCIAUX') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_CAS_SOCIAUX') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_CAS_SOCIAUX') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_CAS_SOCIAUX') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get casSociaux(): CasSocialResponse[] {
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
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les cas sociaux.');
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
          console.error('Erreur lors du chargement des cas sociaux.', error);
          this.errorMessage.set('Impossible de charger les cas sociaux.');
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
    void this.router.navigate(['dashboard', 'nouveau-cas-social']);
  }

  modifier(casSocial: CasSocialResponse): void {
    void this.router.navigate(['dashboard', 'modifier-cas-social', casSocial.slug]);
  }

  async supprimer(casSocial: CasSocialResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'le cas social', this.reference(casSocial),
      casSocial.nomFichierJustificatif ? 'Le justificatif joint sera également supprimé.' : 'Cette action est irréversible.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(casSocial.slug);
    this.service.delete(casSocial.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Le cas social a été supprimé.');
        if (this.casSociaux.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression du cas social.', error);
        this.sweetAlert.showError('Impossible de supprimer le cas social.');
      }
    });
  }

  reference(casSocial: CasSocialResponse): string {
    return `CS-${casSocial.slug.slice(0, 8).toUpperCase()}`;
  }

  typeLabel(type: CasSocialResponse['typeCas']): string {
    return {
      MALADIE: 'Maladie (hospitalisation)',
      ACCIDENT_CIRCULATION: 'Accident de la circulation',
      RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE: 'Radiation / révocation / retraite disciplinaire'
    }[type];
  }

  beneficiaryLabel(casSocial: CasSocialResponse): string {
    if (casSocial.typeBeneficiaire === 'PERSONNE_DESIGNEE') {
      return `${casSocial.prenomBeneficiaire ?? ''} ${casSocial.nomBeneficiaire ?? ''} · ${casSocial.telephoneBeneficiaire ?? ''}`.trim();
    }
    return casSocial.typeBeneficiaire === 'PARENTS' ? 'Parents de l’adhérent' : 'Adhérent';
  }

  prestationLabel(casSocial: CasSocialResponse): string {
    if (casSocial.statutPrestation === 'NON_ELIGIBLE') return 'Non éligible';
    if (casSocial.statutPrestation === 'REMBOURSEMENT_COTISATIONS') {
      return casSocial.montantRemboursement === null
        ? 'Remboursement des cotisations'
        : `Remboursement : ${casSocial.montantRemboursement.toLocaleString('fr-FR')} FCFA`;
    }
    return casSocial.montantPrestation === null
      ? 'À définir'
      : `${casSocial.montantPrestation.toLocaleString('fr-FR')} FCFA`;
  }
}
