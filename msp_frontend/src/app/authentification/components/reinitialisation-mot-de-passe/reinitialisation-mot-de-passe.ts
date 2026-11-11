import {Component, inject, signal} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthentificationService} from '../../services/authentification-service';

@Component({
  selector: 'app-reinitialisation-mot-de-passe',
  imports: [ReactiveFormsModule, RouterLink],
  template: `<main class="container py-5" style="max-width:34rem"><h1>Choisir un nouveau mot de passe</h1>
    @if (message()) { <div class="alert position-relative pe-5" [class.alert-success]="success()" [class.alert-danger]="!success()" [attr.role]="success() ? 'status' : 'alert'">
      <button type="button" class="btn-close position-absolute top-0 end-0 m-2" data-bs-dismiss="alert" aria-label="Fermer l’alerte"></button>
      {{message()}}
    </div> }
    <form [formGroup]="form" (ngSubmit)="submit()" class="vstack gap-3">
      <label>Nouveau mot de passe<input class="form-control" type="password" formControlName="password" autocomplete="new-password"></label>
      <label>Confirmer le mot de passe<input class="form-control" type="password" formControlName="confirmationPassword" autocomplete="new-password"></label>
      <button class="btn btn-success" [disabled]="form.invalid || loading()">Réinitialiser</button>
    </form><a routerLink="/authentification" class="d-inline-block mt-3">Retour à la connexion</a></main>`
})
export class ReinitialisationMotDePasse {
  private readonly auth = inject(AuthentificationService);
  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';
  readonly loading = signal(false);
  readonly message = signal('');
  readonly success = signal(false);
  readonly form = new FormGroup({
    password: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
    confirmationPassword: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
  });
  submit(): void {
    if (!this.token) { this.message.set('Le lien de réinitialisation est invalide.'); return; }
    if (this.form.invalid) return;
    this.loading.set(true);
    const {password, confirmationPassword} = this.form.getRawValue();
    this.auth.reinitialiserMotDePasse(this.token, password, confirmationPassword).subscribe({
      next: response => { this.success.set(true); this.message.set(response.message || 'Mot de passe réinitialisé.'); this.loading.set(false); },
      error: error => { this.success.set(false); this.message.set(error?.error?.message || 'Le lien est invalide ou expiré.'); this.loading.set(false); }
    });
  }
}
