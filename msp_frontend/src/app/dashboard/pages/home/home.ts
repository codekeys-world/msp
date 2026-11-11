import {Component, computed, inject, OnInit, signal} from '@angular/core';
import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize} from 'rxjs';
import {Manuel, ManuelService} from '../services/manuel.service';
import {RouterLink} from '@angular/router';
import {OrdreMissionService} from '../../ordre-missions/services/ordre-mission.service';
import {OrdreMissionStatistiques} from '../../ordre-missions/models/ordre-mission-statistiques';
import {AdherentService} from '../services/adherent.service';
import {AdherentStatistiques} from '../models/utilisateur-detail';
import {AuthentificationService} from '../../../authentification/services/authentification-service';
import {PvService} from '../services/pv.service';
import {PvStatistiques} from '../models/pv';
import {CourrierService} from '../services/courrier.service';
import {CourrierStatistiques} from '../models/courrier';
import {PlainteService} from '../services/plainte.service';
import {PlainteStatistiques} from '../models/plainte';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private readonly ordreMissionService = inject(OrdreMissionService);
  private readonly adherentService = inject(AdherentService);
  private readonly auth = inject(AuthentificationService);
  private readonly pvService = inject(PvService);
  private readonly courrierService = inject(CourrierService);
  private readonly plainteService = inject(PlainteService);

  readonly canViewAdherentStats = this.auth.hasRole('LISTER_ADHERENT') === true;
  readonly canViewMissionStats = this.auth.hasRole('LISTER_ORDRE_MISSION') === true;
  readonly canViewPvStats = this.auth.hasRole('LISTER_PV') === true;
  readonly canViewCourrierStats = this.auth.hasRole('LISTER_COURRIER') === true;
  readonly canViewPlainteStats = this.auth.hasRole('LISTER_PLAINTE') === true;
  readonly canListAdherents = this.auth.canListAdherents();
  readonly canCreateAdherents = this.auth.canManageAdherents();
  readonly canAddCourrier = this.auth.hasRole('AJOUTER_COURRIER') === true;
  readonly canCreateOrdreMission = this.auth.hasRole('AJOUTER_ORDRE_MISSION') === true;
  readonly canCreatePv = this.auth.hasRole('AJOUTER_PV') === true;
  adherentStatistiques = signal<AdherentStatistiques | null>(null);
  isLoadingAdherentStats = signal(true);
  adherentStatsError = signal<string | null>(null);
  ordreMissionStatistiques = signal<OrdreMissionStatistiques | null>(null);
  isLoadingMissionStats = signal(true);
  missionStatsError = signal<string | null>(null);
  pvStatistiques = signal<PvStatistiques | null>(null);
  isLoadingPvStats = signal(true);
  pvStatsError = signal<string | null>(null);
  courrierStatistiques = signal<CourrierStatistiques | null>(null);
  isLoadingCourrierStats = signal(true);
  courrierStatsError = signal<string | null>(null);
  plainteStatistiques = signal<PlainteStatistiques | null>(null);
  isLoadingPlainteStats = signal(true);
  plainteStatsError = signal<string | null>(null);
  missionCompletionRate = computed(() => {
    const statistiques = this.ordreMissionStatistiques();
    if (!statistiques || statistiques.total === 0) {
      return 0;
    }
    return Math.round((statistiques.terminees / statistiques.total) * 100);
  });
  femaleAdherentRate = computed(() => {
    const statistiques = this.adherentStatistiques();
    if (!statistiques || statistiques.total === 0) return 0;
    return Math.round((statistiques.femmes / statistiques.total) * 100);
  });
  courrierTreatmentRate = computed(() => {
    const statistiques = this.courrierStatistiques();
    if (!statistiques || statistiques.total === 0) return 0;
    return Math.round((statistiques.traites / statistiques.total) * 100);
  });
  plainteTreatmentRate = computed(() => {
    const statistiques = this.plainteStatistiques();
    if (!statistiques || statistiques.total === 0) return 0;
    return Math.round((statistiques.traitees / statistiques.total) * 100);
  });

  private readonly manuelService = inject(ManuelService);
  private readonly destroyRef = inject(DestroyRef);
  readonly manuels = signal<Manuel[]>([]);
  readonly manuelsLoading = signal(true);
  readonly manuelsError = signal<string | null>(null);
  readonly downloadingRoles = signal<string[]>([]);

  chargerManuels(): void {
    this.manuelsLoading.set(true);
    this.manuelsError.set(null);
    this.manuelService.list().pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.manuelsLoading.set(false))).subscribe({
      next: response => this.manuels.set(response.donnees.resultat),
      error: () => this.manuelsError.set('Impossible de charger vos manuels. Veuillez réessayer.')
    });
  }

  telechargerManuel(manuel: Manuel): void {
    if (this.downloadingRoles().includes(manuel.role)) return;
    this.downloadingRoles.update(roles => [...roles, manuel.role]);
    this.manuelsError.set(null);
    this.manuelService.download(manuel.role).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.downloadingRoles.update(roles => roles.filter(role => role !== manuel.role)))
    ).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'manuel_' + manuel.role.toLowerCase() + '.pdf';
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: () => this.manuelsError.set('Le téléchargement a échoué. Veuillez réessayer.')
    });
  }

  ngOnInit(): void {
    this.chargerManuels();
    this.chargerStatistiquesOrdreMission();
    this.chargerStatistiquesAdherents();
    this.chargerStatistiquesPv();
    this.chargerStatistiquesCourriers();
    this.chargerStatistiquesPlaintes();
  }

  chargerStatistiquesPlaintes(): void {
    this.isLoadingPlainteStats.set(true);
    this.plainteStatsError.set(null);
    this.plainteService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.plainteStatistiques.set(response.donnees.resultat);
        this.isLoadingPlainteStats.set(false);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des statistiques des plaintes.', error);
        this.plainteStatsError.set('Statistiques des plaintes indisponibles.');
        this.isLoadingPlainteStats.set(false);
      }
    });
  }

  chargerStatistiquesCourriers(): void {
    this.isLoadingCourrierStats.set(true);
    this.courrierStatsError.set(null);
    this.courrierService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.courrierStatistiques.set(response.donnees.resultat);
        this.isLoadingCourrierStats.set(false);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des statistiques des courriers.', error);
        this.courrierStatsError.set('Statistiques des courriers indisponibles.');
        this.isLoadingCourrierStats.set(false);
      }
    });
  }

  chargerStatistiquesPv(): void {
    this.isLoadingPvStats.set(true);
    this.pvStatsError.set(null);
    this.pvService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.pvStatistiques.set(response.donnees.resultat);
        this.isLoadingPvStats.set(false);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des statistiques des procès-verbaux.', error);
        this.pvStatsError.set('Statistiques des procès-verbaux indisponibles.');
        this.isLoadingPvStats.set(false);
      }
    });
  }

  chargerStatistiquesAdherents(): void {
    this.isLoadingAdherentStats.set(true);
    this.adherentStatsError.set(null);
    this.adherentService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.adherentStatistiques.set(response.donnees.resultat);
        this.isLoadingAdherentStats.set(false);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des statistiques des adhérents.', error);
        this.adherentStatsError.set('Statistiques des adhérents indisponibles.');
        this.isLoadingAdherentStats.set(false);
      }
    });
  }

  chargerStatistiquesOrdreMission(): void {
    this.isLoadingMissionStats.set(true);
    this.missionStatsError.set(null);
    this.ordreMissionService.getStatistiquesPubliques().subscribe({
      next: response => {
        this.ordreMissionStatistiques.set(response.donnees.resultat);
        this.isLoadingMissionStats.set(false);
      },
      error: (error: unknown) => {
        console.error('Erreur lors du chargement des statistiques des ordres de mission.', error);
        this.missionStatsError.set('Statistiques des missions indisponibles.');
        this.isLoadingMissionStats.set(false);
      }
    });
  }
}
