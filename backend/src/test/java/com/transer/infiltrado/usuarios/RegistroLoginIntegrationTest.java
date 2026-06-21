package com.transer.infiltrado.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transer.infiltrado.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class RegistroLoginIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;

    // Inyectado desde application-test.yml → application.yml fallback
    @Value("${infiltrado.jwt.secret}")
    private String jwtSecret;

    // ── Registro ──────────────────────────────────────────────────────────────

    @Test
    void registro_valido_devuelve201_y_token() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("test@ejemplo.com", "Ana García", "3001234567", "Segura123!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.nombre", is("Ana García")))
                .andExpect(jsonPath("$.esAdmin", is(false)));
    }

    @Test
    void registro_emailDuplicado_devuelve409() throws Exception {
        String body = json("dup@ejemplo.com", "Usuario Uno", null, "Clave1234!");
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        // Segundo registro con el mismo email (case-insensitive)
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("DUP@EJEMPLO.COM", "Usuario Dos", null, "Clave1234!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void registro_passwordCorta_devuelve422() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("short@ej.com", "Nombre", null, "abc")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errores.password", notNullValue()));
    }

    @Test
    void registro_emailInvalido_devuelve422() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("no-es-email", "Nombre", null, "Clave1234!")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errores.email", notNullValue()));
    }

    @Test
    void registro_noExpone_esAdmin_en_request() throws Exception {
        // Aunque la petición incluya es_admin, el campo no existe en RegistroRequest
        // y si se incluyera en el JSON debe ser ignorado (Jackson ignora propiedades desconocidas por defecto)
        String bodyConAdmin = objectMapper.writeValueAsString(Map.of(
                "email", "admin@test.com",
                "nombre", "Atacante",
                "password", "Clave1234!",
                "esAdmin", true
        ));
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(bodyConAdmin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esAdmin", is(false)));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_credencialesValidas_devuelve200_y_token() throws Exception {
        // Registro previo
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("login@ej.com", "Usuario Login", null, "MiClave99!")))
                .andExpect(status().isCreated());

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "login@ej.com", "password", "MiClave99!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.nombre", is("Usuario Login")));
    }

    @Test
    void login_passwordIncorrecta_devuelve401_generico() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("pass@ej.com", "Alguien", null, "RealPass1!")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "pass@ej.com", "password", "Incorrecta!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", is("Credenciales inválidas")));
    }

    @Test
    void login_emailInexistente_devuelve401_igual_que_passwordIncorrecta() throws Exception {
        // La respuesta no revela si el email existe o no
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "noexiste@ej.com", "password", "Cualquier1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", is("Credenciales inválidas")));
    }

    @Test
    void login_caseInsensitive_email() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("CASE@ej.com", "Mayúsculas", null, "Clave1234!")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "case@EJ.COM", "password", "Clave1234!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    // ── Autenticación JWT ─────────────────────────────────────────────────────

    @Test
    void endpoint_protegido_sin_token_devuelve401() throws Exception {
        mockMvc.perform(get("/api/cualquier-ruta-protegida"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpoint_protegido_con_token_valido_pasa_filtro() throws Exception {
        String body = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("jwt@ej.com", "JWT User", null, "Clave1234!")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).get("token").asText();

        // /api/auth/** es público; cualquier otra ruta requiere auth.
        // Verificamos que el filtro no rechaza una petición con token válido
        // (el 404 indica que pasó el filtro de seguridad correctamente).
        mockMvc.perform(get("/api/ruta-inexistente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── Caducidad y claims JWT ────────────────────────────────────────────────

    @Test
    void token_expirado_devuelve401() throws Exception {
        // Construye un token ya vencido (emitido hace 13h, expiró hace 1h).
        // Usa el mismo secreto que el contexto de test para que la firma sea válida
        // y el rechazo sea solo por expiración — no por firma inválida.
        String tokenVencido = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("nombre", "Ghost")
                .claim("admin", false)
                .issuedAt(Date.from(Instant.now().minus(13, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/ruta-protegida")
                        .header("Authorization", "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", is("No autorizado")));
    }

    @Test
    void token_emitido_tiene_claims_correctos() throws Exception {
        String body = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("claims@ej.com", "Claimante", null, "Clave1234!")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).get("token").asText();
        Claims claims = jwtService.validar(token).orElseThrow();

        // Subject debe ser un UUID, no el email (PII fuera del token)
        assertThat(claims.getSubject()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

        // Claim nombre presente
        assertThat(claims.get("nombre", String.class)).isEqualTo("Claimante");

        // Claim admin siempre false en registro self-service
        assertThat(claims.get("admin", Boolean.class)).isFalse();

        // TTL ≈ 12 horas (43 200 s ± 60 s de margen por tiempo de ejecución)
        long ttlSegundos = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000L;
        assertThat(ttlSegundos).isBetween(43140L, 43260L);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String json(String email, String nombre, String celular, String password) throws Exception {
        var map = new java.util.LinkedHashMap<String, Object>();
        map.put("email",    email);
        map.put("nombre",   nombre);
        if (celular != null) map.put("celular", celular);
        map.put("password", password);
        return objectMapper.writeValueAsString(map);
    }
}
