package com.codekeys.evenement;

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
@Table(name = "_evenement")
@EntityListeners(AuditingEntityListener.class)
public class Evenement extends AbstractEntity {
    private String adresseIp;
    private String client;
    private String action;
    private LocalDateTime dateEvenement;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}
