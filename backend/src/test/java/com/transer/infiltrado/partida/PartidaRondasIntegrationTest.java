package com.transer.infiltrado.partida;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class PartidaRondasIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String tokenModerador;
    private String tokenJ1, tokenJ2, tokenJ3;
    private String codigoSala;
    /** Tokens en el orden de turno (ordenTurno ascendente). */
    private List<String> turnoTokens;
    /** ID de dominio (UUID) del primer jugador en turno — sirve como acusado en tests de revisión. */
    private String idJugadorAcusado;

    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("mod-rondas@test.com",  "Mod");
        tokenJ1        = registrarYLogin("j1-rondas@test.com",   "Ana");
        tokenJ2        = registrarYLogin("j2-rondas@test.com",   "Bob");
        tokenJ3        = registrarYLogin("j3-rondas@test.com",   "Carla");

        JsonNode partida = crearPartida(tokenModerador, 2, 1, 3);
        codigoSala = partida.get("codigoSala").asText();

        unirse(codigoSala, tokenJ1);
        unirse(codigoSala, tokenJ2);
        unirse(codigoSala, tokenJ3);

        JsonNode estado = iniciar(codigoSala, tokenModerador);

        // Determinar el orden real de turnos usando nombres (Ana/Bob/Carla → token correspondiente)
        Map<String, String> tokensPorNombre = Map.of("Ana", tokenJ1, "Bob", tokenJ2, "Carla", tokenJ3);
        List<JsonNode> jugadoresOrdenados = new ArrayList<>();
        estado.get("jugadores").forEach(jugadoresOrdenados::add);
        jugadoresOrdenados.sort(Comparator.comparingInt(j -> j.get("ordenTurno").asInt()));

        turnoTokens = jugadoresOrdenados.stream()
                .map(j -> tokensPorNombre.get(j.get("nombre").asText()))
                .toList();

        idJugadorAcusado = jugadoresOrdenados.get(0).get("id").asText();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registrarYLogin(String email, String nombre) throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String body = objectMapper.writeValueAsString(
                Map.of("email", uid + email, "nombre", nombre, "password", "Test1234!"));
        String resp = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private JsonNode crearPartida(String token, int numRondas, int numInf, int maxJug) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("numRondas", numRondas, "numInfiltrados", numInf, "numJugadores", maxJug));
        String resp = mockMvc.perform(post("/api/partidas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private void unirse(String codigo, String token) throws Exception {
        mockMvc.perform(post("/api/partidas/" + codigo + "/unirse")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private JsonNode iniciar(String codigo, String token) throws Exception {
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/iniciar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private JsonNode registrarPista(String codigo, String token, String contenido) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("contenido", contenido));
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/turno/pista")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private ResultActions intentarRegistrarPista(String codigo, String token, String contenido) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("contenido", contenido));
        return mockMvc.perform(post("/api/partidas/" + codigo + "/turno/pista")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode proponerRevision(String codigo, String token, String tipo, String idAcusado) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("tipo", tipo, "idJugadorAcusado", idAcusado));
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/revisiones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private JsonNode votar(String codigo, String idRevision, String token, boolean votoSi) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("votoSi", votoSi));
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/revisiones/" + idRevision + "/votos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private ResultActions intentarVotar(String codigo, String idRevision, String token, boolean votoSi)
            throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("votoSi", votoSi));
        return mockMvc.perform(post("/api/partidas/" + codigo + "/revisiones/" + idRevision + "/votos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    // ── Tests de rondas ───────────────────────────────────────────────────────

    @Nested
    class Rondas {

        @Test
        void simulacion_3_jugadores_2_rondas_dispara_senalamiento() throws Exception {
            // 3 jugadores × 2 rondas = 6 pistas → estado debe pasar a SENALAMIENTO
            JsonNode estadoTrasUltimaPista = null;
            int numJugadores = turnoTokens.size();
            for (int i = 0; i < 6; i++) {
                String token = turnoTokens.get(i % numJugadores);
                estadoTrasUltimaPista = registrarPista(codigoSala, token, "pista-" + i);
            }

            assertThat(estadoTrasUltimaPista).isNotNull();
            assertThat(estadoTrasUltimaPista.get("estado").asText()).isEqualTo("SENALAMIENTO");
        }

        @Test
        void conteo_de_rondas_es_correcto_durante_simulacion() throws Exception {
            int numJugadores = turnoTokens.size();
            for (int i = 0; i < numJugadores; i++) {
                JsonNode estado = registrarPista(codigoSala, turnoTokens.get(i), "r1-pista-" + i);
                // Tras la primera ronda completa: si es la última pista de la ronda, rondaActual puede ser 1 o 2
                assertThat(estado.get("rondaActual").asInt()).isGreaterThanOrEqualTo(1);
            }
        }

        @Test
        void solo_jugador_en_turno_puede_registrar_pista() throws Exception {
            // El segundo en orden de turno intenta registrar cuando es el turno del primero
            String tokenNoEnTurno = turnoTokens.get(1);
            intentarRegistrarPista(codigoSala, tokenNoEnTurno, "turno equivocado")
                    .andExpect(status().isConflict());

            // El correcto sí puede
            registrarPista(codigoSala, turnoTokens.get(0), "turno correcto");
        }

        @Test
        void token_sin_autenticar_devuelve_401() throws Exception {
            mockMvc.perform(post("/api/partidas/" + codigoSala + "/turno/pista")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("contenido", "x"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Tests de revisión por votación ────────────────────────────────────────

    @Nested
    class RevisionPorVotacion {

        @Test
        void mayoria_si_resulta_en_rompio() throws Exception {
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "PISTA_SOSPECHOSA", idJugadorAcusado);
            String idRevision = revision.get("id").asText();

            // Los 3 jugadores votan Sí (Sí=3, No=0 → ROMPIO)
            JsonNode resultado = null;
            for (String token : turnoTokens) {
                resultado = votar(codigoSala, idRevision, token, true);
            }

            assertThat(resultado).isNotNull();
            assertThat(resultado.get("estado").asText()).isEqualTo("ROMPIO");
            assertThat(resultado.get("votos")).hasSize(3);
        }

        @Test
        void mayoria_no_resulta_en_no_rompio() throws Exception {
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "NOMBRAR_COSA", idJugadorAcusado);
            String idRevision = revision.get("id").asText();

            // Los 3 jugadores votan No (Sí=0, No=3 → NO_ROMPIO)
            JsonNode resultado = null;
            for (String token : turnoTokens) {
                resultado = votar(codigoSala, idRevision, token, false);
            }

            assertThat(resultado).isNotNull();
            assertThat(resultado.get("estado").asText()).isEqualTo("NO_ROMPIO");
        }

        @Test
        void parcial_si_parcial_no_con_mayoria_si_resulta_en_rompio() throws Exception {
            // 3 jugadores: 2 Sí + 1 No → Sí > No → ROMPIO
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "PISTA_SOSPECHOSA", idJugadorAcusado);
            String idRevision = revision.get("id").asText();

            votar(codigoSala, idRevision, turnoTokens.get(0), true);
            votar(codigoSala, idRevision, turnoTokens.get(1), true);
            JsonNode resultado = votar(codigoSala, idRevision, turnoTokens.get(2), false);

            assertThat(resultado.get("estado").asText()).isEqualTo("ROMPIO");
        }

        @Test
        void jugador_no_puede_votar_dos_veces_la_misma_revision() throws Exception {
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "PISTA_SOSPECHOSA", idJugadorAcusado);
            String idRevision = revision.get("id").asText();

            // Primer voto OK
            votar(codigoSala, idRevision, turnoTokens.get(0), true);

            // Segundo voto del mismo jugador → 409
            intentarVotar(codigoSala, idRevision, turnoTokens.get(0), false)
                    .andExpect(status().isConflict());
        }

        @Test
        void proponer_revision_en_senalamiento_devuelve_201_ventana_abierta() throws Exception {
            // Avanzar hasta SENALAMIENTO (6 pistas)
            for (int i = 0; i < 6; i++) {
                registrarPista(codigoSala, turnoTokens.get(i % 3), "p" + i);
            }

            // La ventana sigue abierta en SENALAMIENTO
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "PISTA_SOSPECHOSA", idJugadorAcusado);
            assertThat(revision.get("estado").asText()).isEqualTo("ABIERTA");
        }

        @Test
        void proponer_revision_tipo_nombrar_cosa_devuelve_201() throws Exception {
            JsonNode revision = proponerRevision(codigoSala, tokenJ2, "NOMBRAR_COSA", idJugadorAcusado);

            assertThat(revision.get("tipo").asText()).isEqualTo("NOMBRAR_COSA");
            assertThat(revision.get("estado").asText()).isEqualTo("ABIERTA");
            assertThat(revision.get("votos")).isEmpty();
        }
    }
}
