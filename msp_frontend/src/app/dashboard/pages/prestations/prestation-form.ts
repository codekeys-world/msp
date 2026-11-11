import {Component, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, finalize, Subject} from 'rxjs';
import {PrestationService} from '../services/prestation.service';
import {PrestationAdherentOption, PrestationResponse, LienDefunt, TypeBeneficiaire, TypePrestation} from '../models/prestation';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-prestation-form',
  imports: [ReactiveFormsModule],
  templateUrl: './prestation-form.html',
  styleUrl: './prestation-form.scss',
})
export class PrestationForm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(PrestationService);
  private readonly auth = inject(AuthentificationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly adherentSearchChanges = new Subject<string>();

  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly isSearchingAdherents = signal(false);
  readonly errorMessages = signal<string[]>([]);
  readonly successMessage = signal('');
  readonly slug = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly existingFilename = signal<string | null>(null);
  readonly adherents = signal<PrestationAdherentOption[]>([]);
  readonly selectedAdherent = signal<PrestationAdherentOption | null>(null);
  readonly adherentSearchTerm = signal('');
  readonly adherentSearchError = signal('');

  readonly form = new FormGroup({
    adherentSlug: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    typePrestation: new FormControl<TypePrestation | ''>('', {nonNullable: true, validators: [Validators.required]}),
    datePrestation: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    heuresHospitalisation: new FormControl('', {nonNullable: true}),
    lienDefunt: new FormControl<LienDefunt | ''>('', {nonNullable: true}),
    nomDefunt: new FormControl('', {nonNullable: true}),
    prenomDefunt: new FormControl('', {nonNullable: true}),
    typeBeneficiaire: new FormControl<TypeBeneficiaire | ''>('', {nonNullable: true, validators: [Validators.required]}),
    nomBeneficiaire: new FormControl('', {nonNullable: true}),
    prenomBeneficiaire: new FormControl('', {nonNullable: true}),
    telephoneBeneficiaire: new FormControl('', {nonNullable: true}),
    dureeCotisationMois: new FormControl('', {nonNullable: true}),
    montantCotisationsVersees: new FormControl('', {nonNullable: true}),
    observation: new FormControl('', {nonNullable: true, validators: [Validators.maxLength(2000)]}),
  });

  get isEditing(): boolean {
    return this.slug() !== null;
  }

  get isDeathOfMember(): boolean {
    return this.form.controls.typePrestation.value === 'DECES_ADHERENT'
      || (this.form.controls.typePrestation.value === 'DECES' && this.form.controls.lienDefunt.value === 'ADHERENT');
  }

  get isDesignatedBeneficiary(): boolean {
    return this.form.controls.typeBeneficiaire.value === 'PERSONNE_DESIGNEE';
  }

  get isDeathCase(): boolean {
    return this.form.controls.typePrestation.value === 'DECES';
  }

  get isRetirement(): boolean {
    return this.form.controls.typePrestation.value === 'RETRAITE';
  }

  get requiresContributionDuration(): boolean {
    const type = this.form.controls.typePrestation.value;
    return type === 'DECES_ADHERENT' || type === 'RETRAITE'
      || (type === 'DECES' && this.form.controls.lienDefunt.value === 'ADHERENT');
  }

  get requiresContributionAmount(): boolean {
    const type = this.form.controls.typePrestation.value;
    const months = Number(this.form.controls.dureeCotisationMois.value);
    return (type === 'DECES_ADHERENT' || (type === 'DECES' && this.form.controls.lienDefunt.value === 'ADHERENT'))
      && this.form.controls.dureeCotisationMois.value !== '' && months < 60;
  }

  ngOnInit(): void {
    this.adherentSearchChanges.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(term => this.loadAdherents(term));
    this.form.controls.dureeCotisationMois.valueChanges.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => this.updateConditionalValidators());
    const slug = this.route.snapshot.paramMap.get('slug');
    const authorized = slug
      ? this.auth.hasRole('MODIFIER_PRESTATION') === true
      : this.auth.hasRole('AJOUTER_PRESTATION') === true;
    if (!authorized) {
      void this.router.navigate(['dashboard', 'prestations']);
      return;
    }
    if (!slug) {
      this.loadAdherents('');
      return;
    }

    this.slug.set(slug);
    this.isLoading.set(true);
    this.service.get(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => this.fillForm(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors du chargement de la prestation.', error);
        this.errorMessages.set(['Impossible de charger la prestation à modifier.']);
      }
    });
  }

  onAdherentSearch(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    const term = event.target.value.trim();
    this.adherentSearchTerm.set(event.target.value);
    this.adherentSearchError.set('');
    this.selectedAdherent.set(null);
    this.form.controls.adherentSlug.setValue('');
    this.adherentSearchChanges.next(term);
  }

  selectAdherent(adherent: PrestationAdherentOption): void {
    this.selectedAdherent.set(adherent);
    this.adherentSearchTerm.set(`${adherent.nomComplet} · ${adherent.numeroInscription}`);
    this.form.controls.adherentSlug.setValue(adherent.slug);
    this.form.controls.adherentSlug.markAsTouched();
    this.adherents.set([]);
    this.adherentSearchError.set('');
  }

  onTypeChanged(): void {
    const type = this.form.controls.typePrestation.value;
    this.form.controls.dureeCotisationMois.setValue('');
    this.form.controls.montantCotisationsVersees.setValue('');
    if (true) this.form.controls.heuresHospitalisation.setValue('');
    if (type !== 'DECES') {
      this.form.controls.lienDefunt.setValue('');
      this.form.controls.nomDefunt.setValue('');
      this.form.controls.prenomDefunt.setValue('');
    }
    if (type === 'RETRAITE') {
      this.form.controls.typeBeneficiaire.setValue('ADHERENT');
      this.form.controls.nomBeneficiaire.setValue('');
      this.form.controls.prenomBeneficiaire.setValue('');
      this.form.controls.telephoneBeneficiaire.setValue('');
    }
    if (!this.requiresContributionDuration) {
      this.form.controls.dureeCotisationMois.setValue('');
      this.form.controls.montantCotisationsVersees.setValue('');
    }
    if (true
      && !(type === 'DECES_ADHERENT' || type === 'DECES')) {
      this.form.controls.montantCotisationsVersees.setValue('');
    }
    if ((this.form.controls.typeBeneficiaire.value === 'PARENTS' && !this.isDeathOfMember)
      || (this.form.controls.typeBeneficiaire.value === 'ADHERENT' && this.isDeathOfMember)) {
      this.form.controls.typeBeneficiaire.setValue('');
    }
    this.updateConditionalValidators();
  }

  onRelationChanged(): void {
    this.form.controls.nomDefunt.setValue('');
    this.form.controls.prenomDefunt.setValue('');
    if (!this.isDeathOfMember && this.form.controls.typeBeneficiaire.value === 'PARENTS') {
      this.form.controls.typeBeneficiaire.setValue('');
    }
    if (!this.isDeathOfMember) {
      this.form.controls.dureeCotisationMois.setValue('');
      this.form.controls.montantCotisationsVersees.setValue('');
    }
    if (this.isDeathOfMember && this.form.controls.typeBeneficiaire.value === 'ADHERENT') {
      this.form.controls.typeBeneficiaire.setValue('');
    }
    this.updateConditionalValidators();
  }

  onBeneficiaryChanged(): void {
    if (this.isRetirement) {
      this.form.controls.typeBeneficiaire.setValue('ADHERENT');
      this.form.controls.nomBeneficiaire.setValue('');
      this.form.controls.prenomBeneficiaire.setValue('');
      this.form.controls.telephoneBeneficiaire.setValue('');
      this.updateConditionalValidators();
      return;
    }
    if (!this.isDesignatedBeneficiary) {
      this.form.controls.nomBeneficiaire.setValue('');
      this.form.controls.prenomBeneficiaire.setValue('');
      this.form.controls.telephoneBeneficiaire.setValue('');
    }
    this.updateConditionalValidators();
  }

  onFileSelected(event: Event): void {
    if (!(event.target instanceof HTMLInputElement)) return;
    this.selectedFile.set(event.target.files?.[0] ?? null);
  }

  submit(): void {
    this.updateConditionalValidators();
    this.form.markAllAsTouched();
    this.errorMessages.set([]);
    this.successMessage.set('');
    if (this.form.invalid || this.isLoading() || this.isSubmitting()) return;

    const slug = this.slug();
    this.isSubmitting.set(true);
    const request = slug ? this.service.update(slug, this.toFormData()) : this.service.create(this.toFormData());
    request.pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (response: AppResponse<PrestationResponse>) => {
        if (!slug) {
          this.successMessage.set(response.message || 'La prestation a été enregistrée.');
          this.resetForm();
          return;
        }
        this.successMessage.set(response.message || 'La prestation a été modifiée.');
        this.existingFilename.set(response.donnees.resultat.nomFichierJustificatif);
        this.selectedFile.set(null);
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement du prestation.', error);
        this.errorMessages.set(this.extractErrors(error));
      }
    });
  }

  retourListe(): void {
    void this.router.navigate(['dashboard', 'prestations']);
  }

  private loadAdherents(term: string): void {
    this.isSearchingAdherents.set(true);
    this.service.searchAdherents(term).pipe(finalize(() => this.isSearchingAdherents.set(false))).subscribe({
      next: response => this.adherents.set(response.donnees.resultat),
      error: error => {
        console.error('Erreur lors de la recherche des adhérents pour un prestation.', error);
        this.adherentSearchError.set('Impossible de rechercher les adhérents.');
      }
    });
  }

  private fillForm(prestation: PrestationResponse): void {
    const adherent: PrestationAdherentOption = {
      slug: prestation.adherentSlug,
      nomComplet: prestation.adherentNomComplet,
      numeroInscription: prestation.adherentNumeroInscription,
      telephonePrincipal: ''
    };
    this.selectedAdherent.set(adherent);
    this.adherentSearchTerm.set(`${adherent.nomComplet} · ${adherent.numeroInscription}`);
    this.form.patchValue({
      adherentSlug: prestation.adherentSlug,
      typePrestation: prestation.typePrestation,
      datePrestation: String(prestation.datePrestation).slice(0, 10),
      heuresHospitalisation: '',
      lienDefunt: prestation.lienDefunt ?? '',
      nomDefunt: prestation.nomDefunt ?? '',
      prenomDefunt: prestation.prenomDefunt ?? '',
      typeBeneficiaire: prestation.typeBeneficiaire,
      nomBeneficiaire: prestation.nomBeneficiaire ?? '',
      prenomBeneficiaire: prestation.prenomBeneficiaire ?? '',
      telephoneBeneficiaire: prestation.telephoneBeneficiaire ?? '',
      dureeCotisationMois: prestation.dureeCotisationMois?.toString() ?? '',
      montantCotisationsVersees: prestation.montantCotisationsVersees?.toString() ?? '',
      observation: prestation.observation ?? ''
    });
    this.existingFilename.set(prestation.nomFichierJustificatif);
    this.adherents.set([]);
    this.updateConditionalValidators();
  }

  private updateConditionalValidators(): void {
    const type = this.form.controls.typePrestation.value;
    const relation = this.form.controls.lienDefunt.value;
    const beneficiary = this.form.controls.typeBeneficiaire.value;
    this.form.controls.heuresHospitalisation.setValidators(
      false ? [Validators.required, Validators.min(72)] : []
    );
    this.form.controls.lienDefunt.setValidators(type === 'DECES' ? [Validators.required] : []);
    const deceasedDetailsRequired = type === 'DECES' && !!relation && relation !== 'ADHERENT';
    this.form.controls.nomDefunt.setValidators(deceasedDetailsRequired ? [Validators.required, Validators.maxLength(100)] : []);
    this.form.controls.prenomDefunt.setValidators(deceasedDetailsRequired ? [Validators.required, Validators.maxLength(100)] : []);
    const designated = beneficiary === 'PERSONNE_DESIGNEE' && !this.isRetirement;
    this.form.controls.nomBeneficiaire.setValidators(designated ? [Validators.required, Validators.maxLength(100)] : []);
    this.form.controls.prenomBeneficiaire.setValidators(designated ? [Validators.required, Validators.maxLength(100)] : []);
    this.form.controls.telephoneBeneficiaire.setValidators(designated
      ? [Validators.required, Validators.maxLength(30)] : []);
    this.form.controls.dureeCotisationMois.setValidators(
      this.requiresContributionDuration ? [Validators.required, Validators.min(0)] : []
    );
    this.form.controls.montantCotisationsVersees.setValidators(
      this.requiresContributionAmount ? [Validators.required, Validators.min(0)] : []
    );
    for (const control of [
      this.form.controls.heuresHospitalisation, this.form.controls.lienDefunt, this.form.controls.nomDefunt,
      this.form.controls.prenomDefunt, this.form.controls.nomBeneficiaire, this.form.controls.prenomBeneficiaire,
      this.form.controls.telephoneBeneficiaire, this.form.controls.dureeCotisationMois,
      this.form.controls.montantCotisationsVersees
    ]) {
      control.updateValueAndValidity({emitEvent: false});
    }
  }

  private toFormData(): FormData {
    const value = this.form.getRawValue();
    const data = new FormData();
    data.append('adherentSlug', value.adherentSlug);
    data.append('typePrestation', value.typePrestation);
    data.append('datePrestation', value.datePrestation);
    data.append('typeBeneficiaire', this.isRetirement ? 'ADHERENT' : value.typeBeneficiaire);
    if (false && value.heuresHospitalisation) {
      data.append('heuresHospitalisation', value.heuresHospitalisation);
    }
    if (value.typePrestation === 'DECES' && value.lienDefunt) {
      data.append('lienDefunt', value.lienDefunt);
      if (value.nomDefunt) data.append('nomDefunt', value.nomDefunt);
      if (value.prenomDefunt) data.append('prenomDefunt', value.prenomDefunt);
    }
    if (value.typeBeneficiaire === 'PERSONNE_DESIGNEE') {
      data.append('nomBeneficiaire', value.nomBeneficiaire);
      data.append('prenomBeneficiaire', value.prenomBeneficiaire);
      data.append('telephoneBeneficiaire', value.telephoneBeneficiaire);
    }
    if (this.requiresContributionDuration && value.dureeCotisationMois !== '') {
      data.append('dureeCotisationMois', value.dureeCotisationMois);
    }
    if (this.requiresContributionAmount && value.montantCotisationsVersees !== '') {
      data.append('montantCotisationsVersees', value.montantCotisationsVersees);
    }
    data.append('observation', value.observation);
    const file = this.selectedFile();
    if (file) data.append('justificatif', file, file.name);
    return data;
  }

  private resetForm(): void {
    this.form.reset({
      adherentSlug: '', typePrestation: '', datePrestation: '', heuresHospitalisation: '', lienDefunt: '',
      nomDefunt: '', prenomDefunt: '', typeBeneficiaire: '', nomBeneficiaire: '',
      prenomBeneficiaire: '', telephoneBeneficiaire: '', dureeCotisationMois: '',
      montantCotisationsVersees: '', observation: ''
    });
    this.selectedAdherent.set(null);
    this.adherentSearchTerm.set('');
    this.adherents.set([]);
    this.selectedFile.set(null);
    this.existingFilename.set(null);
    if (this.fileInput) this.fileInput.nativeElement.value = '';
    this.updateConditionalValidators();
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
