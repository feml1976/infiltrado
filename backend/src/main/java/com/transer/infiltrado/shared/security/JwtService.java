package com.transer.infiltrado.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtService implements TokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    static final String DEV_FALLBACK_SECRET = "changethissecretinproduction-must-be-at-least-32-chars";

    private static final String CLAIM_NOMBRE   = "nombre";
    private static final String CLAIM_ES_ADMIN = "admin";
    private static final Set<String> DEV_PROFILES = Set.of("dev", "test", "default");

    private final String      secret;
    private final int         expirationHours;
    private final Environment environment;

    public JwtService(
            @Value("${infiltrado.jwt.secret}") String secret,
            @Value("${infiltrado.jwt.expiration-hours}") int expirationHours,
            Environment environment) {
        this.secret          = secret;
        this.expirationHours = expirationHours;
        this.environment     = environment;
    }

    @PostConstruct
    void validarSecretoEnProduccion() {
        boolean esPerfilSeguro = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(DEV_PROFILES::contains);
        if (!esPerfilSeguro && DEV_FALLBACK_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET no puede ser el valor por defecto en un perfil de producción. " +
                    "Define la variable de entorno JWT_SECRET con un secreto seguro.");
        }
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
