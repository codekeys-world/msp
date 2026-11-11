import {Component, inject, OnInit, signal} from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {finalize} from 'rxjs';
import {AdherentService} from '../services/adherent.service';
import {UtilisateurDetail} from '../models/utilisateur-detail';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

type FamilyForm = FormGroup;

@Component({
  selector: 'app-adherent-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './adherent-form.html',
  styleUrl: './adherent-form.scss',
})
export class AdherentForm implements OnInit {
  private readonly adherentService = inject(AdherentService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sweetAlert = inject(SweetAlertService);
  readonly isSaving = signal(false);
  readonly isEditing = signal(false);
  readonly showValidationErrors = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly photoPreview = signal<string | null>(null);
  readonly adherentSlug = signal<string | null>(null);

  readonly adherentForm = new FormGroup({
    nom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    prenom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    dateNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    lieuNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    genre: new FormControl('HOMME', {nonNullable: true, validators: [Validators.required]}),
    adresse: new FormControl('', {nonNullable: true}),
    matricule: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    numeroAdhesion: new FormControl('', {nonNullable: true}),
    email: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
    telephonePrincipal: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    telephoneSecondaire: new FormControl('', {nonNullable: true}),
    pere: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    mere: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    observations: new FormControl('', {nonNullable: true}),
    conjoints: new FormArray<FamilyForm>([]),
    descendants: new FormArray<FamilyForm>([]),
  });
  private profileFile: File | null = null;

  get conjoints(): FormArray<FamilyForm> {
    return this.adherentForm.controls.conjoints;
  }

  get descendants(): FormArray<FamilyForm> {
    return this.adherentForm.controls.descendants;
  }

  get maxConjoints(): number {
    return this.adherentForm.controls.genre.value === 'HOMME' ? 4 : 1;
  }

  get peutAjouterConjoint(): boolean {
    return this.conjoints.length < this.maxConjoints;
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.isEditing.set(true);
      this.adherentSlug.set(slug);
      this.adherentService.getAdherent(slug).subscribe({
        next: response => {
          const adherent = response.donnees.resultat;
          this.remplirFormulaire(adherent);
        },
        error: error => {
          console.error("Erreur lors du chargement de l'adhérent.", error);
          this.errorMessage.set("Impossible de charger les informations de l'adhérent.");
        }
      });
    }
  }

  ajouterConjoint(): void {
    if (!this.peutAjouterConjoint) return;
    const genreConjoint = this.adherentForm.controls.genre.value === 'HOMME' ? 'FEMME' : 'HOMME';
    this.conjoints.push(new FormGroup({
      nom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      prenom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      dateNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      lieuNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      genre: new FormControl(genreConjoint, {nonNullable: true, validators: [Validators.required]}),
      adresse: new FormControl('', {nonNullable: true}),
      telephonePrincipal: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      telephoneSecondaire: new FormControl('', {nonNullable: true}),
      acteDeMariage: new FormControl<File | null>(null, {validators: [Validators.required]}),
    }));
  }

  ajouterDescendant(): void {
    this.descendants.push(new FormGroup({
      nom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      prenom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      dateNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      lieuNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      genre: new FormControl('HOMME', {nonNullable: true, validators: [Validators.required]}),
      adresse: new FormControl('', {nonNullable: true}),
      telephonePrincipal: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      telephoneSecondaire: new FormControl('', {nonNullable: true}),
      acteDeNaissance: new FormControl<File | null>(null, {validators: [Validators.required]}),
      certificatVieCollectif: new FormControl<File | null>(null, {validators: [Validators.required]}),
    }));
  }

  retirerConjoint(index: number): void {
    this.conjoints.removeAt(index);
  }

  retirerDescendant(index: number): void {
    this.descendants.removeAt(index);
  }

  changerGenre(): void {
    while (this.conjoints.length > this.maxConjoints) this.conjoints.removeAt(this.conjoints.length - 1);
    const genreConjoint = this.adherentForm.controls.genre.value === 'HOMME' ? 'FEMME' : 'HOMME';
    this.conjoints.controls.forEach(conjoint => conjoint.get('genre')?.setValue(genreConjoint));
  }

  onProfileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.profileFile = file;
    if (!file) {
      this.photoPreview.set(null);
      return;
    }
    const reader = new FileReader();
    reader.onload = () => this.photoPreview.set(typeof reader.result === 'string' ? reader.result : null);
    reader.readAsDataURL(file);
  }

  onFamilyFileSelected(event: Event, group: FamilyForm, controlName: string): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    group.get(controlName)?.setValue(file);
  }

  submitAdherent(): void {
    this.adherentForm.markAllAsTouched();
    if (this.adherentForm.invalid) {
      this.showValidationErrors.set(true);
      return;
    }
    if (this.isSaving()) return;
    this.showValidationErrors.set(false);
    this.errorMessage.set(null);
    this.isSaving.set(true);

    const formValue = this.adherentForm.getRawValue();
    const conjoints = this.isEditing() ? [] : formValue.conjoints;
    const descendants = this.isEditing() ? [] : formValue.descendants;
    const payload = {
      adherent: {
        nom: formValue.nom,
        prenom: formValue.prenom,
        dateNaissance: formValue.dateNaissance,
        lieuNaissance: formValue.lieuNaissance,
        genre: formValue.genre,
        adresse: formValue.adresse,
        matricule: formValue.matricule,
        numeroAdhesion: formValue.numeroAdhesion || null,
        email: formValue.email,
        telephonePrincipal: formValue.telephonePrincipal,
        telephoneSecondaire: formValue.telephoneSecondaire,
        pere: formValue.pere,
        mere: formValue.mere,
        observations: formValue.observations,
        numeroInscription: formValue.matricule,
      },
      conjoints: conjoints.map(person => ({
        nom: person['nom'],
        prenom: person['prenom'],
        dateNaissance: person['dateNaissance'],
        lieuNaissance: person['lieuNaissance'],
        genre: person['genre'],
        adresse: person['adresse'],
        telephonePrincipal: person['telephonePrincipal'],
        telephoneSecondaire: person['telephoneSecondaire'],
      })),
      descendants: descendants.map(person => ({
        nom: person['nom'],
        prenom: person['prenom'],
        dateNaissance: person['dateNaissance'],
        lieuNaissance: person['lieuNaissance'],
        genre: person['genre'],
        adresse: person['adresse'],
        telephonePrincipal: person['telephonePrincipal'],
        telephoneSecondaire: person['telephoneSecondaire'],
      })),
    };
    const request = new FormData();
    request.append('request', new Blob([JSON.stringify(payload)], {type: 'application/json'}));
    if (this.profileFile) request.append('profileImage', this.profileFile);
    conjoints.forEach((person, index) => {
      const acte = person['acteDeMariage'];
      if (acte instanceof File) request.append(`conjointActe-${index}`, acte);
    });
    descendants.forEach((person, index) => {
      if (person['acteDeNaissance'] instanceof File) request.append(`descendantActe-${index}`, person['acteDeNaissance']);
      if (person['certificatVieCollectif'] instanceof File) request.append(`descendantCertificat-${index}`, person['certificatVieCollectif']);
    });

    const operation = this.isEditing()
      ? this.adherentService.updateAdherent(this.adherentSlug()!, request)
      : this.adherentService.createAdherent(request);
    operation.pipe(finalize(() => this.isSaving.set(false))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || "L'adhérent a été enregistré.");
        void this.router.navigate(['dashboard', 'adherents']);
      },
      error: error => {
        console.error("Erreur lors de l'enregistrement de l'adhérent.", error);
        this.errorMessage.set(error?.error?.message || "L'enregistrement de l'adhérent a échoué.");
      }
    });
  }

  private remplirFormulaire(adherent: UtilisateurDetail): void {
    this.adherentForm.patchValue({
      nom: adherent.nom,
      prenom: adherent.prenom,
      dateNaissance: adherent.dateNaissance,
      lieuNaissance: adherent.lieuNaissance,
      genre: adherent.genre,
      adresse: adherent.adresse ?? '',
      matricule: adherent.matricule,
      numeroAdhesion: adherent.numeroAdhesion ?? '',
      email: adherent.email,
      telephonePrincipal: adherent.telephonePrincipal,
      telephoneSecondaire: adherent.telephoneSecondaire ?? '',
      pere: adherent.pere,
      mere: adherent.mere,
      observations: adherent.observations ?? '',
    });
    // Charger la photo de profil existante si disponible
    if (adherent.profileImage && adherent.profileImageType) {
      const bytes = Uint8Array.from(adherent.profileImage);
      const blob = new Blob([bytes], {type: adherent.profileImageType});
      const url = URL.createObjectURL(blob);
      this.photoPreview.set(url);
    }
  }
}
