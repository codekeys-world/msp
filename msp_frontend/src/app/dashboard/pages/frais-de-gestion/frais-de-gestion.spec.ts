import {provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Subject} from 'rxjs';
import {describe, it, expect, vi} from 'vitest';
import {FraisDeGestionComponent} from './frais-de-gestion';
import {FraisDeGestionService} from '../services/frais-de-gestion.service';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';
import {AppResponse} from '../../../app-response';
import {FraisDeGestionPage} from '../models/frais-de-gestion';

const response = (annee: number, page = 0): AppResponse<FraisDeGestionPage> => ({
  donnees: {resultat: {content: [{slug: `fg-${annee}`, annee, montantTotal: 10000,
    montantDepense: 2500, solde: 7500, nombreDepenses: 1}], number: page,
    size: 8, totalElements: 9, totalPages: 2, first: page === 0, last: page === 1}},
  message: '', developerMessage: '', status: 'OK', statusCode: 200
});

async function setup() {
  const first = new Subject<AppResponse<FraisDeGestionPage>>();
  const second = new Subject<AppResponse<FraisDeGestionPage>>();
  const lister = vi.fn().mockReturnValueOnce(first).mockReturnValue(second);
  TestBed.configureTestingModule({
    imports: [FraisDeGestionComponent],
    providers: [provideZonelessChangeDetection(), provideRouter([]),
      {provide: FraisDeGestionService, useValue: {lister}},
      {provide: AuthentificationService, useValue: {
        canEngagerDepense: () => true, hasRole: () => true, canSupprimerDepense: () => true
      }},
      {provide: SweetAlertService, useValue: {}}]
  });
  const fixture = TestBed.createComponent(FraisDeGestionComponent);
  await fixture.whenStable();
  return {fixture, first, second, lister};
}

describe('Frais de gestion asynchronous rendering', () => {
  it('displays the list and balance after HTTP completes without another click', async () => {
    const {fixture, first} = await setup();
    first.next(response(2026));
    first.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Exercice 2026');
    expect(fixture.nativeElement.textContent).toMatch(/7\s*500/);
  });

  it('displays HTTP errors without another click', async () => {
    const {fixture, first} = await setup();
    first.error(new Error('Serveur indisponible'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
    expect(fixture.nativeElement.querySelector('.alert-danger').textContent).toContain('Impossible');
  });

  it('displays an empty result without another click', async () => {
    const {fixture, first} = await setup();
    const empty = response(2026);
    empty.donnees.resultat.content = [];
    empty.donnees.resultat.totalElements = 0;
    first.next(empty);
    first.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
    expect(fixture.nativeElement.querySelector('.alert-info')).not.toBeNull();
  });

  it('ignores a superseded request and renders the new page automatically', async () => {
    const {fixture, first, second, lister} = await setup();
    fixture.componentInstance.page = 1;
    fixture.componentInstance.charger();
    await fixture.whenStable();
    expect(lister).toHaveBeenLastCalledWith(1, 8, '');
    first.next(response(2026));
    first.complete();
    expect(fixture.componentInstance.loading).toBe(true);
    second.next(response(2025, 1));
    second.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Exercice 2025');
    expect(fixture.nativeElement.textContent).not.toContain('Exercice 2026');
  });
});
