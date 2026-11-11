package com.codekeys.decaissement;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraisDeGestionMapper {
    private final DocumentService documentService;

    public FraisDeGestion requestToEntity(FraisDeGestionRequest request) {
        FraisDeGestion fraisDeGestion = new FraisDeGestion();
        mettreAJourEntity(fraisDeGestion, request);
        return fraisDeGestion;
    }

    public void mettreAJourEntity(FraisDeGestion fraisDeGestion, FraisDeGestionRequest request) {
        fraisDeGestion.setAnnee(request.getAnnee());
        fraisDeGestion.setMontantTotal(request.getMontantTotal());
        fraisDeGestion.setObservation(nettoyer(request.getObservation()));
    }

    public FraisDeGestionResponse entityToResponse(FraisDeGestion fraisDeGestion) {
        long montantDepense = fraisDeGestion.getDepenses() != null
                ? fraisDeGestion.getDepenses().stream()
                .filter(d -> d.getStatut() == StatutDepense.VALIDEE)
                .mapToLong(Depense::getMontant)
                .sum()
                : 0L;
        long nombreDepenses = fraisDeGestion.getDepenses() != null ? fraisDeGestion.getDepenses().size() : 0;
        return entityToResponse(fraisDeGestion, montantDepense, nombreDepenses, true);
    }

    public FraisDeGestionResponse entityToResponse(FraisDeGestion fraisDeGestion, long montantDepense,
                                                   long nombreDepenses, boolean inclureJustificatif) {
        FraisDeGestionResponse response = new FraisDeGestionResponse();
        response.setSlug(fraisDeGestion.getSlug());
        response.setAnnee(fraisDeGestion.getAnnee());
        response.setMontantTotal(fraisDeGestion.getMontantTotal());
        response.setObservation(fraisDeGestion.getObservation());

        response.setMontantDepense(montantDepense);
        response.setSolde(fraisDeGestion.getMontantTotal() - montantDepense);
        response.setNombreDepenses(Math.toIntExact(nombreDepenses));

        response.setJustificatif(inclureJustificatif && fraisDeGestion.getJustificatif() != null
                ? documentService.readFileFromLocation(fraisDeGestion.getJustificatif().getChemin()) : null);
        response.setNomFichierJustificatif(fraisDeGestion.getJustificatif() != null
                ? fraisDeGestion.getJustificatif().getNom() : null);
        response.setTypeFichierJustificatif(fraisDeGestion.getJustificatif() != null
                ? fraisDeGestion.getJustificatif().getType() : null);
        response.setDateCreation(fraisDeGestion.getDateCreation());
        response.setDateModification(fraisDeGestion.getDateModification());
        response.setCreerPar(fraisDeGestion.getCreerPar());
        response.setModifierPar(fraisDeGestion.getModifierPar());
        return response;
    }

    private String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }
}
