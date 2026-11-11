package com.codekeys.ordreMission;

import com.codekeys.common.BaseEntity;
import com.codekeys.fichier.Document;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "_ordre_mission")
@EntityListeners(AuditingEntityListener.class)
public class OrdreMission extends BaseEntity {
    @Column(unique = true)
    private String libelle;
    @Column(nullable = false)
    private String objet;
    @Column(nullable = false)
    private String imputationBudgetaire;
    @Column(nullable = false)
    private String moyenDeplacement;
    @Column(nullable = false)
    private String itineraire;
    @Column(nullable = false)
    private LocalDate dateDepart;
    @Column(nullable = false)
    private LocalDate dateRetour;
    private String observation;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ordre_mission_id")
    private Document ordreMission;
}
