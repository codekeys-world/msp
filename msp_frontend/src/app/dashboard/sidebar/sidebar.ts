import { Component, Input, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from "@angular/router";
import {AuthentificationService} from '../../authentification/services/authentification-service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrls: ['../dashboard.shared.scss','./sidebar.scss'],
})
export class Sidebar {
  readonly auth = inject(AuthentificationService);

  canListAdherents() { return this.auth.canListAdherents(); }
  canManageAdherents() { return this.auth.canManageAdherents(); }
  canConfectionCard() { return this.auth.canConfectionCard(); }
  canListPv() { return this.auth.hasRole('LISTER_PV') === true; }
  canAddPv() { return this.auth.hasRole('AJOUTER_PV') === true; }
  canListOrdreMission() { return this.auth.hasRole('LISTER_ORDRE_MISSION') === true; }
  canAddOrdreMission() { return this.auth.hasRole('AJOUTER_ORDRE_MISSION') === true; }
  canListCourriers() { return this.auth.hasRole('LISTER_COURRIER') === true; }
  canAddCourrier() { return this.auth.hasRole('AJOUTER_COURRIER') === true; }
  canListPlaintes() { return this.auth.hasRole('LISTER_PLAINTE') === true; }
  canAddPlainte() { return this.auth.hasRole('AJOUTER_PLAINTE') === true; }
  canListConvocations() { return this.auth.hasRole('LISTER_CONVOCATION') === true; }
  canAddConvocation() { return this.auth.hasRole('AJOUTER_CONVOCATION') === true; }
  canListCasSociaux() { return this.auth.hasRole('LISTER_CAS_SOCIAUX') === true; }
  canAddCasSocial() { return this.auth.hasRole('AJOUTER_CAS_SOCIAUX') === true; }
  canListPrestations() { return this.auth.hasRole('LISTER_PRESTATION') === true; }
  canAddPrestation() { return this.auth.hasRole('AJOUTER_PRESTATION') === true; }
  canListArchive() { return this.auth.hasRole('LISTER_ARCHIVE') === true; }
  canViewControle() { return this.auth.canViewControle(); }
  canListEncaissements() { return this.auth.hasRole('LISTER_ENCAISSEMENT') === true; }
  canAddEncaissement() { return this.auth.hasRole('AJOUTER_ENCAISSEMENT') === true; }
  canViewSituationEncaissement() { return this.auth.hasRole('LISTER_ENCAISSEMENT') === true; }
  canListDepenses() { return this.auth.canListerDepense(); }
  canAddDepense() { return this.auth.canEngagerDepense(); }
  canValiderDepense() { return this.auth.canValiderDecaissement(); }
  canManageMateriel() { return this.auth.canManageMateriel(); }
  canViewMateriel() { return this.auth.hasRole('LISTER_MATERIEL') === true || this.auth.canManageMateriel(); }
  canDeleguerAdjoint() {
    return this.auth.hasRole('DELEGUER_ADJOINT') === true ||
      this.auth.hasRole('SECRETAIRE_ADMINISTRATIF') === true ||
      this.auth.hasRole('PRESIDENT') === true ||
      this.auth.hasRole('TRESORIER') === true ||
      this.auth.hasRole('SECRETAIRE_ORGANISATION') === true ||
      this.auth.hasRole('SECRETAIRE_AFFAIRE_SOCIALE') === true;
  }

  canManageAdministration() {
    return this.auth.canManageUserAccess();
  }

  hasAnyAdherentAccess() {
    return this.canListAdherents() || this.canManageAdherents() || this.canConfectionCard();
  }

  @Input()
  collapsed = false;
}
