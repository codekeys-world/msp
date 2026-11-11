import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {OrdreMissionService} from '../services/ordre-mission.service';
import {AppResponse} from '../../../app-response';
import {OrdreMissionResponse} from '../models/ordre-mission-response';

@Component({
  selector: 'app-ordre-mission-form',
  imports: [ReactiveFormsModule],
  templateUrl: './ordre-mission-form.html',
})
export class OrdreMissionForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ordreMissionService = inject(OrdreMissionService);

  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  ordreMission = signal<File | null>(null);
  existingFilename = signal<string | null>(null);
  errorMessages = signal<string[]>([]);
  message = signal<string>('');
  submiting = signal<boolean>(false);
  isLoading = signal<boolean>(false);
  slug = signal<string | null>(null);

  ordreMissionForm = new FormGroup({
    libelle: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    objet: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    imputationBudgetaire: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    moyenDeplacement: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    itineraire: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    dateDepart: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    dateRetour: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    observation: new FormControl('', {nonNullable: true}),
  });

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.slug.set(slug);
      const mission = this.router.getCurrentNavigation()?.extras.state?.['ordreMission']
        ?? history.state?.ordreMission;
      if (this.estDonneesMission(mission) && mission.slug === slug) {
        this.remplirFormulaire(mission);
      } else {
       this.isLoading.set(true);
       this.ordreMissionService.getOrdreMission(slug)
         .pipe(finalize(() => this.isLoading.set(false)))
         .subscribe({
           next: response => this.remplirFormulaire(response.donnees.resultat),
           error: (error: unknown) => {
             console.error("Erreur lors du chargement de l'ordre de mission à modifier.", error);
             this.errorMessages.set(["Impossible de charger les informations de cette mission."]);
           }
         });
      }
    }
  }

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  onOrdreMissionSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.ordreMission.set(input.files[0]);
      this.errorMessages.set([]);
    }
  }

  onOrdreMissionSubmit(): void {
    this.ordreMissionForm.markAllAsTouched();
    this.errorMessages.set([]);
    this.message.set('');

    if (this.ordreMissionForm.invalid) {
      return;
    }
    if (!this.isEditing && !this.ordreMission()) {
      this.errorMessages.set(["Veuillez sélectionner le fichier de l'ordre de mission."]);
      return;
    }
    if (this.isLoading() || this.submiting()) {
      return;
    }

    const formData = this.creerFormData();
    this.submiting.set(true);

    const slug = this.slug();
    const request = slug
      ? this.ordreMissionService.updateOrdreMission(slug, formData)
      : this.ordreMissionService.saveOrdreMission(formData);

    request.subscribe({
      next: (response: AppResponse<OrdreMissionResponse>) => {
        this.message.set(response.message || (this.isEditing
          ? "L'ordre de mission a été modifié."
          : "L'ordre de mission a été enregistré."));
        this.submiting.set(false);
        if (this.isEditing) {
          this.existingFilename.set(response.donnees.resultat.nomFichierOrdreMission ?? null);
          this.ordreMission.set(null);
          if (this.fileInput) {
            this.fileInput.nativeElement.value = '';
          }
        } else {
          this.onReinitialiseOrdreMissionForm();
        }
      },
      error: (error: unknown) => {
        console.error("Erreur lors de l'enregistrement de l'ordre de mission.", error);
        this.errorMessages.set(this.extraireErreurs(error));
        this.submiting.set(false);
      }
    });
  }

  onReinitialiseOrdreMissionForm(): void {
    this.ordreMissionForm.reset();
    this.ordreMission.set(null);
    this.existingFilename.set(null);
    this.errorMessages.set([]);
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  retourListe(): void {
    this.router.navigate(['dashboard', 'ordre-missions']);
  }

  private estDonneesMission(value: unknown): value is Pick<
    OrdreMissionResponse,
    'slug' | 'libelle' | 'objet' | 'imputationBudgetaire' | 'moyenDeplacement' |
    'itineraire' | 'dateDepart' | 'dateRetour' | 'observation' | 'nomFichierOrdreMission'
  > {
    if (typeof value !== 'object' || value === null) {
      return false;
    }

    const mission = value as Record<string, unknown>;
    return typeof mission['slug'] === 'string'
      && typeof mission['libelle'] === 'string'
      && typeof mission['objet'] === 'string'
      && typeof mission['imputationBudgetaire'] === 'string'
      && typeof mission['moyenDeplacement'] === 'string'
      && typeof mission['itineraire'] === 'string'
      && typeof mission['dateDepart'] === 'string'
      && typeof mission['dateRetour'] === 'string';
  }

  private remplirFormulaire(mission: Pick<
    OrdreMissionResponse,
    'libelle' | 'objet' | 'imputationBudgetaire' | 'moyenDeplacement' |
    'itineraire' | 'dateDepart' | 'dateRetour' | 'observation' | 'nomFichierOrdreMission'
  >): void {
    this.ordreMissionForm.patchValue({
      libelle: mission.libelle,
      objet: mission.objet,
      imputationBudgetaire: mission.imputationBudgetaire,
      moyenDeplacement: mission.moyenDeplacement,
      itineraire: mission.itineraire,
      dateDepart: String(mission.dateDepart).slice(0, 10),
      dateRetour: String(mission.dateRetour).slice(0, 10),
      observation: mission.observation ?? '',
    });
    this.existingFilename.set(mission.nomFichierOrdreMission ?? null);
  }

  private creerFormData(): FormData {
    const values = this.ordreMissionForm.getRawValue();
    const formData = new FormData();
    formData.append('libelle', values.libelle);
    formData.append('objet', values.objet);
    formData.append('imputationBudgetaire', values.imputationBudgetaire);
    formData.append('moyenDeplacement', values.moyenDeplacement);
    formData.append('itineraire', values.itineraire);
    formData.append('dateDepart', values.dateDepart);
    formData.append('dateRetour', values.dateRetour);
    formData.append('observation', values.observation);

    const fichier = this.ordreMission();
    if (fichier) {
      formData.append('ordreMission', fichier, fichier.name);
    }
    return formData;
  }

  private extraireErreurs(error: unknown): string[] {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const apiError = error.error;
      if (typeof apiError === 'object' && apiError !== null) {
        if ('erreurs' in apiError && Array.isArray(apiError.erreurs)) {
          return apiError.erreurs.map(String);
        }
        if ('message' in apiError && typeof apiError.message === 'string') {
          return [apiError.message];
        }
      }
    }
    return ["Une erreur est survenue. Veuillez réessayer."];
  }
}
