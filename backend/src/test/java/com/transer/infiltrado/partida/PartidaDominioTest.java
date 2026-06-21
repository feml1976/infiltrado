package com.transer.infiltrado.partida;

import com.transer.infiltrado.partida.domain.*;
import com.transer.infiltrado.partida.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios puros del dominio — sin Spring, sin BD.
 * Cada test crea su propia instancia de Partida directamente.
 */
class PartidaDominioTest {

    // ── Stubs deterministas ───────────────────────────────────────────────────

    /** Asigna siempre las primeras numInfiltrados posiciones como INFILTRADO. */
    private static final AsignadorRoles ASIGNADOR_PRIMEROS = (numJugadores, numInfiltrados) -> {
        Set<Integer> pos = new HashSet<>();
        for (int i = 1; i <= numInfiltrados; i++) pos.add(i);
        return pos;
    };

    private static final UUID COSA_FIJA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final SelectorCosa SELECTOR_FIJO = () -> new SeleccionCosa(COSA_FIJA, "PALABRA");

    /** Genera "0001", "0002", … garantizando unicidad por tamaño del conjunto. */
    private static final GeneradorCodigo4Digitos GENERADOR_SECUENCIAL = (existentes) -> {
        int n = existentes.size() + 1;
        return String.format("%04d", n);
    };

    private UUID idModerador;

    @BeforeEach
    void setUp() {
        idModerador = UUID.randomUUID();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Crea una partida con cupo alto para no interferir con las pruebas de unirJugador. */
    private Partida nuevaPartida(int numRondas, int numInfiltrados) {
        return Partida.crear(idModerador, "SALA01", numRondas, numInfiltrados, 20);
    }

    private void unirN(Partida p, int n) {
        IntStream.rangeClosed(1, n).forEach(i ->
                p.unirJugador(UUID.randomUUID(), "Jugador " + i));
    }

    private Partida partidaIniciada(int numJugadores, int numRondas, int numInfiltrados) {
        Partida p = nuevaPartida(numRondas, numInfiltrados);
        unirN(p, numJugadores);
        p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL);
        return p;
    }

    // ── Creación ──────────────────────────────────────────────────────────────

    @Nested
    class Creacion {

        @Test
        void numRondas_2_es_valido() {
            assertThatNoException().isThrownBy(() -> nuevaPartida(2, 1));
        }

        @Test
        void numRondas_5_es_valido() {
            assertThatNoException().isThrownBy(() -> nuevaPartida(5, 1));
        }

        @Test
        void numRondas_1_lanza_excepcion() {
            assertThatThrownBy(() -> nuevaPartida(1, 1))
                    .isInstanceOf(NumRondasInvalidasException.class);
        }

        @Test
        void numRondas_6_lanza_excepcion() {
            assertThatThrownBy(() -> nuevaPartida(6, 1))
                    .isInstanceOf(NumRondasInvalidasException.class);
        }

        @Test
        void estado_inicial_es_LOBBY() {
            assertThat(nuevaPartida(3, 1).getEstado()).isEqualTo(EstadoPartida.LOBBY);
        }

        @Test
        void numJugadores_menor_que_3_lanza_excepcion() {
            assertThatThrownBy(() -> Partida.crear(idModerador, "SALA01", 2, 1, 2))
                    .isInstanceOf(JugadoresInsuficientesException.class);
        }
    }

    // ── Unirse al lobby ───────────────────────────────────────────────────────

    @Nested
    class UnirseAlLobby {

        @Test
        void unir_jugador_en_LOBBY_funciona() {
            Partida p = nuevaPartida(3, 1);
            UUID idUsuario = UUID.randomUUID();
            p.unirJugador(idUsuario, "Ana");
            assertThat(p.getJugadores()).hasSize(1);
        }

