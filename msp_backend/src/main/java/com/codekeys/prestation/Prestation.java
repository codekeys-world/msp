package com.codekeys.prestation;

import com.codekeys.common.BaseEntity;
import com.codekeys.fichier.Document;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_prestation")
@EntityListeners(AuditingEntityListener.class)
public class Prestation extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePrestation typePrestation;
    @Column(nullable = false)
    private LocalDate datePrestation;
    @Enumerated(EnumType.STRING)
    private com.codekeys.CasSocial.LienDefunt lienDefunt;
    private String nomDefunt;
    private String prenomDefunt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.codekeys.CasSocial.TypeBeneficiaire typeBeneficiaire;
    private String nomBeneficiaire;
    private String prenomBeneficiaire;
    private String telephoneBeneficiaire;
    private Long montantPrestation;
    private Integer dureeCotisationMois;
    private Long montantCotisationsVersees;
    private Long montantRemboursement;
    @Enumerated(EnumType.STRING)
    private com.codekeys.CasSocial.StatutPrestation statutPrestation;
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Utilisateur adherent;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "justificatif_id")
    private Document justificatif;
}
