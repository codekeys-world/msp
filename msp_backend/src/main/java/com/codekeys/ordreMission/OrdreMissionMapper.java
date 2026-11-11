package com.codekeys.ordreMission;

import com.codekeys.fichier.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdreMissionMapper {
    private final DocumentService documentService;
    public OrdreMission ordreMissionRequestToOrdreMission(OrdreMissionRequest ordreMissionRequest) {
        return OrdreMission.builder()
                .id(ordreMissionRequest.getId())
                .libelle(ordreMissionRequest.getLibelle())
                .objet(ordreMissionRequest.getObjet())
                .imputationBudgetaire(ordreMissionRequest.getImputationBudgetaire())
                .moyenDeplacement(ordreMissionRequest.getMoyenDeplacement())
                .itineraire(ordreMissionRequest.getItineraire())
                .dateDepart(ordreMissionRequest.getDateDepart())
                .dateRetour(ordreMissionRequest.getDateRetour())
                .observation(ordreMissionRequest.getObservation())
                .build();
    }

    public OrdreMissionResponse ordreMissionToOrdreMissionResponse(OrdreMission ordreMission) {
        OrdreMissionResponse ordreMissionResponse = new OrdreMissionResponse();
        ordreMissionResponse.setSlug(ordreMission.getSlug());
        ordreMissionResponse.setLibelle(ordreMission.getLibelle());
        ordreMissionResponse.setObjet(ordreMission.getObjet());
        ordreMissionResponse.setImputationBudgetaire(ordreMission.getImputationBudgetaire());
        ordreMissionResponse.setMoyenDeplacement(ordreMission.getMoyenDeplacement());
        ordreMissionResponse.setItineraire(ordreMission.getItineraire());
        ordreMissionResponse.setOrdreMission(ordreMission.getOrdreMission() != null && ordreMission.getOrdreMission().getChemin() != null
                ? this.documentService.readFileFromLocation(ordreMission.getOrdreMission().getChemin()) : null);
        ordreMissionResponse.setNomFichierOrdreMission(ordreMission.getOrdreMission() != null
                ? ordreMission.getOrdreMission().getNom() : null);
        ordreMissionResponse.setTypeFichierOrdreMission(ordreMission.getOrdreMission() != null
                ? ordreMission.getOrdreMission().getType() : null);
        ordreMissionResponse.setDateDepart(ordreMission.getDateDepart());
        ordreMissionResponse.setDateRetour(ordreMission.getDateRetour());
        ordreMissionResponse.setObservation(ordreMission.getObservation());
        ordreMissionResponse.setCreerPar(ordreMission.getCreerPar());
        ordreMissionResponse.setModifierPar(ordreMission.getModifierPar());
        ordreMissionResponse.setDateCreation(ordreMission.getDateCreation());
        ordreMissionResponse.setDateModification(ordreMission.getDateModification());
        return ordreMissionResponse;
    }
}
