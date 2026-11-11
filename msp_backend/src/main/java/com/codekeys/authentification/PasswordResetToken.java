package com.codekeys.authentification;

import com.codekeys.common.AbstractEntity;
import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "_password_reset_token")
@Getter @Setter
public class PasswordResetToken extends AbstractEntity {
    private String token;
    private LocalDateTime expiresAt;
    @ManyToOne(optional = false)
    private Utilisateur utilisateur;
}
