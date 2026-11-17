package com.codekeys.securite;

import com.codekeys.utilisateur.Utilisateur;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;

public final class UtilisateurAuthentifie {
    private UtilisateurAuthentifie() {}

    public static @NonNull Utilisateur exiger(@Nullable Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Utilisateur utilisateur)) {
            throw new AuthenticationCredentialsNotFoundException("Veuillez vous connecter pour accéder à votre compte.");
        }
        return utilisateur;
    }
}
