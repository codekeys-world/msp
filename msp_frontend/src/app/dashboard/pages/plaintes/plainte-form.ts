import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {PlainteService} from '../services/plainte.service';
import {PlainteResponse} from '../models/plainte';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-plainte-form',
  imports: [ReactiveFormsModule],
  templateUrl: './plainte-form.html',
  styleUrl: './plainte-form.scss',
})
export class PlainteForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(PlainteService);
  private readonly auth = inject(AuthentificationService);

  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly errorMessages = signal<string[]>([]);
  readonly successMessage = signal('');
  readonly slug = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly existingFilename = signal<string | null>(null);

  readonly form = new FormGroup({
    datePv: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    observation: new FormControl('', {nonNullable: true}),
  });

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_PLAINTE') === true
      : this.auth.hasRole('AJOUTER_PLAINTE') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'plaintes']);
      return;
    }
    if (!slug) return;

    this.slug.set(slug);
    const plainte = this.router.getCurrentNavigation()?.extras.state?.['plainte'] ?? history.state?.plainte;
    if (this.isPlainteResponse(plainte) && plainte.slug === slug) {
      this.fillForm(plainte);
      return;
    }
    this.isLoading.set(true);
    this.service.get(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement de la plainte.', error);
        this.errorMessages.set(['Impossible de charger la plainte à modifier.']);
      }
    });
  }

  onFileSelected(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.selectedFile.set(event.target.files?.[0] ?? null);
  }

  submit(): void {
    this.form.markAllAsTouched();
    this.errorMessages.set([]);
    this.successMessage.set('');
    if (this.form.invalid || this.isLoading() || this.isSubmitting()) return;

    const slug = this.slug();
    this.isSubmitting.set(true);
    const request = slug ? this.service.update(slug, this.toFormData()) : this.service.create(this.toFormData());
    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<PlainteResponse>) => {
        if (!slug) {
          this.successMessage.set(response.message || 'La plainte a été enregistrée.');
          this.form.reset({datePv: '', observation: ''});
          this.selectedFile.set(null);
          this.existingFilename.set(null);
          if (this.fileInput) this.fileInput.nativeElement.value = '';
          return;
        }
        this.successMessage.set(response.message || 'La plainte a été modifiée.');
        this.existingFilename.set(response.donnees.resultat.nomFichierPlainte ?? null);
        this.selectedFile.set(null);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement de la plainte.', error);
        this.errorMessages.set(this.extractErrors(error));
      }
    });
  }

  retourListe(): void {
    void this.router.navigate(['dashboard', 'plaintes']);
  }

  private fillForm(plainte: PlainteResponse): void {
    this.form.patchValue({
      datePv: String(plainte.datePv).slice(0, 10),
      observation: plainte.observation ?? '',
    });
    this.existingFilename.set(plainte.nomFichierPlainte ?? null);
  }

  private isPlainteResponse(value: unknown): value is PlainteResponse {
    if (typeof value !== 'object' || value === null) return false;
    const plainte = value as Record<string, unknown>;
    return typeof plainte['slug'] === 'string' && typeof plainte['datePv'] === 'string';
  }

  private toFormData(): FormData {
    const values = this.form.getRawValue();
    const data = new FormData();
    data.append('datePv', values.datePv);
    data.append('observation', values.observation);
    const file = this.selectedFile();
    if (file) data.append('documentPlainte', file, file.name);
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
