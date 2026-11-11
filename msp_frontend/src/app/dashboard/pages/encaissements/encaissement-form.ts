import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {EncaissementService} from '../services/encaissement.service';
import {EncaissementResponse, TypeEncaissement} from '../models/encaissement';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

@Component({
  selector: 'app-encaissement-form',
  imports: [ReactiveFormsModule],
  templateUrl: './encaissement-form.html',
  styleUrl: './encaissement-form.scss',
})
export class EncaissementForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(EncaissementService);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);

  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly errorMessages = signal<string[]>([]);
  readonly successMessage = signal('');
  readonly slug = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly existingFilename = signal<string | null>(null);

  readonly form = new FormGroup({
    typeEncaissement: new FormControl<TypeEncaissement | ''>('', {nonNullable: true, validators: [Validators.required]}),
    dateEncaissement: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    dateDebutPeriode: new FormControl('', {nonNullable: true}),
    dateFinPeriode: new FormControl('', {nonNullable: true}),
    montant: new FormControl<number | ''>('', {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
    typeBudget: new FormControl('', {nonNullable: true}),
    observation: new FormControl('', {nonNullable: true, validators: [Validators.maxLength(2000)]}),
  });

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  get isVersement(): boolean {
    return this.form.controls.typeEncaissement.value === 'VERSEMENT';
  }

  readonly typesBudget = [
    'Kayes', 'Nioro', 'Kita', 'Koulikoro', 'Dioïla', 'Nara', 'Sikasso', 'Bougouni', 'Koutiala',
    'Ségou', 'San', 'Mopti', 'Bandiagara', 'Douentza', 'Tombouctou', 'Taoudénit', 'Gao', 'Ménaka', 'Kidal', 'District de Bamako'
  ];

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_ENCAISSEMENT') === true
      : this.auth.hasRole('AJOUTER_ENCAISSEMENT') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'encaissements']);
      return;
    }
    if (!slug) return;

    this.slug.set(slug);
    this.isLoading.set(true);
    this.service.get(slug, false).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.populateForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement de l\'encaissement.', error);
        this.errorMessages.set(['Impossible de charger l\'encaissement.']);
      }
    });
  }

  private populateForm(data: EncaissementResponse): void {
    this.form.patchValue({
      typeEncaissement: data.typeEncaissement,
      dateEncaissement: data.dateEncaissement,
      dateDebutPeriode: data.dateDebutPeriode ?? '',
      dateFinPeriode: data.dateFinPeriode ?? '',
      montant: data.montant,
      typeBudget: data.typeBudget ?? '',
      observation: data.observation ?? '',
    });
    if (data.nomFichierJustificatif) {
      this.existingFilename.set(data.nomFichierJustificatif);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile.set(input.files[0]);
    }
  }

  clearFile(): void {
    this.selectedFile.set(null);
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  getFileSize(): string {
    const file = this.selectedFile();
    if (!file) return '';
    const bytes = file.size;
    if (bytes < 1024) return `${bytes} octets`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.sweetAlert.showError('Veuillez remplir tous les champs obligatoires.');
      return;
    }
    if (this.isVersement && this.form.controls.dateDebutPeriode.value && this.form.controls.dateFinPeriode.value) {
      const debut = new Date(this.form.controls.dateDebutPeriode.value as string);
      const fin = new Date(this.form.controls.dateFinPeriode.value as string);
      if (debut > fin) {
        this.sweetAlert.showError('La date de début de période ne peut pas être postérieure à la date de fin.');
        return;
      }
    }
    this.isSubmitting.set(true);
    this.errorMessages.set([]);
    this.successMessage.set('');

    const formData = new FormData();
    const value = this.form.value;
    formData.append('typeEncaissement', value.typeEncaissement as string);
    formData.append('dateEncaissement', value.dateEncaissement as string);
    if (value.dateDebutPeriode) formData.append('dateDebutPeriode', value.dateDebutPeriode as string);
    if (value.dateFinPeriode) formData.append('dateFinPeriode', value.dateFinPeriode as string);
    formData.append('montant', String(value.montant));
    if (value.typeBudget) formData.append('typeBudget', value.typeBudget);
    if (value.observation) formData.append('observation', value.observation as string);
    const file = this.selectedFile();
    if (file) formData.append('justificatif', file);

    const request = this.isEditing
      ? this.service.update(this.slug()!, formData)
      : this.service.create(formData);

    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: response => {
        this.successMessage.set(response.message || 'L\'encaissement a été enregistré.');
        setTimeout(() => {
          void this.router.navigate(['dashboard', 'encaissements']);
        }, 1200);
      },
      error: error => {
        console.error('Erreur lors de l\'enregistrement de l\'encaissement.', error);
        const messages = this.extractMessages(error);
        this.errorMessages.set(messages);
        this.sweetAlert.showError(messages[0] || 'Impossible d\'enregistrer l\'encaissement.');
      }
    });
  }

  annuler(): void {
    void this.router.navigate(['dashboard', 'encaissements']);
  }

  private extractMessages(error: any): string[] {
    if (error?.donnees?.erreurs) {
      return error.donnees.erreurs as string[];
    }
    if (error?.error?.message) {
      return [error.error.message];
    }
    return ['Une erreur est survenue.'];
  }
}
