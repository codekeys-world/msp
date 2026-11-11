package com.codekeys.fichier;

import com.codekeys.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_document")
@EntityListeners(AuditingEntityListener.class)
public class Document extends AbstractEntity {
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String chemin;
    private String type;
    private Long taille;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;
}
