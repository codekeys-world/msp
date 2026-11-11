import { ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { FraisDeGestionService } from '../services/frais-de-gestion.service';
import { FraisDeGestionResponse, FraisDeGestionPage, formatMontant } from '../models/frais-de-gestion';
import { AuthentificationService } from '../../../authentification/services/authentification-service';
import { DocumentViewer } from '../../../document-viewer/document-viewer';
import { AppResponse } from '../../../app-response';
import { debounceTime, distinctUntilChanged, finalize, Subject, Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SweetAlertService } from '../../../shared/services/sweet-alert.service';

@Component({
    selector: 'app-frais-de-gestion',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, DocumentViewer],
    templateUrl: './frais-de-gestion.html',
    styleUrl: './frais-de-gestion.scss'
})
export class FraisDeGestionComponent implements OnInit {
    private readonly fraisDeGestionService = inject(FraisDeGestionService);
    private readonly authentificationService = inject(AuthentificationService);
    private readonly router = inject(Router);
    private readonly sweetAlert = inject(SweetAlertService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly changeDetector = inject(ChangeDetectorRef);
    private readonly searchChanges = new Subject<string>();
    private listSubscription?: Subscription;

    fraisDeGestions: FraisDeGestionResponse[] = [];
    loading = false;
    page = 0;
    size = 8;
    terme = '';
    private termeActif = '';
    totalPages = 0;
    totalElements = 0;
    premierePage = true;
    dernierePage = false;
    errorMessage = '';

    canAdd = false;
    canEdit = false;
    canDelete = false;

    justificatifViewer: { show: boolean; data: string | null | undefined; nom: string | undefined; type: string | undefined } = {
        show: false, data: undefined, nom: undefined, type: undefined
    };

    ngOnInit(): void {
        this.canAdd = this.authentificationService.canEngagerDepense();
        this.canEdit = this.authentificationService.hasRole('MODIFIER_DEPENSE') === true;
        this.canDelete = this.authentificationService.canSupprimerDepense();
        this.searchChanges.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            takeUntilDestroyed(this.destroyRef)
        ).subscribe(terme => this.appliquerRecherche(terme));
        this.charger();
    }

    charger(): void {
        this.listSubscription?.unsubscribe();
        this.loading = true;
        this.errorMessage = '';
        this.changeDetector.markForCheck();
        this.listSubscription = this.fraisDeGestionService.lister(this.page, this.size, this.termeActif)
            .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => {
                this.loading = false;
                this.changeDetector.markForCheck();
            }))
            .subscribe({
            next: (response: AppResponse<FraisDeGestionPage>) => {
                const resultat = response.donnees?.resultat;
                this.fraisDeGestions = resultat?.content ?? [];
                this.page = resultat?.number ?? 0;
                this.totalPages = resultat?.totalPages ?? 0;
                this.totalElements = resultat?.totalElements ?? 0;
                this.premierePage = resultat?.first ?? true;
                this.dernierePage = resultat?.last ?? false;
                this.changeDetector.markForCheck();
            },
            error: (error: unknown) => {
                console.error('Erreur lors du chargement des frais de gestion.', error);
                this.errorMessage = 'Impossible de charger les frais de gestion. Veuillez réessayer.';
                this.changeDetector.markForCheck();
            }
        });
    }

    onSearchChange(terme: string): void {
        this.searchChanges.next(terme.trim());
    }

    rechercher(): void {
        const terme = this.terme.trim();
        this.searchChanges.next(terme);
        this.appliquerRecherche(terme);
    }

    private appliquerRecherche(terme: string): void {
        if (terme === this.termeActif) return;
        this.termeActif = terme;
        this.page = 0;
        this.charger();
    }

    ajouter(): void {
        this.router.navigate(['dashboard/frais-de-gestion/nouveau']);
    }

    async supprimer(frais: FraisDeGestionResponse): Promise<void> {
        if (!frais.slug || this.deletingSlug) return;
        const confirmed = await this.sweetAlert.confirmDeletion(
            'le frais de gestion', `l’exercice ${frais.annee}`,
            'Les frais associés contenant des dépenses ne peuvent pas être supprimés.'
        );
        if (!confirmed) return;

        this.deletingSlug = frais.slug;
        this.changeDetector.markForCheck();
        this.fraisDeGestionService.supprimer(frais.slug)
            .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => {
                this.deletingSlug = null;
                this.changeDetector.markForCheck();
            }))
            .subscribe({
                next: response => {
                    this.sweetAlert.showSuccess(response.message || 'Le frais de gestion a été supprimé.');
                    if (this.fraisDeGestions.length <= 1 && this.page > 0) this.page--;
                    this.charger();
                },
                error: error => {
                    console.error('Erreur lors de la suppression du frais de gestion.', error);
                    this.sweetAlert.showError(error?.error?.message || 'Impossible de supprimer le frais de gestion.');
                }
            });
    }

    ouvrirJustificatif(fdg: FraisDeGestionResponse): void {
        if (!fdg.slug || !fdg.nomFichierJustificatif) return;
        this.fraisDeGestionService.rechercher(fdg.slug).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: response => {
                const detail = response.donnees?.resultat;
                this.justificatifViewer = {
                    show: true,
                    data: detail?.justificatif,
                    nom: detail?.nomFichierJustificatif,
                    type: detail?.typeFichierJustificatif
                };
                this.changeDetector.markForCheck();
            },
            error: error => {
                console.error('Erreur lors du chargement du justificatif des frais de gestion.', error);
                window.alert('Impossible de charger le justificatif. Veuillez réessayer.');
            }
        });
    }

    fermerJustificatif(): void {
        this.justificatifViewer = { show: false, data: undefined, nom: undefined, type: undefined };
    }

    formatMontant = formatMontant;
    deletingSlug: string | null = null;
}
