package com.codekeys.securite;

import com.codekeys.token.Token;
import com.codekeys.token.TokenRepository;
import com.codekeys.utilisateur.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${application.security.token.expiration}")
    private long tokenExpiration;
    @Value("${application.security.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;
    @Value("${application.security.token.secret-key}")
    private String secretKey;
    private final TokenRepository tokenRepository;

    public String generateToken(Utilisateur utilisateur) {
        return generateToken(new HashMap<>(), utilisateur);
    }

    public String generateToken(HashMap<String, Object> claims, Utilisateur utilisateur) {
        return generateTokenPair(claims, utilisateur).getToken();
    }

    public Token generateTokenPair(HashMap<String, Object> claims, Utilisateur utilisateur) {
        List<@Nullable String> authorities = utilisateur.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .toList();
        long now = System.currentTimeMillis();
        Date dateCreation = new Date(now);
        String bearer = buildJwt(claims, utilisateur, dateCreation, new Date(now + tokenExpiration))
                .claim("authorities", authorities)
                .compact();
        String refreshToken = buildJwt(Map.of("token_type", "refresh"), utilisateur, dateCreation,
                new Date(now + refreshTokenExpiration)).compact();
        Token token = Token.builder()
                .slug(UUID.randomUUID().toString())
                .token(bearer)
                .refreshToken(refreshToken)
                .estExpire(false)
                .utilisateur(utilisateur)
                .build();
        return this.tokenRepository.save(token);
    }

    private io.jsonwebtoken.JwtBuilder buildJwt(Map<String, Object> claims, Utilisateur utilisateur,
                                                 Date dateCreation, Date dateExpiration) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(utilisateur.getUsername())
                .setIssuedAt(dateCreation)
                .setExpiration(dateExpiration)
                .signWith(this.getSignInKey());
    }

    private Key getSignInKey() {
        byte[] decode = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(decode);
    }

    public String extraireUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String validerEtExtraireUsernameRefresh(String token) {
        Claims claims = this.extractAllClaims(token);
        if (!"refresh".equals(claims.get("token_type", String.class))) {
            throw new MalformedJwtException("Le token fourni n'est pas un refresh token.");
        }
        return claims.getSubject();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean estValidToken(String token, Utilisateur utilisateur) {
        String username = this.extraireUsername(token);
        return username.equals(utilisateur.getUsername()) && !isTokenExpired(token);
    }

    private Date extractExpiration(String token) {
        return this.extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return this.extractExpiration(token).before(new Date());
    }
}
