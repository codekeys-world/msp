import { Component, DestroyRef, HostListener, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter, tap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthentificationService } from '../../authentification/services/authentification-service';
import { Sidebar } from '../sidebar/sidebar';
import { Navbar } from "../navbar/navbar";


@Component({
  selector: 'app-dashboard',
  imports: [Sidebar, RouterOutlet, Navbar],
  templateUrl: './dashboard.html',
  styleUrls: ['../dashboard.shared.scss','./dashboard.scss'],
  encapsulation : ViewEncapsulation.None
})
export class Dashboard implements OnInit {
  public authentificationService = inject(AuthentificationService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  sidebarCollapsed = false;
  isMobile = window.matchMedia('(max-width: 768px)').matches;
  mobileMenuOpen = false;

  @HostListener('window:resize')
  onResize(): void {
    this.isMobile = window.matchMedia('(max-width: 768px)').matches;
    if (!this.isMobile) this.mobileMenuOpen = false;
  }

  @HostListener('document:keydown.escape')
  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  ngOnInit(): void {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      tap((event: NavigationEnd) => {
        this.closeMobileMenu();
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  toggleSidebar(): void {
    if (this.isMobile) {
      this.mobileMenuOpen = !this.mobileMenuOpen;
    } else {
      this.sidebarCollapsed = !this.sidebarCollapsed;
    }
  }
}
