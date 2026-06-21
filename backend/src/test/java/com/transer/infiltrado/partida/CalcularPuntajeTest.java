package com.transer.infiltrado.partida;

import com.transer.infiltrado.partida.application.CalcularPuntajeUseCase;
import com.transer.infiltrado.partida.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CalcularPuntajeTest {

    private CalcularPuntajeUseCase useCase;

    // IDs fijos para reproducibilidad
    private final UUID idNormal1     = UUID.randomUUID();
    private final UUID idNormal2     = UUID.randomUUID();
    private final UUID idInfiltrado  = UUID.randomUUID();
    private final UUID idPartida     = UUID.randomUUID();

    private Jugador normal1, normal2, infiltrado;

    @BeforeEach
    void setUp() {
        useCase = new CalcularPuntajeUseCase();
        normal1    = Jugador.reconstituir(idNormal1, UUID.randomUUID(), "Ana",   1,
                RolJugador.NORMAL,    null, 0, false, false);
        normal2    = Jugador.reconstituir(idNormal2, UUID.randomUUID(), "Bob",   2,
                RolJugador.NORMAL,    null, 0, false, false);
        infiltrado = Jugador.reconstituir(idInfiltrado, UUID.randomUUID(), "Carla", 3,
                RolJugador.INFILTRADO, null, 0, false, false);
    }

    // ── R1: +10 al infiltrado que adivinó ─────────────────────────────────────

    @Test
    void r1_infiltrado_acierta_recibe_10() {
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "Perro");
        // Alguien señala al infiltrado para desactivar R2 y aislar solo R1
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idInfiltrado);

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "perro",
                List.of(s), List.of(adiv), List.of());

        assertThat(r.deltasPorJugador().get(idInfiltrado)).isEqualTo(10);
        assertThat(r.aciertosPorInfiltrado().get(idInfiltrado)).isTrue();
    }

    @Test
    void r1_infiltrado_falla_no_recibe_puntos_por_acierto() {
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "Gato");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "perro",
                List.of(), List.of(adiv), List.of());

        assertThat(r.aciertosPorInfiltrado().get(idInfiltrado)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"  Perro , perro", "GATO, gato", "Café, cafe", "niño, nino"})
    void r1_normalizacion_ignora_mayusculas_espacios_y_tildes(String texto, String cosa) {
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, texto);

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                cosa,
                List.of(), List.of(adiv), List.of());

        assertThat(r.aciertosPorInfiltrado().get(idInfiltrado)).isTrue();
    }

    // ── R2: +10 al infiltrado no descubierto ──────────────────────────────────

    @Test
    void r2_infiltrado_no_senalado_recibe_10() {
        // nadie señala al infiltrado
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idNormal2);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "otra-cosa",
                List.of(s), List.of(adiv), List.of());

        assertThat(r.deltasPorJugador().get(idInfiltrado)).isEqualTo(10);
    }

    @Test
    void r2_infiltrado_senalado_no_recibe_bonus_no_descubierto() {
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idInfiltrado);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "otra-cosa",
                List.of(s), List.of(adiv), List.of());

        // +10 para normal1 (R3), 0 para infiltrado (descubierto → sin R2)
        assertThat(r.deltasPorJugador().get(idInfiltrado)).isEqualTo(0);
    }

    // ── R3: +10 al señalador por cada infiltrado correctamente señalado ───────

    @Test
    void r3_senalar_infiltrado_da_10_al_senalador() {
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idInfiltrado);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "cosa",
                List.of(s), List.of(adiv), List.of());

        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(10);
    }

    // ── R4: -10 al señalador por cada no-infiltrado señalado ─────────────────

    @Test
    void r4_senalar_normal_quita_10_al_senalador() {
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idNormal2);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "cosa",
                List.of(s), List.of(adiv), List.of());

        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(-10);
    }

    // ── R5: -10 al acusado por revisión NOMBRAR_COSA ROMPIO ───────────────────

    @Test
    void r5_revision_nombrar_cosa_rompio_quita_10_al_acusado() {
        Revision rev = crearRevisionRompio(idNormal1, TipoRevision.NOMBRAR_COSA);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "cosa",
                List.of(), List.of(adiv), List.of(rev));

        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(-10);
    }

    // ── R6: -5 al acusado por revisión PISTA_SOSPECHOSA ROMPIO ───────────────

    @Test
    void r6_revision_pista_sospechosa_rompio_quita_5_al_acusado() {
        Revision rev = crearRevisionRompio(idNormal1, TipoRevision.PISTA_SOSPECHOSA);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "cosa",
                List.of(), List.of(adiv), List.of(rev));

        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(-5);
    }

    @Test
    void revision_no_rompio_no_penaliza() {
        Revision rev = crearRevisionNoRompio(idNormal1, TipoRevision.NOMBRAR_COSA);
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "x");

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "cosa",
                List.of(), List.of(adiv), List.of(rev));

        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(0);
    }

    // ── Escenario multi-infiltrado ─────────────────────────────────────────────

    @Test
    void multi_infiltrado_cada_uno_evaluado_independientemente() {
        UUID idInf2 = UUID.randomUUID();
        Jugador infiltrado2 = Jugador.reconstituir(idInf2, UUID.randomUUID(), "Dave", 4,
                RolJugador.INFILTRADO, null, 0, false, false);

        Adivinanza adiv1 = Adivinanza.nueva(idPartida, idInfiltrado, "perro"); // acierta
        Adivinanza adiv2 = Adivinanza.nueva(idPartida, idInf2, "gato");        // falla

        // Solo normal1 señala a inf2
        Senalamiento s = Senalamiento.nuevo(idPartida, idNormal1, idInf2);

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado, infiltrado2),
                "perro",
                List.of(s), List.of(adiv1, adiv2), List.of());

        // infiltrado: +10 (acierto R1) + +10 (no descubierto R2) = 20
        assertThat(r.deltasPorJugador().get(idInfiltrado)).isEqualTo(20);
        // infiltrado2: 0 (sin acierto, descubierto)
        assertThat(r.deltasPorJugador().get(idInf2)).isEqualTo(0);
        // normal1: +10 (señaló infiltrado2 correctamente R3)
        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(10);
        // normal2: 0 (no señaló a nadie, no fue señalado)
        assertThat(r.deltasPorJugador().get(idNormal2)).isEqualTo(0);
    }

    @Test
    void combinacion_todas_las_reglas_en_una_partida() {
        Adivinanza adiv = Adivinanza.nueva(idPartida, idInfiltrado, "perro"); // R1 acierta
        // Nadie señala al infiltrado → R2 se activa
        Senalamiento sError = Senalamiento.nuevo(idPartida, idNormal1, idNormal2);  // R4: -10
        Revision revNombrar = crearRevisionRompio(idNormal2, TipoRevision.NOMBRAR_COSA); // R5: -10
        Revision revPista   = crearRevisionRompio(idNormal2, TipoRevision.PISTA_SOSPECHOSA); // R6: -5

        CalcularPuntajeUseCase.Resultado r = useCase.calcular(
                List.of(normal1, normal2, infiltrado),
                "perro",
                List.of(sError), List.of(adiv), List.of(revNombrar, revPista));

        assertThat(r.deltasPorJugador().get(idInfiltrado)).isEqualTo(20); // R1+R2
        assertThat(r.deltasPorJugador().get(idNormal1)).isEqualTo(-10);   // R4
        assertThat(r.deltasPorJugador().get(idNormal2)).isEqualTo(-15);   // R5+R6
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Revision crearRevisionRompio(UUID idAcusado, TipoRevision tipo) {
        UUID idVotante1 = UUID.randomUUID();
        UUID idVotante2 = UUID.randomUUID();
        Revision rev = Revision.nueva(idPartida, idAcusado, tipo);
        rev.registrarVoto(idVotante1, true,  2);
        rev.registrarVoto(idVotante2, false, 2);
        // empate → NO_ROMPIO en el dominio. Necesitamos mayoría.
        // Reconstruir con 3 votos para forzar ROMPIO
        Revision rev3 = Revision.nueva(idPartida, idAcusado, tipo);
        rev3.registrarVoto(UUID.randomUUID(), true, 3);
        rev3.registrarVoto(UUID.randomUUID(), true, 3);
        rev3.registrarVoto(UUID.randomUUID(), false, 3);
        return rev3; // 2 sí > 1 no → ROMPIO
    }

    private Revision crearRevisionNoRompio(UUID idAcusado, TipoRevision tipo) {
        Revision rev = Revision.nueva(idPartida, idAcusado, tipo);
        rev.registrarVoto(UUID.randomUUID(), false, 2);
        rev.registrarVoto(UUID.randomUUID(), false, 2); // 0 sí ≤ 2 no → NO_ROMPIO
        return rev;
    }
}
