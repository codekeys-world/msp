import {afterEach, describe, expect, it, vi} from 'vitest';
import {CookieConsentService} from './cookie-consent.service';

afterEach(() => { localStorage.clear(); vi.restoreAllMocks(); });

describe('Préférences de cookies', () => {
  it('ne présume pas de consentement et mémorise le refus', () => {
    const consent = new CookieConsentService();
    expect(consent.choice()).toBeNull();
    expect(consent.open()).toBe(true);
    consent.save(false);
    const restored = new CookieConsentService();
    expect(restored.choice()?.optional).toBe(false);
    expect(restored.open()).toBe(false);
  });

  it('redemande un choix après six mois ou si les données sont corrompues', () => {
    localStorage.setItem('msp-cookie-consent-v1', JSON.stringify({optional:true,updatedAt:Date.now()-181*86400000}));
    expect(new CookieConsentService().open()).toBe(true);
    localStorage.setItem('msp-cookie-consent-v1','incorrect');
    expect(new CookieConsentService().choice()).toBeNull();
  });

  it('reste utilisable lorsque le navigateur refuse le stockage', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('Storage disabled'); });
    const consent = new CookieConsentService();
    expect(() => consent.save(false)).not.toThrow();
    expect(consent.choice()?.optional).toBe(false);
    expect(consent.open()).toBe(false);
  });
});
