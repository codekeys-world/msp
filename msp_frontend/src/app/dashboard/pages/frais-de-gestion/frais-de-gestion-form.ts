import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FraisDeGestionService } from '../services/frais-de-gestion.service';
import { AppResponse } from '../../../app-response';
import { SweetAlertService } from '../../../shared/services/sweet-alert.service';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-frais-de-gestion-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './frais-de-gestion-form.html',
    styleUrl: './frais-de-gestion-form.scss'
})
export class FraisDeGestionFormComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly fraisDeGestionService = inject(FraisDeGestionService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly sweetAlert = inject(SweetAlertService);

    form: FormGroup = this.fb.group({
        annee: ['', [Validators.required, Validators.min(2000), Validators.max(2100)]],
        montantTotal: ['', [Validators.required, Validators.min(1)]],
        observation: [''],
        justificatif: [null]
    });

    loading = false;
    submitting = false;
    slug: string | null = null;
    fichierSelectionne: string = '';
    fichier: File | null = null;

    ngOnInit(): void {
        this.slug = this.route.snapshot.paramMap.get('slug');
        if (this.slug) {
            this.charger();
        }
    }

    charger(): void {
        if (!this.slug) return;
        this.loading = true;
        this.fraisDeGestionService.rechercher(this.slug).pipe(finalize(() => this.loading = false)).subscribe({
            next: (response: AppResponse<any>) => {
                const fdg = response.donnees?.resultat;
                this.form.patchValue({
                    annee: fdg.annee,
                    montantTotal: fdg.montantTotal,
                    observation: fdg.observation
                });
                this.fichierSelectionne = fdg.nomFichierJustificatif || '';
            },
            error: error => {
                console.error('Erreur lors du chargement du frais de gestion.', error);
                this.sweetAlert.showError('Impossible de charger le frais de gestion.');
            }
        });
    }

    onFichierChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.fichier = input.files[0];
            this.fichierSelectionne = this.fichier.name;
        }
    }

    soumettre(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.sweetAlert.showError('Veuillez renseigner une année et un montant total valides.');
            return;
        }

        this.submitting = true;
        const formData = new FormData();
        formData.append('annee', this.form.get('annee')!.value.toString());
        formData.append('montantTotal', this.form.get('montantTotal')!.value.toString());
        if (this.form.get('observation')!.value) {
            formData.append('observation', this.form.get('observation')!.value);
        }
        if (this.fichier) formData.append('justificatif', this.fichier);

        const obs$ = this.slug
            ? this.fraisDeGestionService.modifier(this.slug, formData)
            : this.fraisDeGestionService.enregistrer(formData);

        obs$.pipe(finalize(() => this.submitting = false)).subscribe({
            next: response => {
                this.sweetAlert.showSuccess(response.message || 'Le frais de gestion a été enregistré.');
                void this.router.navigate(['dashboard/frais-de-gestion']);
            },
            error: error => {
                console.error('Erreur lors de l’enregistrement du frais de gestion.', error);
                this.sweetAlert.showError(error?.error?.message || 'Impossible d’enregistrer le frais de gestion.');
            }
        });
    }

    annuler(): void {
        void this.router.navigate(['dashboard/frais-de-gestion']);
    }
}
