import {inject, Injectable} from '@angular/core';
import {Meta, Title} from '@angular/platform-browser';
import {RouterStateSnapshot, Routes, TitleStrategy} from '@angular/router';

const LABELS: Record<string, string> = {
  home: 'Tableau de bord', profil: 'Mon profil', adherents: 'Adhérents', adherent: 'Fiche adhérent',
  'adherent/create': 'Nouvel adhérent', 'cartes-membres': 'Cartes de membre', 'carte-membre': 'Carte de membre',
  'proces-verbaux': 'Procès-verbaux', plaintes: 'Plaintes', 'cas-sociaux': 'Cas sociaux', prestations: 'Prestations',
  convocations: 'Convocations', 'ordre-missions': 'Ordres de mission', courrier: 'Courrier',
  controle: 'Journal des actions', 'journal-actions': 'Journal des actions', archives: 'Archives',
  encaissements: 'Encaissements', 'situation-encaissements': 'Situation des encaissements',
  'frais-de-gestion': 'Frais de gestion', depenses: 'Dépenses', 'situation-depenses': 'Situation des dépenses',
  materiels: 'Matériels', adjoints: 'Désignation des adjoints', utilisateurs: 'Utilisateurs', assemblees: 'Assemblées',
  'nouveau-pv': 'Nouveau procès-verbal', 'nouvelle-plainte': 'Nouvelle plainte',
  'nouveau-cas-social': 'Nouveau cas social', 'nouvelle-prestation': 'Nouvelle prestation',
  'nouvelle-convocation': 'Nouvelle convocation', 'nouveau-ordre-missions': 'Nouvel ordre de mission',
  'nouveau-courrier': 'Nouveau courrier', 'nouvel-encaissement': 'Nouvel encaissement', 'nouveau-materiel': 'Nouveau matériel',
};

export function withPageMetadata(routes: Routes, privateArea = false): Routes {
  return routes.map(route => {
    const path = route.path ?? '';
    const privatePage = privateArea || path === 'dashboard';
    const segments = path.split('/').filter(part => !part.startsWith(':'));
    const base = segments[0];
    let label = LABELS[path] ?? LABELS[base];
    if (base?.startsWith('modifier-')) label = 'Modifier : ' + (LABELS[base.replace('modifier-', '')] ?? base.replace('modifier-', '').replaceAll('-', ' '));
    if (segments.includes('modifier')) label = 'Modifier : ' + (LABELS[base] ?? 'adhérent');
    if (segments.includes('nouveau') || segments.includes('nouvelle')) label = 'Ajouter : ' + LABELS[base];
    if (segments.includes('carte')) label = 'Carte de membre';
    if (!privatePage) label = path === 'authentification' ? 'Connexion'
      : path === 'reinitialiser-mot-de-passe' ? 'Réinitialiser le mot de passe'
      : path === 'confidentialite' ? 'Confidentialité et cookies'
      : path === '**' ? 'Page introuvable' : 'Mutuelle des Surveillants de Prison du Mali';
    label ??= 'Espace adhérent';
    return {
      ...route,
      title: `${label} — MSP`,
      data: {...route.data,
        description: privatePage ? `${label} : consultez et gérez les informations de votre espace sécurisé de la Mutuelle des Surveillants de Prison du Mali.`
          : path === '' || path === 'index' ? 'Découvrez la Mutuelle des Surveillants de Prison du Mali : solidarité, cotisations, prestations sociales et vie associative.'
          : `${label} — Mutuelle des Surveillants de Prison du Mali.`,
        noindex: privatePage || ['authentification', 'reinitialiser-mot-de-passe', '**'].includes(path)},
      ...(route.children ? {children: withPageMetadata(route.children, privatePage)} : {}),
    };
  });
}

@Injectable()
export class PageTitleStrategy extends TitleStrategy {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);

  override updateTitle(snapshot: RouterStateSnapshot): void {
    const title = this.buildTitle(snapshot) ?? 'MSP — Mutuelle des Surveillants de Prison du Mali';
    let route = snapshot.root;
    while (route.firstChild) route = route.firstChild;
    const description = route.data['description'] ?? 'Mutuelle des Surveillants de Prison du Mali.';
    this.title.setTitle(title);
    this.meta.updateTag({name: 'description', content: description});
    this.meta.updateTag({name: 'robots', content: route.data['noindex'] ? 'noindex, nofollow' : 'index, follow'});
    this.meta.updateTag({property: 'og:title', content: title});
    this.meta.updateTag({property: 'og:description', content: description});
    this.meta.updateTag({property: 'og:type', content: 'website'});
    this.meta.updateTag({property: 'og:locale', content: 'fr_ML'});
  }
}
