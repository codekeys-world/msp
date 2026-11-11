import {Component, computed, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {finalize} from 'rxjs';
import {AdherentService} from '../services/adherent.service';
import {UtilisateurDetail} from '../models/utilisateur-detail';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {QrCodeUtil} from './qr-code.util';

export type CardTheme = 'classic' | 'navy' | 'prestige';
export type CardDisplayMode = 'both' | 'front' | 'back';

@Component({
  selector: 'app-carte-membre',
  imports: [CommonModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './carte-membre.html',
  styleUrl: './carte-membre.scss',
})
export class CarteMembre implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly adherentService = inject(AdherentService);
  private readonly auth = inject(AuthentificationService);
  private readonly sanitizer = inject(DomSanitizer);

  @ViewChild('cardFrontRef') cardFrontRef?: ElementRef<HTMLDivElement>;
  @ViewChild('cardBackRef') cardBackRef?: ElementRef<HTMLDivElement>;
  @ViewChild('printContainerRef') printContainerRef?: ElementRef<HTMLDivElement>;

  readonly adherent = signal<UtilisateurDetail | null>(null);
  readonly allAdherents = signal<UtilisateurDetail[]>([]);
  readonly filteredAdherents = signal<UtilisateurDetail[]>([]);
  readonly isLoading = signal(true);
  readonly isLoadingList = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searchAdherentQuery = signal('');

  // Options de confection
  readonly selectedTheme = signal<CardTheme>('classic');
  readonly displayMode = signal<CardDisplayMode>('both');
  readonly showQrCode = signal(true);
  readonly showOfficialSeal = signal(true);
  readonly showSignatures = signal(true);
  readonly customPhotoUrl = signal<string | null>(null);

  // Dates de validité de la carte
  dateDelivrance = this.formatDateInput(new Date());
  dateExpiration = this.formatDateInput(new Date(new Date().setFullYear(new Date().getFullYear() + 2)));

  // Autorisations
  readonly canManage = this.auth.canManageAdherents();

  readonly qrCodeSvg = computed<SafeHtml>(() => {
    const member = this.adherent();
    if (!member) return '';
    const payload = `MSP-MALI|MAT:${member.matricule}|NUM:${member.numeroInscription}|NOM:${member.nom}|PRENOM:${member.prenom}|DELIVR:${this.dateDelivrance}|EXP:${this.dateExpiration}`;
    const darkColor = this.selectedTheme() === 'prestige' ? '#143825' : (this.selectedTheme() === 'navy' ? '#0f2942' : '#0c4a2b');
    const svgStr = QrCodeUtil.generateSvg(payload, 110, darkColor, '#ffffff');
    return this.sanitizer.bypassSecurityTrustHtml(svgStr);
  });

  readonly memberPhoto = computed<string>(() => {
    const custom = this.customPhotoUrl();
    if (custom) return custom;
    const member = this.adherent();
    if (member?.profileImage) {
      const mime = member.profileImageType || 'image/jpeg';
      return `data:${mime};base64,${member.profileImage}`;
    }
    return '';
  });

  getAdherentPhoto(adherent: UtilisateurDetail): string {
    if (adherent.profileImage && adherent.profileImageType) {
      return `data:${adherent.profileImageType};base64,${adherent.profileImage}`;
    }
    return '';
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    this.loadAdherentsList(slug);
  }

  loadAdherentsList(preferredSlug: string | null = null): void {
    this.isLoadingList.set(true);
    this.adherentService.listAdherents(0, 100).pipe(
      finalize(() => {
        this.isLoadingList.set(false);
        this.isLoading.set(false);
      })
    ).subscribe({
      next: (res: any) => {
        const list: UtilisateurDetail[] = res.donnees.resultat.content || [];
        this.allAdherents.set(list);
        this.filteredAdherents.set(list);

        if (preferredSlug) {
          const found = list.find((a: UtilisateurDetail) => a.slug === preferredSlug);
          if (found) {
            this.selectAdherent(found);
          } else {
            this.loadSingleAdherent(preferredSlug);
          }
        } else if (list.length > 0) {
          this.selectAdherent(list[0]);
        }
      },
      error: (err: unknown) => {
        console.error("Erreur lors de la récupération des adhérents", err);
        if (preferredSlug) {
          this.loadSingleAdherent(preferredSlug);
        } else {
          this.errorMessage.set("Impossible de charger la liste des adhérents.");
        }
      }
    });
  }

  loadSingleAdherent(slug: string): void {
    this.isLoading.set(true);
    this.adherentService.getAdherent(slug).pipe(
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: res => {
        this.adherent.set(res.donnees.resultat);
        this.customPhotoUrl.set(null);
      },
      error: (err: unknown) => {
        console.error("Erreur lors du chargement de l'adhérent", err);
        this.errorMessage.set("Impossible de trouver le dossier de cet adhérent.");
      }
    });
  }

  selectAdherent(adherent: UtilisateurDetail): void {
    this.adherent.set(adherent);
    this.customPhotoUrl.set(null);
    this.errorMessage.set(null);
  }

  filterAdherents(): void {
    const q = this.searchAdherentQuery().trim().toLowerCase();
    if (!q) {
      this.filteredAdherents.set(this.allAdherents());
      return;
    }
    const filtered = this.allAdherents().filter(a =>
      (a.nom && a.nom.toLowerCase().includes(q)) ||
      (a.prenom && a.prenom.toLowerCase().includes(q)) ||
      (a.matricule && a.matricule.toLowerCase().includes(q)) ||
      (a.numeroInscription && a.numeroInscription.toLowerCase().includes(q))
    );
    this.filteredAdherents.set(filtered);
  }

  onPhotoUploaded(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = e => {
        this.customPhotoUrl.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  resetPhoto(): void {
    this.customPhotoUrl.set(null);
  }

  setTheme(theme: CardTheme): void {
    this.selectedTheme.set(theme);
  }

  setDisplayMode(mode: CardDisplayMode): void {
    this.displayMode.set(mode);
  }

  imprimerCarte(): void {
    if (!this.adherent() || !this.printContainerRef) {
      return;
    }

    // Récupère les styles calculés du conteneur des badges
    const badgesElement = this.printContainerRef.nativeElement;
    const computedStyles = window.getComputedStyle(badgesElement);

    // Clone le conteneur des badges pour l'impression
    const printContent = badgesElement.cloneNode(true) as HTMLElement;
    
    // Applique le thème sur le clone
    printContent.setAttribute('data-theme', this.selectedTheme());

    // Crée une nouvelle fenêtre pour l'impression
    const printWindow = window.open('', '_blank', 'width=900,height=600');
    if (!printWindow) {
      alert('Veuillez autoriser les pop-ups pour imprimer la carte.');
      return;
    }

    // Récupère tous les styles CSS du document principal
    const styles = this.getCssStyles();

    // Écrit le contenu HTML dans la nouvelle fenêtre
    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Carte Membre MSP - ${this.adherent()!.nom} ${this.adherent()!.prenom}</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
        <style>
          ${styles}
        </style>
      </head>
      <body>
        <div class="badges-layout ${this.displayMode() === 'front' ? 'show-front-only' : this.displayMode() === 'back' ? 'show-back-only' : ''}">
          ${printContent.innerHTML}
        </div>
      </body>
      </html>
    `);

    printWindow.document.close();

    // Attend le chargement des images puis imprime
    setTimeout(() => {
      printWindow.print();
      printWindow.close();
    }, 500);
  }

  private getCssStyles(): string {
    let cssText = '';
    
    // Récupère tous les styles des sheets du document
    for (const sheet of document.styleSheets) {
      try {
        const rules = sheet.cssRules || sheet.rules;
        if (rules) {
          for (const rule of rules) {
            // Inclut seulement les règles pertinentes pour les badges
            const ruleText = rule.cssText;
            if (ruleText.includes('id-card') || 
                ruleText.includes('card-') || 
                ruleText.includes('badge') ||
                ruleText.includes('national-stripe') ||
                ruleText.includes('card-header') ||
                ruleText.includes('card-badge') ||
                ruleText.includes('card-front') ||
                ruleText.includes('card-back') ||
                ruleText.includes('card-photo') ||
                ruleText.includes('card-chip') ||
                ruleText.includes('card-info') ||
                ruleText.includes('card-validity') ||
                ruleText.includes('card-front-footer') ||
                ruleText.includes('card-mag-stripe') ||
                ruleText.includes('sig-block') ||
                ruleText.includes('official-seal') ||
                ruleText.includes('security-hologram') ||
                ruleText.includes('info-row') ||
                ruleText.includes('info-grid') ||
                ruleText.includes('legal-notice') ||
                ruleText.includes('back-footer') ||
                ruleText.includes('qr-code') ||
                ruleText.includes('contacts-box') ||
                ruleText.includes('card-back-footer') ||
                ruleText.includes('serial-number') ||
                ruleText.includes('barcode-visual') ||
                ruleText.includes('entity-') ||
                ruleText.includes('republic-') ||
                ruleText.includes('badges-layout') ||
                ruleText.includes('id-card-wrapper') ||
                ruleText.includes('card-side-label') ||
                ruleText.includes('data-theme')) {
              cssText += ruleText + '\n';
            }
          }
        }
      } catch (e) {
        // Ignore les erreurs CORS pour les sheets externes
      }
    }

    // Ajoute les styles de base
    const baseStyles = `
      * { margin: 0; padding: 0; box-sizing: border-box; }
      body { 
        background: #fff; 
        display: flex; 
        justify-content: center; 
        align-items: flex-start;
        padding: 10mm; 
        font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
      }
      .badges-layout {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10mm;
      }
      .badges-layout.show-front-only .id-card-wrapper:nth-child(n+2) {
        display: none;
      }
      .badges-layout.show-back-only .id-card-wrapper:first-child {
        display: none;
      }
      .id-card-wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.6rem;
      }
      .card-side-label {
        font-size: 0.78rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        color: #5c7468;
        text-transform: uppercase;
      }
      @media print {
        body { padding: 0; }
        .badges-layout {
          flex-direction: row !important;
          flex-wrap: wrap !important;
          gap: 8mm !important;
          justify-content: center !important;
        }
        .id-card-badge {
          width: 85.6mm !important;
          height: 53.98mm !important;
          border-radius: 3.18mm !important;
          border: 0.2mm solid #cccccc !important;
          box-shadow: none !important;
          -webkit-print-color-adjust: exact !important;
          print-color-adjust: exact !important;
          page-break-inside: avoid !important;
          break-inside: avoid !important;
        }
      }
    `;

    return baseStyles + cssText;
  }

  private formatDateInput(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
