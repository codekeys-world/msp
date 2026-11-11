import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {finalize, Observable} from 'rxjs';
import {AdherentService} from '../services/adherent.service';
import {PersonneFamille, UtilisateurDetail} from '../models/utilisateur-detail';
import {AppResponse} from '../../../app-response';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

@Component({
  selector: 'app-adherent-detail',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './adherent-detail.html',
  styleUrl: './adherent-detail.scss',
})
export class AdherentDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(AdherentService);
  private readonly auth = inject(AuthentificationService);
  private readonly sweetAlert = inject(SweetAlertService);
  readonly adherent = signal<UtilisateurDetail | null>(null);
  readonly selectedTab = signal<'conjoints' | 'descendants'>('conjoints');
  readonly isLoading = signal(true);
  readonly isDeleting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly familyEditor = signal<'conjoints' | 'descendants' | null>(null);
  readonly familyEditingSlug = signal<string | null>(null);
  readonly isSavingFamily = signal(false);
  readonly canManage = this.auth.canManageAdherents();
  readonly canManageAccess = this.auth.canManageUserAccess();
  readonly canConfectionCard = this.auth.canConfectionCard();
  readonly accessOptions = signal<{roles: {slug: string; nom: string}[]; authorisations: {id: number; nom: string}[]} | null>(null);
  readonly isEditingAccess = signal(false);
  readonly isSavingAccess = signal(false);
  readonly selectedRoleSlug = signal('');
  readonly selectedAuthorisationIds = signal<number[]>([]);
  readonly photoUrl = signal<string | null>(null);
  readonly familyForm = new FormGroup({
    nom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    prenom: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    dateNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    lieuNaissance: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    genre: new FormControl('HOMME', {nonNullable: true, validators: [Validators.required]}),
    adresse: new FormControl('', {nonNullable: true}),
    telephonePrincipal: new FormControl('', {nonNullable: true}),
    telephoneSecondaire: new FormControl('', {nonNullable: true}),
    acteDeMariage: new FormControl<File | null>(null),
    acteDeNaissance: new FormControl<File | null>(null),
    certificatVieCollectif: new FormControl<File | null>(null),
  });

  ngOnInit(): void {
    if (!this.auth.canViewAdherentDetail()) {
      this.router.navigate(['/dashboard/home']);
      return;
    }
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.errorMessage.set("L'identifiant de l'adhérent est manquant.");
      this.isLoading.set(false);
      return;
    }
    this.service.getAdherent(slug).pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: response => {
        this.adherent.set(response.donnees.resultat);
        this.selectedAuthorisationIds.set([]);
        // Charger la photo de profil si disponible
        const adherentData = response.donnees.resultat;
        if (adherentData.profileImage && adherentData.profileImageType) {
          const dataUrl = `data:${adherentData.profileImageType};base64,${adherentData.profileImage}`;
          this.photoUrl.set(dataUrl);
        }
        if (this.canManageAccess) this.service.getAccessOptions().subscribe({
          next: options => {
            this.accessOptions.set(options.donnees.resultat);
            const current = options.donnees.resultat.roles.find(role => role.nom === response.donnees.resultat.role);
            this.selectedRoleSlug.set(current?.slug ?? '');
          },
          error: error => console.error('Impossible de charger les rôles et autorisations.', error)
        });
      },
      error: error => {
        console.error("Erreur lors du chargement du dossier de l'adhérent.", error);
        this.errorMessage.set("Impossible de charger le dossier de l'adhérent.");
      }
    });
  }

  demarrerEditionAcces(): void {
    const member = this.adherent();
    const options = this.accessOptions();
    if (!member || !options) return;
    this.selectedRoleSlug.set(options.roles.find(role => role.nom === member.role)?.slug ?? '');
    this.selectedAuthorisationIds.set(options.authorisations.filter(auth => member.authorisations.includes(auth.nom)).map(auth => auth.id));
    this.isEditingAccess.set(true);
  }

  toggleAuthorisation(id: number, event: Event): void {
    const checked = event.target instanceof HTMLInputElement && event.target.checked;
    this.selectedAuthorisationIds.update(ids => checked ? [...new Set([...ids, id])] : ids.filter(value => value !== id));
  }

  enregistrerAcces(): void {
    const member = this.adherent();
    const roleSlug = this.selectedRoleSlug();
    if (!member || !roleSlug || this.isSavingAccess()) return;
    this.isSavingAccess.set(true);
    this.service.updateUserAccess(member.slug, roleSlug, this.selectedAuthorisationIds()).pipe(finalize(() => this.isSavingAccess.set(false))).subscribe({
      next: response => {
        this.adherent.set(response.donnees.resultat);
        this.isEditingAccess.set(false);
        this.sweetAlert.showSuccess(response.message || 'Les droits de l’utilisateur ont été mis à jour.');
      },
      error: error => this.sweetAlert.showError(error?.error?.message || 'Impossible de mettre à jour les droits de cet utilisateur.')
    });
  }

  ouvrirAjoutFamille(type: 'conjoints' | 'descendants'): void {
    this.familyEditingSlug.set(null);
    this.familyEditor.set(type);
    this.familyForm.reset({
      nom: '', prenom: '', dateNaissance: '', lieuNaissance: '',
      genre: type === 'conjoints'
        ? (this.adherent()?.genre === 'HOMME' ? 'FEMME' : 'HOMME')
        : 'HOMME',
      adresse: '', telephonePrincipal: '', telephoneSecondaire: '',
      acteDeMariage: null, acteDeNaissance: null, certificatVieCollectif: null,
    });
    this.familyForm.controls.genre.enable();
    if (type === 'conjoints') this.familyForm.controls.genre.disable();
    this.configureFamilyValidators(type, true, false, false, false);
  }

  ouvrirModificationFamille(type: 'conjoints' | 'descendants', person: PersonneFamille): void {
    this.familyEditingSlug.set(person.slug);
    this.familyEditor.set(type);
    this.familyForm.reset({
      nom: person.nom, prenom: person.prenom,
      dateNaissance: person.dateNaissance ?? '', lieuNaissance: person.lieuNaissance ?? '',
      genre: person.genre ?? 'HOMME', adresse: person.adresse ?? '',
      telephonePrincipal: person.telephonePrincipal ?? '', telephoneSecondaire: person.telephoneSecondaire ?? '',
      acteDeMariage: null, acteDeNaissance: null, certificatVieCollectif: null,
    });
    this.familyForm.controls.genre.enable();
    if (type === 'conjoints') this.familyForm.controls.genre.disable();
    this.configureFamilyValidators(
      type,
      false,
      type === 'conjoints' && !person.nomActeMariage,
      type === 'descendants' && !person.nomActeNaissance,
      type === 'descendants' && !person.nomCertificatVieCollectif
    );
  }

  choisirDocumentFamille(event: Event, champ: 'acteDeMariage' | 'acteDeNaissance' | 'certificatVieCollectif'): void {
    const input = event.target;
    if (input instanceof HTMLInputElement) {
      this.familyForm.controls[champ].setValue(input.files?.[0] ?? null);
      this.familyForm.controls[champ].markAsTouched();
    }
  }

  async enregistrerFamille(): Promise<void> {
    const adherent = this.adherent();
    const type = this.familyEditor();
    if (!adherent || !type || this.isSavingFamily()) return;
    this.familyForm.markAllAsTouched();
    if (this.familyForm.invalid) return;

    const values = this.familyForm.getRawValue();
    const payload = {
      nom: values.nom,
      prenom: values.prenom,
      dateNaissance: values.dateNaissance,
      lieuNaissance: values.lieuNaissance,
      genre: values.genre,
      adresse: values.adresse,
      telephonePrincipal: values.telephonePrincipal,
      telephoneSecondaire: values.telephoneSecondaire,
    };
    const request = new FormData();
    request.append('request', new Blob([JSON.stringify(payload)], {type: 'application/json'}));
    if (values.acteDeMariage) request.append('acteDeMariage', values.acteDeMariage);
    if (values.acteDeNaissance) request.append('acteDeNaissance', values.acteDeNaissance);
    if (values.certificatVieCollectif) request.append('certificatVieCollectif', values.certificatVieCollectif);

    const slug = this.familyEditingSlug();
    let operation: Observable<AppResponse<PersonneFamille>>;
    if (type === 'conjoints') {
      operation = slug
        ? this.service.updateConjoint(adherent.slug, slug, request)
        : this.service.createConjoint(adherent.slug, request);
    } else {
      operation = slug
        ? this.service.updateDescendant(adherent.slug, slug, request)
        : this.service.createDescendant(adherent.slug, request);
    }

    this.isSavingFamily.set(true);
    operation.pipe(finalize(() => this.isSavingFamily.set(false))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Les informations familiales ont été enregistrées.');
        this.familyEditor.set(null);
        this.familyEditingSlug.set(null);
        this.rechargerDossier(adherent.slug);
      },
      error: error => {
        console.error('Erreur lors de l’enregistrement des informations familiales.', error);
        this.sweetAlert.showError(error?.error?.message || 'Impossible d’enregistrer ces informations familiales.');
      }
    });
  }

  async supprimerFamille(type: 'conjoints' | 'descendants', person: PersonneFamille): Promise<void> {
    const adherent = this.adherent();
    if (!adherent || !this.canManage) return;
    const label = type === 'conjoints' ? 'le conjoint' : 'le descendant';
    const confirmed = await this.sweetAlert.confirmDeletion(label, `${person.prenom} ${person.nom}`,
      'Les documents justificatifs associés seront également supprimés.');
    if (!confirmed) return;

    const operation = type === 'conjoints'
      ? this.service.deleteConjoint(adherent.slug, person.slug)
      : this.service.deleteDescendant(adherent.slug, person.slug);
    operation.subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'Les informations familiales ont été supprimées.');
        this.rechargerDossier(adherent.slug);
      },
      error: error => {
        console.error('Erreur lors de la suppression des informations familiales.', error);
        this.sweetAlert.showError('Impossible de supprimer ces informations familiales.');
      }
    });
  }

  annulerEditionFamille(): void {
    this.familyEditor.set(null);
    this.familyEditingSlug.set(null);
  }

  get familleSelectionnee(): PersonneFamille[] {
    const adherent = this.adherent();
    return !adherent ? [] : this.selectedTab() === 'conjoints' ? adherent.conjoints : adherent.descendants;
  }

  get peutAjouterConjoint(): boolean {
    const adherent = this.adherent();
    return !!adherent && (adherent.genre === 'HOMME' ? adherent.conjoints.length < 4 : adherent.conjoints.length < 1);
  }

  async supprimer(): Promise<void> {
    const adherent = this.adherent();
    if (!adherent || !this.canManage || this.isDeleting()) return;
    const confirmed = await this.sweetAlert.confirmDeletion(
      'l’adhérent',
      `${adherent.prenom} ${adherent.nom}`,
      'Ses conjoints, descendants et documents associés seront également supprimés.'
    );
    if (!confirmed) return;

    this.isDeleting.set(true);
    this.service.deleteAdherent(adherent.slug).pipe(finalize(() => this.isDeleting.set(false))).subscribe({
      next: response => {
        this.sweetAlert.showSuccess(response.message || 'L’adhérent a été supprimé.');
        void this.router.navigate(['dashboard', 'adherents']);
      },
      error: error => {
        console.error("Erreur lors de la suppression de l'adhérent.", error);
        this.sweetAlert.showError("Impossible de supprimer le dossier de l'adhérent.");
      }
    });
  }

  private configureFamilyValidators(
    type: 'conjoints' | 'descendants',
    newRecord: boolean,
    requireMarriageCertificate: boolean,
    requireBirthCertificate: boolean,
    requireLifeCertificate: boolean
  ): void {
    const spouse = type === 'conjoints';
    this.familyForm.controls.telephonePrincipal.setValidators(
      spouse ? [Validators.required] : []
    );
    this.familyForm.controls.acteDeMariage.setValidators(
      spouse && (newRecord || requireMarriageCertificate) ? [Validators.required] : []
    );
    this.familyForm.controls.acteDeNaissance.setValidators(
      !spouse && (newRecord || requireBirthCertificate) ? [Validators.required] : []
    );
    this.familyForm.controls.certificatVieCollectif.setValidators(
      !spouse && (newRecord || requireLifeCertificate) ? [Validators.required] : []
    );
    Object.values(this.familyForm.controls).forEach(control => control.updateValueAndValidity());
  }

  private rechargerDossier(slug: string): void {
    this.service.getAdherent(slug).subscribe({
      next: response => {
        const data = response.donnees.resultat;
        this.adherent.set(data);
        if (data.profileImage && data.profileImageType) {
          this.photoUrl.set(`data:${data.profileImageType};base64,${data.profileImage}`);
        } else {
          this.photoUrl.set(null);
        }
      },
      error: error => {
        console.error("Erreur lors du rechargement du dossier de l'adhérent.", error);
        this.errorMessage.set("Impossible de recharger le dossier de l'adhérent.");
      }
    });
  }
}
