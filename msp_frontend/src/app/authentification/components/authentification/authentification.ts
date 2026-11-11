import { Component, inject, signal} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from "@angular/router";
import { AuthentificationService } from '../../services/authentification-service';
import { AppResponse } from '../../../app-response';
import { AuthentificationResponse } from '../../models/authentification-response';

@Component({
  selector: 'app-authentification',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './authentification.html',
  styleUrl: './authentification.scss',
})
export class Authentification {
  authentificationService = inject(AuthentificationService);
  router = inject(Router);
  errorMessage = signal<Array<string>>([]);
  submiting = signal<boolean>(false);
  showForgotPassword = signal(false);
  forgotEmail = new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]});
  forgotMessage = signal('');
  forgotLoading = signal(false);

  sendResetLink(): void {
    this.forgotEmail.markAsTouched();
    if (this.forgotEmail.invalid) return;
    this.forgotLoading.set(true);
    this.authentificationService.demanderReinitialisation(this.forgotEmail.value).subscribe({
      next: response => { this.forgotMessage.set(response.message || 'Si cette adresse est associée à un compte, un lien sera envoyé.'); this.forgotLoading.set(false); },
      error: () => { this.forgotMessage.set('Impossible d’envoyer le lien. Vérifiez la configuration du serveur mail.'); this.forgotLoading.set(false); }
    });
  }

  loginForm = new FormGroup({
    username : new FormControl('', {nonNullable : true, validators: [Validators.required]}),
    password : new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(6)]})
  });

  onSubmitLogin(){
    this.submiting.set(true);
    this.loginForm.markAllAsTouched();
    if(this.loginForm.invalid) {
      this.submiting.set(false);
      return;
    }
    const authentificationRequest = this.loginForm.getRawValue();
    this.authentificationService.login(authentificationRequest).subscribe({
      next:(resultat : AppResponse<AuthentificationResponse>)=>{
        this.router.navigate(['dashboard', 'home']);
        this.submiting.set(false);
        //Redirection sur tableau de bord
      },
      error:(error: unknown)=>{
        console.error('Erreur lors de la connexion.', error);
        this.loginForm.controls.password.patchValue('');
        const apiError = typeof error === 'object' && error !== null && 'error' in error
          ? error.error
          : null;
        if (typeof apiError === 'object' && apiError !== null && 'erreurs' in apiError && Array.isArray(apiError.erreurs)) {
          this.errorMessage.set(apiError.erreurs.map(String));
        } else if (typeof apiError === 'object' && apiError !== null && 'message' in apiError && typeof apiError.message === 'string') {
          this.errorMessage.set([apiError.message]);
        } else {
          this.errorMessage.set(['Échec de la connexion. Vérifiez vos identifiants et réessayez.']);
        }
        this.submiting.set(false);
      }
    });
  }
}
