import {FormControl, FormGroup} from '@angular/forms';
import {describe, expect, it} from 'vitest';
import {dateRange, matchingPasswords, pastDate, requiredValue} from './form-validators';

describe('Validation des formulaires', () => {
  it('refuse une date inexistante ou future et accepte une naissance passée', () => {
    const field = new FormControl('2020-02-31', pastDate);
    expect(field.invalid).toBe(true);
    field.setValue('2999-01-01');
    expect(field.invalid).toBe(true);
    field.setValue('1990-05-17');
    expect(field.valid).toBe(true);
  });
  it('refuse les espaces seuls et conserve les valeurs numériques valides', () => {
    const field = new FormControl<unknown>('   ', requiredValue);
    expect(field.hasError('required')).toBe(true);
    field.setValue(' Aminata ');
    expect(field.valid).toBe(true);
    field.setValue(0);
    expect(field.valid).toBe(true);
    field.setValue(null);
    expect(field.invalid).toBe(true);
  });

  it('revérifie la confirmation lorsque le mot de passe change', () => {
    const form = new FormGroup({
      password: new FormControl('nouveau123'),
      confirmation: new FormControl('different123'),
      current: new FormControl('actuel123'),
    }, {validators: matchingPasswords('password', 'confirmation', 'current')});
    expect(form.hasError('passwordMismatch')).toBe(true);
    form.controls.confirmation.setValue('nouveau123');
    expect(form.valid).toBe(true);
    form.patchValue({password: 'actuel123', confirmation: 'actuel123'});
    expect(form.hasError('passwordUnchanged')).toBe(true);
  });

  it('refuse une date de fin antérieure au début et accepte la même date', () => {
    const form = new FormGroup({
      start: new FormControl('2026-11-16'), end: new FormControl('2026-11-15'),
    }, {validators: dateRange('start', 'end')});
    expect(form.hasError('dateRange')).toBe(true);
    form.controls.end.setValue('2026-11-16');
    expect(form.valid).toBe(true);
    form.controls.end.setValue('');
    expect(form.valid).toBe(true);
  });
});
