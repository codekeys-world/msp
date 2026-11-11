import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {finalize, Subscription} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import Swal from 'sweetalert2/dist/sweetalert2.esm.all.js';
import {AdminService} from '../services/admin.service';
import {PageResponse, UtilisateurDetail} from '../models/utilisateur-detail';
import {AppResponse} from '../../../app-response';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-utilisateurs',
  imports: [NgClass],
  templateUrl: './utilisateurs.html',
  styleUrl: './utilisateurs.scss',
})
export class Utilisateurs implements OnInit {
  private readonly service = inject(AdminService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly destroyRef = inject(DestroyRef);
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly size = 20;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly utilisateurs = signal<AppResponse<PageResponse<UtilisateurDetail>> | null>(null);
  readonly togglingSlug = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUtilisateurs();
  }

  get totalPages(): number {
    return this.utilisateurs()?.donnees.resultat.totalPages ?? 0;
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

  goToPreviousPage(): void {
    this.goToPage(this.page() - 1);
  }

  goToNextPage(): void {
    this.goToPage(this.page() + 1);
  }

  goToPage(pageNumber: number): void {
    this.page.set(pageNumber);
    this.loadUtilisateurs();
  }

  loadUtilisateurs(): void {
    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.listerUtilisateurs(this.page(), this.size)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => this.utilisateurs.set(response),
        error: error => {
          console.error('Erreur lors du chargement des utilisateurs.', error);
          this.errorMessage.set('Impossible de charger la liste des utilisateurs.');
        }
      });
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMINISTRATEUR':
        return 'bg-danger';
      case 'SYSADMIN':
        return 'bg-dark';
      case 'PRESIDENT':
        return 'bg-primary';
      case 'TRESORIER':
        return 'bg-success';
      case 'SECRETAIRE_ADMINISTRATIF':
        return 'bg-info';
      case 'SECRETAIRE_ORGANISATION':
        return 'bg-warning text-dark';
      case 'SECRETAIRE_AFFAIRE_SOCIALE':
        return 'bg-secondary';
      case 'COMMISSION_CONTROLE':
        return 'bg-info text-dark';
      default:
        return 'bg-secondary';
    }
  }

  toggleStatut(utilisateur: UtilisateurDetail): void {
    const nouveauStatut = !utilisateur.enabled;
    const label = nouveauStatut ? 'activer' : 'désactiver';
    const description = `Le compte de ${utilisateur.prenom} ${utilisateur.nom} (${utilisateur.matricule}) sera ${nouveauStatut ? 'activé' : 'désactivé'}.`;

    Swal.fire({
      titleText: `Voulez-vous ${label} le compte ?`,
      text: description,
      icon: 'warning',
      showCloseButton: true,
      showCancelButton: true,
      confirmButtonText: `Oui, ${label}`,
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#198754',
      cancelButtonColor: '#6c757d',
      reverseButtons: true,
      focusCancel: true,
      allowOutsideClick: false,
    }).then((result) => {
      if (result.isConfirmed) {
        this.togglingSlug.set(utilisateur.slug);
        this.service.modifierStatutUtilisateur(utilisateur.slug, nouveauStatut)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.sweetAlert.showSuccess(`Le compte a été ${nouveauStatut ? 'activé' : 'désactivé'} avec succès.`);
              this.togglingSlug.set(null);
              this.loadUtilisateurs();
            },
            error: (error) => {
              console.error('Erreur lors de la modification du statut.', error);
              this.togglingSlug.set(null);
              this.sweetAlert.showError('Impossible de modifier le statut du compte.');
            }
          });
      }
    });
  }
}
