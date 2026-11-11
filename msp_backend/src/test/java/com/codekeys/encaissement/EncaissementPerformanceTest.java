package com.codekeys.encaissement;

import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EncaissementPerformanceTest {
    @Test
    void metadataDoesNotReadTheFileAndDefaultDetailStillIncludesIt() {
        EncaissementRepository repository = mock(EncaissementRepository.class);
        DocumentService documents = mock(DocumentService.class);
        EncaissementService service = new EncaissementService(repository,
                new EncaissementMapper(documents), documents);
        Encaissement entity = new Encaissement();
        entity.setSlug("enc-1");
        entity.setJustificatif(Document.builder().nom("preuve.pdf")
                .type("application/pdf").chemin("preuve.pdf").build());
        when(repository.findBySlug("enc-1")).thenReturn(Optional.of(entity));

        EncaissementResponse metadata = service.rechercherParSlug("enc-1", false);

        assertNull(metadata.getJustificatif());
        assertEquals("preuve.pdf", metadata.getNomFichierJustificatif());
        assertEquals("application/pdf", metadata.getTypeFichierJustificatif());
        verifyNoInteractions(documents);

        byte[] content = {1, 2, 3};
        when(documents.readFileFromLocation("preuve.pdf")).thenReturn(content);
        assertArrayEquals(content, service.rechercherParSlug("enc-1").getJustificatif());
        verify(documents).readFileFromLocation("preuve.pdf");
    }
}
