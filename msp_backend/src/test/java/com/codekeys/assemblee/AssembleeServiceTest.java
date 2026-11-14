package com.codekeys.assemblee;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssembleeServiceTest {
    private AssembleeRepository repository;
    private AssembleeService service;
    private final AssembleeRequest request = new AssembleeRequest(" Assemblée générale ", LocalDate.of(2026, 12, 1), " Bamako ");

    @BeforeEach void setup() {
        repository = mock(AssembleeRepository.class);
        service = new AssembleeService(repository);
    }

    @Test void creationNormaliseLesChampsEtGenereUnSlug() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var resultat = service.creer(request);
        assertEquals("Assemblée générale", resultat.nom());
        assertEquals("Bamako", resultat.lieu());
        assertEquals(request.date(), resultat.date());
        assertDoesNotThrow(() -> java.util.UUID.fromString(resultat.slug()));
    }

    @Test void modificationConserveLeSlugEtMetAJourLesTroisChamps() {
        var assemblee = new Assemblee();
        assemblee.setSlug("stable");
        when(repository.findBySlug("stable")).thenReturn(Optional.of(assemblee));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var resultat = service.modifier("stable", request);
        assertEquals("stable", resultat.slug());
        assertEquals("Assemblée générale", resultat.nom());
        assertEquals(request.date(), resultat.date());
        assertEquals("Bamako", resultat.lieu());
    }

    @Test void suppressionEtTotalProviennentDuRepository() {
        var assemblee = new Assemblee();
        when(repository.findBySlug("slug")).thenReturn(Optional.of(assemblee));
        assertTrue(service.supprimer("slug"));
        verify(repository).delete(assemblee);
        when(repository.count()).thenReturn(0L, 7L);
        assertEquals(0, service.compter());
        assertEquals(7, service.compter());
    }

    @Test void slugInconnuNeCreeNiNeSupprimeRien() {
        when(repository.findBySlug(anyString())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.consulter("absent"));
        assertThrows(EntityNotFoundException.class, () -> service.modifier("absent", request));
        assertThrows(EntityNotFoundException.class, () -> service.supprimer("absent"));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any(Assemblee.class));
    }

    @Test void recherchePagineeEtPaginationInvalide() {
        var assemblee = new Assemblee();
        assemblee.setSlug("slug");
        when(repository.findByNomContainingIgnoreCaseOrLieuContainingIgnoreCase(eq("Bamako"), eq("Bamako"), any()))
                .thenReturn(new PageImpl<>(List.of(assemblee), PageRequest.of(0, 8), 1));
        var page = service.lister(0, 8, " Bamako ");
        assertEquals(1, page.getTotalElements());
        assertEquals("slug", page.getContent().get(0).slug());
        assertThrows(IllegalArgumentException.class, () -> service.lister(-1, 8, ""));
        assertThrows(IllegalArgumentException.class, () -> service.lister(0, 101, ""));
        assertThrows(IllegalArgumentException.class, () -> service.lister(0, 0, ""));
    }

    @Test void lesTroisChampsSontObligatoiresEtLesLongueursLimitees() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(request).isEmpty());
            assertEquals(3, validator.validate(new AssembleeRequest(" ", null, "")).size());
            assertEquals(2, validator.validate(new AssembleeRequest("a".repeat(201), request.date(), "b".repeat(256))).size());
        }
    }
}
