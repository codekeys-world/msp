package com.codekeys.pv;

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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PvService {
    private final PvRepository pvRepository;
    private final PvMapper pvMapper;
    private final DocumentService documentService;

    @Transactional
    public PvResponse enregistrer(@Valid PvRequest request) throws IOException {
        verifierNumeroDisponible(request.getNumero(), null);
        Pv pv = pvMapper.pvRequestToPv(request);
        MultipartFile fichier = request.getDocumentPv();
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le document du procès-verbal est obligatoire.");
        }
        pv.setSlug(UUID.randomUUID().toString());
        pv.setDocumentPv(documentService.uploadFile(fichier, TypeDocument.PV, pv.getSlug() + "/"));
        return pvMapper.pvToPvResponse(pvRepository.save(pv));
    }

    @Transactional
    public PageReponse<PvResponse> lister(int page, int size, String terme) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Les paramètres de pagination du procès-verbal sont invalides.");
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<@NonNull Pv> pvs = StringUtils.hasText(terme)
                ? pvRepository.rechercherParInformation(terme.trim(), pageRequest)
                : pvRepository.findAll(pageRequest);
        List<PvResponse> items = pvs.stream().map(pvMapper::pvToPvResponse).toList();
        return new PageReponse<>(items, pvs.getNumber(), pvs.getSize(), pvs.getTotalElements(),
                pvs.getTotalPages(), pvs.isFirst(), pvs.isLast());
    }

    @Transactional
    public PvStatistiques obtenirStatistiques() {
        return new PvStatistiques(
                pvRepository.count(),
                pvRepository.countEnAttenteValidation(),
                pvRepository.countApprouves(),
                pvRepository.countRefuses()
        );
    }

    @Transactional
    public PvResponse rechercherParSlug(String slug) {
        return pvMapper.pvToPvResponse(rechercher(slug));
    }

    @Transactional
    public PvResponse modifier(String slug, @Valid PvRequest request) throws IOException {
        Pv pv = rechercher(slug);
        verifierNumeroDisponible(request.getNumero(), slug);
        Document ancienDocument = pv.getDocumentPv();
        Pv nouvellesDonnees = pvMapper.pvRequestToPv(request);
        pv.setNumero(nouvellesDonnees.getNumero());
        pv.setObjet(nouvellesDonnees.getObjet());
        pv.setType(nouvellesDonnees.getType());
        pv.setDatePv(nouvellesDonnees.getDatePv());
        pv.setObservation(nouvellesDonnees.getObservation());

        MultipartFile fichier = request.getDocumentPv();
        if (fichier != null && !fichier.isEmpty()) {
            pv.setDocumentPv(documentService.uploadFile(fichier, TypeDocument.PV,
                    pv.getSlug() + "/" + UUID.randomUUID()));
        }
        pv = pvRepository.save(pv);
        if (pv.getDocumentPv() != ancienDocument) {
            documentService.supprimerFichier(ancienDocument);
        }
        return pvMapper.pvToPvResponse(pv);
    }

    @Transactional
    public boolean supprimer(String slug) throws IOException {
        Pv pv = rechercher(slug);
        documentService.supprimerFichier(pv.getDocumentPv());
        pvRepository.delete(pv);
        return true;
    }

    private Pv rechercher(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Le slug du procès-verbal est obligatoire.");
        }
        return pvRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Aucun procès-verbal n'a été trouvé."));
    }

    private void verifierNumeroDisponible(String numero, String slugExclu) {
        if (!StringUtils.hasText(numero)) {
            throw new IllegalArgumentException("Le numéro du procès-verbal est obligatoire.");
        }
        boolean existe = slugExclu == null
                ? pvRepository.existsByNumeroIgnoreCase(numero.trim())
                : pvRepository.existsByNumeroIgnoreCaseAndSlugNot(numero.trim(), slugExclu);
        if (existe) {
            throw new IllegalArgumentException("Ce numéro de procès-verbal est déjà utilisé.");
        }
    }
}
