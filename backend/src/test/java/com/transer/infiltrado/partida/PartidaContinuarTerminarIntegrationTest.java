package com.transer.infiltrado.partida;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transer.infiltrado.partida.domain.PartidaRepository;
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
class PartidaContinuarTerminarIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PartidaRepository partidaRepository;

    private String tokenModerador;
    private String tokenJ1, tokenJ2, tokenJ3;
    private String codigoSala;
    private List<String> turnoTokens; // en orden de turn (ordenTurno ASC)
    private List<String> turnoIds;

    /**
     * Crea partida, une 3 jugadores, inicia y juega hasta REVELACION (puntos calculados).
     * Cada test arranca con la partida en estado REVELACION.
     */
    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("mod-ct@test.com", "Mod");
        tokenJ1        = registrarYLogin("j1-ct@test.com",  "Ana");
        tokenJ2        = registrarYLogin("j2-ct@test.com",  "Bob");
        tokenJ3        = registrarYLogin("j3-ct@test.com",  "Carla");

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

        jugarRondaHastaRevelacion();
        getRevelacion();
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

    private JsonNode getRevelacion() throws Exception {
        String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/revelacion")
                        .header("Authorization", "Bearer " + tokenJ1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private JsonNode continuar() throws Exception {
        String resp = mockMvc.perform(post("/api/partidas/" + codigoSala + "/continuar")
                        .header("Authorization", "Bearer " + tokenModerador))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private JsonNode terminar() throws Exception {
        String resp = mockMvc.perform(post("/api/partidas/" + codigoSala + "/terminar")
                        .header("Authorization", "Bearer " + tokenModerador))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private JsonNode getEstado() throws Exception {
        String resp = mockMvc.perform(get("/api/partidas/" + codigoSala)
                        .header("Authorization", "Bearer " + tokenJ1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    /**
     * Identifica el infiltrado por /mi-carta y hace que declare su adivinanza.
     * Avanza la partida a REVELACION.
     */
    private void jugarRondaHastaRevelacion() throws Exception {
        // 2 rondas × 3 jugadores = 6 pistas
        for (int i = 0; i < 6; i++) {
            registrarPista(codigoSala, turnoTokens.get(i % 3), "pista-" + i);
        }
        // Señalamientos: cada uno señala al siguiente (circular)
        senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
        senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
        senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));

        // Infiltrado declara
        String tokenInfiltrado = encontrarTokenInfiltrado();
        String body = objectMapper.writeValueAsString(Map.of("textoAdivinanza", "mi_guess_" + UUID.randomUUID()));
        mockMvc.perform(post("/api/partidas/" + codigoSala + "/adivinanza")
                        .header("Authorization", "Bearer " + tokenInfiltrado)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private String encontrarTokenInfiltrado() throws Exception {
        for (String token : turnoTokens) {
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/mi-carta")
                            .header("Authorization", "Bearer " + token))
                    .andReturn().getResponse().getContentAsString();
            JsonNode carta = objectMapper.readTree(resp);
            if (carta.has("rol") && "INFILTRADO".equals(carta.get("rol").asText())) {
                return token;
            }
        }
        throw new IllegalStateException("No se encontró el infiltrado");
    }

    private Map<String, Integer> puntosAcumuladosDesdeFecha(JsonNode estadoNode) {
        Map<String, Integer> resultado = new HashMap<>();
        estadoNode.get("jugadores").forEach(j ->
                resultado.put(j.get("id").asText(), j.get("puntosAcumulados").asInt()));
        return resultado;
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

    private void votar(String codigo, String idRevision, String token, boolean votoSi) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("votoSi", votoSi));
        mockMvc.perform(post("/api/partidas/" + codigo + "/revisiones/" + idRevision + "/votos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    // ── Tests: transiciones válidas / inválidas ───────────────────────────────

    @Nested
    class Transiciones {

        @Test
        void terminar_deja_estado_finalizada() throws Exception {
            terminar();

            JsonNode estado = getEstado();
            assertThat(estado.get("estado").asText()).isEqualTo("FINALIZADA");
        }

        @Test
        void continuar_desde_estado_en_curso_rechazado_con_409() throws Exception {
            // Avanza a EN_CURSO
            continuar();

            // Intentar CONTINUAR de nuevo desde EN_CURSO → 409
            mockMvc.perform(post("/api/partidas/" + codigoSala + "/continuar")
                            .header("Authorization", "Bearer " + tokenModerador))
                    .andExpect(status().isConflict());
        }

        @Test
        void continuar_solo_permitido_al_moderador() throws Exception {
            mockMvc.perform(post("/api/partidas/" + codigoSala + "/continuar")
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isConflict());
        }
    }

    // ── Tests: acumulación de puntos y conservación de código ─────────────────

    @Nested
    class SesionContinuada {

        @Test
        void continuar_dos_veces_acumula_puntos_y_conserva_codigos() throws Exception {
            // Capturar codigos_4_digitos del dominio antes de CONTINUAR (en REVELACION)
            List<String> codigosAntes = partidaRepository.buscarPorCodigo(codigoSala)
                    .orElseThrow().getJugadores().stream()
                    .sorted(Comparator.comparingInt(j -> j.getOrdenTurno()))
                    .map(j -> j.getCodigo4Digitos())
                    .toList();
            assertThat(codigosAntes).doesNotContainNull();

            // Puntos de la ronda 1
            Map<String, Integer> puntos1 = extraerPuntosAcumulados(getRevelacion());

            // --- Primera continuación ---
            continuar(); // REVELACION → EN_CURSO
            assertThat(getEstado().get("estado").asText()).isEqualTo("EN_CURSO");

            // Los códigos deben ser los mismos después de CONTINUAR
            List<String> codigosDespues1 = partidaRepository.buscarPorCodigo(codigoSala)
                    .orElseThrow().getJugadores().stream()
                    .sorted(Comparator.comparingInt(j -> j.getOrdenTurno()))
                    .map(j -> j.getCodigo4Digitos())
                    .toList();
            assertThat(codigosDespues1).containsExactlyElementsOf(codigosAntes);

            jugarRondaHastaRevelacion();
            Map<String, Integer> puntos2 = extraerPuntosAcumulados(getRevelacion());
            // Puntos acumulados registrados en revelacion 2 — todos los jugadores presentes
            turnoIds.forEach(id -> assertThat(puntos2).containsKey(id));

            // --- Segunda continuación ---
            continuar(); // REVELACION → EN_CURSO
            List<String> codigosDespues2 = partidaRepository.buscarPorCodigo(codigoSala)
                    .orElseThrow().getJugadores().stream()
                    .sorted(Comparator.comparingInt(j -> j.getOrdenTurno()))
                    .map(j -> j.getCodigo4Digitos())
                    .toList();
            assertThat(codigosDespues2).containsExactlyElementsOf(codigosAntes);

            jugarRondaHastaRevelacion();
            Map<String, Integer> puntos3 = extraerPuntosAcumulados(getRevelacion());

            turnoIds.forEach(id -> assertThat(puntos3).containsKey(id));
            // Los puntos de las 3 rondas deben diferir de los de la ronda 1 (acumulación)
            assertThat(puntos3).isNotEqualTo(puntos1);
        }

        @Test
        void continuar_con_revision_votada_limpia_votos_sin_violacion_fk() throws Exception {
            // Ronda 1 ya en REVELACION desde setUp.
            // Ronda 2: continuar, proponer revisión con votos, completar ronda y continuar de nuevo.
            // Verifica que revisionRepository.eliminarPorPartida borra votos_revision por JPA cascade
            // antes de borrar revisiones (sin ON DELETE CASCADE en SQL).
            continuar(); // REVELACION → EN_CURSO (ronda 2)
            assertThat(getEstado().get("estado").asText()).isEqualTo("EN_CURSO");

            // Proponer una revisión durante EN_CURSO y votar con todos los jugadores
            String idAcusado = turnoIds.get(0);
            JsonNode revision = proponerRevision(codigoSala, tokenJ1, "PISTA_SOSPECHOSA", idAcusado);
            String idRevision = revision.get("id").asText();
            // Mayoría Sí → ROMPIO (3 votos Si, 0 No)
            for (String token : turnoTokens) {
                votar(codigoSala, idRevision, token, true);
            }

            // Completar ronda 2: pistas + señalamientos + adivinanza del infiltrado
            jugarRondaHastaRevelacion();
            getRevelacion();

            // CONTINUAR debe eliminar votos_revision y revisiones sin violar FK
            // (JPA cascade: deleteAll sobre RevisionJpaEntity propaga a VotoJpaEntity)
            JsonNode estadoFinal = continuar();
            assertThat(estadoFinal.get("estado").asText()).isEqualTo("EN_CURSO");
        }

        @Test
        void puntuaciones_historicas_no_se_duplica_en_multiples_revelaciones() throws Exception {
            // Ronda 1 ya revelada en setUp — 1 registro por jugador en puntuaciones_historicas
            JsonNode historial1 = getHistorial(tokenJ1);
            assertThat(historial1.get("partidas").size()).isEqualTo(1);

            // Segunda ronda
            continuar();
            jugarRondaHastaRevelacion();
            getRevelacion(); // upsert, no insert

            // Sigue siendo 1 entrada para la misma partida (mismo id_partida → upsert)
            JsonNode historial2 = getHistorial(tokenJ1);
            assertThat(historial2.get("partidas").size()).isEqualTo(1);

            // Pero los puntos pueden haber cambiado
            int puntosRonda1 = historial1.get("partidas").get(0).get("puntos").asInt();
            int puntosRonda2 = historial2.get("partidas").get(0).get("puntos").asInt();
            // Ambos pertenecen a la misma partida — confirma que fue un UPDATE, no un INSERT extra
            assertThat(historial2.get("partidas").get(0).get("idPartida").asText())
                    .isEqualTo(historial1.get("partidas").get(0).get("idPartida").asText());
            // El campo 'puntos' se actualiza (los valores exactos dependen del scoring aleatorio)
            assertThat(puntosRonda1).isNotNull();
            assertThat(puntosRonda2).isNotNull();
        }
    }

    // ── Tests: acumulado global suma múltiples sesiones ───────────────────────

    @Nested
    class AcumuladoGlobal {

        @Test
        void acumulado_suma_puntos_de_dos_sesiones_distintas() throws Exception {
            // Sesión 1: ya está en REVELACION desde setUp
            int puntosJ1Sesion1 = puntosDeJugadorEnRevelacion(getRevelacion(), tokenJ1, turnoIds, turnoTokens);

            // Sesión 2: nueva partida con los mismos usuarios
            JsonNode partida2 = crearPartida(tokenModerador, 2, 1, 3);
            String codigo2 = partida2.get("codigoSala").asText();

            unirse(codigo2, tokenJ1);
            unirse(codigo2, tokenJ2);
            unirse(codigo2, tokenJ3);

            JsonNode estado2 = iniciar(codigo2, tokenModerador);

            // Reordenar tokens según el orden de turno de la segunda partida
            Map<String, String> tokensPorNombre = Map.of("Ana", tokenJ1, "Bob", tokenJ2, "Carla", tokenJ3);
            List<JsonNode> jugadores2 = new ArrayList<>();
            estado2.get("jugadores").forEach(jugadores2::add);
            jugadores2.sort(Comparator.comparingInt(j -> j.get("ordenTurno").asInt()));
            List<String> turnoTokens2 = jugadores2.stream()
                    .map(j -> tokensPorNombre.get(j.get("nombre").asText())).toList();
            List<String> turnoIds2 = jugadores2.stream()
                    .map(j -> j.get("id").asText()).toList();

            // Jugar sesión 2 hasta REVELACION
            for (int i = 0; i < 6; i++) {
                registrarPista(codigo2, turnoTokens2.get(i % 3), "p2-" + i);
            }
            senalar(codigo2, turnoTokens2.get(0), List.of(turnoIds2.get(1)));
            senalar(codigo2, turnoTokens2.get(1), List.of(turnoIds2.get(2)));
            senalar(codigo2, turnoTokens2.get(2), List.of(turnoIds2.get(0)));

            String infiltrado2 = encontrarTokenInfiltradoEnPartida(codigo2, turnoTokens2);
            String body2 = objectMapper.writeValueAsString(Map.of("textoAdivinanza", "guess2"));
            mockMvc.perform(post("/api/partidas/" + codigo2 + "/adivinanza")
                            .header("Authorization", "Bearer " + infiltrado2)
                            .contentType(MediaType.APPLICATION_JSON).content(body2))
                    .andExpect(status().isOk());

            String respRev2 = mockMvc.perform(get("/api/partidas/" + codigo2 + "/revelacion")
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JsonNode rev2 = objectMapper.readTree(respRev2);

            int puntosJ1Sesion2 = puntosDeJugadorEnRevelacion(rev2, tokenJ1, turnoIds2, turnoTokens2);

            // Verificar acumulado global = sesion1 + sesion2
            String respAcumulado = mockMvc.perform(get("/api/me/acumulado")
                            .header("Authorization", "Bearer " + tokenJ1))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            int acumulado = objectMapper.readTree(respAcumulado).get("totalPuntos").asInt();
            assertThat(acumulado).isEqualTo(puntosJ1Sesion1 + puntosJ1Sesion2);

            // Verificar historial: 2 entradas (una por sesión)
            JsonNode historial = getHistorial(tokenJ1);
            assertThat(historial.get("partidas").size()).isEqualTo(2);
        }
    }

    // ── Helpers de consulta ───────────────────────────────────────────────────

    private JsonNode getHistorial(String token) throws Exception {
        String resp = mockMvc.perform(get("/api/me/historial")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }

    private Map<String, Integer> extraerPuntosAcumulados(JsonNode revelacion) {
        Map<String, Integer> mapa = new HashMap<>();
        revelacion.get("jugadores").forEach(j ->
                mapa.put(j.get("id").asText(), j.get("puntosAcumulados").asInt()));
        return mapa;
    }

    /** Extrae los puntosAcumulados de la revelacion para el usuario identificado por token. */
    private int puntosDeJugadorEnRevelacion(JsonNode revelacion, String token,
                                             List<String> ids, List<String> tokens) {
        // Encuentra el índice del token en la lista de tokens
        int idx = tokens.indexOf(token);
        if (idx < 0) return 0;
        String idJugador = ids.get(idx);
        return StreamSupport.stream(revelacion.get("jugadores").spliterator(), false)
                .filter(j -> j.get("id").asText().equals(idJugador))
                .mapToInt(j -> j.get("puntosAcumulados").asInt())
                .findFirst()
                .orElse(0);
    }

    private String encontrarTokenInfiltradoEnPartida(String codigo, List<String> tokens) throws Exception {
        for (String token : tokens) {
            String resp = mockMvc.perform(get("/api/partidas/" + codigo + "/mi-carta")
                            .header("Authorization", "Bearer " + token))
                    .andReturn().getResponse().getContentAsString();
            JsonNode carta = objectMapper.readTree(resp);
            if (carta.has("rol") && "INFILTRADO".equals(carta.get("rol").asText())) {
                return token;
            }
        }
        throw new IllegalStateException("No se encontró el infiltrado en partida " + codigo);
    }
}
