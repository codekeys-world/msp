import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {PrestationService} from '../services/prestation.service';
import {PrestationPage, PrestationResponse} from '../models/prestation';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-prestations',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './prestations.html',
  styleUrl: './prestations.scss',
})
export class Prestations implements OnInit {
  private readonly service = inject(PrestationService);
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
  readonly pageResponse = signal<AppResponse<PrestationPage> | null>(null);
  readonly searchTerm = signal('');
  readonly deletingSlug = signal<string | null>(null);
  readonly canList = this.auth.hasRole('LISTER_PRESTATION') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_PRESTATION') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_PRESTATION') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_PRESTATION') === true;
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get prestations(): PrestationResponse[] {
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
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les prestations.');
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
          console.error('Erreur lors du chargement des prestations.', error);
          this.errorMessage.set('Impossible de charger les prestations.');
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
    void this.router.navigate(['dashboard', 'nouvelle-prestation']);
  }

  modifier(prestation: PrestationResponse): void {
    void this.router.navigate(['dashboard', 'modifier-prestation', prestation.slug]);
  }

  async supprimer(prestation: PrestationResponse): Promise<void> {
    if (!this.canDelete || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'la prestation', this.reference(prestation),
      prestation.nomFichierJustificatif ? 'Le justificatif joint sera également supprimé.' : 'Cette action est irréversible.'
    );
    if (!confirmed) return;
    this.deletingSlug.set(prestation.slug);
    this.service.delete(prestation.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'La prestation a été supprimée.');
        if (this.prestations.length <= 1 && this.page() > 0) this.page.update(value => value - 1);
        this.load();
      },
      error: error => {
        console.error('Erreur lors de la suppression de la prestation.', error);
        this.sweetAlert.showError('Impossible de supprimer la prestation.');
      }
    });
  }

  reference(prestation: PrestationResponse): string {
    return `PR-${prestation.slug.slice(0, 8).toUpperCase()}`;
  }

  typeLabel(type: PrestationResponse['typePrestation']): string {
    return {
      RETRAITE: 'Retraite',
      DECES: 'Décès d\'un proche',
      DECES_ADHERENT: 'Décès de l\'adhérent',
      REFORME_INVALIDITE: 'Réforme pour invalidité'
    }[type];
  }

  deathLabel(prestation: PrestationResponse): string {
    if (prestation.typePrestation === 'DECES_ADHERENT') return 'Décès de l\'adhérent';
    if (prestation.typePrestation !== 'DECES') return '';
    const lien = {
      ADHERENT: 'Adhérent',
      PERE: 'Père',
      MERE: 'Mère',
      EPOUX: 'Époux',
      EPOUSE: 'Épouse',
      ENFANT: 'Enfant'
    }[prestation.lienDefunt ?? 'ADHERENT'];
    return prestation.lienDefunt === 'ADHERENT'
      ? 'Décès de l\'adhérent'
      : `Décès de ${lien.toLowerCase()} : ${prestation.prenomDefunt ?? ''} ${prestation.nomDefunt ?? ''}`.trim();
  }

  beneficiaryLabel(prestation: PrestationResponse): string {
    if (prestation.typeBeneficiaire === 'PERSONNE_DESIGNEE') {
      return `${prestation.prenomBeneficiaire ?? ''} ${prestation.nomBeneficiaire ?? ''} · ${prestation.telephoneBeneficiaire ?? ''}`.trim();
    }
    return prestation.typeBeneficiaire === 'PARENTS' ? 'Parents de l\'adhérent' : 'Adhérent';
  }

  prestationLabel(prestation: PrestationResponse): string {
    if (prestation.statutPrestation === 'NON_ELIGIBLE') return 'Non éligible';
    if (prestation.statutPrestation === 'REMBOURSEMENT_COTISATIONS') {
      return prestation.montantRemboursement === null
        ? 'Remboursement des cotisations'
        : `Remboursement : ${prestation.montantRemboursement.toLocaleString('fr-FR')} FCFA`;
    }
    return prestation.montantPrestation === null
      ? 'À définir'
      : `${prestation.montantPrestation.toLocaleString('fr-FR')} FCFA`;
  }
}
