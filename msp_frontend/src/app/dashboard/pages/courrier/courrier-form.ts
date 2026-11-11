import {Component, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize} from 'rxjs';
import {CourrierService} from '../services/courrier.service';
import {CourrierResponse, TypeCourrier} from '../models/courrier';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-courrier-form',
  imports: [ReactiveFormsModule],
  templateUrl: './courrier-form.html',
  styleUrl: './courrier-form.scss',
})
export class CourrierForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(CourrierService);
  private readonly auth = inject(AuthentificationService);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly errorMessages = signal<string[]>([]);
  readonly successMessage = signal('');
  readonly slug = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly existingFilename = signal<string | null>(null);

  readonly form = new FormGroup({
    nom: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    objet: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    typeCourrier: new FormControl<TypeCourrier>('ARRIVE', {validators: [Validators.required], nonNullable: true}),
    description: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    dateArrivee: new FormControl('', {nonNullable: true}),
    dateDepart: new FormControl('', {nonNullable: true}),
  });

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  get typeCourrier(): TypeCourrier {
    return this.form.controls.typeCourrier.value;
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_COURRIER') === true
      : this.auth.hasRole('AJOUTER_COURRIER') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'courrier']);
      return;
    }
    this.updateDateValidators();
    this.form.controls.typeCourrier.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.updateDateValidators());
    if (!slug) return;

    this.slug.set(slug);
    const courrier = this.router.getCurrentNavigation()?.extras.state?.['courrier'] ?? history.state?.courrier;
    if (this.isCourrierResponse(courrier) && courrier.slug === slug) {
      this.fillForm(courrier);
      return;
    }
    this.isLoading.set(true);
    this.service.get(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement du courrier à modifier.', error);
        this.errorMessages.set(['Impossible de charger le courrier à modifier.']);
      }
    });
  }

  onFileSelected(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    const file = event.target.files?.[0] ?? null;
    this.selectedFile.set(file);
    if (file) this.errorMessages.set([]);
  }

  submit(): void {
    this.form.markAllAsTouched();
    this.errorMessages.set([]);
    this.successMessage.set('');
    if (this.form.invalid) return;
    if (!this.isEditing && !this.selectedFile()) {
      this.errorMessages.set(['Le document du courrier est obligatoire.']);
      return;
    }
    if (this.isLoading() || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    const slug = this.slug();
    const request = slug ? this.service.update(slug, this.toFormData()) : this.service.create(this.toFormData());
    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<CourrierResponse>) => {
        if (!slug) {
          this.successMessage.set(response.message || 'Le courrier a été enregistré.');
          this.form.reset({
            nom: '', objet: '', typeCourrier: 'ARRIVE', description: '', dateArrivee: '', dateDepart: ''
          });
          this.selectedFile.set(null);
          if (this.fileInput) this.fileInput.nativeElement.value = '';
          return;
        }
        this.successMessage.set(response.message || 'Le courrier a été modifié.');
        this.existingFilename.set(response.donnees.resultat.nomFichierCourrier ?? null);
        this.selectedFile.set(null);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement du courrier.', error);
        this.errorMessages.set(this.extractErrors(error));
      }
    });
  }

  retourListe(): void {
    void this.router.navigate(['dashboard', 'courrier']);
  }

  private updateDateValidators(): void {
    const arrival = this.form.controls.dateArrivee;
    const departure = this.form.controls.dateDepart;
    arrival.setValidators(this.typeCourrier === 'ARRIVE' ? [Validators.required] : []);
    departure.setValidators(this.typeCourrier === 'DEPART' ? [Validators.required] : []);
    arrival.updateValueAndValidity({emitEvent: false});
    departure.updateValueAndValidity({emitEvent: false});
  }

  private fillForm(courrier: CourrierResponse): void {
    this.form.patchValue({
      nom: courrier.nom,
      objet: courrier.objet,
      typeCourrier: courrier.typeCourrier,
      description: courrier.description,
      dateArrivee: courrier.dateArrivee ?? '',
      dateDepart: courrier.dateDepart ?? '',
    });
    this.existingFilename.set(courrier.nomFichierCourrier ?? null);
  }

  private isCourrierResponse(value: unknown): value is CourrierResponse {
    if (typeof value !== 'object' || value === null) return false;
    const courrier = value as Record<string, unknown>;
    return typeof courrier['slug'] === 'string'
      && typeof courrier['nom'] === 'string'
      && typeof courrier['objet'] === 'string'
      && (courrier['typeCourrier'] === 'ARRIVE' || courrier['typeCourrier'] === 'DEPART')
      && typeof courrier['description'] === 'string';
  }

  private toFormData(): FormData {
    const values = this.form.getRawValue();
    const data = new FormData();
    data.append('nom', values.nom);
    data.append('objet', values.objet);
    data.append('typeCourrier', values.typeCourrier);
    data.append('description', values.description);
    if (values.dateArrivee) data.append('dateArrivee', values.dateArrivee);
    if (values.dateDepart) data.append('dateDepart', values.dateDepart);
    const file = this.selectedFile();
    if (file) data.append('documentCourrier', file, file.name);
    return data;
  }

  private extractErrors(error: unknown): string[] {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const body = error.error;
      if (typeof body === 'object' && body !== null) {
        if ('erreurs' in body && Array.isArray(body.erreurs)) return body.erreurs.map(String);
        if ('message' in body && typeof body.message === 'string') return [body.message];
      }
    }
    return ['Une erreur est survenue. Veuillez réessayer.'];
  }
}
