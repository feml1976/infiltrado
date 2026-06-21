package com.transer.infiltrado.partida;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import com.transer.infiltrado.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class PartidaLobbyIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;
    @Autowired PartidaRepository partidaRepository;

    // Tokens obtenidos tras registro real (requieren FK en usuarios)
    private String tokenModerador;
    private String tokenJugador1;
    private String tokenJugador2;
    private String tokenJugador3;

    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("moderador@test.com",    "Moderador");
        tokenJugador1  = registrarYLogin("jugador1@test.com",     "Ana");
        tokenJugador2  = registrarYLogin("jugador2@test.com",     "Bob");
        tokenJugador3  = registrarYLogin("jugador3@test.com",     "Carla");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registrarYLogin(String email, String nombre) throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String body = objectMapper.writeValueAsString(
                Map.of("email", uid + email, "nombre", nombre, "password", "Test1234!"));
        String resp = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private String crearPartidaJson(int numRondas, int numInfiltrados, int numJugadores) throws Exception {
        return objectMapper.writeValueAsString(
                Map.of("numRondas", numRondas, "numInfiltrados", numInfiltrados, "numJugadores", numJugadores));
    }

    private JsonNode crearPartida(String token, int numRondas, int numInfiltrados, int numJugadores) throws Exception {
        String resp = mockMvc.perform(post("/api/partidas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearPartidaJson(numRondas, numInfiltrados, numJugadores)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private void unirse(String codigoSala, String token) throws Exception {
        mockMvc.perform(post("/api/partidas/" + codigoSala + "/unirse")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private JsonNode iniciar(String codigoSala, String token) throws Exception {
        String resp = mockMvc.perform(post("/api/partidas/" + codigoSala + "/iniciar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    // ── Crear partida ─────────────────────────────────────────────────────────

    @Nested
    class CrearPartida {

        @Test
        void crear_partida_devuelve_201_con_codigo_sala() throws Exception {
            mockMvc.perform(post("/api/partidas")
                            .header("Authorization", "Bearer " + tokenModerador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(crearPartidaJson(2, 1, 5)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codigoSala", matchesPattern("[A-Z0-9]{6}")))
                    .andExpect(jsonPath("$.estado", is("LOBBY")))
                    .andExpect(jsonPath("$.id", notNullValue()));
        }

        @Test
        void numRondas_invalido_devuelve_422() throws Exception {
            mockMvc.perform(post("/api/partidas")
                            .header("Authorization", "Bearer " + tokenModerador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(crearPartidaJson(1, 1, 5)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void sin_token_devuelve_401() throws Exception {
            mockMvc.perform(post("/api/partidas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(crearPartidaJson(2, 1, 5)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Unirse a partida ──────────────────────────────────────────────────────

    @Nested
    class UnirseAPartida {

        @Test
        void unirse_con_codigo_valido_devuelve_estado() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();

            mockMvc.perform(post("/api/partidas/" + codigo + "/unirse")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("LOBBY")))
                    .andExpect(jsonPath("$.jugadores", hasSize(1)));
        }

        @Test
        void codigo_invalido_devuelve_404() throws Exception {
            mockMvc.perform(post("/api/partidas/XXXXXX/unirse")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isNotFound());
        }

        @Test
        void sala_llena_devuelve_409() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 3);
            String codigo = partida.get("codigoSala").asText();

            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);

            String tokenExtra = registrarYLogin("extra@test.com", "Extra");
            mockMvc.perform(post("/api/partidas/" + codigo + "/unirse")
                            .header("Authorization", "Bearer " + tokenExtra))
                    .andExpect(status().isConflict());
        }

        @Test
        void duplicado_devuelve_409() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);

            mockMvc.perform(post("/api/partidas/" + codigo + "/unirse")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isConflict());
        }

        @Test
        void unirse_partida_iniciada_devuelve_409() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();

            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);
            iniciar(codigo, tokenModerador);

            String tokenExtra = registrarYLogin("tarde@test.com", "Tarde");
            mockMvc.perform(post("/api/partidas/" + codigo + "/unirse")
                            .header("Authorization", "Bearer " + tokenExtra))
                    .andExpect(status().isConflict());
        }
    }

    // ── Iniciar partida ───────────────────────────────────────────────────────

    @Nested
    class IniciarPartida {

        private String prepararSala(int cupo) throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, cupo);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);
            return codigo;
        }

        @Test
        void iniciar_con_3_jugadores_cambia_a_EN_CURSO() throws Exception {
            String codigo = prepararSala(5);

            mockMvc.perform(post("/api/partidas/" + codigo + "/iniciar")
                            .header("Authorization", "Bearer " + tokenModerador))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("EN_CURSO")));
        }

        @Test
        void iniciar_asigna_roles_sin_revelarlos_en_estado() throws Exception {
            String codigo = prepararSala(5);
            JsonNode estado = iniciar(codigo, tokenModerador);

            // El estado público nunca incluye el rol
            for (JsonNode j : estado.get("jugadores")) {
                assertThat(j.has("rol")).isFalse();
            }
        }

        @Test
        void iniciar_genera_codigos_unicos_por_jugador() throws Exception {
            String codigo = prepararSala(5);
            iniciar(codigo, tokenModerador);

            // Verificar en dominio que los códigos son distintos y no nulos
            partidaRepository.buscarPorCodigo(codigo).ifPresent(p -> {
                var codigos = p.getJugadores().stream()
                        .map(j -> j.getCodigo4Digitos()).toList();
                assertThat(codigos).doesNotContainNull();
                assertThat(new HashSet<>(codigos)).hasSameSizeAs(codigos);
            });
        }

        @Test
        void solo_moderador_puede_iniciar() throws Exception {
            String codigo = prepararSala(5);

            mockMvc.perform(post("/api/partidas/" + codigo + "/iniciar")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isConflict());
        }

        @Test
        void iniciar_con_menos_de_3_jugadores_devuelve_422() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);

            mockMvc.perform(post("/api/partidas/" + codigo + "/iniciar")
                            .header("Authorization", "Bearer " + tokenModerador))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void iniciar_cuando_numInfiltrados_excede_regla_50_devuelve_422() throws Exception {
            // cupo=5, numInfiltrados=2: si solo 3 se unen → max=1 → viola la regla
            JsonNode partida = crearPartida(tokenModerador, 2, 2, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);

            mockMvc.perform(post("/api/partidas/" + codigo + "/iniciar")
                            .header("Authorization", "Bearer " + tokenModerador))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ── Consultar estado ──────────────────────────────────────────────────────

    @Nested
    class EstadoPartidaTests {

        @Test
        void obtener_estado_sin_roles_antes_de_revelacion() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);
            iniciar(codigo, tokenModerador);

            String resp = mockMvc.perform(get("/api/partidas/" + codigo)
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonNode node = objectMapper.readTree(resp);
            for (JsonNode j : node.get("jugadores")) {
                assertThat(j.has("rol")).isFalse();
                assertThat(j.has("codigo4Digitos")).isFalse();
            }
        }

        @Test
        void sala_inexistente_devuelve_404() throws Exception {
            mockMvc.perform(get("/api/partidas/NOEXIST")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Mi carta ──────────────────────────────────────────────────────────────

    @Nested
    class MiCarta {

        private String prepararSalaIniciada() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);
            unirse(codigo, tokenJugador2);
            unirse(codigo, tokenJugador3);
            iniciar(codigo, tokenModerador);
            return codigo;
        }

        @Test
        void jugador_ve_su_propia_carta_con_rol() throws Exception {
            String codigo = prepararSalaIniciada();

            mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rol", oneOf("NORMAL", "INFILTRADO")));
        }

        @Test
        void jugador_no_inscrito_obtiene_403() throws Exception {
            String codigo = prepararSalaIniciada();
            String tokenAjeno = registrarYLogin("ajeno@test.com", "Externo");

            mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + tokenAjeno))
                    .andExpect(status().isForbidden());
        }

        @Test
        void moderador_no_es_jugador_y_obtiene_403() throws Exception {
            String codigo = prepararSalaIniciada();

            mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + tokenModerador))
                    .andExpect(status().isForbidden());
        }

        @Test
        void sala_inexistente_devuelve_403_sin_revelar_existencia() throws Exception {
            mockMvc.perform(get("/api/partidas/XXXXXX/mi-carta")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void carta_en_LOBBY_devuelve_200_con_rol_nulo() throws Exception {
            JsonNode partida = crearPartida(tokenModerador, 2, 1, 5);
            String codigo = partida.get("codigoSala").asText();
            unirse(codigo, tokenJugador1);

            mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rol").value(nullValue()));
        }

        @Test
        void carta_incluye_nombre_de_cosa() throws Exception {
            String codigo = prepararSalaIniciada();

            mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + tokenJugador1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombreCosa", notNullValue()))
                    .andExpect(jsonPath("$.tipo", oneOf("PALABRA", "IMAGEN")));
        }
    }
}
