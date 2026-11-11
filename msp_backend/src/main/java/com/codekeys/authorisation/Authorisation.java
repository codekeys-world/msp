package com.codekeys.authorisation;

import com.codekeys.common.AbstractEntity;
import com.codekeys.common.BaseEntity;
import com.codekeys.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_authorisation")
public class Authorisation extends AbstractEntity {
    @Column(nullable = false, unique = true)
    private String nom;
    @ManyToMany(mappedBy = "authorisations")
    @JsonIgnore
    private List<Role> roles;
}
