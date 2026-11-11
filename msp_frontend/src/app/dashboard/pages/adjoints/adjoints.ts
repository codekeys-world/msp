import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize} from 'rxjs';
import {AdjointService} from '../services/adjoint.service';
import {AdjointPage, AdjointResponse, formatRoleLabel} from '../models/adjoint';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {ProfilService} from '../services/profil.service';
import {AdherentService} from '../services/adherent.service';
import {UtilisateurDetail} from '../models/utilisateur-detail';

@Component({
  selector: 'app-adjoints',
  imports: [DatePipe],
  templateUrl: './adjoints.html',
  styleUrl: './adjoints.scss',
})
export class Adjoints implements OnInit {
  private readonly service = inject(AdjointService);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly profilService = inject(ProfilService);
  private readonly adherentService = inject(AdherentService);

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<AdjointPage> | null>(null);
  readonly showForm = signal(false);
  readonly selectedRole = signal('');
  readonly delegantSlug = signal('');

  // Formulaire
  readonly adjointSlug = signal('');
  readonly dateDebut = signal('');
  readonly dateFin = signal('');
  readonly motif = signal('');
  readonly submitting = signal(false);

  // Recherche utilisateur dans la liste des adhérents
  readonly searchQuery = signal('');
  readonly adherentList = signal<UtilisateurDetail[]>([]);
  readonly filteredAdherents = signal<UtilisateurDetail[]>([]);
  readonly selectedUser = signal<UtilisateurDetail | null>(null);
  readonly showAdherentList = signal(false);

  readonly allRolesDisponibles = [
    {value: 'SECRETAIRE_ADMINISTRATIF', label: 'Secrétaire administratif'},
    {value: 'PRESIDENT', label: 'Président'},
    {value: 'TRESORIER', label: 'Trésorier'},
    {value: 'SECRETAIRE_ORGANISATION', label: 'Secrétaire à l\'organisation'},
    {value: 'SECRETAIRE_AFFAIRE_SOCIALE', label: 'Secrétaire aux affaires sociales'}
  ];

  private utilisateurRole = signal('');
  private isAdminOrSysadmin = false;

  get rolesDisponibles() {
    if (this.isAdminOrSysadmin) {
      return this.allRolesDisponibles;
    }
    const role = this.utilisateurRole();
    if (!role) return [];
    return this.allRolesDisponibles.filter(r => r.value === role);
  }

