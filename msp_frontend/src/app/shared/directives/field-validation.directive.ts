import {Directive, DoCheck, ElementRef, inject, OnDestroy, Renderer2} from '@angular/core';
import {NgControl} from '@angular/forms';

/** Feedback for fields that do not already have a dedicated error message. */
@Directive({selector: '[appFieldValidation]'})
export class FieldValidationDirective implements DoCheck, OnDestroy {
  private readonly field = inject(NgControl, {self: true});
  private readonly element = inject(ElementRef<HTMLElement>).nativeElement;
  private readonly renderer = inject(Renderer2);
  private feedback?: HTMLElement;

  ngDoCheck(): void {
    const control = this.field.control;
    const invalid = !!control?.invalid && (control.touched || control.dirty);
    this.renderer[invalid ? 'addClass' : 'removeClass'](this.element, 'is-invalid');
    this.renderer.setAttribute(this.element, 'aria-invalid', String(invalid));
    if (!this.feedback) {
      this.feedback = this.renderer.createElement('div');
      this.renderer.setAttribute(this.feedback!, 'class', 'invalid-feedback');
      this.renderer.insertBefore(this.element.parentNode, this.feedback, this.element.nextSibling);
    }
    const errors = control?.errors;
    const message = errors?.['required'] ? 'Ce champ est obligatoire.'
      : errors?.['email'] ? 'Saisissez une adresse e-mail valide.'
      : errors?.['minlength'] ? `Saisissez au moins ${errors['minlength'].requiredLength} caractères.`
      : errors?.['maxlength'] ? `Saisissez au maximum ${errors['maxlength'].requiredLength} caractères.`
      : errors?.['min'] ? `La valeur minimale est ${errors['min'].min}.`
      : errors?.['max'] ? `La valeur maximale est ${errors['max'].max}.`
      : errors?.['pastDate'] ? 'La date de naissance doit être passée.'
      : 'Saisissez une valeur valide.';
    this.renderer.setProperty(this.feedback, 'textContent', invalid ? message : '');
    this.renderer.setStyle(this.feedback, 'display', invalid ? 'block' : 'none');
  }

  ngOnDestroy(): void {
    if (this.feedback?.parentNode) this.renderer.removeChild(this.feedback.parentNode, this.feedback);
  }
}
