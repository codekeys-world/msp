import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {ConvocationService} from '../services/convocation.service';
import {ConvocationResponse} from '../models/convocation';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-convocation-form',
  imports: [ReactiveFormsModule],
  templateUrl: './convocation-form.html',
  styleUrl: './convocation-form.scss',
})
export class ConvocationForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ConvocationService);
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
    description: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    dateConvocation: new FormControl('', {validators: [Validators.required], nonNullable: true}),
  });

  get isEditing(): boolean { return this.slug() !== null; }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_CONVOCATION') === true
      : this.auth.hasRole('AJOUTER_CONVOCATION') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'convocations']);
      return;
    }
    if (!slug) return;
    this.slug.set(slug);
    const convocation = this.router.getCurrentNavigation()?.extras.state?.['convocation'] ?? history.state?.convocation;
    if (this.isConvocationResponse(convocation) && convocation.slug === slug) {
      this.fillForm(convocation);
      return;
    }
    this.isLoading.set(true);
    this.service.get(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement de la convocation.', error);
        this.errorMessages.set(['Impossible de charger la convocation à modifier.']);
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
    const payload = this.toFormData();
    const request = slug ? this.service.update(slug, payload) : this.service.create(payload);
    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<ConvocationResponse>) => {
        if (!slug) {
          this.successMessage.set(response.message || 'La convocation a été enregistrée.');
          this.form.reset({description: '', dateConvocation: ''});
          this.selectedFile.set(null);
          this.existingFilename.set(null);
          if (this.fileInput) this.fileInput.nativeElement.value = '';
          return;
        }
        this.successMessage.set(response.message || 'La convocation a été modifiée.');
        this.existingFilename.set(response.donnees.resultat.nomFichierConvocation ?? null);
        this.selectedFile.set(null);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement de la convocation.', error);
        this.errorMessages.set(this.extractErrors(error));
      }
    });
  }

  retourListe(): void { void this.router.navigate(['dashboard', 'convocations']); }

  private fillForm(convocation: ConvocationResponse): void {
    this.form.patchValue({
      description: convocation.description,
      dateConvocation: String(convocation.dateConvocation).slice(0, 10),
    });
    this.existingFilename.set(convocation.nomFichierConvocation ?? null);
  }

  private isConvocationResponse(value: unknown): value is ConvocationResponse {
    if (typeof value !== 'object' || value === null) return false;
    const convocation = value as Record<string, unknown>;
    return typeof convocation['slug'] === 'string'
      && typeof convocation['description'] === 'string'
      && typeof convocation['dateConvocation'] === 'string';
  }

  private toFormData(): FormData {
    const values = this.form.getRawValue();
    const data = new FormData();
    data.append('description', values.description);
    data.append('dateConvocation', values.dateConvocation);
    const file = this.selectedFile();
    if (file) data.append('documentConvocation', file, file.name);
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
