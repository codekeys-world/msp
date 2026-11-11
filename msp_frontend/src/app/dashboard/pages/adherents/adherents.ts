import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {finalize, Subscription} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AdherentService} from '../services/adherent.service';
import {PageResponse, UtilisateurDetail} from '../models/utilisateur-detail';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

@Component({
  selector: 'app-adherents',
  imports: [RouterLink],
  templateUrl: './adherents.html',
  styleUrl: './adherents.scss',
})
export class Adherents implements OnInit {
  private readonly service = inject(AdherentService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  private readonly destroyRef = inject(DestroyRef);
  private listSubscription?: Subscription;
  readonly page = signal(0);
  readonly size = 8;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly adherents = signal<AppResponse<PageResponse<UtilisateurDetail>> | null>(null);
  readonly deletingSlug = signal<string | null>(null);
  readonly searchTerm = signal('');
  activeSearchTerm = '';
  readonly canManage = this.auth.canManageAdherents();
  readonly canConfectionCard = this.auth.canConfectionCard();
  readonly canExport = this.auth.canExportAdherents();

  getPhotoUrl(adherent: UtilisateurDetail): string | null {
    if (adherent.profileImage && adherent.profileImageType) {
      return `data:${adherent.profileImageType};base64,${adherent.profileImage}`;
    }
    return null;
  }

  ngOnInit(): void {
    if (!this.auth.canListAdherents()) {
      this.router.navigate(['/dashboard/home']);
      return;
    }
    this.loadAdherents();
  }

  get totalPages(): number {
    return this.adherents()?.donnees.resultat.totalPages ?? 0;
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

  loadAdherents(): void {
    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.listAdherents(this.page(), this.size, this.activeSearchTerm)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.adherents.set(response),
      error: error => {
        console.error('Erreur lors du chargement des adhérents.', error);
        this.errorMessage.set('Impossible de charger la liste des adhérents.');
      }
    });
  }

  onSearchInput(event: Event): void {
    if (event.target instanceof HTMLInputElement) {
      this.searchTerm.set(event.target.value);
      this.applySearch(event.target.value.trim());
    }
  }

  searchAdherents(): void {
    const terme = this.searchTerm().trim();
    this.applySearch(terme);
  }

  private applySearch(terme: string): void {
    if (this.activeSearchTerm === terme) return;
    this.activeSearchTerm = terme;
    this.page.set(0);
    this.loadAdherents();
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.applySearch('');
  }

  view(adherent: Pick<UtilisateurDetail, 'slug'>): void {
    void this.router.navigate(['dashboard', 'adherent', adherent.slug]);
  }

  makeCard(adherent: Pick<UtilisateurDetail, 'slug'>, event?: Event): void {
    if (event) event.stopPropagation();
    void this.router.navigate(['dashboard', 'carte-membre', adherent.slug]);
  }

  edit(adherent: UtilisateurDetail): void {
    void this.router.navigate(['dashboard', 'adherent', adherent.slug, 'modifier']);
  }

  async delete(adherent: UtilisateurDetail): Promise<void> {
    if (!this.canManage || this.deletingSlug()) return;
    const confirmed = await this.sweetAlert.confirmDeletion('l’adhérent', `${adherent.prenom} ${adherent.nom}`,
      'Ses conjoints et descendants seront également supprimés.');
    if (!confirmed) return;

    this.deletingSlug.set(adherent.slug);
    this.service.deleteAdherent(adherent.slug).pipe(finalize(() => this.deletingSlug.set(null))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'L’adhérent a été supprimé.');
        if ((this.adherents()?.donnees.resultat.content.length ?? 0) <= 1 && this.page() > 0) {
          this.page.update(page => page - 1);
        }
        this.loadAdherents();
      },
      error: error => {
        console.error('Erreur lors de la suppression de l’adhérent.', error);
        this.sweetAlert.showError('Impossible de supprimer cet adhérent.');
      }
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page() || this.isLoading()) return;
    this.page.set(page);
    this.loadAdherents();
  }

  exportToExcel(): void {
    this.service.exportAdherents().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => {
        const data = response.donnees.resultat;
        if (!data || data.length === 0) {
          this.sweetAlert.showError('Aucun adhérent à exporter.');
          return;
        }
        this.generateExcelFile(data);
        this.sweetAlert.showSuccess(`${data.length} adhérent(s) exporté(s) avec succès.`);
      },
      error: error => {
        console.error('Erreur lors de l\'export des adhérents.', error);
        this.sweetAlert.showError('Impossible d\'exporter la liste des adhérents.');
      }
    });
  }

  private generateExcelFile(data: UtilisateurDetail[]): void {
    const headers = ['Matricule', 'Numéro d\'adhésion', 'Nom', 'Prénom', 'Genre', 'Email', 'Téléphone principal', 'Téléphone secondaire', 'Date de naissance', 'Lieu de naissance', 'Adresse', 'Numéro d\'inscription', 'Père', 'Mère', 'Conjoints', 'Enfants', 'Observations'];
    
    const rows = data.map(adherent => [
      adherent.matricule || '',
      adherent.numeroAdhesion || '',
      adherent.nom || '',
      adherent.prenom || '',
      adherent.genre === 'HOMME' ? 'Homme' : 'Femme',
      adherent.email || '',
      adherent.telephonePrincipal || '',
      adherent.telephoneSecondaire || '',
      adherent.dateNaissance ? new Date(adherent.dateNaissance).toLocaleDateString('fr-FR') : '',
      adherent.lieuNaissance || '',
      adherent.adresse || '',
      adherent.numeroInscription || '',
      adherent.pere || '',
      adherent.mere || '',
      (adherent.conjoints?.length ?? 0).toString(),
      (adherent.descendants?.length ?? 0).toString(),
      adherent.observations || ''
    ]);

    let csvContent = '\uFEFF';
    csvContent += headers.join(';') + '\r\n';
    rows.forEach(row => {
      csvContent += row.map(cell => '"' + String(cell).replace(/"/g, '""') + '"').join(';') + '\r\n';
    });

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'liste_adherents_' + new Date().toISOString().slice(0, 10) + '.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
