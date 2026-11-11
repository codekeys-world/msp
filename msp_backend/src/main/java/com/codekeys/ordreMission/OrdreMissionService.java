package com.codekeys.ordreMission;

import com.codekeys.common.PageReponse;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class OrdreMissionService {
    private final OrdreMissionRepository ordreMissionRepository;
    private final OrdreMissionMapper ordreMissionMapper;
    private final DocumentService documentService;

    @Transactional
    public OrdreMissionResponse enregistrerOrdreMission(@Valid OrdreMissionRequest ordreMissionRequest) throws IOException {
        OrdreMission ordreMission = this.ordreMissionMapper.ordreMissionRequestToOrdreMission(ordreMissionRequest);
        MultipartFile fichier = ordreMissionRequest.getOrdreMission();
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier de l'ordre de mission est obligatoire.");
        }
        ordreMission.setSlug(UUID.randomUUID().toString());
        String sousDossierOrdreMission = this.buildSousDossierOrdreMission(ordreMission);
        Document fichierOrdreMission = documentService.uploadFile(fichier, TypeDocument.ORDRE_MISSION, sousDossierOrdreMission);
        ordreMission.setOrdreMission(fichierOrdreMission);
        ordreMission = this.ordreMissionRepository.save(ordreMission);
        return this.ordreMissionMapper.ordreMissionToOrdreMissionResponse(ordreMission);
    }

    private String buildSousDossierOrdreMission(OrdreMission ordreMission) {
        return ordreMission.getSlug() + "/";
    }

    @Transactional
    public PageReponse<OrdreMissionResponse> listerOrdreMission(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination des ordres de mission sont invalides.");
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull OrdreMission> ordreMissions = StringUtils.hasText(terme)
                ? this.ordreMissionRepository.rechercherParInformation(terme.trim(), pageRequest)
                : this.ordreMissionRepository.findAll(pageRequest);
        List<OrdreMissionResponse> ordreMissionResponses = ordreMissions.stream().map(this.ordreMissionMapper::ordreMissionToOrdreMissionResponse)
                .toList();
        return new PageReponse<OrdreMissionResponse>(
                ordreMissionResponses,
                ordreMissions.getNumber(),
                ordreMissions.getSize(),
                ordreMissions.getTotalElements(),
                ordreMissions.getTotalPages(),
                ordreMissions.isFirst(),
                ordreMissions.isLast()
        );
    }

    @Transactional
    public OrdreMissionStatistiques obtenirStatistiques() {
        LocalDate aujourdHui = LocalDate.now();
        return new OrdreMissionStatistiques(
                this.ordreMissionRepository.count(),
                this.ordreMissionRepository.countEnAttenteValidation(),
                this.ordreMissionRepository.countApprouvees(),
                this.ordreMissionRepository.countRefusees(),
                this.ordreMissionRepository.countEnCours(aujourdHui),
                this.ordreMissionRepository.countTerminees(aujourdHui)
        );
    }

    @Transactional
    public OrdreMissionResponse rechercherParSlug(String slug) {
        return this.ordreMissionMapper.ordreMissionToOrdreMissionResponse(this.rechercherOrdreMission(slug));
    }

    @Transactional
    public OrdreMissionResponse modifierOrdreMission(String slug, @Valid OrdreMissionRequest request) throws IOException {
        OrdreMission ordreMission = this.rechercherOrdreMission(slug);
        Document ancienDocument = ordreMission.getOrdreMission();

        ordreMission.setLibelle(request.getLibelle());
        ordreMission.setObjet(request.getObjet());
        ordreMission.setImputationBudgetaire(request.getImputationBudgetaire());
        ordreMission.setMoyenDeplacement(request.getMoyenDeplacement());
        ordreMission.setItineraire(request.getItineraire());
        ordreMission.setDateDepart(request.getDateDepart());
        ordreMission.setDateRetour(request.getDateRetour());
        ordreMission.setObservation(request.getObservation());

        MultipartFile nouveauFichier = request.getOrdreMission();
        if (nouveauFichier != null && !nouveauFichier.isEmpty()) {
            String sousDossier = this.buildSousDossierOrdreMission(ordreMission) + UUID.randomUUID();
            Document documentModifie = this.documentService.uploadFile(
                    nouveauFichier,
                    TypeDocument.ORDRE_MISSION,
                    sousDossier
            );
            ordreMission.setOrdreMission(documentModifie);
        }

        ordreMission = this.ordreMissionRepository.save(ordreMission);
        if (ordreMission.getOrdreMission() != ancienDocument) {
            this.documentService.supprimerFichier(ancienDocument);
        }
        return this.ordreMissionMapper.ordreMissionToOrdreMissionResponse(ordreMission);
    }

    @Transactional
    public boolean supprimerOrdreMission(String slug) throws IOException {
        OrdreMission ordreMission = this.rechercherOrdreMission(slug);
        this.documentService.supprimerFichier(ordreMission.getOrdreMission());
        this.ordreMissionRepository.delete(ordreMission);
        return true;
    }

    private OrdreMission rechercherOrdreMission(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug de l'ordre de mission est obligatoire.");
        }
        return this.ordreMissionRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucun ordre de mission n'a été trouvé."));
    }
}
