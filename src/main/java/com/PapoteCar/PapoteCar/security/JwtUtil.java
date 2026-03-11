package com.PapoteCar.PapoteCar.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Gestion des tokens JWT.
 * Chaque token expire à minuit (00:00:00) du jour de sa création.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ─── Génération ──────────────────────────────────────────────────────────

    /**
     * Génère un token JWT pour le login donné.
     * Expiration : minuit du jour courant (Europe/Paris).
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(getMidnightExpiration())
                .signWith(secretKey)
                .compact();
    }

    // ─── Extraction ──────────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ─── Privé ───────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Calcule l'instant correspondant à minuit (00:00:00) du lendemain,
     * ce qui représente la fin du jour courant.
     */
    private Date getMidnightExpiration() {
        LocalDateTime midnight = LocalDate.now(ZoneId.of("Europe/Paris"))
                .plusDays(1)
                .atStartOfDay();
        return Date.from(midnight.atZone(ZoneId.of("Europe/Paris")).toInstant());
    }
}