  ngOnInit(): void {
    this.profilService.getProfil()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          const profil = response.donnees.resultat;
          this.delegantSlug.set(profil.slug);
          const roleNom = profil.role || '';
          this.utilisateurRole.set(roleNom);
          this.isAdminOrSysadmin = roleNom === 'ADMINISTRATEUR' || roleNom === 'SYSADMIN';
          this.load();
        },
        error: error => {
          console.error('Erreur lors du chargement du profil.', error);
          this.errorMessage.set('Impossible de charger le profil.');
          this.isLoading.set(false);
        }
      });
  }

  get adjoints(): AdjointResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  load(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    const role = this.utilisateurRole();
    if (!role) {
      this.isLoading.set(false);
      return;
    }

    if (this.isAdminOrSysadmin) {
      this.service.listerActifs(0, 50)
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: response => this.pageResponse.set(response),
          error: error => {
            console.error('Erreur lors du chargement des adjoints.', error);
            this.errorMessage.set('Impossible de charger les adjoints.');
          }
        });
    } else {
      this.service.listerParRole(role, this.delegantSlug(), 0, 50)
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: response => this.pageResponse.set(response),
          error: error => {
            console.error('Erreur lors du chargement des adjoints.', error);
            this.errorMessage.set('Impossible de charger les adjoints.');
          }
        });
    }
  }

  openForm(role: string): void {
    this.selectedRole.set(role);
    this.showForm.set(true);
    this.adjointSlug.set('');
    this.dateDebut.set(new Date().toISOString().split('T')[0]);
    this.dateFin.set('');
    this.motif.set('');
    this.selectedUser.set(null);
    this.searchQuery.set('');
    this.showAdherentList.set(false);
    this.loadAdherents();
  }

  loadAdherents(): void {
    this.adherentService.listAdherents(0, 500, '')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          const allAdherents = response.donnees.resultat?.content ?? [];
          this.adherentList.set(allAdherents);
          this.filteredAdherents.set(allAdherents);
        },
        error: error => {
          console.error('Erreur lors du chargement des adhérents.', error);
        }
      });
  }

  onSearchQueryChange(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    const query = event.target.value.trim().toLowerCase();
    this.searchQuery.set(event.target.value);
    this.showAdherentList.set(true);

    if (!query) {
      this.filteredAdherents.set(this.adherentList());
      return;
    }

    const filtered = this.adherentList().filter(a =>
      a.nom.toLowerCase().includes(query) ||
      a.prenom.toLowerCase().includes(query) ||
      a.matricule.toLowerCase().includes(query) ||
      (a.telephonePrincipal && a.telephonePrincipal.includes(query))
    );
    this.filteredAdherents.set(filtered);
  }

  selectUser(user: UtilisateurDetail): void {
    this.selectedUser.set(user);
    this.adjointSlug.set(user.slug);
    this.searchQuery.set('');
    this.showAdherentList.set(false);
  }

  clearSelectedUser(): void {
    this.selectedUser.set(null);
    this.adjointSlug.set('');
    this.showAdherentList.set(true);
  }

  toggleAdherentList(): void {
    this.showAdherentList.set(!this.showAdherentList());
  }

  closeForm(): void {
    this.showForm.set(false);
    this.selectedRole.set('');
  }

  async submit(): Promise<void> {
    if (!this.adjointSlug() || !this.dateDebut()) {
      this.sweetAlert.showError('Veuillez remplir les champs obligatoires.');
      return;
    }

    this.submitting.set(true);
    const formData = new FormData();
    const requestJson = JSON.stringify({
      adjointSlug: this.adjointSlug(),
      dateDebut: this.dateDebut(),
      dateFin: this.dateFin() || null,
      motif: this.motif() || null
    });
    formData.append('request', new Blob([requestJson], {type: 'application/json'}));

    this.service.designer(formData, this.selectedRole(), this.delegantSlug())
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.submitting.set(false)))
      .subscribe({
        next: response => {
          this.sweetAlert.showSuccess(response.message || 'Adjoint désigné avec succès !');
          this.closeForm();
          this.load();
        },
        error: error => {
          console.error('Erreur lors de la désignation.', error);
          this.sweetAlert.showError(error.error?.message || 'Impossible de désigner l\'adjoint.');
        }
      });
  }

  async desactiver(adjoint: AdjointResponse): Promise<void> {
    const confirmed = await this.sweetAlert.confirmDeletion(
      'la délégation', adjoint.adjointNomComplet,
      'L\'adjoint ne pourra plus exercer les fonctions en votre absence.'
    );
    if (!confirmed) return;

    this.service.desactiver(adjoint.slug)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          this.sweetAlert.showSuccess(response.message || 'Délégation désactivée.');
          this.load();
        },
        error: error => {
          console.error('Erreur lors de la désactivation.', error);
          this.sweetAlert.showError('Impossible de désactiver la délégation.');
        }
      });
  }

  async reactivier(adjoint: AdjointResponse): Promise<void> {
    this.service.reactivier(adjoint.slug)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          this.sweetAlert.showSuccess(response.message || 'Délégation réactivée.');
          this.load();
        },
        error: error => {
          console.error('Erreur lors de la réactivation.', error);
          this.sweetAlert.showError('Impossible de réactiver la délégation.');
        }
      });
  }

  getRoleLabel(role: string): string {
    return formatRoleLabel(role);
  }

  getRoleForLabel(role: string): string {
    const found = this.rolesDisponibles.find(r => r.value === role);
    return found ? found.label : role;
  }


  onDateDebutChange(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.dateDebut.set(event.target.value);
  }

  onDateFinChange(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.dateFin.set(event.target.value);
  }

  onMotifChange(event: Event): void {
    if (!(event.target instanceof HTMLTextAreaElement)) return;
    this.motif.set(event.target.value);
  }
}
