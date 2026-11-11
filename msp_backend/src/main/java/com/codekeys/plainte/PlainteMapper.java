package com.codekeys.plainte;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlainteMapper {
    private final DocumentService documentService;

    public Plainte plainteRequestToPlainte(PlainteRequest request) {
        return Plainte.builder()
                .datePv(request.getDatePv())
                .observation(request.getObservation())
                .build();
    }

    public PlainteResponse plainteToPlainteResponse(Plainte plainte) {
        PlainteResponse response = new PlainteResponse();
        response.setSlug(plainte.getSlug());
        response.setDatePv(plainte.getDatePv());
        response.setObservation(plainte.getObservation());
        response.setDocumentPlainte(plainte.getDocumentPlainte() != null
                ? documentService.readFileFromLocation(plainte.getDocumentPlainte().getChemin()) : null);
        response.setNomFichierPlainte(plainte.getDocumentPlainte() != null
                ? plainte.getDocumentPlainte().getNom() : null);
        response.setTypeFichierPlainte(plainte.getDocumentPlainte() != null
                ? plainte.getDocumentPlainte().getType() : null);
        response.setDateCreation(plainte.getDateCreation());
        response.setDateModification(plainte.getDateModification());
        response.setCreerPar(plainte.getCreerPar());
        response.setModifierPar(plainte.getModifierPar());
        if (plainte.getValidationEntite() != null) {
            response.setDateValidation(plainte.getValidationEntite().getDateValidation());
            response.setDateRefus(plainte.getValidationEntite().getDateRefus());
            response.setMessageValidation(plainte.getValidationEntite().getMessage());
            if (plainte.getValidationEntite().getValidateur() != null) {
                response.setValidateur(plainte.getValidationEntite().getValidateur().getNomComplet());
            }
        }
        return response;
    }
}
