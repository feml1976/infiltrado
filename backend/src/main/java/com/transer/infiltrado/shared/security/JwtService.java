package com.transer.infiltrado.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService implements TokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    // TODO Paso 16 — Endurecimiento: agregar @PostConstruct que rechace el arranque
    // si el perfil activo no es dev/test y el secreto coincide con el valor de fallback
    // del application.yml. Un secreto conocido públicamente (está en el repo) permite
    // firmar tokens arbitrarios con privilegios de admin.
    static final String DEV_FALLBACK_SECRET = "changethissecretinproduction-must-be-at-least-32-chars";

    private static final String CLAIM_NOMBRE   = "nombre";
    private static final String CLAIM_ES_ADMIN = "admin";

    private final String secret;
    private final int expirationHours;

    public JwtService(
            @Value("${infiltrado.jwt.secret}") String secret,
            @Value("${infiltrado.jwt.expiration-hours}") int expirationHours) {
        this.secret          = secret;
        this.expirationHours = expirationHours;
    }

    @Override
    public String generar(UUID userId, String nombre, boolean esAdmin) {
        Instant now     = Instant.now();
        Instant expires = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_NOMBRE, nombre)
                .claim(CLAIM_ES_ADMIN, esAdmin)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .signWith(secretKey())
                .compact();
    }

    /** Valida el token y extrae los claims. Vacío si el token es inválido o expiró. */
    public Optional<Claims> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException ex) {
            log.debug("JWT inválido o expirado: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public UUID extraerUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extraerNombre(Claims claims) {
        return claims.get(CLAIM_NOMBRE, String.class);
    }

    public boolean extraerEsAdmin(Claims claims) {
        return Boolean.TRUE.equals(claims.get(CLAIM_ES_ADMIN, Boolean.class));
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
