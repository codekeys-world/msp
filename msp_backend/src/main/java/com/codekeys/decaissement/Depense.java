package com.codekeys.decaissement;

import com.codekeys.common.BaseEntity;
import com.codekeys.fichier.Document;
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
@Table(name = "_depense", indexes = {
        @Index(name = "idx_depense_date", columnList = "date_depense"),
        @Index(name = "idx_depense_frais_statut", columnList = "frais_de_gestion_id, statut")
})
@EntityListeners(AuditingEntityListener.class)
public class Depense extends BaseEntity {
    @Column(nullable = false)
    private String libelle;
    @Column(nullable = false)
    private LocalDate dateDepense;
    @Column(nullable = false)
    private Long montant;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDepense statut;
    private String observation;
    private String observationValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frais_de_gestion_id", nullable = false)
    private FraisDeGestion fraisDeGestion;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "justificatif_id")
    private Document justificatif;
}
