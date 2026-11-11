import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {Router} from '@angular/router';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {OrdreMissionService} from '../services/ordre-mission.service';
import {OrdreMissionDetails} from '../ordre-mission-details/ordre-mission-details';
import {AppResponse} from '../../../app-response';
import {PageResponseOrdreMissionResponse} from '../models/page-response-ordre-mission-response';
import {OrdreMissionResponse} from '../models/ordre-mission-response';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-ordre-mission',
  imports: [
    OrdreMissionDetails


  ],
  templateUrl: './ordre-mission.html',
  styleUrl: './ordre-mission.scss',
})
export class OrdreMission implements OnInit {
  page = signal<number>(0);
  size = signal<number>(5);
  router = inject(Router);
  ordreMissionService = inject(OrdreMissionService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly auth = inject(AuthentificationService);
  isLoading = signal<boolean>(true);
  deletingSlug = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  searchTerm = signal<string>('');
  activeSearchTerm = '';
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchChanges = new Subject<string>();
  private listSubscription?: Subscription;

  // Autorisations
  readonly canList = this.auth.hasRole('LISTER_ORDRE_MISSION') === true;
  readonly canAdd = this.auth.hasRole('AJOUTER_ORDRE_MISSION') === true;
  readonly canEdit = this.auth.hasRole('MODIFIER_ORDRE_MISSION') === true;
  readonly canDelete = this.auth.hasRole('SUPPRIMER_ORDRE_MISSION') === true;

  ordreMissions = signal<AppResponse<PageResponseOrdreMissionResponse> | undefined>(undefined);

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.onListOrdreMission();
  }

  get totalPages(): number {
    return this.ordreMissions()?.donnees.resultat.totalPages ?? 0;
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

  goToAddOrdreMissionForm(): void {
    this.router.navigate(['dashboard', 'nouveau-ordre-missions']);
  }

  editOrdreMission(ordreMission: OrdreMissionResponse): void {
    this.router.navigate(['dashboard', 'modifier-ordre-missions', ordreMission.slug], {
      state: {
        ordreMission: {
          slug: ordreMission.slug,
          libelle: ordreMission.libelle,
          objet: ordreMission.objet,
          imputationBudgetaire: ordreMission.imputationBudgetaire,
          moyenDeplacement: ordreMission.moyenDeplacement,
          itineraire: ordreMission.itineraire,
          dateDepart: ordreMission.dateDepart,
          dateRetour: ordreMission.dateRetour,
          observation: ordreMission.observation,
          nomFichierOrdreMission: ordreMission.nomFichierOrdreMission
        }
      }
    });
  }

  async deleteOrdreMission(ordreMission: OrdreMissionResponse): Promise<void> {
    if (this.deletingSlug()) {
      return;
    }

    this.deletingSlug.set(ordreMission.slug);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    const confirmed = await this.sweetAlert.confirmDeletion(
      "l'ordre de mission",
      ordreMission.libelle,
      'Le fichier joint sera également supprimé.'
    );
    if (!confirmed) {
      this.deletingSlug.set(null);
      return;
    }

    this.ordreMissionService.deleteOrdreMission(ordreMission.slug).subscribe({
      next: (response: AppResponse<boolean>) => {
        const message = response.message || "L'ordre de mission a été supprimé.";
        this.successMessage.set(message);
        this.sweetAlert.showSuccess(message);
        const currentContent = this.ordreMissions()?.donnees.resultat.content ?? [];
        if (currentContent.length <= 1 && this.page() > 0) {
          this.page.update(currentPage => currentPage - 1);
        }
        this.deletingSlug.set(null);
        this.onListOrdreMission();
      },
      error: (error: unknown) => {
        console.error("Erreur lors de la suppression de l'ordre de mission.", error);
        const message = "Impossible de supprimer l'ordre de mission. Veuillez réessayer.";
        this.errorMessage.set(message);
        this.sweetAlert.showError(message);
        this.deletingSlug.set(null);
      }
    });
  }

  onListOrdreMission(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.listSubscription?.unsubscribe();
    this.listSubscription = this.ordreMissionService.listOrdreMission(this.page(), this.size(), this.activeSearchTerm)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
      next: (response: AppResponse<PageResponseOrdreMissionResponse>) => {
        this.ordreMissions.set(response);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des ordres de mission.', error);
        this.errorMessage.set('Impossible de charger les ordres de mission. Veuillez réessayer.');
      }
    });
  }

  onSearchInput(event: Event): void {
    if (event.target instanceof HTMLInputElement) {
      this.searchTerm.set(event.target.value);
      this.searchChanges.next(event.target.value.trim());
    }
  }

  searchOrdreMission(): void {
    this.searchChanges.next(this.searchTerm().trim());
    this.applySearch(this.searchTerm().trim());
  }

  private applySearch(term: string): void {
    if (this.activeSearchTerm === term) {
      return;
    }
    this.activeSearchTerm = term;
    this.page.set(0);
    this.onListOrdreMission();
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.searchChanges.next('');
    this.applySearch('');
  }

  goToPage(page: number): void {
    if (
      this.isLoading() ||
      !Number.isInteger(page) ||
      page < 0 ||
      page >= this.totalPages ||
      page === this.page()
    ) {
      return;
    }

    this.page.set(page);
    this.onListOrdreMission();
  }

  goToFirstPage(): void {
    this.goToPage(0);
  }

  goToPreviousPage(): void {
    this.goToPage(this.page() - 1);
  }

  goToNextPage(): void {
    this.goToPage(this.page() + 1);
  }

  goToLastPage(): void {
    this.goToPage(this.totalPages - 1);
  }
}
