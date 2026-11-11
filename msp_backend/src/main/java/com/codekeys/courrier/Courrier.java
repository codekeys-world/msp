package com.codekeys.courrier;

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
@Table(name = "_courrier")
@EntityListeners(AuditingEntityListener.class)
public class Courrier extends BaseEntity {
    @Column(unique = true)
    private String nom;
    @Column(nullable = false)
    private String objet;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCourrier typeCourrier;
    @Column(nullable = false)
    private String description;
    // Date de réception des courriers entrants.
    private LocalDate dateArrivee;
    // Date d'envoi des courriers sortants.
    private LocalDate dateDepart;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courrier_id")
    private Document documentCourrier;
}
