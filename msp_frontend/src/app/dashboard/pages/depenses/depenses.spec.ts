import {TestBed} from '@angular/core/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Subject} from 'rxjs';
import {describe, it, expect, vi} from 'vitest';
import {DepensesComponent} from './depenses';
import {DepenseService} from '../services/depense.service';
import {FraisDeGestionService} from '../services/frais-de-gestion.service';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {AppResponse} from '../../../app-response';
import {DepensePage, DepenseResponse, StatutDepense} from '../models/depense';

const response = <T>(resultat: T): AppResponse<T> => ({
  donnees: {resultat}, message: '', developerMessage: '', status: 'OK', statusCode: 200
});

async function setup() {
  const list = new Subject<AppResponse<DepensePage>>();
  const decision = new Subject<AppResponse<DepenseResponse>>();
  const service = {
    lister: vi.fn(() => list),
    valider: vi.fn(() => decision),
    rejeter: vi.fn(() => decision)
  };
  TestBed.configureTestingModule({
    imports: [DepensesComponent],
    providers: [
      provideZonelessChangeDetection(),
      {provide: DepenseService, useValue: service},
      {provide: FraisDeGestionService, useValue: {}},
      {provide: AuthentificationService, useValue: {
        canEngagerDepense: () => true, hasRole: () => true,
        canSupprimerDepense: () => true, canValiderDecaissement: () => true
      }},
      {provide: Router, useValue: {}},
      {provide: ActivatedRoute, useValue: {snapshot: {paramMap: {get: () => null}}}}
    ]
  });
  TestBed.overrideComponent(DepensesComponent, {set: {
    imports: [], template: `
      <span id="loading">{{loading}}</span>
      <span id="pending">{{decisionPending}}</span>
      <span id="dialog">{{decision !== null}}</span>
      <span id="error">{{decisionError}}</span>
      @for (dep of depenses; track dep.slug) { <span class="status">{{dep.statut}}</span> }
    `
  }});
  const fixture = TestBed.createComponent(DepensesComponent);
  await fixture.whenStable();
  return {fixture, list, decision, service};
}

describe('Depenses asynchronous rendering', () => {
  it('displays the list when HTTP completes without another click', async () => {
    const {fixture, list} = await setup();
    list.next(response({content: [{slug: 'dep-1', statut: StatutDepense.EN_ATTENTE}],
      number: 0, size: 8, totalElements: 1, totalPages: 1, first: true, last: true}));
    list.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#loading').textContent).toBe('false');
    expect(fixture.nativeElement.querySelector('.status').textContent).toBe('EN_ATTENTE');
  });

  it.each(['valider', 'rejeter'] as const)('renders %s errors and releases the button', async type => {
    const {fixture, decision} = await setup();
    fixture.componentInstance.ouvrirDecision('dep-1', type);
    fixture.componentInstance.decision!.observation = 'Motif';
    fixture.componentInstance.confirmerDecision();
    await fixture.whenStable();
    decision.error(new Error('Serveur indisponible'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#pending').textContent).toBe('false');
    expect(fixture.nativeElement.querySelector('#error').textContent).toContain('Impossible');
  });

  it.each(['valider', 'rejeter'] as const)('renders %s success without reloading the list', async type => {
    const {fixture, list, decision, service} = await setup();
    list.next(response({content: [{slug: 'dep-1', statut: StatutDepense.EN_ATTENTE}],
      number: 0, size: 8, totalElements: 1, totalPages: 1, first: true, last: true}));
    list.complete();
    await fixture.whenStable();
    fixture.componentInstance.ouvrirDecision('dep-1', type);
    fixture.componentInstance.decision!.observation = 'Motif';
    fixture.componentInstance.confirmerDecision();
    await fixture.whenStable();
    const statut = type === 'valider' ? StatutDepense.VALIDEE : StatutDepense.REJETEE;
    decision.next(response({slug: 'dep-1', statut, observationValidation: 'Motif'}));
    decision.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.status').textContent).toBe(statut);
    expect(fixture.nativeElement.querySelector('#pending').textContent).toBe('false');
    expect(fixture.nativeElement.querySelector('#dialog').textContent).toBe('false');
    expect(service.lister).toHaveBeenCalledTimes(1);
    expect(service[type]).toHaveBeenCalledWith('dep-1', 'Motif');
  });
});
