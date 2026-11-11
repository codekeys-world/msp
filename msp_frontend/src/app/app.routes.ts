import { Routes } from '@angular/router';
import { authentificationGuardGuard } from './authentification/guards/authentification-guard-guard';

export const routes: Routes = [
    {path : '', loadComponent : () => import('./index/components/index').then(c => c.Index), pathMatch :'full'},
    {path : 'index', loadComponent : () => import('./index/components/index').then(c => c.Index)},
    {path : 'authentification', loadComponent : () => import('./authentification/components/authentification/authentification').then(c => c.Authentification)},
    {path: 'reinitialiser-mot-de-passe', loadComponent: () => import('./authentification/components/reinitialisation-mot-de-passe/reinitialisation-mot-de-passe').then(c => c.ReinitialisationMotDePasse)},
    {
        path : 'dashboard',
        loadComponent : () => import('./dashboard/dashboard/dashboard').then(c =>c.Dashboard),
        canActivate : [authentificationGuardGuard],
        children : [
            {
                path: '', redirectTo: 'home', pathMatch: 'full',
            },
            {
                path: 'profil',
                loadComponent: () => import('./dashboard/pages/profil/profil').then(c => c.Profil),
            },
            {
                path : 'home',
                loadComponent: () => import('./dashboard/pages/home/home').then(c => c.Home),
            },
            {
                path : 'adherent/create',
                loadComponent: () => import('./dashboard/pages/adherent-form/adherent-form').then(c => c.AdherentForm),
            },
            {
                path : 'adherents',
                loadComponent: () => import('./dashboard/pages/adherents/adherents').then(c => c.Adherents),
            },
            {
                path : 'adherent/:slug/modifier',
                loadComponent: () => import('./dashboard/pages/adherent-form/adherent-form').then(c => c.AdherentForm),
            },
            {
                path : 'adherent/:slug',
                loadComponent: () => import('./dashboard/pages/adherent-detail/adherent-detail').then(c => c.AdherentDetail),
            },
            {
                path : 'cartes-membres',
                loadComponent: () => import('./dashboard/pages/carte-membre/carte-membre').then(c => c.CarteMembre),
            },
            {
                path : 'carte-membre/:slug',
                loadComponent: () => import('./dashboard/pages/carte-membre/carte-membre').then(c => c.CarteMembre),
            },
            {
                path : 'adherent/:slug/carte',
                loadComponent: () => import('./dashboard/pages/carte-membre/carte-membre').then(c => c.CarteMembre),
            },
            {
              path : 'proces-verbaux',
              loadComponent: () => import('./dashboard/pages/proces-verbaux/proces-verbaux').then(c => c.ProcesVerbaux)
            },
            {
              path : 'plaintes',
              loadComponent: () => import('./dashboard/pages/plaintes/plaintes').then(c => c.Plaintes),
            },
            {
              path : 'cas-sociaux',
              loadComponent: () => import('./dashboard/pages/cas-sociaux/cas-sociaux').then(c => c.CasSociaux),
            },
            {
              path : 'nouveau-cas-social',
              loadComponent: () => import('./dashboard/pages/cas-sociaux/cas-social-form').then(c => c.CasSocialForm),
            },
            {
              path : 'modifier-cas-social/:slug',
              loadComponent: () => import('./dashboard/pages/cas-sociaux/cas-social-form').then(c => c.CasSocialForm),
            },
            {
              path : 'prestations',
              loadComponent: () => import('./dashboard/pages/prestations/prestations').then(c => c.Prestations),
            },
            {
              path : 'nouvelle-prestation',
              loadComponent: () => import('./dashboard/pages/prestations/prestation-form').then(c => c.PrestationForm),
            },
            {
              path : 'modifier-prestation/:slug',
              loadComponent: () => import('./dashboard/pages/prestations/prestation-form').then(c => c.PrestationForm),
            },
            {
              path : 'convocations',
              loadComponent: () => import('./dashboard/pages/convocations/convocations').then(c => c.Convocations),
            },
            {
              path : 'nouvelle-convocation',
              loadComponent: () => import('./dashboard/pages/convocations/convocation-form').then(c => c.ConvocationForm),
            },
            {
              path : 'modifier-convocation/:slug',
              loadComponent: () => import('./dashboard/pages/convocations/convocation-form').then(c => c.ConvocationForm),
            },
            {
              path : 'nouvelle-plainte',
              loadComponent: () => import('./dashboard/pages/plaintes/plainte-form').then(c => c.PlainteForm),
            },
            {
              path : 'modifier-plainte/:slug',
              loadComponent: () => import('./dashboard/pages/plaintes/plainte-form').then(c => c.PlainteForm),
            },
            {
              path : 'nouveau-pv',
              loadComponent: () => import('./dashboard/pages/proces-verbaux/pv-form').then(c => c.PvForm),
            },
            {
              path : 'modifier-pv/:slug',
              loadComponent: () => import('./dashboard/pages/proces-verbaux/pv-form').then(c => c.PvForm),
            },
            {
              path : 'ordre-missions',
              loadComponent: () => import('./dashboard/ordre-missions/ordre-mission/ordre-mission').then(c => c.OrdreMission)
            },
          {
            path : 'courrier',
              loadComponent: () => import('./dashboard/pages/courrier/courrier').then(c => c.Courrier),
          },
          {
            path : 'nouveau-courrier',
            loadComponent: () => import('./dashboard/pages/courrier/courrier-form').then(c => c.CourrierForm),
          },
          {
            path : 'modifier-courrier/:slug',
            loadComponent: () => import('./dashboard/pages/courrier/courrier-form').then(c => c.CourrierForm),
          },
          {
            path : 'nouveau-ordre-missions',
            loadComponent: () => import('./dashboard/ordre-missions/ordre-mission-form/ordre-mission-form').then(c => c.OrdreMissionForm)
          },
          {
            path : 'modifier-ordre-missions/:slug',
            loadComponent: () => import('./dashboard/ordre-missions/ordre-mission-form/ordre-mission-form').then(c => c.OrdreMissionForm)
          },
          {
            path : 'controle',
            loadComponent: () => import('./dashboard/pages/controle/journal-actions').then(c => c.JournalActions),
          },
          {
            path : 'journal-actions',
            loadComponent: () => import('./dashboard/pages/controle/journal-actions').then(c => c.JournalActions),
          },
          {
            path : 'archives',
            loadComponent: () => import('./dashboard/pages/archive/archive').then(c => c.Archive),
          },
          {
            path : 'encaissements',
            loadComponent: () => import('./dashboard/pages/encaissements/encaissements').then(c => c.Encaissements),
          },
          {
            path : 'nouvel-encaissement',
            loadComponent: () => import('./dashboard/pages/encaissements/encaissement-form').then(c => c.EncaissementForm),
          },
          {
            path : 'modifier-encaissement/:slug',
            loadComponent: () => import('./dashboard/pages/encaissements/encaissement-form').then(c => c.EncaissementForm),
          },
          {
            path : 'situation-encaissements',
            loadComponent: () => import('./dashboard/pages/encaissements/situation-encaissement').then(c => c.SituationEncaissementComponent),
          },
          {
            path : 'frais-de-gestion',
            loadComponent: () => import('./dashboard/pages/frais-de-gestion/frais-de-gestion').then(c => c.FraisDeGestionComponent),
          },
          {
            path : 'frais-de-gestion/nouveau',
            loadComponent: () => import('./dashboard/pages/frais-de-gestion/frais-de-gestion-form').then(c => c.FraisDeGestionFormComponent),
          },
          {
            path : 'frais-de-gestion/modifier/:slug',
            loadComponent: () => import('./dashboard/pages/frais-de-gestion/frais-de-gestion-form').then(c => c.FraisDeGestionFormComponent),
          },
          {
            path : 'depenses',
            loadComponent: () => import('./dashboard/pages/depenses/depenses').then(c => c.DepensesComponent),
          },
          {
            path : 'depenses/nouvelle',
            loadComponent: () => import('./dashboard/pages/depenses/depense-form').then(c => c.DepenseFormComponent),
          },
          {
            path : 'depenses/modifier/:slug',
            loadComponent: () => import('./dashboard/pages/depenses/depense-form').then(c => c.DepenseFormComponent),
          },
          {
            path : 'depenses/frais-de-gestion/:fraisDeGestionSlug',
            loadComponent: () => import('./dashboard/pages/depenses/depenses').then(c => c.DepensesComponent),
          },
          {
            path : 'situation-depenses',
            loadComponent: () => import('./dashboard/pages/depenses/situation-depense').then(c => c.SituationDepenseComponent),
          },
          {
            path : 'materiels',
            loadComponent: () => import('./dashboard/pages/materiels/materiels').then(c => c.Materiels),
          },
          {
            path : 'nouveau-materiel',
            loadComponent: () => import('./dashboard/pages/materiels/materiel-form').then(c => c.MaterielForm),
          },
          {
            path : 'modifier-materiel/:slug',
            loadComponent: () => import('./dashboard/pages/materiels/materiel-form').then(c => c.MaterielForm),
          },
          {
            path : 'adjoints',
            loadComponent: () => import('./dashboard/pages/adjoints/adjoints').then(c => c.Adjoints),
          },
          {
            path : 'utilisateurs',
            loadComponent: () => import('./dashboard/pages/utilisateurs/utilisateurs').then(c => c.Utilisateurs),
          }
        ]
    },

    {path : '**', redirectTo : ''}
];
