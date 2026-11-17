package com.codekeys.prestation;

import com.codekeys.CasSocial.LienDefunt;
import com.codekeys.CasSocial.StatutPrestation;
import com.codekeys.CasSocial.TypeBeneficiaire;
import com.codekeys.fichier.DocumentService;
import com.codekeys.role.Role;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrestationMontantsTest {
    private final PrestationRepository repository = mock(PrestationRepository.class);
    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final DocumentService documents = mock(DocumentService.class);
    private final PrestationService service = new PrestationService(repository, users, new PrestationMapper(documents), documents);

    @BeforeEach void setup() {
        when(users.findUtilisateurBySlug("membre")).thenReturn(Optional.of(Utilisateur.builder()
                .slug("membre").nom("TRAORÉ").prenom("Aminata").role(Role.builder().nom("ADHERENT").build()).build()));
        when(repository.save(any(Prestation.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test void preservesAmountsForEachDeceasedRelative() throws Exception {
        for (LienDefunt lien : LienDefunt.values()) {
            var request = request(TypePrestation.DECES, lien, 60);
            var response = service.enregistrer(request);
            assertEquals(lien == LienDefunt.ADHERENT ? 1500000L : lien == LienDefunt.ENFANT ? 20000L : 50000L,
                    response.getMontantPrestation());
            assertEquals(StatutPrestation.PRESTATION, response.getStatutPrestation());
            assertNull(response.getMontantRemboursement());
        }
    }

    @Test void bothAdherentDeathRepresentationsUseTheSameContributionThreshold() throws Exception {
        for (var type : new TypePrestation[]{TypePrestation.DECES, TypePrestation.DECES_ADHERENT}) {
            var response = service.enregistrer(request(type, LienDefunt.ADHERENT, 59));
            assertNull(response.getMontantPrestation());
            assertEquals(125000L, response.getMontantRemboursement());
            assertEquals(StatutPrestation.REMBOURSEMENT_COTISATIONS, response.getStatutPrestation());
            response = service.enregistrer(request(type, LienDefunt.ADHERENT, 60));
            assertEquals(1500000L, response.getMontantPrestation());
            assertEquals(StatutPrestation.PRESTATION, response.getStatutPrestation());
        }
    }

    private PrestationRequest request(TypePrestation type, LienDefunt lien, int months) {
        var request = new PrestationRequest();
        request.setAdherentSlug("membre");
        request.setTypePrestation(type);
        request.setLienDefunt(type == TypePrestation.DECES ? lien : null);
        request.setTypeBeneficiaire(lien == LienDefunt.ADHERENT ? TypeBeneficiaire.PARENTS : TypeBeneficiaire.ADHERENT);
        request.setDureeCotisationMois(months);
        request.setMontantCotisationsVersees(125000L);
        if (lien != LienDefunt.ADHERENT) {
            request.setNomDefunt("TRAORÉ");
            request.setPrenomDefunt("Ousmane");
        }
        return request;
    }
}
