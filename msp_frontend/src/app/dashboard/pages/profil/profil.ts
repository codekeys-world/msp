import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {finalize} from 'rxjs';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {UtilisateurDetail} from '../models/utilisateur-detail';
import {ProfilService} from '../services/profil.service';

@Component({
  selector: 'app-profil',
  imports: [ReactiveFormsModule],
  templateUrl: './profil.html',
  styleUrl: './profil.scss',
})
export class Profil implements OnInit {
  private readonly profilService = inject(ProfilService);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  readonly profil = signal<UtilisateurDetail | null>(null);
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly isSavingPhoto = signal(false);
  readonly isSavingPassword = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly passwordError = signal<string | null>(null);
  readonly selectedPhoto = signal<File | null>(null);
  readonly photoPreview = signal<string | null>(null);
  readonly initials = computed(() => {
    const user = this.profil();
    return `${user?.prenom ?? ''} ${user?.nom ?? ''}`
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .map(part => part[0].toUpperCase())
      .join('');
  });
  readonly photoUrl = computed(() => {
    const user = this.profil();
    if (!user || !user.profileImage) return null;
    return `data:${user.profileImageType || 'image/jpeg'};base64,${user.profileImage}`;
  });

  readonly profilForm = new FormGroup({
    nom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    prenom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    email: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
    matricule: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    telephonePrincipal: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    telephoneSecondaire: new FormControl('', {nonNullable: true}),
    dateNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    lieuNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    adresse: new FormControl('', {nonNullable: true}),
    numeroInscription: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    pere: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    mere: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    genre: new FormControl('HOMME', {nonNullable: true, validators: [Validators.required]}),
    observations: new FormControl('', {nonNullable: true}),
  });

  readonly passwordForm = new FormGroup({
    ancienMotDePasse: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
    nouveauMotDePasse: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
    confirmationPassword: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
  });

  ngOnInit(): void {
    this.chargerProfil();
  }

  chargerProfil(): void {
    this.isLoading.set(true);
    this.profilService.getProfil().pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => {
        const user = response.donnees.resultat;
        this.profil.set(user);
        this.auth.updateCurrentUserProfile(`${user.prenom} ${user.nom}`, user.email);
        this.profilForm.patchValue({
          nom: user.nom,
          prenom: user.prenom,
          email: user.email,
          matricule: user.matricule,
          telephonePrincipal: user.telephonePrincipal,
          telephoneSecondaire: user.telephoneSecondaire ?? '',
          dateNaissance: user.dateNaissance,
          lieuNaissance: user.lieuNaissance,
          adresse: user.adresse ?? '',
          numeroInscription: user.numeroInscription,
          pere: user.pere,
          mere: user.mere,
          genre: user.genre,
          observations: user.observations ?? '',
        });
      },
      error: error => {
        console.error('Erreur lors du chargement du profil.', error);
        this.errorMessage.set(error?.error?.message ?? 'Impossible de charger votre profil.');
      },
    });
  }

  enregistrerProfil(): void {
    this.profilForm.markAllAsTouched();
    if (this.profilForm.invalid || this.isSaving()) return;
    this.errorMessage.set(null);
    this.isSaving.set(true);
    this.profilService.updateProfil(this.profilForm.getRawValue())
      .pipe(finalize(() => this.isSaving.set(false)))
      .subscribe({
        next: response => {
          this.profil.set(response.donnees.resultat);
          this.auth.updateCurrentUserProfile(response.donnees.resultat.prenom + ' ' + response.donnees.resultat.nom, response.donnees.resultat.email);
          this.sweetAlert.showSuccess(response.message || 'Votre profil a été mis à jour.');
        },
        error: error => {
          console.error('Erreur lors de la mise à jour du profil.', error);
          this.errorMessage.set(error?.error?.message ?? 'La mise à jour du profil a échoué.');
        },
      });
  }

  choisirPhoto(event: Event): void {
    const photo = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.selectedPhoto.set(photo);
    if (!photo) {
      this.photoPreview.set(null);
      return;
    }
    if (!photo.type.startsWith('image/')) {
      this.selectedPhoto.set(null);
      this.photoPreview.set(null);
      this.errorMessage.set('Veuillez sélectionner un fichier image.');
      return;
    }
    this.errorMessage.set(null);
    const reader = new FileReader();
    reader.onload = () => this.photoPreview.set(typeof reader.result === 'string' ? reader.result : null);
    reader.readAsDataURL(photo);
  }

  enregistrerPhoto(): void {
    const photo = this.selectedPhoto();
    if (!photo || this.isSavingPhoto()) return;
    this.isSavingPhoto.set(true);
    this.profilService.updatePhoto(photo).pipe(finalize(() => this.isSavingPhoto.set(false))).subscribe({
      next: response => {
        this.profil.set(response.donnees.resultat);
        this.selectedPhoto.set(null);
        this.photoPreview.set(null);
        this.sweetAlert.showSuccess(response.message || 'Votre photo a été mise à jour.');
      },
      error: error => {
        console.error('Erreur lors de la mise à jour de la photo de profil.', error);
        this.errorMessage.set(error?.error?.message ?? 'La mise à jour de la photo a échoué.');
      },
    });
  }

  changerMotDePasse(): void {
    this.passwordForm.markAllAsTouched();
    this.passwordError.set(null);
    if (this.passwordForm.invalid || this.isSavingPassword()) return;
    const {ancienMotDePasse, nouveauMotDePasse, confirmationPassword} = this.passwordForm.getRawValue();
    if (nouveauMotDePasse !== confirmationPassword) {
      this.passwordError.set('Le nouveau mot de passe et sa confirmation ne concordent pas.');
      return;
    }
    if (ancienMotDePasse === nouveauMotDePasse) {
      this.passwordError.set('Le nouveau mot de passe doit être différent du mot de passe actuel.');
      return;
    }
    this.isSavingPassword.set(true);
    this.profilService.updatePassword({ancienMotDePasse, nouveauMotDePasse, confirmationPassword})
      .pipe(finalize(() => this.isSavingPassword.set(false)))
      .subscribe({
        next: response => {
          this.passwordForm.reset();
          this.sweetAlert.showSuccess(response.message || 'Votre mot de passe a été mis à jour.');
        },
        error: error => {
          console.error('Erreur lors du changement de mot de passe.', error);
          this.passwordError.set(error?.error?.message ?? 'Le changement de mot de passe a échoué.');
        },
      });
  }

}
