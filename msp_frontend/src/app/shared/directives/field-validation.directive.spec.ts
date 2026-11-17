import {Component} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {describe, expect, it} from 'vitest';
import {FieldValidationDirective} from './field-validation.directive';
import {requiredValue} from '../utils/form-validators';

@Component({
  imports: [ReactiveFormsModule, FieldValidationDirective],
  template: '<input appFieldValidation [formControl]="field">',
})
class TestForm {
  readonly field = new FormControl('', requiredValue);
}

describe('Messages de validation', () => {
  it('affiche une erreur après interaction et la retire après correction', () => {
    const fixture = TestBed.createComponent(TestForm);
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    const feedback = fixture.nativeElement.querySelector('.invalid-feedback') as HTMLElement;
    expect(input.classList.contains('is-invalid')).toBe(false);
    fixture.componentInstance.field.markAsTouched();
    fixture.detectChanges();
    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(feedback.textContent).toBe('Ce champ est obligatoire.');
    fixture.componentInstance.field.setValue('Aminata');
    fixture.detectChanges();
    expect(input.getAttribute('aria-invalid')).toBe('false');
    expect(feedback.style.display).toBe('none');
  });
});
