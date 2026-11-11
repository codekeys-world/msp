package com.codekeys.pv;

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
@Table(name = "_pv")
@EntityListeners(AuditingEntityListener.class)
public class Pv extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String numero;
    @Column(nullable = false)
    private String objet;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private LocalDate datePv;
    private String observation;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "document_pv_id")
    private Document documentPv;
}
