import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {PvService} from '../services/pv.service';
import {PvResponse} from '../models/pv';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-pv-form',
  imports: [ReactiveFormsModule],
  templateUrl: './pv-form.html',
  styleUrl: './pv-form.scss',
})
export class PvForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(PvService);
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
    numero: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    objet: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    type: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    datePv: new FormControl('', {validators: [Validators.required], nonNullable: true}),
    observation: new FormControl('', {nonNullable: true}),
  });

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_PV') === true
      : this.auth.hasRole('AJOUTER_PV') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'proces-verbaux']);
      return;
    }
    if (!slug) return;

    this.slug.set(slug);
    const pv = this.router.getCurrentNavigation()?.extras.state?.['pv'] ?? history.state?.pv;
    if (this.isPvResponse(pv) && pv.slug === slug) {
      this.fillForm(pv);
      return;
    }
    this.isLoading.set(true);
    this.service.get(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement du procès-verbal.', error);
        this.errorMessages.set(['Impossible de charger le procès-verbal à modifier.']);
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
      this.errorMessages.set(['Le document du procès-verbal est obligatoire.']);
      return;
    }
    if (this.isLoading() || this.isSubmitting()) return;

    const formData = this.toFormData();
    this.isSubmitting.set(true);
    const slug = this.slug();
    const request = slug ? this.service.update(slug, formData) : this.service.create(formData);
    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<PvResponse>) => {
        if (!slug) {
          this.successMessage.set(response.message || 'Le procès-verbal a été enregistré.');
          this.form.reset();
          this.selectedFile.set(null);
          this.existingFilename.set(null);
          if (this.fileInput) this.fileInput.nativeElement.value = '';
          return;
        }
        this.successMessage.set(response.message || 'Le procès-verbal a été modifié.');
        this.existingFilename.set(response.donnees.resultat.nomFichierPv ?? null);
        this.selectedFile.set(null);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement du procès-verbal.', error);
        this.errorMessages.set(this.extractErrors(error));
      }
    });
  }

  retourListe(): void {
    void this.router.navigate(['dashboard', 'proces-verbaux']);
  }

  private fillForm(pv: PvResponse): void {
    this.form.patchValue({
      numero: pv.numero,
      objet: pv.objet,
      type: pv.type,
      datePv: String(pv.datePv).slice(0, 10),
      observation: pv.observation ?? '',
    });
    this.existingFilename.set(pv.nomFichierPv ?? null);
  }

  private isPvResponse(value: unknown): value is PvResponse {
    if (typeof value !== 'object' || value === null) return false;
    const pv = value as Record<string, unknown>;
    return typeof pv['slug'] === 'string'
      && typeof pv['numero'] === 'string'
      && typeof pv['objet'] === 'string'
      && typeof pv['type'] === 'string'
      && typeof pv['datePv'] === 'string';
  }

  private toFormData(): FormData {
    const values = this.form.getRawValue();
    const data = new FormData();
    data.append('numero', values.numero);
    data.append('objet', values.objet);
    data.append('type', values.type);
    data.append('datePv', values.datePv);
    data.append('observation', values.observation);
    const file = this.selectedFile();
    if (file) data.append('documentPv', file, file.name);
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
