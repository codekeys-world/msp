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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_frais_de_gestion")
@EntityListeners(AuditingEntityListener.class)
public class FraisDeGestion extends BaseEntity {
    @Column(nullable = false, unique = true)
    private Integer annee;
    @Column(nullable = false)
    private Long montantTotal;
    private String observation;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "justificatif_id")
    private Document justificatif;

    @OneToMany(mappedBy = "fraisDeGestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Depense> depenses;
}
