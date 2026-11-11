package com.codekeys.configuration;

import com.codekeys.utilisateur.Utilisateur;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component("auditorAware")
public class ApplicationAuditAware implements AuditorAware<@NonNull String> {
    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return Optional.ofNullable(utilisateur.getNomComplet());
    }
}
