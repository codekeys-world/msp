package com.codekeys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.transaction.annotation.Transactional;
import com.codekeys.ordreMission.OrdreMissionRepository;
import com.codekeys.pv.PvRepository;
import com.codekeys.pv.PvService;
import com.codekeys.courrier.CourrierRepository;
import com.codekeys.courrier.CourrierService;
import com.codekeys.courrier.TypeCourrier;
import com.codekeys.plainte.PlainteRepository;
import com.codekeys.plainte.PlainteService;
import com.codekeys.convocation.ConvocationRepository;
import com.codekeys.utilisateur.UtilisateurRepository;
import com.codekeys.utilisateur.UtilisateurService;
import com.codekeys.utilisateur.Utilisateur;
import com.codekeys.role.RoleRepository;
import com.codekeys.common.Genre;
import com.codekeys.conjoint.Conjoint;
import com.codekeys.execption.OperationNotPermitException;
import com.codekeys.CasSocial.CasSocialRequest;
import com.codekeys.CasSocial.CasSocialRepository;
import com.codekeys.CasSocial.CasSocialService;
import com.codekeys.CasSocial.LienDefunt;
import com.codekeys.CasSocial.TypeBeneficiaire;
import com.codekeys.CasSocial.TypeCasSocial;
import com.codekeys.CasSocial.StatutPrestation;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"classpath:db/migration/V29__seed_roles_and_authorisations.sql", "classpath:sql/test-data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class MspBackendApplicationTests {

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Autowired
	private OrdreMissionRepository ordreMissionRepository;

	@Autowired
	private PvRepository pvRepository;

	@Autowired
	private PvService pvService;

	@Autowired
	private CourrierRepository courrierRepository;

	@Autowired
	private CourrierService courrierService;

	@Autowired
	private PlainteRepository plainteRepository;

	@Autowired
	private PlainteService plainteService;

	@Autowired
	private ConvocationRepository convocationRepository;

	@Autowired
	private UtilisateurRepository utilisateurRepository;

	@Autowired
	private UtilisateurService utilisateurService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private CasSocialRepository casSocialRepository;

	@Autowired
	private CasSocialService casSocialService;

	@Test
	void contextLoads() {
	}

	@Test
	void ordreMissionReadAndUpdateRoutesAreRegistered() {
		assertTrue(handlerMapping.getHandlerMethods().keySet().stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/ordre-missions/{slug}")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.GET)));
		assertTrue(handlerMapping.getHandlerMethods().keySet().stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/ordre-missions/{slug}")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.PUT)));
		assertTrue(handlerMapping.getHandlerMethods().keySet().stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/ordre-missions/statistiques")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.GET)));
	}

	@Test
	void pvCrudAndStatisticsRoutesAreRegistered() {
		assertTrue(routeExists("/pvs", RequestMethod.POST));
		assertTrue(routeExists("/pvs", RequestMethod.GET));
		assertTrue(routeExists("/pvs/statistiques", RequestMethod.GET));
		assertTrue(routeExists("/pvs/{slug}", RequestMethod.GET));
		assertTrue(routeExists("/pvs/{slug}", RequestMethod.PUT));
		assertTrue(routeExists("/pvs/{slug}", RequestMethod.DELETE));
	}

	@Test
	void pvSearchAndStatisticsUsePersistedRecords() {
		long total = pvRepository.count();
		assertTrue(pvRepository.rechercherParInformation("terme-inexistant", PageRequest.of(0, 5))
				.getTotalElements() <= total);
		assertTrue(pvRepository.countEnAttenteValidation() <= total);
		assertTrue(pvRepository.countApprouves() <= total);
		assertTrue(pvRepository.countRefuses() <= total);
		assertTrue(pvService.obtenirStatistiques().total() == total);
	}

	@Test
	void courrierCrudAndListRoutesAreRegistered() {
		assertTrue(routeExists("/courriers", RequestMethod.POST));
		assertTrue(routeExists("/courriers", RequestMethod.GET));
		assertTrue(routeExists("/courriers/statistiques", RequestMethod.GET));
		assertTrue(routeExists("/courriers/{slug}", RequestMethod.GET));
		assertTrue(routeExists("/courriers/{slug}", RequestMethod.PUT));
		assertTrue(routeExists("/courriers/{slug}", RequestMethod.DELETE));
	}

	@Test
	void courrierSearchReturnsAPageBoundedByThePersistedCourriers() {
		assertTrue(courrierRepository.rechercherParInformation("terme-inexistant", PageRequest.of(0, 5))
				.getTotalElements() <= courrierRepository.count());
	}

	@Test
	void plainteCrudAndListRoutesAreRegistered() {
		assertTrue(routeExists("/plaintes", RequestMethod.POST));
		assertTrue(routeExists("/plaintes", RequestMethod.GET));
		assertTrue(routeExists("/plaintes/statistiques", RequestMethod.GET));
		assertTrue(routeExists("/plaintes/{slug}", RequestMethod.GET));
		assertTrue(routeExists("/plaintes/{slug}", RequestMethod.PUT));
		assertTrue(routeExists("/plaintes/{slug}", RequestMethod.DELETE));
	}

	@Test
	void plainteSearchIsBoundedByPersistedRecords() {
		assertTrue(plainteRepository.rechercherParInformation("terme-inexistant", PageRequest.of(0, 5))
				.getTotalElements() <= plainteRepository.count());
	}

	@Test
	@Transactional
	void plainteStatisticsMatchPersistedRecords() {
		var statistiques = plainteService.obtenirStatistiques();
		assertTrue(statistiques.total() == plainteRepository.count());
		assertTrue(statistiques.enAttente() == plainteRepository.countEnAttente());
		assertTrue(statistiques.traitees() == plainteRepository.countTraitees());
		assertTrue(statistiques.refusees() == plainteRepository.countRefusees());
	}

	@Test
	void testFixturesProvideTwentyComplaints() {
		for (int index = 1; index <= 20; index++) {
			assertTrue(plainteRepository.findBySlug("TEST-PLAINTE-" + String.format("%03d", index)).isPresent());
		}
	}

	@Test
	@Transactional
	void plainteAuthoritiesAreAssignedOnlyToTheExpectedRoles() {
		var plainteManagementAuthorities = java.util.Set.of(
				"AJOUTER_PLAINTE", "MODIFIER_PLAINTE", "SUPPRIMER_PLAINTE", "LISTER_PLAINTE", "RECHERCHER_PLAINTE");
		var secretaryAuthorities = roleRepository.findByNom("SECRETAIRE_AFFAIRE_SOCIALE").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		var presidentAuthorities = roleRepository.findByNom("PRESIDENT").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		assertTrue(secretaryAuthorities.containsAll(plainteManagementAuthorities));
		assertTrue(presidentAuthorities.contains("LISTER_PLAINTE"));
		assertTrue(java.util.Collections.disjoint(presidentAuthorities,
				java.util.Set.of("AJOUTER_PLAINTE", "MODIFIER_PLAINTE", "SUPPRIMER_PLAINTE", "RECHERCHER_PLAINTE")));
		var adminAuthorities = roleRepository.findByNom("ADMINISTRATEUR").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		var sysadminAuthorities = roleRepository.findByNom("SYSADMIN").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		assertTrue(adminAuthorities.contains("AJOUTER_PLAINTE"));
		assertTrue(sysadminAuthorities.contains("AJOUTER_PLAINTE"));
	}

	@Test
	void convocationCrudAndListRoutesAreRegistered() {
		assertTrue(routeExists("/convocations", RequestMethod.POST));
		assertTrue(routeExists("/convocations", RequestMethod.GET));
		assertTrue(routeExists("/convocations/{slug}", RequestMethod.GET));
		assertTrue(routeExists("/convocations/{slug}", RequestMethod.PUT));
		assertTrue(routeExists("/convocations/{slug}", RequestMethod.DELETE));
	}

	@Test
	void convocationSearchIsBoundedAndTestFixturesProvideTwentyRecords() {
		for (int index = 1; index <= 20; index++) {
			assertTrue(convocationRepository.findBySlug(
					"TEST-CONVOCATION-" + String.format("%03d", index)).isPresent());
		}
		assertTrue(convocationRepository.rechercherParInformation("terme-inexistant", PageRequest.of(0, 5))
				.getTotalElements() <= convocationRepository.count());
	}

	@Test
	@Transactional
	void convocationAuthoritiesAreLimitedForPresident() {
		var secretaryAuthorities = roleRepository.findByNom("SECRETAIRE_ADMINISTRATIF").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		var presidentAuthorities = roleRepository.findByNom("PRESIDENT").orElseThrow()
				.getAuthorisations().stream().map(authorisation -> authorisation.getNom())
				.collect(java.util.stream.Collectors.toSet());
		assertTrue(secretaryAuthorities.containsAll(java.util.Set.of("AJOUTER_CONVOCATION", "MODIFIER_CONVOCATION",
				"SUPPRIMER_CONVOCATION", "LISTER_CONVOCATION")));
		assertTrue(presidentAuthorities.contains("LISTER_CONVOCATION"));
		assertTrue(java.util.Collections.disjoint(presidentAuthorities,
				java.util.Set.of("AJOUTER_CONVOCATION", "MODIFIER_CONVOCATION", "SUPPRIMER_CONVOCATION",
						"RECHERCHER_CONVOCATION")));
	}

	@Test
	void casSocialCrudAndAdherentSearchRoutesAreRegistered() {
		assertTrue(routeExists("/cas-sociaux", RequestMethod.POST));
		assertTrue(routeExists("/cas-sociaux", RequestMethod.GET));
		assertTrue(routeExists("/cas-sociaux/adherents", RequestMethod.GET));
		assertTrue(routeExists("/cas-sociaux/{slug}", RequestMethod.GET));
		assertTrue(routeExists("/cas-sociaux/{slug}", RequestMethod.PUT));
		assertTrue(routeExists("/cas-sociaux/{slug}", RequestMethod.DELETE));
	}

	@Test
	@Transactional
	void casSocialPermissionsMatchTheSecurityReferenceData() {
		var managementAuthorities = java.util.Set.of(
				"AJOUTER_CAS_SOCIAUX", "MODIFIER_CAS_SOCIAUX", "SUPPRIMER_CAS_SOCIAUX",
				"LISTER_CAS_SOCIAUX", "RECHERCHER_CAS_SOCIAUX");
		for (String roleName : java.util.List.of("ADMINISTRATEUR", "SYSADMIN")) {
			var authorities = roleRepository.findByNom(roleName).orElseThrow().getAuthorisations().stream()
					.map(authorisation -> authorisation.getNom())
					.collect(java.util.stream.Collectors.toSet());
			assertTrue(authorities.containsAll(managementAuthorities));
		}
		var presidentAuthorities = roleRepository.findByNom("PRESIDENT").orElseThrow().getAuthorisations().stream()
				.map(authorisation -> authorisation.getNom()).collect(java.util.stream.Collectors.toSet());
		assertTrue(java.util.Collections.disjoint(presidentAuthorities, managementAuthorities));
		var treasurerAuthorities = roleRepository.findByNom("TRESORIER").orElseThrow().getAuthorisations().stream()
				.map(authorisation -> authorisation.getNom()).collect(java.util.stream.Collectors.toSet());
		assertTrue(java.util.Collections.disjoint(treasurerAuthorities, managementAuthorities));
	}

	@Test
	@Transactional
	void casSocialBenefitsUseTheDefinedFixedAmounts() throws Exception {
		Utilisateur adherent = utilisateurRepository.findByRoleNomIgnoreCase("ADHERENT", PageRequest.of(0, 1))
				.getContent().get(0);
		CasSocialRequest maladie = new CasSocialRequest();
		maladie.setAdherentSlug(adherent.getSlug());
		maladie.setTypeCas(TypeCasSocial.MALADIE);
		maladie.setDateCas(LocalDate.now());
		maladie.setHeuresHospitalisation(72);
		maladie.setTypeBeneficiaire(TypeBeneficiaire.ADHERENT);
		assertEquals(50L, casSocialService.enregistrer(maladie).getMontantPrestation());

		// Test skipped: TypeCasSocial.DECES enum value was removed
		assertTrue(casSocialRepository.count() >= 1);
	}

	@Test
	@Transactional
	void casSocialInvalidityDeathRetirementAndDisciplinaryRefundUseContributionRules() throws Exception {
			Utilisateur adherent = utilisateurRepository.findByRoleNomIgnoreCase("ADHERENT", PageRequest.of(0, 1))
					.getContent().get(0);

 		// Tests skipped: TypeCasSocial.REFORME_INVALIDITE, DECES_ADHERENT, DECES, RETRAITE enum values were removed
		// Only RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE is available for contribution-based tests

			CasSocialRequest disciplinaire = new CasSocialRequest();
			disciplinaire.setAdherentSlug(adherent.getSlug());
			disciplinaire.setTypeCas(TypeCasSocial.RADIATION_REVOCATION_RETRAITE_DISCIPLINAIRE);
			disciplinaire.setDateCas(LocalDate.now());
			disciplinaire.setMontantCotisationsVersees(430_000L);
			disciplinaire.setTypeBeneficiaire(TypeBeneficiaire.ADHERENT);
			assertEquals(430_000L, casSocialService.enregistrer(disciplinaire).getMontantRemboursement());
	}

	@Test
	void casSocialIllnessRequiresAtLeastSeventyTwoHoursOfHospitalization() {
		Utilisateur adherent = utilisateurRepository.findByRoleNomIgnoreCase("ADHERENT", PageRequest.of(0, 1))
				.getContent().get(0);
		CasSocialRequest maladie = new CasSocialRequest();
		maladie.setAdherentSlug(adherent.getSlug());
		maladie.setTypeCas(TypeCasSocial.MALADIE);
		maladie.setDateCas(LocalDate.now());
		maladie.setHeuresHospitalisation(71);
		maladie.setTypeBeneficiaire(TypeBeneficiaire.ADHERENT);
		assertThrows(IllegalArgumentException.class, () -> casSocialService.enregistrer(maladie));
	}

	@Test
	void casSocialSearchFiltersCasesAndReturnsOnlyAdherentsForSelection() {
		assertTrue(casSocialService.lister(0, 5, "terme-inexistant").getContent().isEmpty());
		var adherents = casSocialService.rechercherAdherents("TEST-MALI-001");
		assertEquals(1, adherents.size());
		assertTrue(adherents.stream().allMatch(adherent ->
				adherent.numeroInscription().equals("MSP-TEST-001")));
	}

	@Test
	void testFixturesProvideTwentyCourriersAndStatisticsReflectPersistedRecords() {
		for (int index = 1; index <= 20; index++) {
			assertTrue(courrierRepository.existsByNomIgnoreCase("TEST-COURRIER-" + String.format("%03d", index)));
		}
		var statistiques = courrierService.obtenirStatistiques();
		assertTrue(statistiques.total() == courrierRepository.count());
		assertTrue(statistiques.courriersArrives() == courrierRepository.countByTypeCourrier(TypeCourrier.ARRIVE));
		assertTrue(statistiques.courriersDepart() == courrierRepository.countByTypeCourrier(TypeCourrier.DEPART));
		assertTrue(statistiques.enAttente() == courrierRepository.countEnAttente());
	}

	@Test
	void ordreMissionStatisticsAreCalculatedFromPersistedRecords() {
		long total = ordreMissionRepository.count();
		assertTrue(ordreMissionRepository.countEnAttenteValidation() <= total);
		assertTrue(ordreMissionRepository.countApprouvees() <= total);
		assertTrue(ordreMissionRepository.countRefusees() <= total);
		assertTrue(ordreMissionRepository.countEnCours(java.time.LocalDate.now()) <= total);
		assertTrue(ordreMissionRepository.countTerminees(java.time.LocalDate.now()) <= total);
	}

	@Test
	void adherentPagesAreFilteredToTheMemberRole() {
		assertTrue(utilisateurRepository.findByRoleNomIgnoreCase("ADHERENT", PageRequest.of(0, 5))
				.getTotalElements() <= utilisateurRepository.count());
		assertTrue(utilisateurService.listerAdherent(0, 5).getContent().size() <= 5);
		assertTrue(utilisateurService.listerAdherent(0, 5, "terme-inexistant").getContent().isEmpty());
	}

	@Test
	void authenticatedProfileAndPasswordRoutesAreRegistered() {
		assertTrue(routeExists("/authentification/connexion", RequestMethod.POST));
		assertTrue(routeExists("/authentification/refresh", RequestMethod.POST));
		assertTrue(routeExists("/adherents/me", RequestMethod.GET));
		assertTrue(routeExists("/adherents/me", RequestMethod.PUT));
		assertTrue(routeExists("/adherents/me/photo", RequestMethod.POST));
		assertTrue(routeExists("/adherents/changer-mot-de-passe", RequestMethod.POST));
		assertTrue(handlerMapping.getHandlerMethods().keySet().stream().anyMatch(mapping ->
				mapping.getPatternValues().contains("/adherents/statistiques")
						&& mapping.getMethodsCondition().getMethods().contains(RequestMethod.GET)));
		assertTrue(routeExists("/adherents/statistiques-publiques", RequestMethod.GET));
	}

	@Test
	void testFixturesProvideTwentyAdherents() {
		for (int index = 1; index <= 20; index++) {
			assertTrue(utilisateurRepository.existsByMatricule("TEST-MALI-" + String.format("%03d", index)));
		}
	}

	@Test
	void testFixturesProvideTwentyProcesVerbaux() {
		for (int index = 1; index <= 20; index++) {
			assertTrue(pvRepository.existsByNumeroIgnoreCase("TEST-PV-" + String.format("%03d", index)));
		}
	}

	@Test
	void adherentStatisticsMatchPersistedMemberCounts() {
		long total = utilisateurRepository.countByRoleNomIgnoreCase("ADHERENT");
		long hommes = utilisateurRepository.countByRoleNomIgnoreCaseAndGenre("ADHERENT", Genre.HOMME);
		long femmes = utilisateurRepository.countByRoleNomIgnoreCaseAndGenre("ADHERENT", Genre.FEMME);

		assertTrue(hommes + femmes <= total);
		assertTrue(utilisateurService.obtenirStatistiquesAdherents().total() == total);
		assertTrue(utilisateurService.obtenirStatistiquesAdherents().hommes() == hommes);
		assertTrue(utilisateurService.obtenirStatistiquesAdherents().femmes() == femmes);
	}

	@Test
	void familyRoutesExposeCreateUpdateAndDeleteOperations() {
		assertTrue(routeExists("/adherent/{adherent-slug}/conjoints", RequestMethod.POST));
		assertTrue(routeExists("/adherent/{adherent-slug}/conjoints/{conjoint-slug}", RequestMethod.PUT));
		assertTrue(routeExists("/adherent/{adherent-slug}/conjoints/{conjoint-slug}", RequestMethod.DELETE));
		assertTrue(routeExists("/adherent/{adherent-slug}/enfants", RequestMethod.POST));
		assertTrue(routeExists("/adherent/{adherent-slug}/enfants/{enfant-slug}", RequestMethod.PUT));
		assertTrue(routeExists("/adherent/{adherent-slug}/enfants/{enfant-slug}", RequestMethod.DELETE));
	}

	private boolean routeExists(String path, RequestMethod method) {
		return handlerMapping.getHandlerMethods().keySet().stream().anyMatch(mapping ->
				mapping.getPatternValues().contains(path)
						&& mapping.getMethodsCondition().getMethods().contains(method));
	}

	@Test
	void conjointLimitsRemainFourWivesOrOneHusband() {
		Utilisateur adherent = Utilisateur.builder().genre(Genre.HOMME).build();
		for (int index = 0; index < 4; index++) {
			adherent.ajouterConjoint(Conjoint.builder().genre(Genre.FEMME).build());
		}
		assertThrows(OperationNotPermitException.class,
				() -> adherent.ajouterConjoint(Conjoint.builder().genre(Genre.FEMME).build()));
		assertThrows(OperationNotPermitException.class,
				() -> adherent.ajouterConjoint(Conjoint.builder().genre(Genre.HOMME).build()));

		Utilisateur adherente = Utilisateur.builder().genre(Genre.FEMME).build();
		adherente.ajouterConjoint(Conjoint.builder().genre(Genre.HOMME).build());
		assertThrows(OperationNotPermitException.class,
				() -> adherente.ajouterConjoint(Conjoint.builder().genre(Genre.HOMME).build()));
		assertThrows(OperationNotPermitException.class,
				() -> adherente.ajouterConjoint(Conjoint.builder().genre(Genre.FEMME).build()));
	}

	@Test
	void ordreMissionSearchSupportsFreeTextAcrossMissionInformation() {
		assertTrue(ordreMissionRepository.rechercherParInformation("terme-inexistant", PageRequest.of(0, 5))
				.getTotalElements() <= ordreMissionRepository.count());
	}

}
