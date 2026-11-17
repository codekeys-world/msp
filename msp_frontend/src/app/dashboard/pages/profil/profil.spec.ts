import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {Profil} from './profil';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {environment} from '../../../../environments/environment';

const user = {
  slug: 'membre', nom: 'TRAORÉ', prenom: 'Aminata', email: 'aminata@example.test',
  matricule: 'MSP-001', telephonePrincipal: '+22376123456', telephoneSecondaire: null,
  dateNaissance: '1990-05-17', lieuNaissance: 'Sikasso', adresse: 'Quartier Médine, Bamako',
  numeroInscription: 'INS-001', pere: 'Ousmane TRAORÉ', mere: 'Fatou DIALLO', genre: 'FEMME',
  observations: 'Informations personnelles', role: 'ADHERENT', authorisations: [], conjoints: [], descendants: [],
};
const response = (resultat: unknown) => ({donnees: {resultat}, message: 'Profil enregistré.'});
const url = `${environment.apiUrl}adherents/me`;

function setup() {
  const auth = {updateCurrentUserProfile: vi.fn()};
  TestBed.configureTestingModule({imports: [Profil], providers: [
    provideHttpClient(), provideHttpClientTesting(),
    {provide: AuthentificationService, useValue: auth},
    {provide: SweetAlertService, useValue: {showSuccess: vi.fn()}},
  ]});
  const fixture = TestBed.createComponent(Profil);
  const http = TestBed.inject(HttpTestingController);
  fixture.detectChanges();
  return {fixture, http, component: fixture.componentInstance, auth};
}

afterEach(() => TestBed.inject(HttpTestingController).verify());

describe('Profil personnel', () => {
  it('charge toutes les informations de l’API dans les champs visibles', async () => {
    const {fixture, http, component} = setup();
    http.expectOne(url).flush(response(user));
    await fixture.whenStable();
    for (const name of ['dateNaissance', 'lieuNaissance', 'pere', 'mere', 'adresse'] as const) {
      expect((fixture.nativeElement.querySelector(`#${name}`) as HTMLInputElement).value).toBe(user[name]);
    }
    expect(component.profilForm.valid).toBe(true);
    expect(component.missingProfileFields()).toEqual([]);
  });

  it('signale un profil incomplet sans inventer ni réutiliser des données', async () => {
    const {fixture, http, component} = setup();
    http.expectOne(url).flush(response({...user, dateNaissance: null, pere: '', mere: '', lieuNaissance: null, adresse: null}));
    await fixture.whenStable();
    expect(component.missingProfileFields()).toHaveLength(5);
    for (const name of ['dateNaissance', 'lieuNaissance', 'pere', 'mere', 'adresse']) {
      expect((fixture.nativeElement.querySelector(`#${name}`) as HTMLInputElement).value).toBe('');
    }
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('ne sont pas encore renseignées');
  });

  it('envoie les informations d’état civil et affiche la réponse normalisée après sauvegarde', async () => {
    const {fixture, http, component} = setup();
    http.expectOne(url).flush(response(user));
    await fixture.whenStable();
    const updated = {...user, dateNaissance: '1989-02-12', lieuNaissance: 'Kayes', pere: 'Moussa TRAORÉ', mere: 'Aïssata KEITA', adresse: 'Quartier du Fleuve'};
    component.profilForm.patchValue({...updated, telephoneSecondaire: ''});
    component.enregistrerProfil();
    const request = http.expectOne(url);
    expect(request.request.method).toBe('PUT');
    for (const name of ['dateNaissance', 'lieuNaissance', 'pere', 'mere', 'adresse']) {
      expect(request.request.body[name]).toBe(updated[name as keyof typeof updated]);
    }
    request.flush(response({...updated, adresse: 'Quartier du Fleuve, Bamako'}));
    await fixture.whenStable();
    expect((fixture.nativeElement.querySelector('#adresse') as HTMLInputElement).value).toBe('Quartier du Fleuve, Bamako');
    expect(component.profilForm.pristine).toBe(true);
    component.chargerProfil();
    http.expectOne(url).flush(response({...updated, adresse: 'Quartier du Fleuve, Bamako'}));
    await fixture.whenStable();
    expect(component.profilForm.controls.pere.value).toBe(updated.pere);
  });

  it('refuse une date de naissance future', async () => {
    const {http, component, fixture} = setup();
    http.expectOne(url).flush(response(user));
    await fixture.whenStable();
    component.profilForm.controls.dateNaissance.setValue('2999-01-01');
    component.enregistrerProfil();
    expect(component.profilForm.controls.dateNaissance.hasError('pastDate')).toBe(true);
    http.expectNone(url);
  });
});
