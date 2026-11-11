package com.codekeys.materiel;

import com.codekeys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_materiel")
@EntityListeners(AuditingEntityListener.class)
public class Materiel extends BaseEntity {
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String categorie;

    @Column(nullable = false)
    private String fournisseur;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false)
    private BigDecimal prixUnitaire;

    private LocalDate dateAcquisition;

    @Column(nullable = false)
    private String emplacement;

    private String numeroSerie;

    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMateriel statut;
}
