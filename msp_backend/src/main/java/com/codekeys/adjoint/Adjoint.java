package com.codekeys.adjoint;

import com.codekeys.common.AbstractEntity;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_adjoint")
@EntityListeners(AuditingEntityListener.class)
public class Adjoint extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegant_id", nullable = false)
    private Utilisateur delegant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjoint_id", nullable = false)
    private Utilisateur adjoint;

    @Column(nullable = false)
    private String roleDelegue;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "est_actif", nullable = false)
    @Builder.Default
    private boolean estActif = true;

    private String motif;

}
