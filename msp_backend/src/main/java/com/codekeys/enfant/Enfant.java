package com.codekeys.enfant;

import com.codekeys.common.Personne;
import com.codekeys.fichier.Document;
import com.codekeys.utilisateur.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_enfant")
@EntityListeners(AuditingEntityListener.class)
public class Enfant extends Personne {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "acte_naissance_id")
    private Document acteDeNaissance;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "certificat_vie_collectif_id")
    private Document certificatVieCollectif;
    @ManyToOne
    @JoinColumn(name = "adherent_id")
    @JsonIgnore
    private Utilisateur utilisateur;
}
