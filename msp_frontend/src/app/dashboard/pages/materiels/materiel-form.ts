import {Component, inject, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {MaterielResponse, MaterielStatut} from '../models/materiel';
import {MaterielService} from '../services/materiel.service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

@Component({
  selector: 'app-materiel-form',
  imports: [ReactiveFormsModule],
  templateUrl: './materiel-form.html',
})
export class MaterielForm implements OnInit {
  readonly route = inject(ActivatedRoute);
  readonly router = inject(Router);
  private readonly service = inject(MaterielService);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly slug = signal<string | null>(null);

  readonly form = new FormGroup({
    nom: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    categorie: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    fournisseur: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    quantite: new FormControl<number | null>(null, {validators: [Validators.required, Validators.min(0)]}),
    prixUnitaire: new FormControl<number | null>(null, {validators: [Validators.required, Validators.min(0)]}),
    dateAcquisition: new FormControl('', {nonNullable: true}),
    emplacement: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(200)]}),
    numeroSerie: new FormControl('', {nonNullable: true}),
    observation: new FormControl('', {nonNullable: true, validators: [Validators.maxLength(2000)]}),
    statut: new FormControl<MaterielStatut | ''>('', {nonNullable: true, validators: [Validators.required]}),
  });

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_MATERIEL') === true || this.auth.canManageMateriel()
      : this.auth.hasRole('AJOUTER_MATERIEL') === true || this.auth.canManageMateriel();

    if (!authorized) {
      void this.router.navigate(['dashboard', 'materiels']);
      return;
    }

    if (!slug) return;
    this.slug.set(slug);
    this.isLoading.set(true);
    this.service.rechercher(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement du matériel.', error);
        this.sweetAlert.showError('Impossible de charger le matériel.');
      }
    });
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.sweetAlert.showError('Veuillez renseigner les champs obligatoires avec des valeurs valides.');
      return;
    }
    const payload = this.form.getRawValue();
    const request = {
      nom: payload.nom,
      categorie: payload.categorie,
      fournisseur: payload.fournisseur,
      quantite: Number(payload.quantite),
      prixUnitaire: Number(payload.prixUnitaire),
      dateAcquisition: payload.dateAcquisition || null,
      emplacement: payload.emplacement,
      numeroSerie: payload.numeroSerie || null,
      observation: payload.observation || null,
      statut: payload.statut,
    };

    const action = this.slug() ? this.service.modifier(this.slug()!, request) : this.service.enregistrer(request);
    this.isSubmitting.set(true);
    action.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<MaterielResponse>) => {
        const message = response.message || 'Matériel enregistré.';
        this.sweetAlert.showSuccess(message);
        void this.router.navigate(['dashboard', 'materiels']);
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement du matériel.', error);
        this.sweetAlert.showError(error?.error?.message || 'Impossible d’enregistrer le matériel.');
      }
    });
  }

  private fillForm(materiel: MaterielResponse): void {
    this.form.patchValue({
      nom: materiel.nom,
      categorie: materiel.categorie,
      fournisseur: materiel.fournisseur,
      quantite: materiel.quantite,
      prixUnitaire: materiel.prixUnitaire,
      dateAcquisition: materiel.dateAcquisition ?? '',
      emplacement: materiel.emplacement,
      numeroSerie: materiel.numeroSerie ?? '',
      observation: materiel.observation ?? '',
      statut: materiel.statut,
    });
  }
}
