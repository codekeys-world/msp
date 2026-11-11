package com.codekeys.common;

import com.codekeys.validation_entite.ValidationEntite;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity extends AbstractEntity{
    @CreatedBy
    private String creerPar;
    @LastModifiedBy
    private String modifierPar;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "valider_par")
    private ValidationEntite validationEntite;


}
