package com.codekeys.assemblee;

import com.codekeys.common.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "_assemblee")
@Getter
@Setter
public class Assemblee extends AbstractEntity {
    @Column(nullable = false, length = 200)
    private String nom;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false, length = 255)
    private String lieu;
}
