import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { DepenseService } from '../services/depense.service';
import { FraisDeGestionService } from '../services/frais-de-gestion.service';
import { FraisDeGestionResponse } from '../models/frais-de-gestion';
import { DepenseResponse } from '../models/depense';
import { AppResponse } from '../../../app-response';

@Component({
    selector: 'app-depense-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './depense-form.html',
    styleUrl: './depense-form.scss'
})
export class DepenseFormComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly depenseService = inject(DepenseService);
    private readonly fraisDeGestionService = inject(FraisDeGestionService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    form: FormGroup = this.fb.group({
        libelle: ['', Validators.required],
        dateDepense: ['', Validators.required],
        montant: ['', [Validators.required, Validators.min(1)]],
        fraisDeGestionSlug: ['', Validators.required],
        observation: ['']
    });

    loading = false;
    submitting = false;
    slug: string | null = null;
    fichierSelectionne: string = '';
    fraisDeGestions: FraisDeGestionResponse[] = [];

    ngOnInit(): void {
        this.slug = this.route.snapshot.paramMap.get('slug');
        this.chargerFraisDeGestion();
        if (this.slug) {
            this.charger();
        }
    }

    chargerFraisDeGestion(): void {
        this.fraisDeGestionService.listerOptions().subscribe({
            next: (response: AppResponse<FraisDeGestionResponse[]>) => {
                this.fraisDeGestions = response.donnees?.resultat ?? [];
            }
        });
    }

    charger(): void {
        if (!this.slug) return;
        this.loading = true;
        this.depenseService.rechercher(this.slug, false).subscribe({
            next: (response: AppResponse<DepenseResponse>) => {
                const dep = response.donnees?.resultat;
                this.form.patchValue({
                    libelle: dep.libelle,
                    dateDepense: dep.dateDepense,
                    montant: dep.montant,
                    fraisDeGestionSlug: dep.fraisDeGestionSlug,
                    observation: dep.observation
                });
                this.fichierSelectionne = dep.nomFichierJustificatif || '';
                this.loading = false;
            },
            error: () => this.loading = false
        });
    }

    onFichierChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.fichierSelectionne = input.files[0].name;
        }
    }

    soumettre(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.submitting = true;
        const formData = new FormData();
        formData.append('libelle', this.form.get('libelle')!.value);
        formData.append('dateDepense', this.form.get('dateDepense')!.value);
        formData.append('montant', this.form.get('montant')!.value.toString());
        formData.append('fraisDeGestionSlug', this.form.get('fraisDeGestionSlug')!.value);
        if (this.form.get('observation')!.value) {
            formData.append('observation', this.form.get('observation')!.value);
        }
        const input = document.querySelector('input[type="file"]') as HTMLInputElement;
        if (input?.files?.length) {
            formData.append('justificatif', input.files[0]);
        }

        const obs$ = this.slug
            ? this.depenseService.modifier(this.slug, formData)
            : this.depenseService.engager(formData);

        obs$.subscribe({
            next: () => this.router.navigate(['dashboard/depenses']),
            error: () => this.submitting = false
        });
    }

    annuler(): void {
        this.router.navigate(['dashboard/depenses']);
    }
}
