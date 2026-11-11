package com.codekeys.validation_entite;

import com.codekeys.common.AbstractEntity;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_validation_entite")
@EntityListeners(AuditingEntityListener.class)
public class ValidationEntite extends AbstractEntity {
    @Column(nullable = true)
    private LocalDateTime dateValidation;
    @Column(nullable = true)
    private LocalDateTime dateRefus;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "valider_par")
    private Utilisateur validateur;
    @Column(nullable = true)
    private String message;
}
