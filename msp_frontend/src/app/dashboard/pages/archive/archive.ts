import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject, Subscription} from 'rxjs';
import {ArchiveService} from '../services/archive.service';
import {ArchivePage, ArchiveResponse, ARCHIVE_CATEGORIES, formatTaille, libelleTypeDocument} from '../models/archive';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {DocumentViewer} from '../../../document-viewer/document-viewer';

@Component({
  selector: 'app-archive',
  imports: [DatePipe, DocumentViewer],
  templateUrl: './archive.html',
  styleUrl: './archive.scss',
})
export class Archive implements OnInit {
  private readonly service = inject(ArchiveService);
  private readonly auth = inject(AuthentificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchChanges = new Subject<string>();
  private listSubscription?: Subscription;

  readonly page = signal(0);
  readonly size = 8;
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pageResponse = signal<AppResponse<ArchivePage> | null>(null);
  readonly searchTerm = signal('');
  readonly selectedCategory = signal('');
  readonly categories = ARCHIVE_CATEGORIES;
  readonly canListArchive = this.auth.hasRole('LISTER_ARCHIVE') === true;

  activeSearchTerm = '';

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.applySearch(term));
    this.load();
  }

  get archives(): ArchiveResponse[] {
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

  get totalElements(): number {
    return this.pageResponse()?.donnees.resultat.totalElements ?? 0;
  }

  load(): void {
    if (!this.canListArchive) {
      this.isLoading.set(false);
      this.errorMessage.set('Vous ne disposez pas du droit de consulter les archives.');
      return;
    }
    this.listSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.listSubscription = this.service.list(
      this.page(), this.size, this.activeSearchTerm, this.selectedCategory()
    ).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: response => this.pageResponse.set(response),
        error: error => {
          console.error('Erreur lors du chargement des archives.', error);
          this.errorMessage.set('Impossible de charger les archives.');
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

  onCategoryChange(category: string): void {
    this.selectedCategory.set(category);
    this.page.set(0);
    this.activeSearchTerm = '';
    this.searchTerm.set('');
    this.load();
  }

  goToPage(page: number): void {
    if (this.isLoading() || !Number.isInteger(page) || page < 0 || page >= this.totalPages || page === this.page()) return;
    this.page.set(page);
    this.load();
  }

  formatTaille(taille: number | null): string {
    return formatTaille(taille);
  }

  libelleTypeDocument(typeDocument: string): string {
    return libelleTypeDocument(typeDocument);
  }

  reference(archive: ArchiveResponse): string {
    return `ARC-${archive.slug.slice(0, 8).toUpperCase()}`;
  }
}
