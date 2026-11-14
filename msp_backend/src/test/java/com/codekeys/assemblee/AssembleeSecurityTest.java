package com.codekeys.assemblee;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssembleeSecurityTest {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean AssembleeService service() { return mock(AssembleeService.class); }
        @Bean AssembleeControleur controleur(AssembleeService service) { return new AssembleeControleur(service); }
    }

    @Test void seulsLesTroisRolesPeuventGererLesAssemblees() throws Exception {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var controleur = context.getBean(AssembleeControleur.class);
            var request = new AssembleeRequest("Assemblée", LocalDate.now(), "Bamako");
            var service = context.getBean(AssembleeService.class);
            var response = new AssembleeResponse("slug", request.nom(), request.date(), request.lieu());
            when(service.lister(0, 8, "")).thenReturn(new com.codekeys.common.PageReponse<>());
            when(service.consulter("slug")).thenReturn(response);
            when(service.creer(request)).thenReturn(response);
            when(service.modifier("slug", request)).thenReturn(response);
            for (String role : new String[]{"SECRETAIRE_ADMINISTRATIF", "ADMINISTRATEUR", "SYSADMIN"}) {
                authentifier(role);
                assertDoesNotThrow(() -> controleur.lister(0, 8, ""));
                assertDoesNotThrow(() -> controleur.consulter("slug"));
                assertDoesNotThrow(() -> controleur.creer(request));
                assertDoesNotThrow(() -> controleur.modifier("slug", request));
                assertDoesNotThrow(() -> controleur.supprimer("slug"));
            }
            for (String role : new String[]{"ADHERENT", "PRESIDENT", "TRESORIER", "COMMISSION_CONTROLE", "SECRETAIRE_ORGANISATION", "SECRETAIRE_AFFAIRE_SOCIALE"}) {
                authentifier(role);
                assertThrows(AccessDeniedException.class, () -> controleur.lister(0, 8, ""));
                assertThrows(AccessDeniedException.class, () -> controleur.consulter("slug"));
                assertThrows(AccessDeniedException.class, () -> controleur.creer(request));
                assertThrows(AccessDeniedException.class, () -> controleur.modifier("slug", request));
                assertThrows(AccessDeniedException.class, () -> controleur.supprimer("slug"));
                assertDoesNotThrow(controleur::statistiques);
            }
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
            assertThrows(AccessDeniedException.class, controleur::statistiques);
            assertThrows(AccessDeniedException.class, () -> controleur.lister(0, 8, ""));
            var mvc = MockMvcBuilders.standaloneSetup(controleur).build();
            authentifier("ADHERENT");
            mvc.perform(get("/assemblees")).andExpect(status().isForbidden());
            mvc.perform(get("/assemblees/statistiques")).andExpect(status().isOk());
            authentifier("ADMINISTRATEUR");
            mvc.perform(post("/assemblees").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nom\":\"Assemblée\",\"date\":\"" + request.date() + "\",\"lieu\":\"Bamako\"}"))
                    .andExpect(status().isCreated());
            mvc.perform(post("/assemblees").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nom\":\" \",\"date\":null,\"lieu\":\"\"}"))
                    .andExpect(status().isBadRequest());
            when(service.lister(-1, 8, "")).thenThrow(new IllegalArgumentException("Pagination invalide."));
            mvc.perform(get("/assemblees").param("page", "-1")).andExpect(status().isBadRequest());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void authentifier(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test", "", AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }
}