        @Test
        void moderador_no_puede_ser_jugador() {
            Partida p = nuevaPartida(3, 1);
            assertThatThrownBy(() -> p.unirJugador(idModerador, "El Jefe"))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void jugador_duplicado_lanza_excepcion() {
            Partida p = nuevaPartida(3, 1);
            UUID idUsuario = UUID.randomUUID();
            p.unirJugador(idUsuario, "Ana");
            assertThatThrownBy(() -> p.unirJugador(idUsuario, "Ana2"))
                    .isInstanceOf(JugadorYaEnPartidaException.class);
        }

        @Test
        void unir_en_estado_no_LOBBY_lanza_excepcion() {
            Partida p = partidaIniciada(3, 2, 1);
            assertThatThrownBy(() -> p.unirJugador(UUID.randomUUID(), "Tarde"))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void sala_llena_lanza_excepcion() {
            Partida p = Partida.crear(idModerador, "SALA01", 2, 1, 3);
            unirN(p, 3);
            assertThatThrownBy(() -> p.unirJugador(UUID.randomUUID(), "Extra"))
                    .isInstanceOf(SalaLlenaException.class);
        }
    }

    // ── Iniciar — regla del 50% (casos borde pedidos: n=3, 6, 9) ─────────────

    @Nested
    class IniciarRegla50 {

        @Test
        void con_2_jugadores_lanza_JugadoresInsuficientesException() {
            Partida p = nuevaPartida(3, 1);
            unirN(p, 2);
            assertThatThrownBy(() -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL))
                    .isInstanceOf(JugadoresInsuficientesException.class);
        }

        @Test
        void n3_max1_con_1_infiltrado_OK() {
            Partida p = nuevaPartida(2, 1);
            unirN(p, 3);
            assertThatNoException().isThrownBy(
                    () -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL));
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.EN_CURSO);
        }

