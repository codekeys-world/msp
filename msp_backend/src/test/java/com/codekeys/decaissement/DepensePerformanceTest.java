package com.codekeys.decaissement;

import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DepensePerformanceTest {
    private final DepenseRepository repository = mock(DepenseRepository.class);
    private final FraisDeGestionRepository frais = mock(FraisDeGestionRepository.class);
    private final DocumentService documents = mock(DocumentService.class);
    private final DepenseService service = new DepenseService(repository, frais,
            new DepenseMapper(documents), documents);

    @Test
    void blankSearchUsesFilteredPaginationWithoutTextSearchOrFileReads() {
        when(frais.existsBySlug("fg-1")).thenReturn(true);
        Depense entity = new Depense();
        entity.setSlug("dep-1");
        entity.setJustificatif(Document.builder().nom("preuve.pdf").chemin("preuve.pdf").build());
        when(repository.findByFraisDeGestionSlug(eq("fg-1"), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(entity), call.getArgument(1), 20));

        var result = service.listerParFraisDeGestion("fg-1", 0, 8, "  ");

        assertEquals(20, result.getTotalElements());
        assertNull(result.getContent().get(0).getJustificatif());
        assertEquals("preuve.pdf", result.getContent().get(0).getNomFichierJustificatif());
        verify(repository, never()).rechercherParFraisDeGestion(anyString(), anyString(), any());
        verify(frais, never()).findBySlug(anyString());
        verifyNoInteractions(documents);
    }

    @Test
    void nonBlankSearchStillUsesTheTrimmedSearchTerm() {
        when(frais.existsBySlug("fg-1")).thenReturn(true);
        when(repository.rechercherParFraisDeGestion(eq("fg-1"), eq("papier"), any()))
                .thenReturn(Page.empty());

        service.listerParFraisDeGestion("fg-1", 0, 8, " papier ");

        verify(repository).rechercherParFraisDeGestion(eq("fg-1"), eq("papier"), any());
        verify(repository, never()).findByFraisDeGestionSlug(anyString(), any());
    }

    @Test
    void missingBudgetStillFailsBeforeQueryingExpenses() {
        assertThrows(EntityNotFoundException.class,
                () -> service.listerParFraisDeGestion("inconnu", 0, 8, ""));
        verifyNoInteractions(repository);
    }

    @Test
    void metadataSkipsFileReadsAndDefaultDetailPreservesTheAttachment() {
        Depense entity = new Depense();
        entity.setJustificatif(Document.builder().nom("preuve.pdf").chemin("preuve.pdf").build());
        when(repository.findBySlug("dep-1")).thenReturn(Optional.of(entity));

        assertNull(service.rechercherParSlug("dep-1", false).getJustificatif());
        verifyNoInteractions(documents);

        byte[] content = {1, 2, 3};
        when(documents.readFileFromLocation("preuve.pdf")).thenReturn(content);
        assertArrayEquals(content, service.rechercherParSlug("dep-1").getJustificatif());
        verify(documents).readFileFromLocation("preuve.pdf");
    }
}
