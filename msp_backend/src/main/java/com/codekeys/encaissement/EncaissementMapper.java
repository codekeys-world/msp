package com.codekeys.encaissement;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncaissementMapper {
    private final DocumentService documentService;

    public Encaissement requestToEntity(EncaissementRequest request) {
        Encaissement encaissement = new Encaissement();
        mettreAJourEntity(encaissement, request);
        return encaissement;
    }

    public void mettreAJourEntity(Encaissement encaissement, EncaissementRequest request) {
        encaissement.setTypeEncaissement(request.getTypeEncaissement());
        encaissement.setDateEncaissement(request.getDateEncaissement());
        encaissement.setDateDebutPeriode(request.getDateDebutPeriode());
        encaissement.setDateFinPeriode(request.getDateFinPeriode());
        encaissement.setMontant(request.getMontant());
        encaissement.setTypeBudget(nettoyer(request.getTypeBudget()));
        encaissement.setObservation(nettoyer(request.getObservation()));
    }

    public EncaissementResponse entityToResponse(Encaissement encaissement) {
        return entityToResponse(encaissement, true);
    }

    public EncaissementResponse entityToResponse(Encaissement encaissement, boolean inclureJustificatif) {
        EncaissementResponse response = new EncaissementResponse();
        response.setSlug(encaissement.getSlug());
        response.setTypeEncaissement(encaissement.getTypeEncaissement());
        response.setDateEncaissement(encaissement.getDateEncaissement());
        response.setDateDebutPeriode(encaissement.getDateDebutPeriode());
        response.setDateFinPeriode(encaissement.getDateFinPeriode());
        response.setMontant(encaissement.getMontant());
        response.setTypeBudget(encaissement.getTypeBudget());
        response.setObservation(encaissement.getObservation());
        response.setJustificatif(inclureJustificatif && encaissement.getJustificatif() != null
                ? documentService.readFileFromLocation(encaissement.getJustificatif().getChemin()) : null);
        response.setNomFichierJustificatif(encaissement.getJustificatif() != null
                ? encaissement.getJustificatif().getNom() : null);
        response.setTypeFichierJustificatif(encaissement.getJustificatif() != null
                ? encaissement.getJustificatif().getType() : null);
        response.setDateCreation(encaissement.getDateCreation());
        response.setDateModification(encaissement.getDateModification());
        response.setCreerPar(encaissement.getCreerPar());
        response.setModifierPar(encaissement.getModifierPar());
        return response;
    }

    private String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }
}