        @Test
        void n3_max1_con_2_infiltrados_viola_regla() {
            Partida p = nuevaPartida(2, 2);
            unirN(p, 3);
            assertThatThrownBy(() -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL))
                    .isInstanceOf(ReglaInfiltradosException.class);
        }

        @Test
        void n6_max2_con_2_infiltrados_OK() {
            Partida p = nuevaPartida(2, 2);
            unirN(p, 6);
            assertThatNoException().isThrownBy(
                    () -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL));
        }

        @Test
        void n6_max2_con_3_infiltrados_viola_regla() {
            Partida p = nuevaPartida(2, 3);
            unirN(p, 6);
            assertThatThrownBy(() -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL))
                    .isInstanceOf(ReglaInfiltradosException.class);
        }

        @Test
        void n9_max4_con_4_infiltrados_OK() {
            Partida p = nuevaPartida(2, 4);
            unirN(p, 9);
            assertThatNoException().isThrownBy(
                    () -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL));
        }

        @Test
        void n9_max4_con_5_infiltrados_viola_regla() {
            Partida p = nuevaPartida(2, 5);
            unirN(p, 9);
            assertThatThrownBy(() -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL))
                    .isInstanceOf(ReglaInfiltradosException.class);
        }

        @Test
        void iniciar_asigna_cosa_y_roles() {
            Partida p = nuevaPartida(2, 1);
            unirN(p, 3);
            p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL);

            assertThat(p.getIdCosaActual()).isEqualTo(COSA_FIJA);
            long infiltrados = p.getJugadores().stream()
                    .filter(j -> j.getRol() == RolJugador.INFILTRADO).count();
            assertThat(infiltrados).isEqualTo(1);
        }

        @Test
        void iniciar_asigna_codigo_4_digitos_unicos() {
            Partida p = nuevaPartida(2, 1);
            unirN(p, 3);
            p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL);

            List<String> codigos = p.getJugadores().stream()
                    .map(Jugador::getCodigo4Digitos).toList();
            assertThat(codigos).doesNotContainNull();
            assertThat(new HashSet<>(codigos)).hasSize(3); // todos distintos
        }

        @Test
        void iniciar_en_estado_no_LOBBY_lanza_excepcion() {
            Partida p = partidaIniciada(3, 2, 1);
            assertThatThrownBy(() -> p.iniciar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO, GENERADOR_SECUENCIAL))
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }

    // ── Turnos y rondas ───────────────────────────────────────────────────────

    @Nested
    class TurnosYRondas {

        @Test
        void avanzarTurno_fuera_de_EN_CURSO_lanza_excepcion() {
            Partida p = nuevaPartida(2, 1);
            assertThatThrownBy(p::avanzarTurno)
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void avanzarTurno_rota_por_jugadores_sin_cambiar_estado() {
            // 3 jugadores, 2 rondas: 5 avances no completan la última ronda
            Partida p = partidaIniciada(3, 2, 1);
            for (int i = 0; i < 5; i++) p.avanzarTurno();
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.EN_CURSO);
        }

        @Test
        void ultima_ronda_completa_transiciona_a_SENALAMIENTO() {
            // 3 jugadores, 2 rondas: se necesitan 3*2 = 6 avances
            Partida p = partidaIniciada(3, 2, 1);
            for (int i = 0; i < 6; i++) p.avanzarTurno();
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.SENALAMIENTO);
        }

        @Test
        void jugador_en_turno_cambia_secuencialmente() {
            Partida p = partidaIniciada(3, 2, 1);
            Jugador primero = p.getJugadorEnTurno();
            p.avanzarTurno();
            Jugador segundo = p.getJugadorEnTurno();
            assertThat(primero.getId()).isNotEqualTo(segundo.getId());
        }
    }

    // ── Señalamiento ──────────────────────────────────────────────────────────

    @Nested
    class Senalamiento {

        private Partida partidaEnSenalamiento() {
            Partida p = partidaIniciada(3, 2, 1);
            for (int i = 0; i < 6; i++) p.avanzarTurno();
            return p;
        }

        @Test
        void senalamiento_fuera_de_fase_lanza_excepcion() {
            Partida p = partidaIniciada(3, 2, 1);
            UUID a = p.getJugadores().get(0).getId();
            UUID b = p.getJugadores().get(1).getId();
            assertThatThrownBy(() -> p.registrarSenalamiento(a, List.of(b)))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void auto_senalamiento_lanza_excepcion() {
            Partida p = partidaEnSenalamiento();
            UUID idJ = p.getJugadores().get(0).getId();
            assertThatThrownBy(() -> p.registrarSenalamiento(idJ, List.of(idJ)))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void todos_senalados_transiciona_a_ADIVINANZA() {
            Partida p = partidaEnSenalamiento();
            List<Jugador> js = new ArrayList<>(p.getJugadores());
            // Cada uno señala al siguiente
            p.registrarSenalamiento(js.get(0).getId(), List.of(js.get(1).getId()));
            p.registrarSenalamiento(js.get(1).getId(), List.of(js.get(2).getId()));
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.SENALAMIENTO); // aún falta uno
            p.registrarSenalamiento(js.get(2).getId(), List.of(js.get(0).getId()));
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.ADIVINANZA);
        }

        @Test
        void abstencion_cuenta_como_senalamiento() {
            Partida p = partidaEnSenalamiento();
            List<Jugador> js = new ArrayList<>(p.getJugadores());
            p.registrarSenalamiento(js.get(0).getId(), List.of());      // abstención
            p.registrarSenalamiento(js.get(1).getId(), List.of(js.get(2).getId()));
            p.registrarSenalamiento(js.get(2).getId(), List.of(js.get(0).getId()));
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.ADIVINANZA);
        }

        @Test
        void multiples_objetivos_validos() {
            Partida p = partidaEnSenalamiento();
            List<Jugador> js = new ArrayList<>(p.getJugadores());
            // El primero señala a los otros dos
            assertThatNoException().isThrownBy(() ->
                p.registrarSenalamiento(js.get(0).getId(),
                    List.of(js.get(1).getId(), js.get(2).getId()))
            );
        }

        @Test
        void senalar_dos_veces_lanza_excepcion() {
            Partida p = partidaEnSenalamiento();
            UUID a = p.getJugadores().get(0).getId();
            UUID b = p.getJugadores().get(1).getId();
            p.registrarSenalamiento(a, List.of(b));
            assertThatThrownBy(() -> p.registrarSenalamiento(a, List.of(b)))
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }

    // ── Adivinanza ────────────────────────────────────────────────────────────

    @Nested
    class Adivinanza {

        private Partida partidaEnAdivinanza() {
            Partida p = partidaIniciada(3, 2, 1);
            for (int i = 0; i < 6; i++) p.avanzarTurno();
            List<Jugador> js = new ArrayList<>(p.getJugadores());
            p.registrarSenalamiento(js.get(0).getId(), List.of(js.get(1).getId()));
            p.registrarSenalamiento(js.get(1).getId(), List.of(js.get(2).getId()));
            p.registrarSenalamiento(js.get(2).getId(), List.of(js.get(0).getId()));
            return p;
        }

        @Test
        void declaracion_de_normal_lanza_excepcion() {
            Partida p = partidaEnAdivinanza();
            UUID idInocente = p.getJugadores().stream()
                    .filter(j -> j.getRol() == RolJugador.NORMAL)
                    .findFirst().orElseThrow().getId();
            assertThatThrownBy(() -> p.registrarDeclaracion(idInocente))
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void todos_infiltrados_declaran_transiciona_a_REVELACION() {
            Partida p = partidaEnAdivinanza();
            UUID idInfiltrado = p.getJugadores().stream()
                    .filter(j -> j.getRol() == RolJugador.INFILTRADO)
                    .findFirst().orElseThrow().getId();
            p.registrarDeclaracion(idInfiltrado);
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.REVELACION);
        }
    }

    // ── Revelación: CONTINUAR y FINALIZAR ─────────────────────────────────────

    @Nested
    class RevelacionFinal {

        private Partida partidaEnRevelacion() {
            Partida p = partidaIniciada(3, 2, 1);
            for (int i = 0; i < 6; i++) p.avanzarTurno();
            List<Jugador> js = new ArrayList<>(p.getJugadores());
            p.registrarSenalamiento(js.get(0).getId(), List.of(js.get(1).getId()));
            p.registrarSenalamiento(js.get(1).getId(), List.of(js.get(2).getId()));
            p.registrarSenalamiento(js.get(2).getId(), List.of(js.get(0).getId()));
            UUID idInfiltrado = p.getJugadores().stream()
                    .filter(j -> j.getRol() == RolJugador.INFILTRADO)
                    .findFirst().orElseThrow().getId();
            p.registrarDeclaracion(idInfiltrado);
            return p; // estado = REVELACION
        }

        @Test
        void finalizar_transiciona_a_FINALIZADA() {
            Partida p = partidaEnRevelacion();
            p.finalizar();
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.FINALIZADA);
            assertThat(p.getFinalizadaEn()).isNotNull();
        }

        @Test
        void finalizar_fuera_de_REVELACION_lanza_excepcion() {
            Partida p = partidaIniciada(3, 2, 1);
            assertThatThrownBy(p::finalizar)
                    .isInstanceOf(TransicionInvalidaException.class);
        }

        @Test
        void continuar_reinicia_roles_y_vuelve_a_EN_CURSO() {
            Partida p = partidaEnRevelacion();
            p.continuar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO);
            assertThat(p.getEstado()).isEqualTo(EstadoPartida.EN_CURSO);
            assertThat(p.getRondaActual()).isEqualTo(1);
            assertThat(p.getIdCosaActual()).isEqualTo(COSA_FIJA);
        }

        @Test
        void continuar_preserva_puntos_acumulados() {
            Partida p = partidaEnRevelacion();

            UUID idJ = p.getJugadores().get(0).getId();
            p.sumarPuntos(idJ, 10);
            assertThat(p.getJugadores().get(0).getPuntosAcumulados()).isEqualTo(10);

            p.continuar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO);

            int puntosPostContinuar = p.getJugadores().stream()
                    .filter(j -> j.getId().equals(idJ))
                    .findFirst().orElseThrow()
                    .getPuntosAcumulados();
            assertThat(puntosPostContinuar).isEqualTo(10);
        }

        @Test
        void continuar_conserva_codigos_4_digitos() {
            Partida p = partidaEnRevelacion();

            List<String> codigosAntes = p.getJugadores().stream()
                    .map(Jugador::getCodigo4Digitos).toList();

            p.continuar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO);

            List<String> codigosDespues = p.getJugadores().stream()
                    .map(Jugador::getCodigo4Digitos).toList();

            assertThat(codigosDespues).containsExactlyElementsOf(codigosAntes);
        }

        @Test
        void continuar_asigna_nuevos_roles() {
            Partida p = partidaEnRevelacion();
            p.continuar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO);
            long infiltrados = p.getJugadores().stream()
                    .filter(j -> j.getRol() == RolJugador.INFILTRADO).count();
            assertThat(infiltrados).isEqualTo(1); // numInfiltrados = 1
        }

        @Test
        void continuar_fuera_de_REVELACION_lanza_excepcion() {
            Partida p = partidaIniciada(3, 2, 1);
            assertThatThrownBy(() -> p.continuar(ASIGNADOR_PRIMEROS, SELECTOR_FIJO))
                    .isInstanceOf(TransicionInvalidaException.class);
        }
    }
}
