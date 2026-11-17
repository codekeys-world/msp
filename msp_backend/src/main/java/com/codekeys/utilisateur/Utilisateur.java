package com.codekeys.utilisateur;

import com.codekeys.common.Genre;
import com.codekeys.authorisation.Authorisation;
import com.codekeys.common.Personne;
import com.codekeys.conjoint.Conjoint;
import com.codekeys.enfant.Enfant;
import com.codekeys.evenement.Evenement;
import com.codekeys.execption.OperationNotPermitException;
import com.codekeys.fichier.Document;
import com.codekeys.role.Role;
import com.codekeys.token.Token;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_utilisateur")
@EntityListeners(AuditingEntityListener.class)
public class Utilisateur extends Personne implements UserDetails {
    @Column(unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(unique = true)
    private String matricule;
    @Column(unique = true, nullable = false)
    private String numeroInscription;
    private String numeroAdhesion;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", nullable = true)
    private Document profileImage;
    private boolean accountLocked;
    private boolean accountNonExpired;
    @Builder.Default
    private boolean enabled = true;
    @Column(nullable = false)
    private String pere;
    @Column(nullable = false)
    private String mere;
    private String observations;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "_utilisateur_authorisation",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "authorisation_id"))
    @Builder.Default
    private Set<Authorisation> authorisations = new LinkedHashSet<>();
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Token> tokens;
    /*@OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Validation> validations;*/
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Conjoint> conjoints = new ArrayList<>();
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Enfant> enfants = new ArrayList<>();
    @OneToMany(mappedBy = "utilisateur", cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
    private List<Evenement> evenements;



    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        java.util.Set<String> permissions = new java.util.LinkedHashSet<>();
        if (this.role != null && this.role.getAuthorisations() != null) {
            this.role.getAuthorisations().forEach(authorisation -> permissions.add(authorisation.getNom()));
        }
        if (this.authorisations != null) this.authorisations.forEach(authorisation -> permissions.add(authorisation.getNom()));
        List<SimpleGrantedAuthority> grantedAuthorities = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        if (this.role != null) grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.getNom()));
        return grantedAuthorities;
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public @NonNull String getUsername() {
        /*return this.email;*/
        return this.getTelephonePrincipal();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getNomComplet(){
        return this.getPrenom() + " " + this.getNom();
    }

    public void ajouterConjoint(Conjoint conjoint) {
        if (this.getGenre() == Genre.HOMME) {
            if (conjoint.getGenre() != Genre.FEMME) {
                throw new OperationNotPermitException("Un adhérent de genre masculin ne peut déclarer qu'une conjointe.");
            }
            if (this.getConjoints().size() >= 4) {
                throw new OperationNotPermitException("Les conjointes d'un adhérent ne peuvent pas dépasser 4.");
            }
        } else if (this.getGenre() == Genre.FEMME) {
            if (conjoint.getGenre() != Genre.HOMME) {
                throw new OperationNotPermitException("Une adhérente de genre féminin ne peut déclarer qu'un mari.");
            }
            if (this.getConjoints().size() >= 1) {
                throw new OperationNotPermitException("Une femme adhérent ne peut avoir qu'un conjoint.");
            }
        } else {
            throw new OperationNotPermitException("Le genre de l'adhérent doit être renseigné avant d'ajouter un conjoint.");
        }
        this.conjoints.add(conjoint);
    }

    public void enleverConjoint(Conjoint conjoint) {
        this.conjoints.remove(conjoint);
    }

    public void ajouterEnfant(Enfant enfant) {
        this.enfants.add(enfant);
    }

    public void enleverEnfant(Enfant enfant) {
        this.enfants.remove(enfant);
    }


}
