package com.codekeys.conjoint;

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
@Table(name = "_conjoint")
@EntityListeners(AuditingEntityListener.class)
public class Conjoint extends Personne {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "acte_mariage_id")
    private Document acteDeMariage;
    @ManyToOne
    @JoinColumn(name = "adherent_id")
    @JsonIgnore
    private Utilisateur utilisateur;
}
