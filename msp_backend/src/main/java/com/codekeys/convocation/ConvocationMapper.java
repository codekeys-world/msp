package com.codekeys.convocation;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvocationMapper {
    private final DocumentService documentService;

    public Convocation convocationRequestToConvocation(ConvocationRequest request) {
        return Convocation.builder()
                .description(request.getDescription().trim())
                .dateConvocation(request.getDateConvocation())
                .build();
    }

    public ConvocationResponse convocationToConvocationResponse(Convocation convocation) {
        ConvocationResponse response = new ConvocationResponse();
        response.setSlug(convocation.getSlug());
        response.setDescription(convocation.getDescription());
        response.setDateConvocation(convocation.getDateConvocation());
        response.setDocumentConvocation(convocation.getDocumentConvocation() != null
                ? documentService.readFileFromLocation(convocation.getDocumentConvocation().getChemin()) : null);
        response.setNomFichierConvocation(convocation.getDocumentConvocation() != null
                ? convocation.getDocumentConvocation().getNom() : null);
        response.setTypeFichierConvocation(convocation.getDocumentConvocation() != null
                ? convocation.getDocumentConvocation().getType() : null);
        response.setDateCreation(convocation.getDateCreation());
        response.setDateModification(convocation.getDateModification());
        response.setCreerPar(convocation.getCreerPar());
        response.setModifierPar(convocation.getModifierPar());
        if (convocation.getValidationEntite() != null) {
            response.setDateValidation(convocation.getValidationEntite().getDateValidation());
            response.setDateRefus(convocation.getValidationEntite().getDateRefus());
            response.setMessageValidation(convocation.getValidationEntite().getMessage());
            if (convocation.getValidationEntite().getValidateur() != null) {
                response.setValidateur(convocation.getValidationEntite().getValidateur().getNomComplet());
            }
        }
        return response;
    }
}
