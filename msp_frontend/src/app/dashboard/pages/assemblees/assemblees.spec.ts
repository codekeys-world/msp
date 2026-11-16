import {TestBed} from '@angular/core/testing';
import {provideZonelessChangeDetection} from '@angular/core';
import {Observable, Subject, of} from 'rxjs';
import {describe, it, expect, vi} from 'vitest';
import {Assemblees} from './assemblees';
import {Assemblee, AssembleeService} from '../services/assemblee.service';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {AppResponse} from '../../../app-response';
import {SweetAlertService} from '../../../shared/services/sweet-alert.service';

const response = <T>(resultat: T): AppResponse<T> => ({donnees: {resultat}, message: '', developerMessage: '', status: 'OK', statusCode: 200});
const assemblee: Assemblee = {slug: 'ag-1', nom: 'Assemblée générale', date: '2026-12-01', lieu: 'Bamako'};

async function setup(autorise = true) {
  const mutation = new Subject<AppResponse<Assemblee>>();
  const alerts = {confirmDeletion: vi.fn(async () => true), showSuccess: vi.fn(), showError: vi.fn()};
  const service = {
    list: vi.fn(() => of(response({content: [assemblee], number: 0, size: 8, totalElements: 1, totalPages: 1, first: true, last: true}))),
    create: vi.fn((): Observable<AppResponse<Assemblee>> => mutation), update: vi.fn(() => mutation), delete: vi.fn(() => of(response(true)))
  };
  TestBed.configureTestingModule({imports: [Assemblees], providers: [
    provideZonelessChangeDetection(), {provide: AssembleeService, useValue: service},
    {provide: SweetAlertService, useValue: alerts},
    {provide: AuthentificationService, useValue: {canManageAssemblees: () => autorise}}
  ]});
  const fixture = TestBed.createComponent(Assemblees);
  await fixture.whenStable();
  return {fixture, component: fixture.componentInstance, service, mutation, alerts};
}

describe('Gestion des assemblées', () => {
  it('affiche les données et bloque les champs vides', async () => {
    const {fixture, component, service} = await setup();
    expect(fixture.nativeElement.textContent).toContain('Assemblée générale');
    expect(fixture.nativeElement.textContent).toContain('01/12/2026');
    expect(fixture.nativeElement.textContent).toContain('Bamako');
    component.ouvrir();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#assemblee-nom').placeholder).toContain('Assemblée générale');
    expect(fixture.nativeElement.querySelector('#assemblee-lieu').placeholder).toContain('Bamako');
    component.form.setValue({nom: ' ', date: '', lieu: ' '});
    component.enregistrer();
    expect(service.create).not.toHaveBeenCalled();
  });

  it('conserve le formulaire en cas d’échec et recharge la liste après création', async () => {
    const {component, service, mutation, fixture} = await setup();
    component.ouvrir();
    component.form.setValue({nom: ' Assemblée générale ', date: assemblee.date, lieu: ' Bamako '});
    component.enregistrer();
    component.enregistrer();
    expect(service.create).toHaveBeenCalledTimes(1);
    expect(service.create).toHaveBeenCalledWith({nom: assemblee.nom, date: assemblee.date, lieu: assemblee.lieu});
    mutation.error(new Error('indisponible'));
    await fixture.whenStable();
    expect(component.saving()).toBe(false);
    expect(component.formVisible()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Enregistrement impossible');
    service.create.mockReturnValue(of(response(assemblee)));
    component.enregistrer();
    expect(component.formVisible()).toBe(false);
    expect(service.list).toHaveBeenCalledTimes(2);
  });

  it('modifie avec le slug existant et confirme la suppression', async () => {
    const {component, service, mutation} = await setup();
    component.ouvrir(assemblee);
    component.form.controls.lieu.setValue('Ségou');
    component.enregistrer();
    expect(service.update).toHaveBeenCalledWith('ag-1', {nom: assemblee.nom, date: assemblee.date, lieu: 'Ségou'});
    mutation.next(response({...assemblee, lieu: 'Ségou'}));
    mutation.complete();
    expect(service.delete).not.toHaveBeenCalled();
    await component.supprimer(assemblee);
    expect(service.delete).toHaveBeenCalledWith('ag-1');
    expect(component.deleting()).toBe(false);
  });

  it('ne supprime rien lorsque la confirmation est annulée', async () => {
    const {component, service, alerts} = await setup();
    alerts.confirmDeletion.mockResolvedValue(false);
    await component.supprimer(assemblee);
    expect(service.delete).not.toHaveBeenCalled();
    expect(component.deleting()).toBe(false);
  });

  it('refuse la gestion et les requêtes pour un autre rôle', async () => {
    const {fixture, service, component} = await setup(false);
    expect(fixture.nativeElement.textContent).toContain('Vous n’avez pas accès');
    expect(service.list).not.toHaveBeenCalled();
    component.form.setValue({nom: assemblee.nom, date: assemblee.date, lieu: assemblee.lieu});
    component.enregistrer();
    expect(service.create).not.toHaveBeenCalled();
  });
});
