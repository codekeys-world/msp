import {AbstractControl, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';

/** Required values cannot consist solely of spaces. Numbers such as zero remain valid. */
export const requiredValue: ValidatorFn = control =>
  typeof control.value === 'string' && !control.value.trim()
    ? {required: true}
    : Validators.required(control);

export const pastDate: ValidatorFn = control => {
  if (!control.value) return null;
  const now = new Date();
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  if (!/^\d{4}-\d{2}-\d{2}$/.test(control.value) || control.value >= today) return {pastDate: true};
  const parsed = new Date(`${control.value}T00:00:00`);
  const [year, month, day] = control.value.split('-').map(Number);
  return parsed.getFullYear() === year && parsed.getMonth() + 1 === month && parsed.getDate() === day ? null : {pastDate: true};
};

export function matchingPasswords(password: string, confirmation: string, current?: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const value = group.get(password)?.value;
    const repeated = group.get(confirmation)?.value;
    if (value && repeated && value !== repeated) return {passwordMismatch: true};
    if (current && value && value === group.get(current)?.value) return {passwordUnchanged: true};
    return null;
  };
}

export function dateRange(start: string, end: string): ValidatorFn {
  return group => {
    const from = group.get(start)?.value;
    const to = group.get(end)?.value;
    return from && to && to < from ? {dateRange: true} : null;
  };
}
