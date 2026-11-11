package com.codekeys.token;

import com.codekeys.common.BaseEntity;
import com.codekeys.utilisateur.Utilisateur;
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
@Table(name = "_token")
@EntityListeners(AuditingEntityListener.class)
public class Token extends BaseEntity {
    private boolean estExpire;
    @Column(columnDefinition = "TEXT")
    private String token;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

}
