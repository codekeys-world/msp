package com.codekeys.assemblee;

import com.codekeys.common.PageReponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssembleeService {
    private final AssembleeRepository repository;

    public PageReponse<AssembleeResponse> lister(int page, int size, String q) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Pagination invalide.");
        }
        var pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("id").descending()));
        var terme = q == null ? "" : q.trim();
        var resultats = terme.isEmpty() ? repository.findAll(pageable)
                : repository.findByNomContainingIgnoreCaseOrLieuContainingIgnoreCase(terme, terme, pageable);
        return new PageReponse<>(resultats.map(AssembleeResponse::from).getContent(), resultats.getNumber(),
                resultats.getSize(), resultats.getTotalElements(), resultats.getTotalPages(), resultats.isFirst(), resultats.isLast());
    }

    public AssembleeResponse consulter(String slug) {
        return AssembleeResponse.from(rechercher(slug));
    }

    public long compter() { return repository.count(); }

    @Transactional
    public AssembleeResponse creer(AssembleeRequest request) {
        var assemblee = new Assemblee();
        assemblee.setSlug(UUID.randomUUID().toString());
        remplir(assemblee, request);
        return AssembleeResponse.from(repository.save(assemblee));
    }

    @Transactional
    public AssembleeResponse modifier(String slug, AssembleeRequest request) {
        var assemblee = rechercher(slug);
        remplir(assemblee, request);
        return AssembleeResponse.from(repository.save(assemblee));
    }

    @Transactional
    public boolean supprimer(String slug) {
        repository.delete(rechercher(slug));
        return true;
    }

    private Assemblee rechercher(String slug) {
        return repository.findBySlug(slug).orElseThrow(() -> new EntityNotFoundException("Assemblée introuvable."));
    }

    private void remplir(Assemblee assemblee, AssembleeRequest request) {
        assemblee.setNom(request.nom().trim());
        assemblee.setDate(request.date());
        assemblee.setLieu(request.lieu().trim());
    }
}
