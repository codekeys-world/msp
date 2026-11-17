import {Injectable, signal} from '@angular/core';

const STORAGE_KEY = 'msp-cookie-consent-v1';
type Consent = {optional: boolean; updatedAt: number};

@Injectable({providedIn: 'root'})
export class CookieConsentService {
  readonly choice = signal<Consent | null>(this.restore());
  readonly open = signal(this.choice() === null);

  save(optional: boolean): void {
    const choice = {optional, updatedAt: Date.now()};
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(choice)); } catch { /* Private browsing may deny storage. */ }
    this.choice.set(choice);
    this.open.set(false);
  }

  private restore(): Consent | null {
    try {
      const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? 'null');
      return saved && typeof saved.optional === 'boolean' && typeof saved.updatedAt === 'number'
        && saved.updatedAt <= Date.now() && Date.now() - saved.updatedAt < 180 * 86400000 ? saved : null;
    } catch { return null; }
  }
}
