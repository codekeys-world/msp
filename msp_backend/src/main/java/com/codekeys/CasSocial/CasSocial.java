package com.codekeys.CasSocial;

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
@Table(name = "_cas_social")
@EntityListeners(AuditingEntityListener.class)
public class CasSocial extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCasSocial typeCas;
    @Column(nullable = false)
    private LocalDate dateCas;
    private Integer heuresHospitalisation;
    @Enumerated(EnumType.STRING)
    private LienDefunt lienDefunt;
    private String nomDefunt;
    private String prenomDefunt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeBeneficiaire typeBeneficiaire;
    private String nomBeneficiaire;
    private String prenomBeneficiaire;
    private String telephoneBeneficiaire;
    private Long montantPrestation;
    private Integer dureeCotisationMois;
    private Long montantCotisationsVersees;
    private Long montantRemboursement;
    @Enumerated(EnumType.STRING)
    private StatutPrestation statutPrestation;
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Utilisateur adherent;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "justificatif_id")
    private Document justificatif;
}
