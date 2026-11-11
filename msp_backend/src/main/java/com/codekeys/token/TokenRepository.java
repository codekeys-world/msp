package com.codekeys.token;

import com.codekeys.utilisateur.Utilisateur;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUtilisateur(Utilisateur utilisateur);

    @Query("FROM Token t WHERE t.utilisateur.telephonePrincipal = :username")
    List<Token> findAllValidTokensByUsername(String username);

    void deleteByToken(String token);
}
