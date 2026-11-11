package com.codekeys.courrier;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourrierMapper {
    private final DocumentService documentService;

    public Courrier courrierRequestToCourrier(CourrierRequest request) {
        return Courrier.builder()
                .nom(request.getNom().trim())
                .objet(request.getObjet().trim())
                .typeCourrier(request.getTypeCourrier())
                .description(request.getDescription().trim())
                .dateArrivee(request.getDateArrivee())
                .dateDepart(request.getDateDepart())
                .build();
    }

    public CourrierResponse courrierToCourrierResponse(Courrier courrier) {
        CourrierResponse response = new CourrierResponse();
        response.setSlug(courrier.getSlug());
        response.setNom(courrier.getNom());
        response.setObjet(courrier.getObjet());
        response.setTypeCourrier(courrier.getTypeCourrier());
        response.setDescription(courrier.getDescription());
        response.setDateArrivee(courrier.getDateArrivee());
        response.setDateDepart(courrier.getDateDepart());
        if (courrier.getDocumentCourrier() != null) {
            response.setDocumentCourrier(documentService.readFileFromLocation(courrier.getDocumentCourrier().getChemin()));
            response.setNomFichierCourrier(courrier.getDocumentCourrier().getNom());
            response.setTypeFichierCourrier(courrier.getDocumentCourrier().getType());
        }
        response.setDateCreation(courrier.getDateCreation());
        response.setDateModification(courrier.getDateModification());
        response.setCreerPar(courrier.getCreerPar());
        response.setModifierPar(courrier.getModifierPar());
        if (courrier.getValidationEntite() != null) {
            response.setDateValidation(courrier.getValidationEntite().getDateValidation());
            response.setDateRefus(courrier.getValidationEntite().getDateRefus());
        }
        return response;
    }
}
