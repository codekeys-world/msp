import {ProviderToken, Type, provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Subject} from 'rxjs';
import {describe, it, expect, vi} from 'vitest';
import {SituationEncaissementComponent} from './encaissements/situation-encaissement';
import {SituationDepenseComponent} from './depenses/situation-depense';
import {EncaissementService} from './services/encaissement.service';
import {DepenseService} from './services/depense.service';
import {AuthentificationService} from '../../authentification/services/authentification-service';
import {AppResponse} from '../../app-response';
import {DepenseSituation} from './models/depense-situation';

const row: DepenseSituation = {mois: 1, libelleMois: 'Janvier', libelle: 'Rapport test',
  statut: 'VALIDEE', nombre: 2, montantTotal: 12500};
const response = (rows = [row]): AppResponse<DepenseSituation[]> => ({
  donnees: {resultat: rows}, message: '', developerMessage: '', status: 'OK', statusCode: 200
});

function reportTests<T extends {annee: number; changerAnnee(): void; loading: boolean}>(
  component: Type<T>, serviceToken: ProviderToken<unknown>
) {
  describe(component.name, () => {
    async function setup() {
      const first = new Subject<AppResponse<DepenseSituation[]>>();
      const second = new Subject<AppResponse<DepenseSituation[]>>();
      const situation = vi.fn().mockReturnValueOnce(first).mockReturnValue(second);
      TestBed.configureTestingModule({
        imports: [component],
        providers: [provideZonelessChangeDetection(), provideRouter([]),
          {provide: serviceToken, useValue: {situation}},
          {provide: AuthentificationService, useValue: {}}]
      });
      const fixture = TestBed.createComponent(component);
      await fixture.whenStable();
      return {fixture, first, second, situation};
    }

    it('renders the report and total after HTTP completes without a click', async () => {
      const {fixture, first} = await setup();
      first.next(response());
      first.complete();
      await fixture.whenStable();
      expect(fixture.nativeElement.textContent).toContain('Rapport test');
      expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
      expect(fixture.nativeElement.querySelector('tfoot').textContent).toMatch(/12\s*500/);
    });

    it('renders an empty report without a click', async () => {
      const {fixture, first} = await setup();
      first.next(response([]));
      first.complete();
      await fixture.whenStable();
      expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
      expect(fixture.nativeElement.querySelector('.alert-info')).not.toBeNull();
    });

    it('renders HTTP errors and releases loading without a click', async () => {
      const {fixture, first} = await setup();
      first.error(new Error('Serveur indisponible'));
      await fixture.whenStable();
      expect(fixture.nativeElement.querySelector('.missions-state')).toBeNull();
      expect(fixture.nativeElement.querySelector('.alert-danger').textContent).toContain('Impossible');
    });

    it('loads the selected year once and ignores the previous response', async () => {
      const {fixture, first, second, situation} = await setup();
      const select = fixture.nativeElement.querySelector('select') as HTMLSelectElement;
      select.selectedIndex = 1;
      select.dispatchEvent(new Event('change'));
      await fixture.whenStable();
      expect(situation).toHaveBeenCalledTimes(2);
      expect(situation).toHaveBeenLastCalledWith(2025);
      first.next(response([{...row, libelle: 'Ancien rapport'}]));
      first.complete();
      expect(fixture.componentInstance.loading).toBe(true);
      second.next(response([{...row, libelle: 'Nouveau rapport'}]));
      second.complete();
      await fixture.whenStable();
      expect(fixture.nativeElement.textContent).toContain('Nouveau rapport');
      expect(fixture.nativeElement.textContent).not.toContain('Ancien rapport');
    });
  });
}

reportTests(SituationEncaissementComponent, EncaissementService);
reportTests(SituationDepenseComponent, DepenseService);
