package com.codekeys.journalAction;

import com.codekeys.utilisateur.Utilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JournalActionFilter extends OncePerRequestFilter {

    private final JournalActionService journalActionService;

    private static final Set<String> METHODES_A_JOURNALISER = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // On exécute la requête
        filterChain.doFilter(request, response);

        // Si la requête ne modifie pas l'état ou est une route technique / de consultation, on ne journalise pas en automatique
        if (!METHODES_A_JOURNALISER.contains(method)) {
            return;
        }

        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs") || path.startsWith("/journal-actions")) {
            return;
        }

        int statusCode = response.getStatus();
        String statut = (statusCode >= 200 && statusCode < 400) ? "SUCCES" : "ECHEC";

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Utilisateur utilisateur = null;
            if (authentication != null && authentication.getPrincipal() instanceof Utilisateur u) {
                utilisateur = u;
            }

            // Ignorer la route de connexion car gérée spécifiquement
            if (path.contains("/authentification/connexion") || path.contains("/authentification/refresh")) {
                return;
            }

            String actionDescription = deduireAction(method, path);
            String module = deduireModule(path);
            String details = String.format("Méthode HTTP: %s | URL: %s | Code statut HTTP: %d", method, path, statusCode);

            journalActionService.enregistrerDepuisRequete(
                    utilisateur,
                    request,
                    actionDescription,
                    module,
                    details,
                    statut
            );
        } catch (Exception ex) {
            log.warn("Impossible d'enregistrer l'action automatique dans le journal: {}", ex.getMessage());
        }
    }

    private String deduireModule(String path) {
        if (path.contains("/adherent")) return "ADHÉRENTS";
        if (path.contains("/carte")) return "CARTES_MEMBRES";
        if (path.contains("/courrier")) return "COURRIERS";
        if (path.contains("/proces-verbaux") || path.contains("/pv")) return "PROCÈS_VERBAUX";
        if (path.contains("/convocation")) return "CONVOCATIONS";
        if (path.contains("/plainte")) return "PLAINTES";
        if (path.contains("/cas-sociaux") || path.contains("/cas-social")) return "CAS_SOCIAUX";
        if (path.contains("/ordre-mission")) return "ORDRES_MISSION";
        if (path.contains("/admin")) return "ADMINISTRATION";
        if (path.contains("/authentification")) return "AUTHENTIFICATION";
        return "SYSTÈME";
    }

    private String deduireAction(String method, String path) {
        String actionVerbe = switch (method) {
            case "POST" -> "Création / Enregistrement";
            case "PUT" -> "Modification / Mise à jour";
            case "DELETE" -> "Suppression";
            case "PATCH" -> "Mise à jour partielle";
            default -> "Opération";
        };

        if (path.contains("/adherents") && method.equals("POST")) return "Création d'un nouvel adhérent";
        if (path.contains("/adherents") && method.equals("PUT")) return "Modification d'un adhérent";
        if (path.contains("/adherents") && method.equals("DELETE")) return "Suppression d'un adhérent";
        if (path.contains("/carte")) return "Génération ou impression de carte de membre";
        if (path.contains("/courriers") && method.equals("POST")) return "Enregistrement d'un nouveau courrier";
        if (path.contains("/courriers") && method.equals("PUT")) return "Mise à jour d'un courrier";
        if (path.contains("/courriers") && method.equals("DELETE")) return "Suppression d'un courrier";
        if (path.contains("/pv") || path.contains("/proces-verbaux")) return actionVerbe + " de procès-verbal";
        if (path.contains("/convocations")) return actionVerbe + " de convocation";
        if (path.contains("/plaintes")) return actionVerbe + " de plainte";
        if (path.contains("/cas-sociaux")) return actionVerbe + " de cas social";
        if (path.contains("/ordre-mission")) return actionVerbe + " d'ordre de mission";
        if (path.contains("/admin/adherents") && path.contains("/role/")) return "Attribution / Changement de rôle adhérent";
        if (path.contains("/deconnexion")) return "Déconnexion de l'application";

        return actionVerbe + " sur " + path;
    }
}
