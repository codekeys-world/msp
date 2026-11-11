package com.codekeys.journalAction;

import com.codekeys.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_journal_action", indexes = {
        @Index(name = "idx_journal_date", columnList = "dateAction"),
        @Index(name = "idx_journal_role", columnList = "role"),
        @Index(name = "idx_journal_module", columnList = "module")
})
@EntityListeners(AuditingEntityListener.class)
public class JournalAction extends AbstractEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String role;

    private String matricule;

    @Column(nullable = false)
    private String adresseIp;

    @Column(nullable = false, length = 500)
    private String client;

    @Column(nullable = false, length = 1000)
    private String action;

    @Column(length = 100)
    private String module;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    @Builder.Default
    private String statut = "SUCCES";

    @Column(nullable = false)
    private LocalDateTime dateAction;

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }
}
