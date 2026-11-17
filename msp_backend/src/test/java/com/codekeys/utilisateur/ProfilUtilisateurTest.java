package com.codekeys.utilisateur;

import com.codekeys.authentification.EmailService;
import com.codekeys.authentification.PasswordResetTokenRepository;
import com.codekeys.authorisation.AuthorisationRepository;
import com.codekeys.authorisation.AuthorisationService;
import com.codekeys.common.Genre;
import com.codekeys.conjoint.ConjointMapper;
import com.codekeys.conjoint.ConjointRepository;
import com.codekeys.enfant.EnfantMapper;
import com.codekeys.enfant.EnfantRepository;
import com.codekeys.fichier.DocumentService;
import com.codekeys.role.RoleRepository;
import com.codekeys.role.RoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfilUtilisateurTest {
    private final UtilisateurRepository repository = mock(UtilisateurRepository.class);
    private Utilisateur stored;
    private MockMvc mvc;

    @BeforeEach void setup() {
        var encoder = mock(BCryptPasswordEncoder.class);
        var roles = mock(RoleService.class);
        var documents = mock(DocumentService.class);
        var conjoints = mock(ConjointMapper.class);
        var enfants = mock(EnfantMapper.class);
        var mapper = new UtilisateurMapper(encoder, roles, mock(AuthorisationService.class), documents, conjoints, enfants);
        var service = new UtilisateurService(repository, mapper, roles, encoder, documents, conjoints, enfants,
                mock(ConjointRepository.class), mock(EnfantRepository.class), mock(AuthorisationRepository.class),
                mock(RoleRepository.class), mock(PasswordResetTokenRepository.class), mock(EmailService.class));
        mvc = MockMvcBuilders.standaloneSetup(new UtilisateurControleur(service)).build();
        stored = Utilisateur.builder().id(1L).slug("membre").nom("TRAORÉ").prenom("Aminata")
                .email("aminata@example.test").matricule("MSP-001").telephonePrincipal("+22376123456")
                .numeroInscription("INS-001").genre(Genre.FEMME).dateNaissance(LocalDate.of(1990, 5, 17))
                .lieuNaissance("Sikasso").pere("Ousmane TRAORÉ").mere("Fatou DIALLO").adresse("Médine, Bamako").build();
        // The security principal may be older than the information currently stored in the database.
        var principal = Utilisateur.builder().slug("membre").nom("Ancien profil").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        when(repository.findUtilisateurBySlug("membre")).thenReturn(Optional.of(stored));
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void returnsCompleteCurrentProfileAndIsoBirthDate() throws Exception {
        mvc.perform(get("/adherents/me"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.donnees.resultat.dateNaissance").value("1990-05-17"))
                .andExpect(jsonPath("$.donnees.resultat.lieuNaissance").value("Sikasso"))
                .andExpect(jsonPath("$.donnees.resultat.pere").value("Ousmane TRAORÉ"))
                .andExpect(jsonPath("$.donnees.resultat.mere").value("Fatou DIALLO"))
                .andExpect(jsonPath("$.donnees.resultat.adresse").value("Médine, Bamako"));
        verify(repository).findUtilisateurBySlug("membre");
    }

    @Test void savesAllPersonalFieldsAndReturnsThemOnReload() throws Exception {
        mvc.perform(put("/adherents/me").contentType(MediaType.APPLICATION_JSON).content("""
                {"nom":" Traoré ","prenom":" Aminata ","email":"aminata@example.test", "matricule":"MSP-001",
                 "telephonePrincipal":"+22376123456","numeroInscription":"INS-001","genre":"FEMME",
                 "dateNaissance":"1989-02-12","lieuNaissance":" Kayes ","pere":" Moussa TRAORÉ ",
                 "mere":" Aïssata KEITA ","adresse":" Quartier du Fleuve ","observations":" Complété "}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.donnees.resultat.dateNaissance").value("1989-02-12"))
                .andExpect(jsonPath("$.donnees.resultat.pere").value("Moussa TRAORÉ"));
        verify(repository).save(stored);
        assertEquals("Kayes", stored.getLieuNaissance());
        assertEquals("Aïssata KEITA", stored.getMere());
        assertEquals("Quartier du Fleuve", stored.getAdresse());
        mvc.perform(get("/adherents/me"))
                .andExpect(jsonPath("$.donnees.resultat.dateNaissance").value("1989-02-12"))
                .andExpect(jsonPath("$.donnees.resultat.mere").value("Aïssata KEITA"))
                .andExpect(jsonPath("$.donnees.resultat.adresse").value("Quartier du Fleuve"));
    }

    @Test void doesNotInventMissingCivilStatusInformation() throws Exception {
        stored.setDateNaissance(null);
        stored.setLieuNaissance(null);
        stored.setPere("");
        stored.setMere("");
        stored.setAdresse(null);
        mvc.perform(get("/adherents/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.donnees.resultat.dateNaissance").isEmpty())
                .andExpect(jsonPath("$.donnees.resultat.pere").value(""))
                .andExpect(jsonPath("$.donnees.resultat.mere").value(""))
                .andExpect(jsonPath("$.donnees.resultat.adresse").isEmpty());
        verify(repository, never()).save(any());
    }

    @Test void rejectsIncompleteOrFutureDatedProfilesBeforeSaving() throws Exception {
        mvc.perform(put("/adherents/me").contentType(MediaType.APPLICATION_JSON).content("""
                {"nom":"TRAORÉ","prenom":"Aminata","email":"aminata@example.test", "matricule":"MSP-001",
                 "telephonePrincipal":"+22376123456","numeroInscription":"INS-001","genre":"FEMME",
                 "dateNaissance":"2999-01-01","lieuNaissance":"Kayes","pere":" ","mere":"Fatou"}
                """))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }
}
