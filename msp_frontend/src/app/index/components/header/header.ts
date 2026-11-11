import { Component, inject } from '@angular/core';
import { RouterLink } from "@angular/router";
import { AuthentificationService } from '../../../authentification/services/authentification-service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private auth = inject(AuthentificationService);

  isLoggedIn() {
    return this.auth.getCurrentUser() !== null;
  }

  onLogout() {
    this.auth.logout().subscribe({
      next: () => {
        window.location.href = '/';
      },
      error: (_error: unknown) => {
        this.auth.clearSession();
        window.location.href = '/';
      }
    });
  }
}
