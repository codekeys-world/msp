package com.codekeys.pv;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PvMapper {
    private final DocumentService documentService;

    public Pv pvRequestToPv(PvRequest request) {
        return Pv.builder()
                .numero(request.getNumero().trim())
                .objet(request.getObjet().trim())
                .type(request.getType().trim())
                .datePv(request.getDatePv())
                .observation(request.getObservation())
                .build();
    }

    public PvResponse pvToPvResponse(Pv pv) {
        PvResponse response = new PvResponse();
        response.setSlug(pv.getSlug());
        response.setNumero(pv.getNumero());
        response.setObjet(pv.getObjet());
        response.setType(pv.getType());
        response.setDatePv(pv.getDatePv());
        response.setObservation(pv.getObservation());
        response.setDocumentPv(pv.getDocumentPv() != null
                ? documentService.readFileFromLocation(pv.getDocumentPv().getChemin()) : null);
        response.setNomFichierPv(pv.getDocumentPv() != null ? pv.getDocumentPv().getNom() : null);
        response.setTypeFichierPv(pv.getDocumentPv() != null ? pv.getDocumentPv().getType() : null);
        response.setDateCreation(pv.getDateCreation());
        response.setDateModification(pv.getDateModification());
        response.setCreerPar(pv.getCreerPar());
        response.setModifierPar(pv.getModifierPar());
        if (pv.getValidationEntite() != null) {
            response.setDateValidation(pv.getValidationEntite().getDateValidation());
            response.setDateRefus(pv.getValidationEntite().getDateRefus());
            response.setMessageValidation(pv.getValidationEntite().getMessage());
            if (pv.getValidationEntite().getValidateur() != null) {
                response.setValidateur(pv.getValidationEntite().getValidateur().getNomComplet());
            }
        }
        return response;
    }
}
