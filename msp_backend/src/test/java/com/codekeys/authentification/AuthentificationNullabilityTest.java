package com.codekeys.authentification;

import com.codekeys.configuration.ApplicationAuditAware;
import com.codekeys.execption.handler.GlobalExceptionHandler;
import com.codekeys.journalAction.JournalActionService;
import com.codekeys.securite.JwtService;
import com.codekeys.token.Token;
import com.codekeys.token.TokenRepository;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthentificationNullabilityTest {
    private final AuthenticationManager manager = mock(AuthenticationManager.class);
    private final JwtService jwt = mock(JwtService.class);
    private final TokenRepository tokens = mock(TokenRepository.class);
    private final AuthentificationService service = new AuthentificationService(manager, jwt, tokens,
            mock(JournalActionService.class), mock(HttpServletRequest.class));

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void rejectsMissingSessionWithoutConsultingTokens() {
        var exception = assertThrows(AuthenticationCredentialsNotFoundException.class, service::getAuthenticatedUserToken);
        assertEquals(HttpStatus.UNAUTHORIZED, new GlobalExceptionHandler().handleException(exception).getStatusCode());
        verifyNoInteractions(tokens);
    }

    @Test void rejectsAbsentOrUnexpectedPrincipalBeforeGeneratingTokens() {
        var request = mock(AuthentificationRequest.class);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.connecter(request));
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(manager.authenticate(any())).thenReturn(authentication);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.connecter(request));
        when(authentication.getPrincipal()).thenReturn("unexpected");
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.connecter(request));
        verifyNoInteractions(jwt);
    }

    @Test void acceptsAnAuthenticatedUserAndReturnsTheTokenPair() {
        var user = Utilisateur.builder().nom("TRAORÉ").prenom("Aminata").build();
        when(manager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        when(jwt.generateTokenPair(any(), eq(user))).thenReturn(Token.builder().token("access-test").refreshToken("refresh-test").build());
        var response = service.connecter(mock(AuthentificationRequest.class));
        assertEquals("access-test", response.getToken());
        assertEquals("refresh-test", response.getRefreshToken());
    }

    @Test void auditingIgnoresMissingOrUnexpectedPrincipals() {
        var auditor = new ApplicationAuditAware();
        assertTrue(auditor.getCurrentAuditor().isEmpty());
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertTrue(auditor.getCurrentAuditor().isEmpty());
        when(authentication.getPrincipal()).thenReturn("unexpected");
        assertTrue(auditor.getCurrentAuditor().isEmpty());
        when(authentication.getPrincipal()).thenReturn(Utilisateur.builder().nom("TRAORÉ").prenom("Aminata").build());
        assertEquals("Aminata TRAORÉ", auditor.getCurrentAuditor().orElseThrow());
    }
}
