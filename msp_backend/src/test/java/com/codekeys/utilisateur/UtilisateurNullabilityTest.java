package com.codekeys.utilisateur;

import com.codekeys.authentification.ModifierMotDePasse;
import com.codekeys.common.Genre;
import com.codekeys.conjoint.ConjointRequest;
import com.codekeys.enfant.EnfantRequest;
import com.codekeys.execption.OperationNotPermitException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurNullabilityTest {
    @Mock UtilisateurRepository repository;
    @Mock UtilisateurMapper mapper;
    @Mock BCryptPasswordEncoder encoder;
    @InjectMocks UtilisateurService service;

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void refusesMissingSessionForProfileAndPassword() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, service::obtenirProfilConnecte);
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> service.modifierMotDePasse(new ModifierMotDePasse("old-password", "new-password", "new-password")));
        verifyNoInteractions(repository, encoder);
    }

    @Test void rejectsAPasswordlessAccountWithoutInvokingTheEncoder() {
        var user = Utilisateur.builder().slug("user").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        when(repository.findUtilisateurBySlug("user")).thenReturn(Optional.of(user));
        assertThrows(OperationNotPermitException.class,
                () -> service.modifierMotDePasse(new ModifierMotDePasse("old-password", "new-password", "new-password")));
        verifyNoInteractions(encoder);
        verify(repository, never()).save(any());
    }

    @Test void checksTheCurrentStoredPasswordBeforeSavingANewOne() {
        var principal = Utilisateur.builder().slug("user").password("stale-hash").build();
        var stored = Utilisateur.builder().slug("user").password("current-hash").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        when(repository.findUtilisateurBySlug("user")).thenReturn(Optional.of(stored));
        when(encoder.matches("old-password", "current-hash")).thenReturn(true);
        when(encoder.encode("new-password")).thenReturn("new-hash");
        assertTrue(service.modifierMotDePasse(new ModifierMotDePasse("old-password", "new-password", "new-password")));
        assertEquals("new-hash", stored.getPassword());
        verify(repository).save(stored);
    }

    @Test void requiresMarriageBirthAndLifeCertificatesBeforeCreatingTheAccount() {
        var request = new AdherentEnregistrementRequest();
        var adherent = new UtilisateurRequest();
        adherent.setGenre(Genre.HOMME);
        request.setAdherent(adherent);
        var conjoint = new ConjointRequest();
        conjoint.setGenre(Genre.FEMME);
        request.setConjoints(List.of(conjoint));
        var multipart = mock(MultipartHttpServletRequest.class);
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerAdherent(request, multipart));
        request.setConjoints(List.of());
        request.setDescendants(List.of(new EnfantRequest()));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerAdherent(request, multipart));
        when(multipart.getFile("descendantActe-0")).thenReturn(new MockMultipartFile("descendantActe-0", new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerAdherent(request, multipart));
        when(multipart.getFile("descendantCertificat-0")).thenReturn(new MockMultipartFile("descendantCertificat-0", new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerAdherent(request, multipart));
        verifyNoInteractions(mapper, repository);
    }
}
