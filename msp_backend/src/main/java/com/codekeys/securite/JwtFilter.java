package com.codekeys.securite;

import com.codekeys.token.Token;
import com.codekeys.token.TokenRepository;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (servletPath.endsWith("/authentification/connexion")
                || servletPath.endsWith("/authentification/refresh")) {
            filterChain.doFilter(request,response);
            return;
        }
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String token;
        final String username;
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        token = authorizationHeader.substring(7);
        try {
            username = this.jwtService.extraireUsername(token);
        } catch (JwtException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Votre session est invalide ou expirée.");
            return;
        }
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);
            if(this.jwtService.isTokenExpired(token)){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Votre session est expirée.");
                return;
            }
            Token tokenDansBdd = this.tokenRepository.findByToken(token).orElse(null);
            if (tokenDansBdd == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Votre session a été révoquée.");
                return;
            }

            if(this.jwtService.estValidToken(token, utilisateur)){
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        utilisateur, null, utilisateur.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
