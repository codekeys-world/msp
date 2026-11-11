package com.codekeys.utilisateur;

import lombok.NonNull;
import com.codekeys.common.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<@NonNull Utilisateur, @NonNull Long> {
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations"
    })
    Optional<Utilisateur> findByEmailIgnoreCase(String email);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations"
    })
    Optional<Utilisateur> findByMatriculeIgnoreCase(String matricule);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Optional<Utilisateur> findByMatricule(String matricule);
    boolean existsByMatricule(String matricule);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);
    boolean existsByTelephonePrincipalAndIdNot(String telephonePrincipal, Long id);
    boolean existsByNumeroInscriptionAndIdNot(String numeroInscription, Long id);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations"
    })
    Optional<Utilisateur> findByTelephonePrincipal(String telephonePrincipal);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations"
    })
    Optional<Utilisateur> findByTelephonePrincipalIgnoreCase(String telephonePrincipal);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Optional<Utilisateur> findUtilisateurBySlug(String slug);

    //Recherche l'adhérent à partir de ses 02 numeros de telephone
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Optional<Utilisateur> findByTelephonePrincipalOrTelephoneSecondaire(String telephonePrincipal, String telephoneSecondaire);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Optional<Utilisateur> findByNumeroInscription(String numeroInscription);

    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    @Query("""
            FROM Utilisateur utilisateur WHERE lower(utilisateur.email) LIKE LOWER(CONCAT('%',:information,'%') ) OR
            lower(utilisateur.matricule) LIKE lower(CONCAT('', :information,'')) OR 
            lower(utilisateur.numeroInscription) LIKE lower(CONCAT('', :information,'')) OR
            lower(utilisateur.nom) LIKE lower(CONCAT('', :information,'')) OR 
            lower(utilisateur.prenom) LIKE lower(CONCAT('', :information,'')) OR 
            lower(utilisateur.telephonePrincipal) LIKE lower(CONCAT('', :information,'')) OR
            lower(utilisateur.telephoneSecondaire) LIKE lower(CONCAT('', :information,'')) OR
            lower(utilisateur.pere) LIKE lower(CONCAT('', :information,'')) OR
            lower(utilisateur.mere) LIKE lower(CONCAT('', :information,''))
            """)
    Page<@NonNull Utilisateur> findAllByInformation(String information, Pageable pageable);

    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Page<@NonNull Utilisateur> findAllByEnabled(Boolean enabled, Pageable pageable);
    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    Page<Utilisateur> findByRoleNomIgnoreCase(String roleName, Pageable pageable);
    long countByRoleNomIgnoreCase(String roleName);
    long countByRoleNomIgnoreCaseAndGenre(String roleName, Genre genre);

    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    @Query(
            value = """
                    select distinct adherent
                    from Utilisateur adherent
                    left join adherent.role role
                    left join adherent.conjoints conjoint
                    left join adherent.enfants descendant
                    where lower(role.nom) = 'adherent'
                      and (
                           lower(coalesce(adherent.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.email, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.matricule, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.numeroInscription, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.telephoneSecondaire, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.adresse, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.pere, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.mere, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.observations, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(cast(adherent.genre as string), '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(cast(adherent.dateNaissance as string), '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                      )
                    """,
            countQuery = """
                    select count(distinct adherent)
                    from Utilisateur adherent
                    left join adherent.role role
                    left join adherent.conjoints conjoint
                    left join adherent.enfants descendant
                    where lower(role.nom) = 'adherent'
                      and (
                           lower(coalesce(adherent.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.email, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.matricule, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.numeroInscription, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.telephoneSecondaire, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.adresse, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.pere, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.mere, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(adherent.observations, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(cast(adherent.genre as string), '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(cast(adherent.dateNaissance as string), '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(conjoint.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.telephonePrincipal, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.nom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.prenom, '')) like lower(concat('%', :terme, '%'))
                        or lower(coalesce(descendant.lieuNaissance, '')) like lower(concat('%', :terme, '%'))
                      )
                    """
    )
    Page<Utilisateur> rechercherAdherentsParInformation(@Param("terme") String terme, Pageable pageable);

    @EntityGraph(attributePaths = {
        "role", "role.authorisations", "authorisations", "profileImage"
    })
    @Query("""
            select adherent
            from Utilisateur adherent
            join adherent.role role
            where lower(role.nom) = 'adherent'
              and adherent not in (
                select cas.adherent from CasSocial cas
              )
            """)
    Page<Utilisateur> findByRoleNomIgnoreCaseAndNoCasSocial(Pageable pageable);

}
