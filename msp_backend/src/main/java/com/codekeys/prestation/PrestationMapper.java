package com.codekeys.prestation;

import com.codekeys.CasSocial.LienDefunt;
import com.codekeys.CasSocial.TypeBeneficiaire;
import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrestationMapper {
    private final DocumentService documentService;

    public Prestation requestToEntity(PrestationRequest request) {
        Prestation prestation = new Prestation();
        mettreAJourEntity(prestation, request);
        return prestation;
    }

    public void mettreAJourEntity(Prestation prestation, PrestationRequest request) {
        prestation.setTypePrestation(request.getTypePrestation());
        prestation.setDatePrestation(request.getDatePrestation());
        prestation.setLienDefunt(request.getLienDefunt());
        prestation.setNomDefunt(nettoyer(request.getNomDefunt()));
        prestation.setPrenomDefunt(nettoyer(request.getPrenomDefunt()));
        prestation.setTypeBeneficiaire(request.getTypeBeneficiaire());
        prestation.setNomBeneficiaire(nettoyer(request.getNomBeneficiaire()));
        prestation.setPrenomBeneficiaire(nettoyer(request.getPrenomBeneficiaire()));
        prestation.setTelephoneBeneficiaire(nettoyer(request.getTelephoneBeneficiaire()));
        prestation.setDureeCotisationMois(request.getDureeCotisationMois());
        prestation.setMontantCotisationsVersees(request.getMontantCotisationsVersees());
        prestation.setObservation(nettoyer(request.getObservation()));
    }

    public PrestationResponse entityToResponse(Prestation prestation) {
        PrestationResponse response = new PrestationResponse();
        response.setSlug(prestation.getSlug());
        response.setTypePrestation(prestation.getTypePrestation());
        response.setDatePrestation(prestation.getDatePrestation());
        response.setLienDefunt(prestation.getLienDefunt());
        response.setNomDefunt(prestation.getNomDefunt());
        response.setPrenomDefunt(prestation.getPrenomDefunt());
        response.setTypeBeneficiaire(prestation.getTypeBeneficiaire());
        response.setNomBeneficiaire(prestation.getNomBeneficiaire());
        response.setPrenomBeneficiaire(prestation.getPrenomBeneficiaire());
        response.setTelephoneBeneficiaire(prestation.getTelephoneBeneficiaire());
        response.setMontantPrestation(prestation.getMontantPrestation());
        response.setDureeCotisationMois(prestation.getDureeCotisationMois());
        response.setMontantCotisationsVersees(prestation.getMontantCotisationsVersees());
        response.setMontantRemboursement(prestation.getMontantRemboursement());
        response.setStatutPrestation(prestation.getStatutPrestation());
        response.setObservation(prestation.getObservation());
        response.setAdherentSlug(prestation.getAdherent().getSlug());
        response.setAdherentNomComplet(prestation.getAdherent().getNomComplet());
        response.setAdherentNumeroInscription(prestation.getAdherent().getNumeroInscription());
        response.setJustificatif(prestation.getJustificatif() != null
                ? documentService.readFileFromLocation(prestation.getJustificatif().getChemin()) : null);
        response.setNomFichierJustificatif(prestation.getJustificatif() != null
                ? prestation.getJustificatif().getNom() : null);
        response.setTypeFichierJustificatif(prestation.getJustificatif() != null
                ? prestation.getJustificatif().getType() : null);
        response.setDateCreation(prestation.getDateCreation());
        response.setDateModification(prestation.getDateModification());
        response.setCreerPar(prestation.getCreerPar());
        response.setModifierPar(prestation.getModifierPar());
        return response;
    }

    private String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }
}
