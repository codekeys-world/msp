package com.codekeys.common;

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
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Personne extends BaseEntity{
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenom;
    @Column(unique = true)
    private String telephonePrincipal;
    @Column(nullable = true)
    private String telephoneSecondaire;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresse;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genre genre;
}
