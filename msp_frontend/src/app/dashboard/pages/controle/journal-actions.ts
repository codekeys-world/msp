import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe, CommonModule} from '@angular/common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {JournalActionService} from '../services/journal-action.service';
import {JournalActionPage, JournalActionResponse, JournalActionStatistiques} from '../models/journal-action';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-journal-actions',
  imports: [CommonModule, DatePipe],
  templateUrl: './journal-actions.html',
  styleUrl: './journal-actions.scss',
})
export class JournalActions implements OnInit {
  private readonly service = inject(JournalActionService);
  private readonly auth = inject(AuthentificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchChanges = new Subject<string>();
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly size = 10;
  readonly isLoading = signal(true);
  readonly isLoadingStats = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<JournalActionPage> | null>(null);
  readonly stats = signal<JournalActionStatistiques | null>(null);

  readonly searchTerm = signal('');
  readonly selectedModule = signal('');
  readonly selectedStatut = signal('');
  readonly selectedDateRange = signal('all'); // all, today, 7days, 30days
  readonly selectedActionDetail = signal<JournalActionResponse | null>(null);

  readonly canView = this.auth.canViewControle();
  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));

    this.loadStats();
    this.load();
  }

  get actions(): JournalActionResponse[] {
    return this.pageResponse()?.donnees.resultat.content ?? [];
  }

  get totalPages(): number {
    return this.pageResponse()?.donnees.resultat.totalPages ?? 0;
  }

  get totalElements(): number {
    return this.pageResponse()?.donnees.resultat.totalElements ?? 0;
  }

  get pageNumbers(): number[] {
    const total = this.totalPages;
    const current = this.page();
    const delta = 2;
    const range: number[] = [];
    for (let i = Math.max(0, current - delta); i <= Math.min(total - 1, current + delta); i++) {
      range.push(i);
    }
    return range;
  }

  get isFirstPage(): boolean {
    return this.page() === 0;
  }

  get isLastPage(): boolean {
    return this.totalPages === 0 || this.page() >= this.totalPages - 1;
  }

  load(): void {
    if (!this.canView) {
      this.isLoading.set(false);
      this.errorMessage.set("Vous ne disposez pas des autorisations nécessaires pour accéder à la Commission de Contrôle.");
      return;
    }

    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const {dateDebut, dateFin} = this.calculerDatesFiltre();

    this.listSubscription = this.service.list(
      this.page(),
      this.size,
      this.activeSearchTerm,
      this.selectedModule(),
      this.selectedStatut(),
      dateDebut,
      dateFin
    )
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => this.pageResponse.set(response),
        error: error => {
          console.error("Erreur lors du chargement du journal d'actions.", error);
          this.errorMessage.set("Impossible de charger le journal des actions.");
        }
      });
  }

  loadStats(): void {
    if (!this.canView) return;
    this.isLoadingStats.set(true);
    this.service.getStatistiques()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoadingStats.set(false)))
      .subscribe({
        next: response => this.stats.set(response.donnees.resultat),
        error: err => console.warn("Erreur chargement des statistiques", err)
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

  onModuleChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.selectedModule.set(val);
    this.page.set(0);
    this.load();
  }

  onStatutChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.selectedStatut.set(val);
    this.page.set(0);
    this.load();
  }

  onDateRangeChange(range: string): void {
    this.selectedDateRange.set(range);
    this.page.set(0);
    this.load();
  }

  resetFilters(): void {
    this.searchTerm.set('');
    this.activeSearchTerm = '';
    this.selectedModule.set('');
    this.selectedStatut.set('');
    this.selectedDateRange.set('all');
    this.page.set(0);
    this.load();
    this.loadStats();
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

  openDetail(action: JournalActionResponse): void {
    this.selectedActionDetail.set(action);
  }

  closeDetail(): void {
    this.selectedActionDetail.set(null);
  }

  copyToClipboard(text: string): void {
    if (!text) return;
    void navigator.clipboard.writeText(text);
  }

  getRoleBadgeClass(role: string): string {
    const r = (role || '').toUpperCase();
    if (r.includes('SYSADMIN')) return 'badge-role--sysadmin';
    if (r.includes('ADMIN')) return 'badge-role--admin';
    if (r.includes('CONTROLE') || r.includes('CTRL')) return 'badge-role--controle';
    if (r.includes('SECRETAIRE')) return 'badge-role--secretaire';
    if (r.includes('PRESIDENT')) return 'badge-role--president';
    if (r.includes('TRESORIER')) return 'badge-role--tresorier';
    return 'badge-role--default';
  }

  getClientIcon(client: string): string {
    const c = (client || '').toLowerCase();
    if (c.includes('chrome')) return 'bi-browser-chrome text-warning';
    if (c.includes('firefox')) return 'bi-browser-firefox text-danger';
    if (c.includes('edge')) return 'bi-browser-edge text-primary';
    if (c.includes('safari')) return 'bi-compass text-info';
    if (c.includes('postman') || c.includes('curl')) return 'bi-terminal text-secondary';
    if (c.includes('android') || c.includes('ios')) return 'bi-phone text-success';
    return 'bi-globe text-muted';
  }

  getModuleBadgeClass(module: string | null | undefined): string {
    const m = (module || '').toUpperCase();
    if (m.includes('AUTHENT')) return 'badge-module--auth';
    if (m.includes('ADHER')) return 'badge-module--adherents';
    if (m.includes('CARTE')) return 'badge-module--carte';
    if (m.includes('COURRIER')) return 'badge-module--courriers';
    if (m.includes('PV') || m.includes('PROCES')) return 'badge-module--pv';
    if (m.includes('CONVOC')) return 'badge-module--convocations';
    if (m.includes('PLAINTE')) return 'badge-module--plaintes';
    if (m.includes('SOCIAL')) return 'badge-module--social';
    if (m.includes('ADMIN')) return 'badge-module--admin';
    return 'badge-module--systeme';
  }

  private calculerDatesFiltre(): {dateDebut: string; dateFin: string} {
    const range = this.selectedDateRange();
    const today = new Date();
    const formatDate = (d: Date) => d.toISOString().split('T')[0];

    if (range === 'today') {
      const todayStr = formatDate(today);
      return {dateDebut: todayStr, dateFin: todayStr};
    } else if (range === '7days') {
      const start = new Date();
      start.setDate(today.getDate() - 7);
      return {dateDebut: formatDate(start), dateFin: formatDate(today)};
    } else if (range === '30days') {
      const start = new Date();
      start.setDate(today.getDate() - 30);
      return {dateDebut: formatDate(start), dateFin: formatDate(today)};
    }
    return {dateDebut: '', dateFin: ''};
  }
}
