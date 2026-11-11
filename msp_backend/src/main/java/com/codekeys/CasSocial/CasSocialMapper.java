package com.codekeys.CasSocial;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CasSocialMapper {
    private final DocumentService documentService;

    public CasSocial requestToEntity(CasSocialRequest request) {
        CasSocial casSocial = new CasSocial();
        mettreAJourEntity(casSocial, request);
        return casSocial;
    }

    public void mettreAJourEntity(CasSocial casSocial, CasSocialRequest request) {
        casSocial.setTypeCas(request.getTypeCas());
        casSocial.setDateCas(request.getDateCas());
        casSocial.setHeuresHospitalisation(request.getHeuresHospitalisation());
        casSocial.setLienDefunt(request.getLienDefunt());
        casSocial.setNomDefunt(nettoyer(request.getNomDefunt()));
        casSocial.setPrenomDefunt(nettoyer(request.getPrenomDefunt()));
        casSocial.setTypeBeneficiaire(request.getTypeBeneficiaire());
        casSocial.setNomBeneficiaire(nettoyer(request.getNomBeneficiaire()));
        casSocial.setPrenomBeneficiaire(nettoyer(request.getPrenomBeneficiaire()));
        casSocial.setTelephoneBeneficiaire(nettoyer(request.getTelephoneBeneficiaire()));
        casSocial.setDureeCotisationMois(request.getDureeCotisationMois());
        casSocial.setMontantCotisationsVersees(request.getMontantCotisationsVersees());
        casSocial.setObservation(nettoyer(request.getObservation()));
    }

    public CasSocialResponse entityToResponse(CasSocial casSocial) {
        CasSocialResponse response = new CasSocialResponse();
        response.setSlug(casSocial.getSlug());
        response.setTypeCas(casSocial.getTypeCas());
        response.setDateCas(casSocial.getDateCas());
        response.setHeuresHospitalisation(casSocial.getHeuresHospitalisation());
        response.setLienDefunt(casSocial.getLienDefunt());
        response.setNomDefunt(casSocial.getNomDefunt());
        response.setPrenomDefunt(casSocial.getPrenomDefunt());
        response.setTypeBeneficiaire(casSocial.getTypeBeneficiaire());
        response.setNomBeneficiaire(casSocial.getNomBeneficiaire());
        response.setPrenomBeneficiaire(casSocial.getPrenomBeneficiaire());
        response.setTelephoneBeneficiaire(casSocial.getTelephoneBeneficiaire());
        response.setMontantPrestation(casSocial.getMontantPrestation());
        response.setDureeCotisationMois(casSocial.getDureeCotisationMois());
        response.setMontantCotisationsVersees(casSocial.getMontantCotisationsVersees());
        response.setMontantRemboursement(casSocial.getMontantRemboursement());
        response.setStatutPrestation(casSocial.getStatutPrestation());
        response.setObservation(casSocial.getObservation());
        response.setAdherentSlug(casSocial.getAdherent().getSlug());
        response.setAdherentNomComplet(casSocial.getAdherent().getNomComplet());
        response.setAdherentNumeroInscription(casSocial.getAdherent().getNumeroInscription());
        response.setJustificatif(casSocial.getJustificatif() != null
                ? documentService.readFileFromLocation(casSocial.getJustificatif().getChemin()) : null);
        response.setNomFichierJustificatif(casSocial.getJustificatif() != null
                ? casSocial.getJustificatif().getNom() : null);
        response.setTypeFichierJustificatif(casSocial.getJustificatif() != null
                ? casSocial.getJustificatif().getType() : null);
        response.setDateCreation(casSocial.getDateCreation());
        response.setDateModification(casSocial.getDateModification());
        response.setCreerPar(casSocial.getCreerPar());
        response.setModifierPar(casSocial.getModifierPar());
        return response;
    }

    private String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }
}
