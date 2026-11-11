package com.codekeys.validation;

import com.codekeys.common.BaseEntity;
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
@Table(name = "_validation")
@EntityListeners(AuditingEntityListener.class)
public class Validation extends BaseEntity {
    private String code;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private LocalDateTime validationExpiration;
    /*@ManyToOne
    @JoinColumn(name = "user_id")
    private Utilisateur  utilisateur;*/
}
