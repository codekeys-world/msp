package com.codekeys.authentification;

import com.codekeys.execption.OperationNotPermitException;
import com.codekeys.journalAction.JournalActionService;
import com.codekeys.securite.JwtService;
import com.codekeys.token.Token;
import com.codekeys.token.TokenRepository;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthentificationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final JournalActionService journalActionService;
    private final HttpServletRequest httpServletRequest;

    public AuthentificationResponse connecter(@Valid AuthentificationRequest authentificationRequest) {
        Authentication authenticate = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authentificationRequest.getUsername(),
                        authentificationRequest.getPassword()
                )
        );
        Utilisateur utilisateur = (Utilisateur) authenticate.getPrincipal();
        try {
            this.journalActionService.enregistrerDepuisRequete(
                    utilisateur,
                    this.httpServletRequest,
                    "Connexion réussie à l'application",
                    "AUTHENTIFICATION",
                    "Session authentifiée pour " + utilisateur.getNomComplet() + " (" + (utilisateur.getRole() != null ? utilisateur.getRole().getNom() : "Utilisateur") + ")",
                    "SUCCES"
            );
        } catch (Exception ignored) {
        }
        return getAuthentificationResponse(utilisateur);
    }

    private AuthentificationResponse getAuthentificationResponse(Authentication authenticate) {
        return getAuthentificationResponse((Utilisateur) authenticate.getPrincipal());
    }

    private AuthentificationResponse getAuthentificationResponse(Utilisateur utilisateur) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("nom", utilisateur.getNomComplet());
        claims.put("email", utilisateur.getEmail());
        Token tokens = this.jwtService.generateTokenPair(claims, utilisateur);
        return AuthentificationResponse.builder()
                .token(tokens.getToken())
                .refreshToken(tokens.getRefreshToken())
                .build();
    }

    public boolean deconnecter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Utilisateur utilisateur) {
            try {
                this.journalActionService.enregistrerDepuisRequete(
                        utilisateur,
                        this.httpServletRequest,
                        "Déconnexion de l'application",
                        "AUTHENTIFICATION",
                        "Session clôturée pour " + utilisateur.getNomComplet(),
                        "SUCCES"
                );
            } catch (Exception ignored) {
            }
            List<Token> allValidTokensByUsername = this.tokenRepository.findAllValidTokensByUsername(utilisateur.getUsername());
            this.tokenRepository.deleteAll(allValidTokensByUsername);
            return true;
        }
        return false;
    }

    public AuthentificationResponse getAuthenticatedUserToken() {
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Token token = this.tokenRepository.findByUtilisateur(utilisateur).orElseThrow(
                () -> new OperationNotPermitException("Aucun token n'a été trouvé !")
        );
        return AuthentificationResponse.builder()
                .token(token.getToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    @Transactional
    public AuthentificationResponse refreshToken(String refreshToken) {
        Token tokenEnregistre = this.tokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new OperationNotPermitException("Votre session ne peut plus être renouvelée, veuillez vous reconnecter.")
        );
        Utilisateur utilisateur = tokenEnregistre.getUtilisateur();
        try {
            String username = this.jwtService.validerEtExtraireUsernameRefresh(refreshToken);
            if (!Objects.equals(username, utilisateur.getUsername())) {
                throw new OperationNotPermitException("Le refresh token ne correspond pas à cette session.");
            }
        } catch (JwtException | IllegalArgumentException exception) {
            throw new OperationNotPermitException("Votre session ne peut plus être renouvelée, veuillez vous reconnecter.");
        }
        this.tokenRepository.delete(tokenEnregistre);
        return getAuthentificationResponse(utilisateur);
    }
}
