package com.codekeys.adjoint;

import com.codekeys.common.PageReponse;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdjointService {

    private final AdjointRepository adjointRepository;
    private final AdjointMapper adjointMapper;
    private final UtilisateurService utilisateurService;

    public AdjointResponse designerAdjoint(@Valid AdjointRequest request, String delegantSlug, String roleDelegue) {
        Utilisateur delegant = utilisateurService.rechercherUtilisateurParSlug(delegantSlug);
        Utilisateur adjoint = utilisateurService.rechercherUtilisateurParSlug(request.getAdjointSlug());

        if (delegant.getSlug().equals(adjoint.getSlug())) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas se désigner lui-même comme adjoint.");
        }

        Adjoint adjointExistant = adjointRepository.findActiveByDelegantSlugAndRoleDelegue(delegantSlug, roleDelegue)
                .orElse(null);
        if (adjointExistant != null) {
            adjointExistant.setEstActif(false);
            adjointExistant.setDateFin(LocalDate.now());
            adjointRepository.save(adjointExistant);
        }

        Adjoint adjointEntity = Adjoint.builder()
                .slug(UUID.randomUUID().toString())
                .delegant(delegant)
                .adjoint(adjoint)
                .roleDelegue(roleDelegue)
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .motif(request.getMotif())
                .estActif(true)
                .build();

        adjointEntity = adjointRepository.save(adjointEntity);
        return adjointMapper.adjointToAdjointResponse(adjointEntity);
    }

    public PageReponse<AdjointResponse> listerAdjointsParDelegant(String delegantSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<@NonNull Adjoint> adjoints = adjointRepository.findByDelegantSlug(delegantSlug, pageable);
        List<AdjointResponse> responses = adjoints.stream()
                .map(adjointMapper::adjointToAdjointResponse)
                .toList();
        return new PageReponse<>(
                responses,
                adjoints.getNumber(),
                adjoints.getSize(),
                adjoints.getTotalElements(),
                adjoints.getTotalPages(),
                adjoints.isFirst(),
                adjoints.isLast()
        );
    }

    public PageReponse<AdjointResponse> listerAdjointsParAdjoint(String adjointSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<@NonNull Adjoint> adjoints = adjointRepository.findByAdjointSlug(adjointSlug, pageable);
        List<AdjointResponse> responses = adjoints.stream()
                .map(adjointMapper::adjointToAdjointResponse)
                .toList();
        return new PageReponse<>(
                responses,
                adjoints.getNumber(),
                adjoints.getSize(),
                adjoints.getTotalElements(),
                adjoints.getTotalPages(),
                adjoints.isFirst(),
                adjoints.isLast()
        );
    }

    public AdjointResponse rechercherAdjoint(String slug) {
        Adjoint adjoint = adjointRepository.findBySlug(slug).orElseThrow(
                () -> new EntityNotFoundException("Aucun adjoint n'a été trouvé.")
        );
        return adjointMapper.adjointToAdjointResponse(adjoint);
    }

    public AdjointResponse desactiverAdjoint(String slug) {
        Adjoint adjoint = adjointRepository.findBySlug(slug).orElseThrow(
                () -> new EntityNotFoundException("Aucun adjoint n'a été trouvé.")
        );
        adjoint.setEstActif(false);
        adjoint.setDateFin(LocalDate.now());
        adjoint = adjointRepository.save(adjoint);
        return adjointMapper.adjointToAdjointResponse(adjoint);
    }

    public AdjointResponse reactivierAdjoint(String slug) {
        Adjoint adjoint = adjointRepository.findBySlug(slug).orElseThrow(
                () -> new EntityNotFoundException("Aucun adjoint n'a été trouvé.")
        );
        adjoint.setEstActif(true);
        adjoint.setDateFin(null);
        adjoint = adjointRepository.save(adjoint);
        return adjointMapper.adjointToAdjointResponse(adjoint);
    }

    public AdjointResponse getAdjointActif(String delegantSlug, String roleDelegue) {
        Adjoint adjoint = adjointRepository.findActiveByDelegantSlugAndRoleDelegue(delegantSlug, roleDelegue)
                .orElseThrow(() -> new EntityNotFoundException("Aucun adjoint actif n'a été trouvé pour ce rôle."));
        return adjointMapper.adjointToAdjointResponse(adjoint);
    }

    public PageReponse<AdjointResponse> listerAdjointsActifs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        Page<@NonNull Adjoint> adjoints = adjointRepository.findActifsParDate(LocalDate.now(), pageable);
        List<AdjointResponse> responses = adjoints.stream()
                .map(adjointMapper::adjointToAdjointResponse)
                .toList();
        return new PageReponse<>(
                responses,
                adjoints.getNumber(),
                adjoints.getSize(),
                adjoints.getTotalElements(),
                adjoints.getTotalPages(),
                adjoints.isFirst(),
                adjoints.isLast()
        );
    }

}
