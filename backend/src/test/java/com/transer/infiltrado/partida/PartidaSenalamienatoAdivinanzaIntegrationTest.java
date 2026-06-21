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
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class PartidaSenalamienatoAdivinanzaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String tokenModerador;
    private String tokenJ1, tokenJ2, tokenJ3;
    private String codigoSala;
    /** Tokens en orden de turno ascendente. */
    private List<String> turnoTokens;
    /** IDs de dominio (UUID) de los jugadores, en orden de turno. */
    private List<String> turnoIds;

    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("mod-sen@test.com",  "Mod");
        tokenJ1        = registrarYLogin("j1-sen@test.com",   "Ana");
        tokenJ2        = registrarYLogin("j2-sen@test.com",   "Bob");
        tokenJ3        = registrarYLogin("j3-sen@test.com",   "Carla");

        JsonNode partida = crearPartida(tokenModerador, 2, 1, 3);
        codigoSala = partida.get("codigoSala").asText();

        unirse(codigoSala, tokenJ1);
        unirse(codigoSala, tokenJ2);
        unirse(codigoSala, tokenJ3);

        JsonNode estado = iniciar(codigoSala, tokenModerador);

        Map<String, String> tokensPorNombre = Map.of("Ana", tokenJ1, "Bob", tokenJ2, "Carla", tokenJ3);
        List<JsonNode> jugadoresOrdenados = new ArrayList<>();
        estado.get("jugadores").forEach(jugadoresOrdenados::add);
        jugadoresOrdenados.sort(Comparator.comparingInt(j -> j.get("ordenTurno").asInt()));

        turnoTokens = jugadoresOrdenados.stream()
                .map(j -> tokensPorNombre.get(j.get("nombre").asText()))
                .toList();
        turnoIds = jugadoresOrdenados.stream()
                .map(j -> j.get("id").asText())
                .toList();

        // Completar 2 rondas × 3 jugadores = 6 pistas → SENALAMIENTO
        for (int i = 0; i < 6; i++) {
            registrarPista(codigoSala, turnoTokens.get(i % 3), "pista-" + i);
        }
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

    private void registrarPista(String codigo, String token, String contenido) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("contenido", contenido));
        mockMvc.perform(post("/api/partidas/" + codigo + "/turno/pista")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private JsonNode senalar(String codigo, String token, List<String> idsSenalados) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idsSenalados", idsSenalados));
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/senalamiento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private ResultActions intentarSenalar(String codigo, String token, List<String> idsSenalados)
            throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idsSenalados", idsSenalados));
        return mockMvc.perform(post("/api/partidas/" + codigo + "/senalamiento")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode adivinanza(String codigo, String token, String texto) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("textoAdivinanza", texto));
        String resp = mockMvc.perform(post("/api/partidas/" + codigo + "/adivinanza")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private ResultActions intentarAdivinanza(String codigo, String token, String texto)
            throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("textoAdivinanza", texto));
        return mockMvc.perform(post("/api/partidas/" + codigo + "/adivinanza")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    /** Devuelve el token del infiltrado y lista de tokens de normales. */
    private Map<String, Object> identificarRoles() throws Exception {
        for (int i = 0; i < turnoTokens.size(); i++) {
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/mi-carta")
                            .header("Authorization", "Bearer " + turnoTokens.get(i)))
                    .andReturn().getResponse().getContentAsString();
            JsonNode carta = objectMapper.readTree(resp);
            if (carta.has("rol") && "INFILTRADO".equals(carta.get("rol").asText())) {
                List<String> normales = new ArrayList<>(turnoTokens);
                normales.remove(i);
                return Map.of("infiltrado", turnoTokens.get(i), "normales", normales);
            }
        }
        throw new IllegalStateException("No se encontró infiltrado");
    }

    // ── Señalamiento ──────────────────────────────────────────────────────────

    @Nested
    class Senalamiento {

        @Test
        void estado_inicial_es_SENALAMIENTO() throws Exception {
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala)
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(objectMapper.readTree(resp).get("estado").asText()).isEqualTo("SENALAMIENTO");
        }

        @Test
        void todos_senalan_transiciona_a_ADIVINANZA() throws Exception {
            senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
            senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
            JsonNode final_ = senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));

            assertThat(final_.get("estado").asText()).isEqualTo("ADIVINANZA");
        }

        @Test
        void ha_senalado_se_actualiza_en_respuesta() throws Exception {
            JsonNode resp = senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));

            // El primer jugador (turnoIds.get(0)) debe tener haSenalado=true
            JsonNode jugadores = resp.get("jugadores");
            JsonNode primerJugador = StreamSupport.stream(jugadores.spliterator(), false)
                    .filter(j -> j.get("id").asText().equals(turnoIds.get(0)))
                    .findFirst().orElseThrow();
            assertThat(primerJugador.get("haSenalado").asBoolean()).isTrue();
        }

        @Test
        void abstencion_lista_vacia_cuenta_como_senalamiento() throws Exception {
            senalar(codigoSala, turnoTokens.get(0), List.of()); // abstención
            senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
            JsonNode final_ = senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));

            assertThat(final_.get("estado").asText()).isEqualTo("ADIVINANZA");
        }

        @Test
        void multiple_objetivos_en_un_senalamiento() throws Exception {
            // El primero señala a los otros dos
            JsonNode resp = senalar(codigoSala, turnoTokens.get(0),
                    List.of(turnoIds.get(1), turnoIds.get(2)));
            assertThat(resp.get("estado").asText()).isEqualTo("SENALAMIENTO"); // otros 2 aún no
        }

        @Test
        void senalamiento_duplicado_devuelve_409() throws Exception {
            senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
            intentarSenalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(2)))
                    .andExpect(status().isConflict());
        }

        @Test
        void auto_senalamiento_devuelve_409() throws Exception {
            intentarSenalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(0)))
                    .andExpect(status().isConflict());
        }

        @Test
        void senalamiento_en_estado_incorrecto_devuelve_409() throws Exception {
            // Crear nueva partida en LOBBY y señalar → debe fallar
            JsonNode nueva = crearPartida(tokenModerador, 2, 1, 3);
            String codigoNueva = nueva.get("codigoSala").asText();
            unirse(codigoNueva, tokenJ1);
            unirse(codigoNueva, tokenJ2);
            unirse(codigoNueva, tokenJ3);
            // En LOBBY, senalamiento debe rechazarse
            intentarSenalar(codigoNueva, tokenJ1, List.of())
                    .andExpect(status().isConflict());
        }

        @Test
        void objetivo_fuera_de_partida_devuelve_404() throws Exception {
            intentarSenalar(codigoSala, turnoTokens.get(0), List.of(UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void no_filtracion_respuesta_no_incluye_rol_ni_cosa() throws Exception {
            JsonNode resp = senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));

            // La respuesta de estado nunca debe incluir rol ni cosa
            assertThat(resp.has("idCosa")).isFalse();
            resp.get("jugadores").forEach(j -> {
                assertThat(j.has("rol")).isFalse();
                assertThat(j.has("codigo4Digitos")).isFalse();
            });
        }
    }

    // ── Adivinanza ────────────────────────────────────────────────────────────

    @Nested
    class Adivinanza {

        private void completarSenalamiento() throws Exception {
            senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
            senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
            senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));
        }

        @Test
        void infiltrado_declara_transiciona_a_REVELACION() throws Exception {
            completarSenalamiento();

            // Encontrar el infiltrado
            Map<String, Object> roles = identificarRoles();
            String tokenInfiltrado = (String) roles.get("infiltrado");

            JsonNode resp = adivinanza(codigoSala, tokenInfiltrado, "creo que es el gato");
            assertThat(resp.get("estado").asText()).isEqualTo("REVELACION");
        }

        @Test
        void normal_no_puede_declarar_en_adivinanza() throws Exception {
            completarSenalamiento();

            Map<String, Object> roles = identificarRoles();
            @SuppressWarnings("unchecked")
            List<String> normales = (List<String>) roles.get("normales");

            intentarAdivinanza(codigoSala, normales.get(0), "el perro")
                    .andExpect(status().isConflict());
        }

        @Test
        void double_adivinanza_devuelve_409() throws Exception {
            completarSenalamiento();

            Map<String, Object> roles = identificarRoles();
            String tokenInfiltrado = (String) roles.get("infiltrado");

            adivinanza(codigoSala, tokenInfiltrado, "primera");
            // Estado ya es REVELACION, segunda llamada falla por estado incorrecto
            intentarAdivinanza(codigoSala, tokenInfiltrado, "segunda")
                    .andExpect(status().isConflict());
        }

        @Test
        void texto_adivinanza_en_blanco_devuelve_422() throws Exception {
            completarSenalamiento();
            String body = objectMapper.writeValueAsString(Map.of("textoAdivinanza", ""));
            mockMvc.perform(post("/api/partidas/" + codigoSala + "/adivinanza")
                            .header("Authorization", "Bearer " + turnoTokens.get(0))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void no_filtracion_respuesta_adivinanza_no_incluye_rol_ni_cosa() throws Exception {
            completarSenalamiento();

            Map<String, Object> roles = identificarRoles();
            String tokenInfiltrado = (String) roles.get("infiltrado");

            JsonNode resp = adivinanza(codigoSala, tokenInfiltrado, "mi adivinanza");

            // Aunque ya estamos en REVELACION, la respuesta base no expone rol ni cosa
            resp.get("jugadores").forEach(j -> {
                assertThat(j.has("rol")).isFalse();
                assertThat(j.has("codigo4Digitos")).isFalse();
            });
        }
    }
}
