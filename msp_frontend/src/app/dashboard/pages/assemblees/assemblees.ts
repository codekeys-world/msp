import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize, Subscription} from 'rxjs';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {Assemblee, AssembleePage, AssembleeService} from '../services/assemblee.service';

@Component({
  selector: 'app-assemblees', imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './assemblees.html', styleUrl: './assemblees.scss'
})
export class Assemblees implements OnInit {
  readonly autorise = inject(AuthentificationService).canManageAssemblees();
  private readonly service = inject(AssembleeService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private listRequest?: Subscription;
  readonly resultats = signal<AssembleePage | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly erreur = signal('');
  readonly erreurForm = signal('');
  readonly succes = signal('');
  readonly formVisible = signal(false);
  readonly edition = signal<Assemblee | null>(null);
  readonly aSupprimer = signal<Assemblee | null>(null);
  recherche = '';
  readonly form = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.maxLength(200), Validators.pattern(/\S/)]],
    date: ['', Validators.required],
    lieu: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]]
  });

  ngOnInit() { if (this.autorise) this.charger(); }
  charger(page = 0) {
    if (!this.autorise) return;
    this.listRequest?.unsubscribe();
    this.loading.set(true);
    this.erreur.set('');
    this.listRequest = this.service.list(page, this.recherche).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))).subscribe({
      next: r => this.resultats.set(r.donnees.resultat),
      error: () => this.erreur.set('Impossible de charger les assemblées. Réessayez.')
    });
  }
  ouvrir(assemblee: Assemblee | null = null) {
    this.edition.set(assemblee);
    this.form.reset(assemblee ?? {nom: '', date: '', lieu: ''});
    this.erreurForm.set('');
    this.succes.set('');
    this.formVisible.set(true);
  }
  enregistrer() {
    if (!this.autorise || this.saving()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.erreurForm.set('');
    const v = this.form.getRawValue();
    const request = {...v, nom: v.nom.trim(), lieu: v.lieu.trim()};
    const edition = this.edition();
    const action = edition ? this.service.update(edition.slug, request) : this.service.create(request);
    action.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.formVisible.set(false);
        this.succes.set(edition ? 'Assemblée modifiée.' : 'Assemblée enregistrée.');
        this.charger();
      },
      error: () => this.erreurForm.set('Enregistrement impossible. Vérifiez les champs et réessayez.')
    });
  }
  supprimer() {
    const assemblee = this.aSupprimer();
    if (!assemblee || !this.autorise || this.deleting()) return;
    this.deleting.set(true);
    this.erreur.set('');
    this.service.delete(assemblee.slug).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.deleting.set(false))).subscribe({
      next: () => {
        this.aSupprimer.set(null);
        this.succes.set('Assemblée supprimée.');
        const page = this.resultats();
        this.charger(page && page.content.length === 1 ? Math.max(0, page.number - 1) : (page?.number ?? 0));
      },
      error: () => { this.aSupprimer.set(null); this.erreur.set('Suppression impossible. Réessayez.'); }
    });
  }
}
