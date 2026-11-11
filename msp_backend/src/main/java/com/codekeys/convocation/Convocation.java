package com.codekeys.convocation;

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
@Table(name = "_convocation")
@EntityListeners(AuditingEntityListener.class)
public class Convocation extends BaseEntity {
    @Column(nullable = false)
    private String description;
    private LocalDate dateConvocation;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "convocation_id")
    private Document documentConvocation;
}
