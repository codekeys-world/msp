package com.codekeys.materiel;

import org.springframework.stereotype.Component;

@Component
public class MaterielMapper {
    public Materiel requestToEntity(MaterielRequest request) {
        if (request == null) {
            return null;
        }
        Materiel materiel = new Materiel();
        materiel.setNom(request.getNom());
        materiel.setCategorie(request.getCategorie());
        materiel.setFournisseur(request.getFournisseur());
        materiel.setQuantite(request.getQuantite());
        materiel.setPrixUnitaire(request.getPrixUnitaire());
        materiel.setDateAcquisition(request.getDateAcquisition());
        materiel.setEmplacement(request.getEmplacement());
        materiel.setNumeroSerie(request.getNumeroSerie());
        materiel.setObservation(request.getObservation());
        materiel.setStatut(request.getStatut());
        return materiel;
    }

    public MaterielResponse entityToResponse(Materiel materiel) {
        if (materiel == null) {
            return null;
        }
        MaterielResponse response = new MaterielResponse();
        response.setSlug(materiel.getSlug());
        response.setNom(materiel.getNom());
        response.setCategorie(materiel.getCategorie());
        response.setFournisseur(materiel.getFournisseur());
        response.setQuantite(materiel.getQuantite());
        response.setPrixUnitaire(materiel.getPrixUnitaire());
        response.setDateAcquisition(materiel.getDateAcquisition());
        response.setEmplacement(materiel.getEmplacement());
        response.setNumeroSerie(materiel.getNumeroSerie());
        response.setObservation(materiel.getObservation());
        response.setStatut(materiel.getStatut());
        return response;
    }
}
