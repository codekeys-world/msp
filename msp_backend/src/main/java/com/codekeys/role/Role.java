package com.codekeys.role;

import com.codekeys.authorisation.Authorisation;
import com.codekeys.common.AbstractEntity;
import com.codekeys.utilisateur.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_role")
@EntityListeners(AuditingEntityListener.class)
public class Role extends AbstractEntity {
    @Column(nullable = false, unique = true)
    private String nom;
    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<Utilisateur> utilisateurs;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private List<Authorisation> authorisations;

    public void ajouterUtilisateur(Utilisateur utilisateur) {
        this.utilisateurs.add(utilisateur);
    }

    public void retirerUtilisateur(Utilisateur utilisateur) {
        this.utilisateurs.remove(utilisateur);
    }

    public void ajouterAuthorisation(Authorisation authorisation) {
        this.authorisations.add(authorisation);
    }

    public void retirerAuthorisation(Authorisation authorisation) {
        this.authorisations.remove(authorisation);
    }
}
