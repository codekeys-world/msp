package com.codekeys.adjoint;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdjointMapper {

    public AdjointResponse adjointToAdjointResponse(Adjoint adjoint) {
        return AdjointResponse.builder()
                .id(adjoint.getId())
                .slug(adjoint.getSlug())
                .delegantNomComplet(adjoint.getDelegant().getPrenom() + " " + adjoint.getDelegant().getNom())
                .delegantMatricule(adjoint.getDelegant().getMatricule())
                .delegantTelephone(adjoint.getDelegant().getTelephonePrincipal())
                .adjointNomComplet(adjoint.getAdjoint().getPrenom() + " " + adjoint.getAdjoint().getNom())
                .adjointMatricule(adjoint.getAdjoint().getMatricule())
                .adjointTelephone(adjoint.getAdjoint().getTelephonePrincipal())
                .roleDelegue(adjoint.getRoleDelegue())
                .dateDebut(adjoint.getDateDebut())
                .dateFin(adjoint.getDateFin())
                .estActif(adjoint.isEstActif())
                .motif(adjoint.getMotif())
                .dateCreation(adjoint.getDateCreation() != null ? adjoint.getDateCreation().toLocalDate() : null)
                .build();
    }

    public List<AdjointResponse> adjointListToResponseList(List<Adjoint> adjoints) {
        return adjoints.stream()
                .map(this::adjointToAdjointResponse)
                .toList();
    }

}
