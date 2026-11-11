import {Injectable} from '@angular/core';
import Swal from 'sweetalert2/dist/sweetalert2.esm.all.js';

@Injectable({
  providedIn: 'root',
})
export class SweetAlertService {
  confirmDeletion(entityName: string, itemName: string, detail?: string): Promise<boolean> {
    return Swal.fire({
      titleText: `Supprimer ${entityName} ?`,
      text: detail
        ? `« ${itemName} » sera supprimé. ${detail} Cette action est irréversible.`
        : `« ${itemName} » sera supprimé. Cette action est irréversible.`,
      icon: 'warning',
      showCloseButton: true,
      showCancelButton: true,
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      reverseButtons: true,
      focusCancel: true,
      allowOutsideClick: false,
      buttonsStyling: true,
    }).then(result => result.isConfirmed);
  }

  showSuccess(message: string): void {
    void Swal.fire({
      text: message,
      icon: 'success',
      showCloseButton: true,
      confirmButtonText: 'Fermer',
      confirmButtonColor: '#198754',
    });
  }

  showError(message: string): void {
    void Swal.fire({
      titleText: 'Opération impossible',
      text: message,
      icon: 'error',
      showCloseButton: true,
      confirmButtonText: 'Fermer',
      confirmButtonColor: '#198754',
    });
  }
}
