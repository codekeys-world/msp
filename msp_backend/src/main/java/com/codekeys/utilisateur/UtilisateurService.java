package com.codekeys.utilisateur;

import com.codekeys.authentification.ModifierMotDePasse;
import com.codekeys.authentification.MotDePasseOublierRequest;
import com.codekeys.authentification.ReinitialiserMotDePasseRequest;
import com.codekeys.authentification.PasswordResetToken;
import com.codekeys.authentification.PasswordResetTokenRepository;
import com.codekeys.authentification.EmailService;
import com.codekeys.common.PageReponse;
import com.codekeys.common.Genre;
import com.codekeys.execption.OperationNotPermitException;
import com.codekeys.fichier.Document;
import com.codekeys.fichier.DocumentService;
import com.codekeys.fichier.TypeDocument;
import com.codekeys.conjoint.Conjoint;
import com.codekeys.conjoint.ConjointMapper;
import com.codekeys.conjoint.ConjointRepository;
import com.codekeys.enfant.Enfant;
import com.codekeys.enfant.EnfantMapper;
import com.codekeys.enfant.EnfantRepository;
import com.codekeys.role.Role;
import com.codekeys.role.RoleService;
import com.codekeys.role.RoleRepository;
import com.codekeys.authorisation.Authorisation;
import com.codekeys.authorisation.AuthorisationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UtilisateurService implements UserDetailsService {
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    //private final GestionnaireFichier gestionnaireFichier;
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final DocumentService documentService;
    private final ConjointMapper conjointMapper;
    private final EnfantMapper enfantMapper;
    private final ConjointRepository conjointRepository;
    private final EnfantRepository enfantRepository;
    private final AuthorisationRepository authorisationRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    @Value("${application.frontend.url}") private String frontendUrl;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String identifiant = username.trim();
        Utilisateur utilisateur = this.utilisateurRepository.findByEmailIgnoreCase(identifiant)
                .or(() -> this.utilisateurRepository.findByMatriculeIgnoreCase(identifiant))
                .or(() -> this.utilisateurRepository.findByTelephonePrincipalIgnoreCase(identifiant))
                .orElseThrow(
                () -> new UsernameNotFoundException("Aucun utilisateur n'a été trouvé avec le nom d'utilisateur fournit")
        );
        utilisateur.getAuthorities();
        return utilisateur;
    }

    public Utilisateur rechercherUtilisateurParSlug(String slug) {
        return this.utilisateurRepository.findUtilisateurBySlug(slug).orElseThrow(
                ()-> new EntityNotFoundException("Aucun utilisateur n'a été trouvé !")
        );
    }


    /*public UtilisateurReponse enregistrerAdherent(@Valid UtilisateurRequest utilisateurRequest) throws IOException {
        Document profile = null;
        if(utilisateurRequest.getProfileImage() != null) {
            profile = this.documentService.uploadFile(utilisateurRequest.getProfileImage(), TypeDocument.PROFILE);
        }
        Utilisateur utilisateur = this.utilisateurMapper.utilisateurRequestToUtilisateur(utilisateurRequest);
        utilisateur.setProfileImage(profile);
        utilisateur = this.utilisateurRepository.save(utilisateur);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }*/

    @Transactional
    public UtilisateurReponseDetaillee enregistrerAdherent(
            @Valid AdherentEnregistrementRequest request,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        UtilisateurRequest utilisateurRequest = request.getAdherent();
        String passwordGenere = this.genererMotDePasse();
        utilisateurRequest.setPassword(passwordGenere);
        utilisateurRequest.setRoleId(null);
        utilisateurRequest.setAuthorisationsId(null);
        List<com.codekeys.conjoint.ConjointRequest> conjoints = request.getConjoints() == null ? List.of() : request.getConjoints();
        List<com.codekeys.enfant.EnfantRequest> descendants = request.getDescendants() == null ? List.of() : request.getDescendants();
        int maxConjoints = utilisateurRequest.getGenre() == com.codekeys.common.Genre.HOMME ? 4 : 1;
        if (conjoints.size() > maxConjoints) {
            throw new IllegalArgumentException("Le nombre de conjoints dépasse la limite autorisée.");
        }
        com.codekeys.common.Genre genreConjointAttendu = utilisateurRequest.getGenre() == com.codekeys.common.Genre.HOMME
                ? com.codekeys.common.Genre.FEMME : com.codekeys.common.Genre.HOMME;
        if (conjoints.stream().anyMatch(conjoint -> conjoint.getGenre() != genreConjointAttendu)) {
            throw new IllegalArgumentException("Le genre du conjoint doit être opposé à celui de l'adhérent.");
        }
        for (int index = 0; index < conjoints.size(); index++) {
            this.exigerFichier(multipartRequest, "conjointActe-" + index, "L'acte de mariage de chaque conjoint est obligatoire.");
        }
        for (int index = 0; index < descendants.size(); index++) {
            this.exigerFichier(multipartRequest, "descendantActe-" + index, "L'acte de naissance de chaque descendant est obligatoire.");
            this.exigerFichier(multipartRequest, "descendantCertificat-" + index, "Le certificat de vie collectif de chaque descendant est obligatoire.");
        }
        Utilisateur utilisateur = this.utilisateurMapper.utilisateurRequestToUtilisateur(utilisateurRequest);
        MultipartFile profile = multipartRequest.getFile("profileImage");
        if (profile != null && !profile.isEmpty()) {
            utilisateur.setProfileImage(this.documentService.uploadFile(profile, TypeDocument.PROFILE, utilisateur.getSlug()));
        }
        utilisateur = this.utilisateurRepository.save(utilisateur);

        try {
            this.emailService.envoyerCredentialsAdherent(
                    utilisateurRequest.getEmail(),
                    utilisateurRequest.getNom() + " " + utilisateurRequest.getPrenom(),
                    utilisateurRequest.getMatricule(),
                    passwordGenere
            );
        } catch (MessagingException e) {
            throw new IOException("Impossible d'envoyer les identifiants par email à l'adhérent.", e);
        }

        for (int index = 0; index < conjoints.size(); index++) {
            Conjoint conjoint = this.conjointMapper.conjointRequestToConjoint(conjoints.get(index));
            conjoint.setUtilisateur(utilisateur);
            utilisateur.ajouterConjoint(conjoint);
            MultipartFile acte = multipartRequest.getFile("conjointActe-" + index);
            conjoint.setActeDeMariage(this.documentService.uploadFile(
                    acte, TypeDocument.CONJOINT, utilisateur.getSlug() + "/" + conjoint.getSlug()
            ));
            this.conjointRepository.save(conjoint);
        }

        for (int index = 0; index < descendants.size(); index++) {
            Enfant enfant = this.enfantMapper.enfantRequestToEnfant(descendants.get(index));
            enfant.setUtilisateur(utilisateur);
            utilisateur.ajouterEnfant(enfant);
            MultipartFile acte = multipartRequest.getFile("descendantActe-" + index);
            enfant.setActeDeNaissance(this.documentService.uploadFile(
                    acte, TypeDocument.DESCENDANT, utilisateur.getSlug() + "/" + enfant.getSlug()
            ));
            MultipartFile certificat = multipartRequest.getFile("descendantCertificat-" + index);
            enfant.setCertificatVieCollectif(this.documentService.uploadFile(
                    certificat, TypeDocument.CERTIFICAT_VIE_COLLECTIF,
                    utilisateur.getSlug() + "/" + enfant.getSlug()
            ));
            this.enfantRepository.save(enfant);
        }
        return this.rechercherDetailleeAdherent(utilisateur.getSlug());
    }

    @Transactional
    public UtilisateurReponse enregistrerAdherent(@Valid UtilisateurRequest utilisateurRequest) {
        String passwordGenere = this.genererMotDePasse();
        utilisateurRequest.setPassword(passwordGenere);
        utilisateurRequest.setRoleId(null);
        utilisateurRequest.setAuthorisationsId(null);
        Utilisateur utilisateur = this.utilisateurMapper.utilisateurRequestToUtilisateur(utilisateurRequest);
        utilisateur = this.utilisateurRepository.save(utilisateur);

        try {
            this.emailService.envoyerCredentialsAdherent(
                    utilisateurRequest.getEmail(),
                    utilisateurRequest.getNom() + " " + utilisateurRequest.getPrenom(),
                    utilisateurRequest.getMatricule(),
                    passwordGenere
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Impossible d'envoyer les identifiants par email à l'adhérent.", e);
        }

        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    private String genererMotDePasse() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom secureRandom = new SecureRandom();
        int longueur = 12;
        StringBuilder motDePasse = new StringBuilder();
        for (int i = 0; i < longueur; i++) {
            motDePasse.append(caracteres.charAt(secureRandom.nextInt(caracteres.length())));
        }
        return motDePasse.toString();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageReponse<UtilisateurReponse> listerUtilisateur(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size,  Sort.by("id").descending());
        Page<@NonNull Utilisateur> utilisateurs = this.utilisateurRepository.findAll(pageRequest);
        List<UtilisateurReponse> utilisateurReponses = utilisateurs.stream()
                .map(this.utilisateurMapper::utilisateurToUtilisateurReponse)
                .toList();
        return new PageReponse<UtilisateurReponse>(
                utilisateurReponses,
                utilisateurs.getNumber(),
                utilisateurs.getSize(),
                utilisateurs.getTotalElements(),
                utilisateurs.getTotalPages(),
                utilisateurs.isFirst(),
                utilisateurs.isLast()
        );
    }

    public UtilisateurReponse rechercherParSlug(String adherentSlug) {
        Utilisateur utilisateur = this.rechercherUtilisateurParSlug(adherentSlug);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    public UtilisateurReponse rechercherParMatricule(String matricule) {
        Utilisateur utilisateur = this.utilisateurRepository.findByMatricule(matricule).orElseThrow(
                () -> new EntityNotFoundException("Aucun adhérent n'a été trouvé.")
        );
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    public UtilisateurReponse rechercherParNumeroTelephone(String numeroTelephone) {
        Utilisateur utilisateur = this.utilisateurRepository.findByTelephonePrincipalOrTelephoneSecondaire(numeroTelephone, numeroTelephone)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur n'a été trouvé avec ce numéro de téléphone"));
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    public UtilisateurReponse rechercherParNumeroInscription(String numeroInscription) {
        Utilisateur utilisateur = this.utilisateurRepository.findByNumeroInscription(numeroInscription)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur n'a été trouvé avec ce numéro de téléphone"));
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    @Transactional
    public boolean supprimerAdherent(String adherentSlug) {
        Utilisateur utilisateur = this.rechercherAdherentParSlug(adherentSlug);
        try {
            this.documentService.supprimerFichier(utilisateur.getProfileImage());
            utilisateur.getConjoints().forEach(conjoint -> {
                try {
                    this.documentService.supprimerFichier(conjoint.getActeDeMariage());
                } catch (IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
            utilisateur.getEnfants().forEach(enfant -> {
                try {
                    this.documentService.supprimerFichier(enfant.getActeDeNaissance());
                    this.documentService.supprimerFichier(enfant.getCertificatVieCollectif());
                } catch (IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
        } catch (IOException | UncheckedIOException exception) {
            throw new IllegalStateException("Impossible de supprimer les documents de l'adhérent.", exception);
        }
        this.utilisateurRepository.delete(utilisateur);
        return true;
    }

    @Transactional
    public UtilisateurReponseDetaillee modifierUtilisateur(
            String adherentSlug,
            @Valid AdherentEnregistrementRequest request,
            MultipartHttpServletRequest multipartRequest
    ) throws IOException {
        UtilisateurRequest utilisateurRequest = request.getAdherent();
        utilisateurRequest.setRoleId(null);
        utilisateurRequest.setAuthorisationsId(null);
        Utilisateur utilisateurBdd = this.rechercherAdherentParSlug(adherentSlug);
        Utilisateur utilisateurNouveauInfo = this.utilisateurMapper.utilisateurRequestToUtilisateur(utilisateurRequest);
        int maxConjoints = utilisateurNouveauInfo.getGenre() == com.codekeys.common.Genre.HOMME ? 4 : 1;
        if (utilisateurBdd.getConjoints().size() > maxConjoints) {
            throw new IllegalArgumentException("Le genre choisi ne permet pas de conserver le nombre actuel de conjoints.");
        }
        utilisateurBdd.setNom(utilisateurNouveauInfo.getNom());
        utilisateurBdd.setPrenom(utilisateurNouveauInfo.getPrenom());
        utilisateurBdd.setTelephonePrincipal(utilisateurNouveauInfo.getTelephonePrincipal());
        utilisateurBdd.setTelephoneSecondaire(utilisateurNouveauInfo.getTelephoneSecondaire());
        utilisateurBdd.setDateNaissance(utilisateurNouveauInfo.getDateNaissance());
        utilisateurBdd.setLieuNaissance(utilisateurNouveauInfo.getLieuNaissance());
        utilisateurBdd.setAdresse(utilisateurNouveauInfo.getAdresse());
        utilisateurBdd.setGenre(utilisateurNouveauInfo.getGenre());
        utilisateurBdd.setEmail(utilisateurNouveauInfo.getEmail());
        utilisateurBdd.setMatricule(utilisateurNouveauInfo.getMatricule());
        utilisateurBdd.setNumeroInscription(utilisateurNouveauInfo.getNumeroInscription());
        //GestionnaireFichier.supprimerFichier(utilisateurBdd.getProfileImage());
        //utilisateurBdd.setProfileImage(this.gestionnaireFichier.saveFile("adherent", utilisateurRequest.getProfileImage(), utilisateurBdd.getSlug()));
        utilisateurBdd.setPere(utilisateurNouveauInfo.getPere());
        utilisateurBdd.setMere(utilisateurNouveauInfo.getMere());
        utilisateurBdd.setObservations(utilisateurNouveauInfo.getObservations());
        if (utilisateurNouveauInfo.getPassword() != null) {
            utilisateurBdd.setPassword(utilisateurNouveauInfo.getPassword());
        }
        MultipartFile profile = multipartRequest.getFile("profileImage");
        if (profile != null && !profile.isEmpty()) {
            Document ancienProfile = utilisateurBdd.getProfileImage();
            Document nouveauProfile = this.documentService.uploadFile(
                    profile, TypeDocument.PROFILE, utilisateurBdd.getSlug()
            );
            if (ancienProfile != null && !ancienProfile.getChemin().equals(nouveauProfile.getChemin())) {
                this.documentService.supprimerFichier(ancienProfile);
            }
            utilisateurBdd.setProfileImage(nouveauProfile);
        }
        utilisateurBdd = this.utilisateurRepository.save(utilisateurBdd);
        return this.rechercherDetailleeAdherent(utilisateurBdd.getSlug());
    }

    @Transactional
    public UtilisateurReponse modifierUtilisateur(String adherentSlug, @Valid UtilisateurRequest utilisateurRequest) {
        utilisateurRequest.setRoleId(null);
        utilisateurRequest.setAuthorisationsId(null);
        Utilisateur utilisateurBdd = this.rechercherAdherentParSlug(adherentSlug);
        Utilisateur utilisateurNouveauInfo = this.utilisateurMapper.utilisateurRequestToUtilisateur(utilisateurRequest);
        int maxConjoints = utilisateurNouveauInfo.getGenre() == com.codekeys.common.Genre.HOMME ? 4 : 1;
        if (utilisateurBdd.getConjoints().size() > maxConjoints) {
            throw new IllegalArgumentException("Le genre choisi ne permet pas de conserver le nombre actuel de conjoints.");
        }
        utilisateurBdd.setNom(utilisateurNouveauInfo.getNom());
        utilisateurBdd.setPrenom(utilisateurNouveauInfo.getPrenom());
        utilisateurBdd.setTelephonePrincipal(utilisateurNouveauInfo.getTelephonePrincipal());
        utilisateurBdd.setTelephoneSecondaire(utilisateurNouveauInfo.getTelephoneSecondaire());
        utilisateurBdd.setDateNaissance(utilisateurNouveauInfo.getDateNaissance());
        utilisateurBdd.setLieuNaissance(utilisateurNouveauInfo.getLieuNaissance());
        utilisateurBdd.setAdresse(utilisateurNouveauInfo.getAdresse());
        utilisateurBdd.setGenre(utilisateurNouveauInfo.getGenre());
        utilisateurBdd.setEmail(utilisateurNouveauInfo.getEmail());
        utilisateurBdd.setMatricule(utilisateurNouveauInfo.getMatricule());
        utilisateurBdd.setNumeroInscription(utilisateurNouveauInfo.getNumeroInscription());
        utilisateurBdd.setPere(utilisateurNouveauInfo.getPere());
        utilisateurBdd.setMere(utilisateurNouveauInfo.getMere());
        utilisateurBdd.setObservations(utilisateurNouveauInfo.getObservations());
        if (utilisateurNouveauInfo.getPassword() != null) {
            utilisateurBdd.setPassword(utilisateurNouveauInfo.getPassword());
        }
        utilisateurBdd = this.utilisateurRepository.save(utilisateurBdd);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateurBdd);
    }

    public PageReponse<UtilisateurReponse> rechercherParInformation(String information, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "id"));
        Page<Utilisateur> utilisateurPage = this.utilisateurRepository.findAllByInformation(information, pageRequest);
        List<UtilisateurReponse> utilisateurReponses = utilisateurPage.getContent()
                .stream()
                .map(this.utilisateurMapper::utilisateurToUtilisateurReponse)
                .toList();

        return new PageReponse<UtilisateurReponse>(
                utilisateurReponses,
                utilisateurPage.getNumber(),
                utilisateurPage.getSize(),
                utilisateurPage.getTotalElements(),
                utilisateurPage.getTotalPages(),
                utilisateurPage.isFirst(),
                utilisateurPage.isLast()
        );
    }

    @Transactional
    public UtilisateurReponse assignerRoleAdherent(String adherentSlug, String roleSlug) {
        Utilisateur utilisateur = this.rechercherUtilisateurParSlug(adherentSlug);
        Role roleBySlug = this.roleService.findRoleBySlug(roleSlug);
        if(utilisateur.getRole().equals(roleBySlug)) {
            throw new OperationNotPermitException("Impossible de réaffecter le même role.");
        }
        utilisateur.setRole(roleBySlug);
        roleBySlug.ajouterUtilisateur(utilisateur);
        utilisateur = this.utilisateurRepository.save(utilisateur);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    @Transactional
    public UtilisateurReponse desactiverUtilisateur(String adherentSlug, boolean enabled) {
        Utilisateur utilisateur = this.rechercherUtilisateurParSlug(adherentSlug);
        utilisateur.setEnabled(enabled);
        utilisateur = this.utilisateurRepository.save(utilisateur);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }

    @Transactional
    public PageReponse<UtilisateurReponseDetaillee> listerAdherent(int page, int size) {
        return this.listerAdherent(page, size, "");
    }

    @Transactional
    public PageReponse<UtilisateurReponseDetaillee> listerAdherent(int page, int size, String terme) {
        PageRequest pageRequest = PageRequest.of(page, size,  Sort.by(Sort.Direction.DESC, "id"));
        Page<Utilisateur> utilisateurPage = terme == null || terme.isBlank()
                ? this.utilisateurRepository.findByRoleNomIgnoreCase("ADHERENT", pageRequest)
                : this.utilisateurRepository.rechercherAdherentsParInformation(terme.trim(), pageRequest);
        List<UtilisateurReponseDetaillee> utilisateurReponseDetaillees = utilisateurPage.getContent()
                .stream()
                .map(this.utilisateurMapper::utilisateurToUtilisateurReponseDetaillee)
                .toList();

        return new PageReponse<UtilisateurReponseDetaillee>(
                utilisateurReponseDetaillees,
                utilisateurPage.getNumber(),
                utilisateurPage.getSize(),
                utilisateurPage.getTotalElements(),
                utilisateurPage.getTotalPages(),
                utilisateurPage.isFirst(),
                utilisateurPage.isLast()
        );
    }

    @Transactional
    public UtilisateurReponseDetaillee rechercherDetailleeAdherent(String adherentSlug) {
        Utilisateur utilisateur = this.rechercherAdherentParSlug(adherentSlug);
        return this.utilisateurMapper.utilisateurToUtilisateurReponseDetaillee(utilisateur);
    }

    public AdherentStatistiques obtenirStatistiquesAdherents() {
        return new AdherentStatistiques(
                this.utilisateurRepository.countByRoleNomIgnoreCase("ADHERENT"),
                this.utilisateurRepository.countByRoleNomIgnoreCaseAndGenre("ADHERENT", Genre.HOMME),
                this.utilisateurRepository.countByRoleNomIgnoreCaseAndGenre("ADHERENT", Genre.FEMME)
        );
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<UtilisateurReponse> exporterAdherents() {
        List<Utilisateur> adherents = this.utilisateurRepository.findByRoleNomIgnoreCaseAndNoCasSocial(
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        return adherents.stream()
                .map(utilisateur -> this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur))
                .toList();
    }

    private Utilisateur rechercherAdherentParSlug(String adherentSlug) {
        Utilisateur utilisateur = this.rechercherUtilisateurParSlug(adherentSlug);
        if (utilisateur.getRole() == null || !"ADHERENT".equalsIgnoreCase(utilisateur.getRole().getNom())) {
            throw new EntityNotFoundException("Aucun adhérent n'a été trouvé.");
        }
        return utilisateur;
    }

    private void exigerFichier(MultipartHttpServletRequest request, String nom, String message) {
        MultipartFile fichier = request.getFile(nom);
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    @Transactional
    public boolean motDePasseOublier(@Valid MotDePasseOublierRequest request) {
        this.utilisateurRepository.findByEmailIgnoreCase(request.getEmail().trim()).ifPresent(utilisateur -> {
            this.passwordResetTokenRepository.deleteByUtilisateurId(utilisateur.getId());
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setSlug(UUID.randomUUID().toString());
            resetToken.setToken(token);
            resetToken.setUtilisateur(utilisateur);
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            this.passwordResetTokenRepository.save(resetToken);
            String resetUrl = this.frontendUrl + "/reinitialiser-mot-de-passe?token=" + token;
            try {
                this.emailService.envoyerLienReinitialisation(utilisateur.getEmail(), utilisateur.getPrenom(), resetUrl);
            } catch (MessagingException exception) {
                throw new IllegalStateException("Impossible d'envoyer le courriel de réinitialisation.", exception);
            }
        });
        return true;
    }

    @Transactional
    public boolean reinitialiserMotDePasse(@Valid ReinitialiserMotDePasseRequest request) {
        if (!Objects.equals(request.getPassword(), request.getConfirmationPassword())) {
            throw new OperationNotPermitException("Les deux mots de passe ne concordent pas.");
        }
        PasswordResetToken resetToken = this.passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new OperationNotPermitException("Ce lien de réinitialisation est invalide ou expiré."));
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            this.passwordResetTokenRepository.delete(resetToken);
            throw new OperationNotPermitException("Ce lien de réinitialisation est invalide ou expiré.");
        }
        Utilisateur utilisateur = resetToken.getUtilisateur();
        if (this.bCryptPasswordEncoder.matches(request.getPassword(), utilisateur.getPassword())) {
            throw new OperationNotPermitException("Le nouveau mot de passe doit être différent de l'ancien.");
        }
        utilisateur.setPassword(this.bCryptPasswordEncoder.encode(request.getPassword()));
        this.utilisateurRepository.save(utilisateur);
        this.passwordResetTokenRepository.delete(resetToken);
        return true;
    }

    @Transactional
    public Map<String, Object> optionsAccesUtilisateur() {
        List<Map<String, String>> roles = this.roleRepository.findAll().stream()
                .map(role -> Map.of("slug", role.getSlug(), "nom", role.getNom())).toList();
        List<Map<String, Object>> authorisations = this.authorisationRepository.findAll().stream()
                .map(authorisation -> Map.<String, Object>of("id", authorisation.getId(), "nom", authorisation.getNom())).toList();
        return Map.of("roles", roles, "authorisations", authorisations);
    }

    @Transactional
    public UtilisateurReponseDetaillee modifierAccesUtilisateur(String slug, @Valid AccesUtilisateurRequest request) {
        Utilisateur utilisateur = this.rechercherUtilisateurParSlug(slug);
        Role role = this.roleService.findRoleBySlug(request.getRoleSlug());
        List<Long> ids = request.getAuthorisationIds().stream().distinct().toList();
        List<Authorisation> authorisations = this.authorisationRepository.findAllById(ids);
        if (authorisations.size() != ids.size()) {
            throw new EntityNotFoundException("Une ou plusieurs autorisations sont introuvables.");
        }
        utilisateur.setRole(role);
        utilisateur.setAuthorisations(new java.util.LinkedHashSet<>(authorisations));
        return this.utilisateurMapper.utilisateurToUtilisateurReponseDetaillee(this.utilisateurRepository.save(utilisateur));
    }

    public boolean modifierMotDePasse(@Valid ModifierMotDePasse request) {
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!this.bCryptPasswordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getPassword())) {
            throw new OperationNotPermitException("Le mot de passe actuel est incorrect.");
        }
        if(!Objects.equals(request.getNouveauMotDePasse(), request.getConfirmationPassword())){
            throw new OperationNotPermitException("Les deux mots de passe ne concordent pas.");
        }

        if(this.bCryptPasswordEncoder.matches(request.getNouveauMotDePasse(), utilisateur.getPassword())){
            throw new OperationNotPermitException("Le nouveau mot de passe et l'ancien ne peuvent pas être les mêmes.");
        }

        utilisateur.setPassword(this.bCryptPasswordEncoder.encode(request.getNouveauMotDePasse()));
        this.utilisateurRepository.save(utilisateur);
        return true;
    }

    @Transactional
    public UtilisateurReponseDetaillee obtenirProfilConnecte() {
        Utilisateur utilisateur = this.utilisateurConnecte();
        return this.utilisateurMapper.utilisateurToUtilisateurReponseDetaillee(utilisateur);
    }

    @Transactional
    public UtilisateurReponseDetaillee modifierProfilConnecte(@Valid ProfilUtilisateurRequest request) {
        Utilisateur utilisateur = this.utilisateurConnecte();
        String email = request.getEmail().trim();
        String matricule = request.getMatricule().trim();
        String telephonePrincipal = request.getTelephonePrincipal().trim();
        String numeroInscription = request.getNumeroInscription().trim();
        if (this.utilisateurRepository.existsByEmailIgnoreCaseAndIdNot(email, utilisateur.getId())) {
            throw new OperationNotPermitException("Cette adresse e-mail est déjà utilisée.");
        }
        if (this.utilisateurRepository.existsByMatriculeIgnoreCaseAndIdNot(matricule, utilisateur.getId())) {
            throw new OperationNotPermitException("Ce matricule est déjà utilisé.");
        }
        if (this.utilisateurRepository.existsByTelephonePrincipalAndIdNot(telephonePrincipal, utilisateur.getId())) {
            throw new OperationNotPermitException("Ce numéro de téléphone est déjà utilisé.");
        }
        if (this.utilisateurRepository.existsByNumeroInscriptionAndIdNot(numeroInscription, utilisateur.getId())) {
            throw new OperationNotPermitException("Ce numéro d'inscription est déjà utilisé.");
        }
        com.codekeys.common.Genre genreConjointAttendu = request.getGenre() == com.codekeys.common.Genre.HOMME
                ? com.codekeys.common.Genre.FEMME : com.codekeys.common.Genre.HOMME;
        int nombreMaximumConjoints = request.getGenre() == com.codekeys.common.Genre.HOMME ? 4 : 1;
        if (utilisateur.getConjoints().size() > nombreMaximumConjoints
                || utilisateur.getConjoints().stream().anyMatch(conjoint -> conjoint.getGenre() != genreConjointAttendu)) {
            throw new OperationNotPermitException("Le genre choisi ne permet pas de conserver les informations des conjoints.");
        }
        utilisateur.setNom(request.getNom().trim().toUpperCase());
        utilisateur.setPrenom(request.getPrenom().trim());
        utilisateur.setEmail(email);
        utilisateur.setMatricule(matricule);
        utilisateur.setTelephonePrincipal(telephonePrincipal);
        utilisateur.setTelephoneSecondaire(request.getTelephoneSecondaire() == null
                ? null : request.getTelephoneSecondaire().trim());
        utilisateur.setDateNaissance(request.getDateNaissance());
        utilisateur.setLieuNaissance(request.getLieuNaissance().trim());
        utilisateur.setAdresse(request.getAdresse() == null ? null : request.getAdresse().trim());
        utilisateur.setNumeroInscription(numeroInscription);
        utilisateur.setPere(request.getPere().trim());
        utilisateur.setMere(request.getMere().trim());
        utilisateur.setGenre(request.getGenre());
        utilisateur.setObservations(request.getObservations() == null ? null : request.getObservations().trim());
        this.utilisateurRepository.save(utilisateur);
        return this.utilisateurMapper.utilisateurToUtilisateurReponseDetaillee(utilisateur);
    }

    @Transactional
    public UtilisateurReponseDetaillee modifierPhotoProfilConnecte(MultipartFile profileImage) throws IOException {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Veuillez sélectionner une photo de profil.");
        }
        if (profileImage.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La photo de profil ne doit pas dépasser 5 Mo.");
        }
        if (profileImage.getContentType() == null || !profileImage.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier sélectionné doit être une image.");
        }
        Utilisateur utilisateur = this.utilisateurConnecte();
        Document anciennePhoto = utilisateur.getProfileImage();
        Document nouvellePhoto = this.documentService.uploadFile(profileImage, TypeDocument.PROFILE, utilisateur.getSlug());
        utilisateur.setProfileImage(nouvellePhoto);
        this.utilisateurRepository.save(utilisateur);
        if (anciennePhoto != null && !anciennePhoto.getChemin().equals(nouvellePhoto.getChemin())) {
            this.documentService.supprimerFichier(anciennePhoto);
        }
        return this.utilisateurMapper.utilisateurToUtilisateurReponseDetaillee(utilisateur);
    }

    private Utilisateur utilisateurConnecte() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Utilisateur utilisateur)) {
            throw new OperationNotPermitException("Impossible d'identifier l'utilisateur connecté.");
        }
        return this.rechercherUtilisateurParSlug(utilisateur.getSlug());
    }

    public UtilisateurReponse updateUtilisateurImageProfile(@Valid MultipartFile profileImage, String adherentSlug) throws IOException {
        Utilisateur utilisateur = this.rechercherAdherentParSlug(adherentSlug);
        if (profileImage != null && !profileImage.isEmpty()) {
            Document ancienProfile = utilisateur.getProfileImage();
            Document profile = this.documentService.uploadFile(profileImage, TypeDocument.PROFILE, utilisateur.getSlug());
            if (ancienProfile != null && !ancienProfile.getChemin().equals(profile.getChemin())) {
                this.documentService.supprimerFichier(ancienProfile);
            }
            utilisateur.setProfileImage(profile);
        }
        utilisateur = this.utilisateurRepository.save(utilisateur);
        return this.utilisateurMapper.utilisateurToUtilisateurReponse(utilisateur);
    }
}
