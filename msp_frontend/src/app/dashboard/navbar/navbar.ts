import { Component, computed, EventEmitter, inject, Output } from '@angular/core';
import { AuthentificationService } from '../../authentification/services/authentification-service';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {

  @Output()
  toggleSidebar = new EventEmitter<void>();

  public authentificationService = inject(AuthentificationService);
  router = inject(Router);

  fullName = computed(() => this.authentificationService.authenticatedUser()?.nom);
  roleName = this.authentificationService.getRoleName();
  initialeName = computed(() => this.authentificationService.getInitiale(this.fullName()));

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  onProfileClick(event: Event): void {
    event.preventDefault();
    void this.router.navigate(['/dashboard/profil']);
  }

  onLogout() {
    this.authentificationService.logout().pipe(
      tap(response => this.router.navigate(['/authentification']))
    ).subscribe();
  }
}
