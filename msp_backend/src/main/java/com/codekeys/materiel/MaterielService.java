package com.codekeys.materiel;

import com.codekeys.common.PageReponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterielService {
    private final MaterielRepository materielRepository;
    private final MaterielMapper materielMapper;

    @Transactional
    public MaterielResponse enregistrer(@Valid MaterielRequest request) {
        validerChamps(request);
        verifierReferenceDisponible(request.getNom(), null);

        Materiel materiel = materielMapper.requestToEntity(request);
        materiel.setSlug(UUID.randomUUID().toString());
        return materielMapper.entityToResponse(materielRepository.save(materiel));
    }

    @Transactional
    public PageReponse<MaterielResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination du matériel sont invalides.");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Materiel> resultats = StringUtils.hasText(terme)
                ? materielRepository.rechercherParInformation(terme.trim(), pageable)
                : materielRepository.findAll(pageable);
        List<MaterielResponse> contenu = resultats.stream()
                .map(materielMapper::entityToResponse)
                .toList();
        return new PageReponse<>(contenu, resultats.getNumber(), resultats.getSize(),
                resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    @Transactional
    public MaterielResponse rechercherParSlug(String slug) {
        return materielMapper.entityToResponse(rechercher(slug));
    }

    @Transactional
    public MaterielResponse modifier(String slug, @Valid MaterielRequest request) {
        validerChamps(request);
        Materiel materiel = rechercher(slug);
        verifierReferenceDisponible(request.getNom(), slug);

        materiel.setNom(request.getNom());
        materiel.setCategorie(request.getCategorie());
        materiel.setFournisseur(request.getFournisseur());
        materiel.setQuantite(request.getQuantite());
        materiel.setPrixUnitaire(request.getPrixUnitaire());
        materiel.setDateAcquisition(request.getDateAcquisition());
        materiel.setEmplacement(request.getEmplacement());
        materiel.setNumeroSerie(request.getNumeroSerie());
        materiel.setObservation(request.getObservation());
        materiel.setStatut(request.getStatut());

        return materielMapper.entityToResponse(materielRepository.save(materiel));
    }

    @Transactional
    public boolean supprimer(String slug) {
        Materiel materiel = rechercher(slug);
        materielRepository.delete(materiel);
        return true;
    }

    private Materiel rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug du matériel est obligatoire.");
        }
        return materielRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucun matériel n'a été trouvé."));
    }

    private void validerChamps(MaterielRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Les informations du matériel sont obligatoires.");
        }
        if (request.getStatut() == null) {
            throw new IllegalArgumentException("Le statut du matériel est obligatoire.");
        }
        if (request.getQuantite() != null && request.getQuantite() < 0) {
            throw new IllegalArgumentException("La quantité du matériel ne peut pas être négative.");
        }
        if (request.getPrixUnitaire() != null && request.getPrixUnitaire().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif.");
        }
    }

    private void verifierReferenceDisponible(String nom, String slugExclu) {
        if (!StringUtils.hasText(nom)) {
            throw new IllegalArgumentException("Le nom du matériel est obligatoire.");
        }
        boolean existe = slugExclu == null
                ? materielRepository.existsByNomIgnoreCase(nom.trim())
                : materielRepository.existsByNomIgnoreCaseAndSlugNot(nom.trim(), slugExclu);
        if (existe) {
            throw new IllegalArgumentException("Ce matériel existe déjà dans le système.");
        }
    }
}
