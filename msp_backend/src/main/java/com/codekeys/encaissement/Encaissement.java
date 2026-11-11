package com.codekeys.encaissement;

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
@Table(name = "_encaissement", indexes = @Index(
        name = "idx_encaissement_situation",
        columnList = "date_encaissement, type_encaissement, montant"))
@EntityListeners(AuditingEntityListener.class)
public class Encaissement extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEncaissement typeEncaissement;
    @Column(nullable = false)
    private LocalDate dateEncaissement;
    private LocalDate dateDebutPeriode;
    private LocalDate dateFinPeriode;
    @Column(nullable = false)
    private Long montant;
    private String typeBudget;
    private String observation;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "justificatif_id")
    private Document justificatif;
}
