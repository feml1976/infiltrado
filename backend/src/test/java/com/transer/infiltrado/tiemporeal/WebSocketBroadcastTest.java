package com.transer.infiltrado.tiemporeal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketBroadcastTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void tearDown() {
        stompClient.stop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String wsUrl() { return "ws://localhost:" + port + "/ws-native"; }

    private String registrarYLogin(String suffix) {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        try {
            ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/registro",
                    Map.of("email", uid + suffix, "nombre", "U" + uid, "password", "Test1234!"),
                    String.class);
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            return objectMapper.readTree(resp.getBody()).get("token").asText();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private JsonNode crearPartida(String token) {
        ResponseEntity<String> r = restTemplate.exchange("/api/partidas", HttpMethod.POST,
                new HttpEntity<>(Map.of("numRondas", 2, "numInfiltrados", 1, "numJugadores", 5),
                        bearerHeaders(token)), String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        try { return objectMapper.readTree(r.getBody()); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void unirse(String codigo, String token) {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/partidas/" + codigo + "/unirse", HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(token)), String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void iniciar(String codigo, String token) {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/partidas/" + codigo + "/iniciar", HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(token)), String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void avanzarTurno(String codigo, String token) {
        ResponseEntity<String> r = restTemplate.exchange(
                "/api/partidas/" + codigo + "/turno/avanzar", HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(token)), String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private StompSession conectarConJwt(String token) throws Exception {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);
        CompletableFuture<StompSession> future = new CompletableFuture<>();
        stompClient.connectAsync(wsUrl(), new WebSocketHttpHeaders(), connectHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession s, StompHeaders h) { future.complete(s); }
                    @Override
                    public void handleTransportError(StompSession s, Throwable ex) { future.completeExceptionally(ex); }
                });
        return future.get(5, TimeUnit.SECONDS);
    }

    // ── Tests de difusión ─────────────────────────────────────────────────────

    @Test
    void avanzar_turno_difunde_turno_de() throws Exception {
        String tokenMod = registrarYLogin("@bcast-td-mod.test");
        String tokenJ1  = registrarYLogin("@bcast-td-j1.test");
        String tokenJ2  = registrarYLogin("@bcast-td-j2.test");
        String tokenJ3  = registrarYLogin("@bcast-td-j3.test");

        JsonNode partida = crearPartida(tokenMod);
        String codigo    = partida.get("codigoSala").asText();
        UUID idPartida   = UUID.fromString(partida.get("id").asText());

        unirse(codigo, tokenJ1);
        unirse(codigo, tokenJ2);
        unirse(codigo, tokenJ3);
        iniciar(codigo, tokenMod);

        // Jugador2 se suscribe para recibir el evento
        StompSession sesion = conectarConJwt(tokenJ2);
        BlockingQueue<JsonNode> mensajes = new LinkedBlockingQueue<>();
        sesion.subscribe("/topic/partida/" + idPartida, new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders h) { return JsonNode.class; }
            @Override public void handleFrame(StompHeaders h, Object payload) { mensajes.add((JsonNode) payload); }
        });
        Thread.sleep(200);

        // El moderador avanza el turno de jugador1 (ordenTurno=1 tiene el primer turno)
        avanzarTurno(codigo, tokenMod);

        JsonNode msg = mensajes.poll(5, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.get("tipo").asText()).isEqualTo("turno_de");

        sesion.disconnect();
    }

    @Test
    void payload_turno_de_no_contiene_rol_ni_cosa() throws Exception {
        String tokenMod = registrarYLogin("@bcast-nr-mod.test");
        String tokenJ1  = registrarYLogin("@bcast-nr-j1.test");
        String tokenJ2  = registrarYLogin("@bcast-nr-j2.test");
        String tokenJ3  = registrarYLogin("@bcast-nr-j3.test");

        JsonNode partida = crearPartida(tokenMod);
        String codigo    = partida.get("codigoSala").asText();
        UUID idPartida   = UUID.fromString(partida.get("id").asText());

        unirse(codigo, tokenJ1);
        unirse(codigo, tokenJ2);
        unirse(codigo, tokenJ3);
        iniciar(codigo, tokenMod);

        StompSession sesion = conectarConJwt(tokenJ2);
        BlockingQueue<JsonNode> mensajes = new LinkedBlockingQueue<>();
        sesion.subscribe("/topic/partida/" + idPartida, new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders h) { return JsonNode.class; }
            @Override public void handleFrame(StompHeaders h, Object payload) { mensajes.add((JsonNode) payload); }
        });
        Thread.sleep(200);

        avanzarTurno(codigo, tokenMod);

        JsonNode msg = mensajes.poll(5, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.get("tipo").asText()).isEqualTo("turno_de");

        JsonNode datos = msg.get("datos");
        // 🔴 Seguridad: rol y cosa NUNCA en turno_de
        assertThat(datos.has("rol")).isFalse();
        assertThat(datos.has("cosa")).isFalse();
        assertThat(datos.has("idCosa")).isFalse();
        assertThat(datos.has("codigo4Digitos")).isFalse();
        // Campos esperados (sin info sensible)
        assertThat(datos.has("idJugador")).isTrue();
        assertThat(datos.has("nombreJugador")).isTrue();
        assertThat(datos.has("ordenTurno")).isTrue();
        assertThat(datos.has("rondaActual")).isTrue();

        sesion.disconnect();
    }

    @Test
    void avanzar_ultima_ronda_difunde_cambio_fase_sin_rol() throws Exception {
        // 3 jugadores, 2 rondas: se necesitan 3*2=6 avances para llegar a SENALAMIENTO
        String tokenMod = registrarYLogin("@bcast-sf-mod.test");
        String tokenJ1  = registrarYLogin("@bcast-sf-j1.test");
        String tokenJ2  = registrarYLogin("@bcast-sf-j2.test");
        String tokenJ3  = registrarYLogin("@bcast-sf-j3.test");

        JsonNode partida = crearPartida(tokenMod);
        String codigo    = partida.get("codigoSala").asText();
        UUID idPartida   = UUID.fromString(partida.get("id").asText());

        unirse(codigo, tokenJ1);
        unirse(codigo, tokenJ2);
        unirse(codigo, tokenJ3);
        iniciar(codigo, tokenMod);

        StompSession sesion = conectarConJwt(tokenJ1);
        BlockingQueue<JsonNode> mensajes = new LinkedBlockingQueue<>();
        sesion.subscribe("/topic/partida/" + idPartida, new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders h) { return JsonNode.class; }
            @Override public void handleFrame(StompHeaders h, Object payload) { mensajes.add((JsonNode) payload); }
        });
        Thread.sleep(200);

        // Avanzar 5 turnos (turno_de) y consumirlos
        for (int i = 0; i < 5; i++) {
            avanzarTurno(codigo, tokenMod);
            JsonNode turnoMsg = mensajes.poll(3, TimeUnit.SECONDS);
            assertThat(turnoMsg).isNotNull();
            assertThat(turnoMsg.get("tipo").asText()).isEqualTo("turno_de");
        }

        // El 6to avance debe producir cambio_fase SENALAMIENTO
        avanzarTurno(codigo, tokenMod);

        JsonNode msg = mensajes.poll(5, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.get("tipo").asText()).isEqualTo("cambio_fase");

        JsonNode datos = msg.get("datos");
        assertThat(datos.get("estado").asText()).isEqualTo("SENALAMIENTO");
        // 🔴 Seguridad: rol y cosa NUNCA en cambio_fase
        assertThat(datos.has("rol")).isFalse();
        assertThat(datos.has("idCosa")).isFalse();
        assertThat(datos.has("jugadores")).isFalse();

        sesion.disconnect();
    }
}
