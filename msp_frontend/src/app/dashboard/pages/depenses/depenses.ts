import { ChangeDetectorRef, Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { DepenseService } from '../services/depense.service';
import { FraisDeGestionService } from '../services/frais-de-gestion.service';
import { DepensePage, DepenseResponse, formatMontant, libelleStatut, couleurStatut, StatutDepense } from '../models/depense';
import { FraisDeGestionResponse } from '../models/frais-de-gestion';
import { AuthentificationService } from '../../../authentification/services/authentification-service';
import { DocumentViewer } from '../../../document-viewer/document-viewer';
import { AppResponse } from '../../../app-response';

@Component({
    selector: 'app-depenses',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, DocumentViewer],
    templateUrl: './depenses.html',
    styleUrl: './depenses.scss'
})
export class DepensesComponent implements OnInit {
    private readonly depenseService = inject(DepenseService);
    private readonly fraisDeGestionService = inject(FraisDeGestionService);
    private readonly authentificationService = inject(AuthentificationService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly changeDetector = inject(ChangeDetectorRef);
    private readonly destroyRef = inject(DestroyRef);

    depenses: DepenseResponse[] = [];
    loading = false;
    page = 0;
    size = 8;
    terme = '';
    totalPages = 0;
    totalElements = 0;
    premierePage = true;
    dernierePage = false;
    errorMessage = '';
    decisionError = '';
    decisionPending = false;
    decision: { slug: string; type: 'valider' | 'rejeter'; observation: string } | null = null;

    canAdd = false;
    canEdit = false;
    canDelete = false;
    canValidate = false;

    fraisDeGestion: FraisDeGestionResponse | null = null;
    fraisDeGestionSlug: string | null = null;

    justificatifViewer: { show: boolean; data: string | null | undefined; nom: string | undefined; type: string | undefined } = {
        show: false, data: undefined, nom: undefined, type: undefined
    };

    ngOnInit(): void {
        this.canAdd = this.authentificationService.canEngagerDepense();
        this.canEdit = this.authentificationService.hasRole('MODIFIER_DEPENSE') === true;
        this.canDelete = this.authentificationService.canSupprimerDepense();
        this.canValidate = this.authentificationService.canValiderDecaissement();
        this.fraisDeGestionSlug = this.route.snapshot.paramMap.get('fraisDeGestionSlug');
        if (this.fraisDeGestionSlug) {
            this.fraisDeGestionService.rechercher(this.fraisDeGestionSlug).subscribe({
                next: (response: AppResponse<FraisDeGestionResponse>) => {
                    this.fraisDeGestion = response.donnees.resultat;
                    this.changeDetector.markForCheck();
                }
            });
        }
        this.charger();
    }

    charger(): void {
        this.loading = true;
        this.errorMessage = '';
        this.changeDetector.markForCheck();
        const obs$ = this.fraisDeGestionSlug
            ? this.depenseService.listerParFraisDeGestion(this.fraisDeGestionSlug, this.page, this.size, this.terme)
            : this.depenseService.lister(this.page, this.size, this.terme);

        obs$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: (response: AppResponse<DepensePage>) => {
                const resultat = response.donnees?.resultat;
                this.depenses = resultat?.content ?? [];
                this.page = resultat?.number ?? 0;
                this.totalPages = resultat?.totalPages ?? 0;
                this.totalElements = resultat?.totalElements ?? 0;
                this.premierePage = resultat?.first ?? true;
                this.dernierePage = resultat?.last ?? false;
                this.loading = false;
                this.changeDetector.markForCheck();
            },
            error: (error: unknown) => {
                console.error('Erreur lors du chargement des dépenses.', error);
                this.errorMessage = 'Impossible de charger les dépenses. Veuillez réessayer.';
                this.loading = false;
                this.changeDetector.markForCheck();
            }
        });
    }

    rechercher(): void {
        this.page = 0;
        this.charger();
    }

    ajouter(): void {
        this.router.navigate(['dashboard/depenses/nouvelle']);
    }

    supprimer(slug: string): void {
        if (confirm('Êtes-vous sûr de vouloir supprimer cette dépense ?')) {
            this.depenseService.supprimer(slug).subscribe(() => this.charger());
        }
    }

    valider(slug: string): void {
        this.ouvrirDecision(slug, 'valider');
    }

    rejeter(slug: string): void {
        this.ouvrirDecision(slug, 'rejeter');
    }

    ouvrirDecision(slug: string, type: 'valider' | 'rejeter'): void {
        this.decision = { slug, type, observation: '' };
        this.decisionError = '';
    }

    fermerDecision(): void {
        if (this.decisionPending) return;
        this.decision = null;
        this.decisionError = '';
    }

    confirmerDecision(): void {
        if (!this.decision || this.decisionPending) return;
        const { slug, type, observation } = this.decision;
        const commentaire = observation.trim();
        if (type === 'rejeter' && !commentaire) {
            this.decisionError = 'Veuillez indiquer le motif du rejet.';
            return;
        }

        this.decisionPending = true;
        this.decisionError = '';
        this.changeDetector.markForCheck();
        const request$ = type === 'valider'
            ? this.depenseService.valider(slug, commentaire || undefined)
            : this.depenseService.rejeter(slug, commentaire);

        request$.pipe(
            takeUntilDestroyed(this.destroyRef),
            finalize(() => {
                this.decisionPending = false;
                this.changeDetector.markForCheck();
            })
        ).subscribe({
            next: response => {
                const depense = response.donnees.resultat;
                this.depenses = this.depenses.map(item => item.slug === slug ? depense : item);
                this.decision = null;
                this.changeDetector.markForCheck();
            },
            error: (error: unknown) => {
                console.error(`Erreur lors de l'action ${type} de la dépense.`, error);
                this.decisionError = `Impossible de ${type} cette dépense. Veuillez réessayer.`;
                this.changeDetector.markForCheck();
            }
        });
    }

    ouvrirJustificatif(dep: DepenseResponse): void {
        if (!dep.slug || !dep.nomFichierJustificatif) return;
        this.depenseService.rechercher(dep.slug).subscribe({
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
                console.error('Erreur lors du chargement du justificatif de la dépense.', error);
                this.alert('Impossible de charger le justificatif. Veuillez réessayer.');
            }
        });
    }

    fermerJustificatif(): void {
        this.justificatifViewer = { show: false, data: undefined, nom: undefined, type: undefined };
    }

    formatMontant = formatMontant;
    libelleStatut = libelleStatut;
    couleurStatut = couleurStatut;
    StatutDepense = StatutDepense;

    alert(message: string): void {
        window.alert(message);
    }
}
