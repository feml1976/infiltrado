package com.transer.infiltrado.partida;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de concurrencia para GET /revelacion.
 *
 * Intencionalmente NO es @Transactional ni @Rollback: cada llamada HTTP
 * corre en su propia transacción de base de datos real, replicando el
 * comportamiento de múltiples clientes abriendo la pantalla de revelación
 * al mismo tiempo.
 *
 * Invariante que protege:
 *   RevelacionUseCase.ejecutar() es @Transactional(readOnly = true) →
 *   no puede llamar a sumarPuntos() bajo ninguna concurrencia.
 *   Si alguien elimina el readOnly o reutiliza el camino de escritura
 *   para el endpoint GET, este test lo detectará.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PartidaRevelacionConcurrenciaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String tokenModerador;
    private String tokenJ1, tokenJ2, tokenJ3;
    private String codigoSala;
    private List<String> turnoTokens;
    private List<String> turnoIds;

    @BeforeEach
    void setUp() throws Exception {
        tokenModerador = registrarYLogin("mod-cc@test.com",  "ModCC");
        tokenJ1        = registrarYLogin("j1-cc@test.com",   "AnaCC");
        tokenJ2        = registrarYLogin("j2-cc@test.com",   "BobCC");
        tokenJ3        = registrarYLogin("j3-cc@test.com",   "CarlaCC");

        JsonNode partida = crearPartida(tokenModerador, 2, 1, 3);
        codigoSala = partida.get("codigoSala").asText();

        unirse(codigoSala, tokenJ1);
        unirse(codigoSala, tokenJ2);
        unirse(codigoSala, tokenJ3);

        JsonNode estado = iniciar(codigoSala, tokenModerador);

        Map<String, String> tokensPorNombre = Map.of("AnaCC", tokenJ1, "BobCC", tokenJ2, "CarlaCC", tokenJ3);
        List<JsonNode> ordenados = new ArrayList<>();
        estado.get("jugadores").forEach(ordenados::add);
        ordenados.sort(Comparator.comparingInt(j -> j.get("ordenTurno").asInt()));

        turnoTokens = ordenados.stream()
                .map(j -> tokensPorNombre.get(j.get("nombre").asText()))
                .toList();
        turnoIds = ordenados.stream()
                .map(j -> j.get("id").asText())
                .toList();

        // 2 rondas × 3 jugadores = 6 pistas → SENALAMIENTO
        for (int i = 0; i < 6; i++) {
            registrarPista(codigoSala, turnoTokens.get(i % 3), "pista-cc-" + i);
        }

        // Todos señalan → ADIVINANZA
        senalar(codigoSala, turnoTokens.get(0), List.of(turnoIds.get(1)));
        senalar(codigoSala, turnoTokens.get(1), List.of(turnoIds.get(2)));
        senalar(codigoSala, turnoTokens.get(2), List.of(turnoIds.get(0)));
    }

    // ── Test ─────────────────────────────────────────────────────────────────

    @Test
    void revelacion_concurrente_no_duplica_puntosAcumulados() throws Exception {
        Map<String, Object> roles = identificarRoles();

        // La última adivinanza dispara calcularYPublicar() (TX única, atómica).
        // A partir de aquí puntosAcumulados en BD queda fijado para esta ronda.
        adivinanza(codigoSala, (String) roles.get("infiltrado"), "intento concurrente");

        // Referencia: valores inmediatamente tras el cálculo (primera llamada GET)
        JsonNode revRef = getRevelacion(tokenJ1);
        Map<String, Integer> ptosRef  = extraerPuntos(revRef);
        int sumaRef = ptosRef.values().stream().mapToInt(Integer::intValue).sum();

        // Simula N "ventanas" abiertas simultáneamente en la pantalla de revelación.
        // Cada Future corre en su propio hilo → su propia transacción de base de datos.
        int N = 8;
        ExecutorService executor = Executors.newFixedThreadPool(N);
        List<Future<JsonNode>> futures = IntStream.range(0, N)
                .mapToObj(i -> executor.submit((Callable<JsonNode>) () -> getRevelacion(tokenJ1)))
                .collect(toList());
        executor.shutdown();
        assertThat(executor.awaitTermination(20, TimeUnit.SECONDS))
                .as("El pool de hilos no terminó en el tiempo esperado")
                .isTrue();

        // ── Aserciones ────────────────────────────────────────────────────────

        for (int i = 0; i < N; i++) {
            JsonNode rev = futures.get(i).get();

            // 1. Cada respuesta concurrente devuelve los mismos puntosAcumulados que la referencia
            Map<String, Integer> ptos = extraerPuntos(rev);
            assertThat(ptos)
                    .describedAs("Llamada concurrente #%d alteró puntosAcumulados (posible doble conteo)", i)
                    .isEqualTo(ptosRef);

            // 2. La suma total no creció respecto a la referencia
            int suma = ptos.values().stream().mapToInt(Integer::intValue).sum();
            assertThat(suma)
                    .describedAs("Suma total de puntos en llamada #%d supera la referencia", i)
                    .isEqualTo(sumaRef);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Integer> extraerPuntos(JsonNode rev) {
        Map<String, Integer> res = new LinkedHashMap<>();
        rev.get("jugadores").forEach(j ->
                res.put(j.get("id").asText(), j.get("puntosAcumulados").asInt()));
        return res;
    }

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

    private void senalar(String codigo, String token, List<String> ids) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("idsSenalados", ids));
        mockMvc.perform(post("/api/partidas/" + codigo + "/senalamiento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private Map<String, Object> identificarRoles() throws Exception {
        for (int i = 0; i < turnoTokens.size(); i++) {
            String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/mi-carta")
                            .header("Authorization", "Bearer " + turnoTokens.get(i)))
                    .andReturn().getResponse().getContentAsString();
            JsonNode carta = objectMapper.readTree(resp);
            if (carta.has("rol") && "INFILTRADO".equals(carta.get("rol").asText())) {
                return Map.of("infiltrado", turnoTokens.get(i));
            }
        }
        throw new IllegalStateException("No se encontró infiltrado en la partida de prueba");
    }

    private void adivinanza(String codigo, String token, String texto) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("textoAdivinanza", texto));
        mockMvc.perform(post("/api/partidas/" + codigo + "/adivinanza")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private JsonNode getRevelacion(String token) throws Exception {
        String resp = mockMvc.perform(get("/api/partidas/" + codigoSala + "/revelacion")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp);
    }
}
