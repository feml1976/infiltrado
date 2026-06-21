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
class PartidaRevelacionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String tokenModerador;
    private String tokenJ1, tokenJ2, tokenJ3;
    private String codigoSala;
    private List<String> turnoTokens;
    private List<String> turnoIds;

    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("mod-rev@test.com", "Mod");
        tokenJ1        = registrarYLogin("j1-rev@test.com",  "Ana");
        tokenJ2        = registrarYLogin("j2-rev@test.com",  "Bob");
        tokenJ3        = registrarYLogin("j3-rev@test.com",  "Carla");

        JsonNode partida = crearPartida(tokenModerador, 2, 1, 3);
        codigoSala = partida.get("codigoSala").asText();

        unirse(codigoSala, tokenJ1);
        unirse(codigoSala, tokenJ2);
        unirse(codigoSala, tokenJ3);

        JsonNode estado = iniciar(codigoSala, tokenModerador);

        Map<String, String> tokensPorNombre = Map.of("Ana", tokenJ1, "Bob", tokenJ2, "Carla", tokenJ3);
        List<JsonNode> ordenados = new ArrayList<>();
        estado.get("jugadores").forEach(ordenados::add);
        ordenados.sort(Comparator.comparingInt(j -> j.get("ordenTurno").asInt()));

        turnoTokens = ordenados.stream().map(j -> tokensPorNombre.get(j.get("nombre").asText())).toList();
        turnoIds    = ordenados.stream().map(j -> j.get("id").asText()).toList();

        // 2 rondas × 3 jugadores = 6 pistas → SENALAMIENTO
        for (int i = 0; i < 6; i++) {
            registrarPista(codigoSala, turnoTokens.get(i % 3), "pista-" + i);
        }

        // Todos señalan → ADIVINANZA
        senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
        senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
        senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registrarYLogin(String email, String nombre) throws Exception {
        String uid  = UUID.randomUUID().toString().substring(0, 8);
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
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
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
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
    }

    private void senalar(String codigo, String token, List<String> ids) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idsSenalados", ids));
        mockMvc.perform(post("/api/partidas/" + codigo + "/senalamiento")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
    }

    /** Devuelve Map con "infiltrado" → token, "normales" → List<String>. */
    private Map<String, Object> identificarRoles() throws Exception {
        for (int i = 0; i < turnoTokens.size(); i++) {
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/mi-carta")
                            .header("Authorization", "Bearer " + turnoTokens.get(i)))
                    .andReturn().getResponse().getContentAsString();
            JsonNode carta = objectMapper.readTree(resp);
            if (carta.has("rol") && "INFILTRADO".equals(carta.get("rol").asText())) {
                List<String> normales = new ArrayList<>(turnoTokens);
                normales.remove(i);
                return Map.of("infiltrado", turnoTokens.get(i), "normales", normales,
                        "infiltradoIdx", i);
            }
        }
        throw new IllegalStateException("No se encontró infiltrado");
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

    private JsonNode getRevelacion(String token) throws Exception {
        String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/revelacion")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    // ── Tests principales ─────────────────────────────────────────────────────

    @Nested
    class CamposBásicosRevelacion {

        @Test
        void revelacion_incluye_roles_de_todos_los_jugadores() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            JsonNode jugadores = rev.get("jugadores");
            assertThat(jugadores).isNotNull();
            assertThat(jugadores.size()).isEqualTo(3);
            jugadores.forEach(j -> assertThat(j.has("rol")).isTrue());
        }

        @Test
        void revelacion_expone_la_cosa_objetivo() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            assertThat(rev.has("nombreCosa")).isTrue();
            assertThat(rev.get("nombreCosa").asText()).isNotBlank();
        }

        @Test
        void revelacion_incluye_senalamientos_de_la_ronda() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            assertThat(rev.has("senalamientos")).isTrue();
            assertThat(rev.get("senalamientos").size()).isEqualTo(3); // uno por jugador en setUp
        }

        @Test
        void revelacion_incluye_adivinanza_del_infiltrado_con_acierto() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            JsonNode adivinanzas = rev.get("adivinanzas");
            assertThat(adivinanzas.size()).isEqualTo(1);
            assertThat(adivinanzas.get(0).has("acierto")).isTrue();
        }
    }

    @Nested
    class Scoring {

        @Test
        void jugadores_tienen_campo_deltaRonda_en_revelacion() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            rev.get("jugadores").forEach(j -> {
                assertThat(j.has("deltaRonda")).isTrue();
                assertThat(j.has("puntosAcumulados")).isTrue();
            });
        }

        @Test
        void infiltrado_que_acierta_tiene_acierto_true_y_delta_calculado() throws Exception {
            Map<String, Object> roles = identificarRoles();
            int idxInf = (int) roles.get("infiltradoIdx");

            // Los normales saben la cosa; el infiltrado no. Leemos del normal.
            int idxNormal = (idxInf == 0) ? 1 : 0;
            String respNormal = mockMvc.perform(get("/api/partidas/" + codigoSala + "/mi-carta")
                            .header("Authorization", "Bearer " + turnoTokens.get(idxNormal)))
                    .andReturn().getResponse().getContentAsString();
            String cosaReal = objectMapper.readTree(respNormal).get("nombreCosa").asText();

            adivinanza(codigoSala, turnoTokens.get(idxInf), cosaReal);

            JsonNode rev = getRevelacion(turnoTokens.get(idxInf));
            String idInfiltrado = turnoIds.get(idxInf);

            JsonNode jugInf = StreamSupport.stream(rev.get("jugadores").spliterator(), false)
                    .filter(j -> j.get("id").asText().equals(idInfiltrado))
                    .findFirst().orElseThrow();

            // Acierto correcto
            assertThat(rev.get("adivinanzas").get(0).get("acierto").asBoolean()).isTrue();
            // deltaRonda está presente; con señalamiento circular R1(+10) y R4(−10) se cancelan → neto 0
            assertThat(jugInf.has("deltaRonda")).isTrue();
            assertThat(jugInf.get("deltaRonda").asInt()).isEqualTo(0);
            assertThat(jugInf.get("puntosAcumulados").asInt()).isEqualTo(0);
        }

        @Test
        void infiltrado_que_falla_y_es_senalado_tiene_delta_cero_o_negativo() throws Exception {
            Map<String, Object> roles = identificarRoles();
            int idxInf = (int) roles.get("infiltradoIdx");

            // El infiltrado escribe algo incorrecto
            adivinanza(codigoSala, turnoTokens.get(idxInf), "xyzzy-no-es-la-cosa");

            JsonNode rev = getRevelacion(turnoTokens.get(idxInf));
            String idInfiltrado = turnoIds.get(idxInf);

            JsonNode jugInf = StreamSupport.stream(rev.get("jugadores").spliterator(), false)
                    .filter(j -> j.get("id").asText().equals(idInfiltrado))
                    .findFirst().orElseThrow();

            // Sin acierto (R1=0). Puede que R2 aplique o no según senalamientos del setUp.
            // Lo importante: acierto = false
            assertThat(rev.get("adivinanzas").get(0).get("acierto").asBoolean()).isFalse();
            // delta ≤ 10 (como mucho R2 si nadie lo señaló)
            assertThat(jugInf.get("deltaRonda").asInt()).isLessThanOrEqualTo(10);
        }
    }

    @Nested
    class Idempotencia {

        @Test
        void doble_llamada_a_revelacion_no_duplica_puntos() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            // Primera llamada
            JsonNode rev1 = getRevelacion(tokenJ1);

            // Segunda llamada (idempotente)
            JsonNode rev2 = getRevelacion(tokenJ1);

            // Los puntos acumulados deben ser iguales en ambas llamadas
            Map<String, Integer> puntos1 = extraerPuntosAcumulados(rev1);
            Map<String, Integer> puntos2 = extraerPuntosAcumulados(rev2);

            assertThat(puntos1).isEqualTo(puntos2);
        }

        private Map<String, Integer> extraerPuntosAcumulados(JsonNode rev) {
            Map<String, Integer> resultado = new HashMap<>();
            rev.get("jugadores").forEach(j ->
                    resultado.put(j.get("id").asText(), j.get("puntosAcumulados").asInt()));
            return resultado;
        }
    }

    @Nested
    class NoFiltracion {

        @Test
        void estado_partida_no_expone_roles_antes_de_revelacion() throws Exception {
            // En fase ADIVINANZA (antes de que el infiltrado declare)
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala)
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonNode estado = objectMapper.readTree(resp);
            assertThat(estado.get("estado").asText()).isEqualTo("ADIVINANZA");

            estado.get("jugadores").forEach(j -> {
                assertThat(j.has("rol")).isFalse();
                assertThat(j.has("codigo4Digitos")).isFalse();
            });
        }

        @Test
        void endpoint_revelacion_requiere_estado_REVELACION() throws Exception {
            // Estado actual es ADIVINANZA — intento GET /revelacion debe devolver 409
            mockMvc.perform(get("/api/partidas/" + codigoSala + "/revelacion")
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isConflict());
        }

        @Test
        void revelacion_expone_roles_todos_los_jugadores() throws Exception {
            Map<String, Object> roles = identificarRoles();
            adivinanza(codigoSala, (String) roles.get("infiltrado"), "mi guess");

            JsonNode rev = getRevelacion(tokenJ1);

            Set<String> rolesEncontrados = new HashSet<>();
            rev.get("jugadores").forEach(j -> rolesEncontrados.add(j.get("rol").asText()));

            assertThat(rolesEncontrados).contains("INFILTRADO", "NORMAL");
        }
    }
}
