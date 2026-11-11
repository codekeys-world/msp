package com.codekeys.plainte;

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
@Table(name = "_plainte")
@EntityListeners(AuditingEntityListener.class)
public class Plainte extends BaseEntity {
    @Column(nullable = false)
    private LocalDate datePv;
    private String observation;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plainte_id")
    private Document documentPlainte;
}
