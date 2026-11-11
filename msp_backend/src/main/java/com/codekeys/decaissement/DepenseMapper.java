package com.codekeys.decaissement;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepenseMapper {
    private final DocumentService documentService;

    public Depense requestToEntity(DepenseRequest request) {
        Depense depense = new Depense();
        mettreAJourEntity(depense, request);
        return depense;
    }

    public void mettreAJourEntity(Depense depense, DepenseRequest request) {
        depense.setLibelle(request.getLibelle());
        depense.setDateDepense(request.getDateDepense());
        depense.setMontant(request.getMontant());
        depense.setObservation(nettoyer(request.getObservation()));
    }

    public DepenseResponse entityToResponse(Depense depense) {
        return entityToResponse(depense, true);
    }

    public DepenseResponse entityToResponse(Depense depense, boolean inclureJustificatif) {
        DepenseResponse response = new DepenseResponse();
        response.setSlug(depense.getSlug());
        response.setLibelle(depense.getLibelle());
        response.setDateDepense(depense.getDateDepense());
        response.setMontant(depense.getMontant());
        response.setStatut(depense.getStatut());
        response.setObservation(depense.getObservation());
        response.setObservationValidation(depense.getObservationValidation());
        response.setFraisDeGestionSlug(depense.getFraisDeGestion() != null ? depense.getFraisDeGestion().getSlug() : null);
        response.setFraisDeGestionAnnee(depense.getFraisDeGestion() != null ? depense.getFraisDeGestion().getAnnee() : null);
        response.setJustificatif(inclureJustificatif && depense.getJustificatif() != null
                ? documentService.readFileFromLocation(depense.getJustificatif().getChemin()) : null);
        response.setNomFichierJustificatif(depense.getJustificatif() != null
                ? depense.getJustificatif().getNom() : null);
        response.setTypeFichierJustificatif(depense.getJustificatif() != null
                ? depense.getJustificatif().getType() : null);
        response.setDateCreation(depense.getDateCreation());
        response.setDateModification(depense.getDateModification());
        response.setCreerPar(depense.getCreerPar());
        response.setModifierPar(depense.getModifierPar());
        return response;
    }

    private String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }
}
