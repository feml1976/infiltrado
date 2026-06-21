package com.transer.infiltrado.partida.domain;

import com.transer.infiltrado.partida.domain.exception.*;

import java.time.Instant;
import java.util.*;

/**
 * Agregado raíz del módulo partida.
 *
 * Máquina de estados:
 *   LOBBY → EN_CURSO → SENALAMIENTO → ADIVINANZA → REVELACION → FINALIZADA
 *                                                              ↘ (continuar) → EN_CURSO
 *
 * Invariantes:
 *   - Mínimo 3 jugadores para iniciar()
 *   - num_infiltrados ≤ floor((inscritos-1)/2) validado en iniciar()
 *   - num_rondas ∈ [2,5] validado en crear()
 *   - num_jugadores ≥ 3 (cupo máximo)
 *   - El moderador no puede ser jugador
 *   - Un jugador no puede señalarse a sí mismo
 *   - Solo los infiltrados pueden declarar en ADIVINANZA
 */
public final class Partida {

    // ── Identidad ─────────────────────────────────────────────────────────────

    private UUID id;
    private final UUID idModerador;
    private final String codigoSala;

    // ── Configuración (inmutable tras crear()) ────────────────────────────────

    private final int numRondasTotal;
    private final int numInfiltrados;
    private final int numJugadores; // cupo máximo de la sala

    // ── Estado de la máquina ──────────────────────────────────────────────────

    private EstadoPartida estado;
    private final List<Jugador> jugadores; // ordenados por ordenTurno
    private int rondaActual;              // 1-indexed; 0 mientras está en LOBBY
    private int indiceTurnoActual;        // 0-indexed en jugadores
    private UUID idCosaActual;
    private String modalidad;             // PALABRA | IMAGEN; null en LOBBY

    // ── Marcas de tiempo ──────────────────────────────────────────────────────

    private final Instant creadoEn;
    private Instant iniciadaEn;
    private Instant finalizadaEn;

    // ── Constructores ─────────────────────────────────────────────────────────

    private Partida(UUID id, UUID idModerador, String codigoSala,
                    int numRondasTotal, int numInfiltrados, int numJugadores,
                    EstadoPartida estado, List<Jugador> jugadores,
                    int rondaActual, int indiceTurnoActual,
                    UUID idCosaActual, String modalidad,
                    Instant creadoEn, Instant iniciadaEn, Instant finalizadaEn) {
        this.id                 = id;
        this.idModerador        = idModerador;
        this.codigoSala         = codigoSala;
        this.numRondasTotal     = numRondasTotal;
        this.numInfiltrados     = numInfiltrados;
        this.numJugadores       = numJugadores;
        this.estado             = estado;
        this.jugadores          = jugadores;
        this.rondaActual        = rondaActual;
        this.indiceTurnoActual  = indiceTurnoActual;
        this.idCosaActual       = idCosaActual;
        this.modalidad          = modalidad;
        this.creadoEn           = creadoEn;
        this.iniciadaEn         = iniciadaEn;
        this.finalizadaEn       = finalizadaEn;
    }

    /**
     * Crea una partida nueva en estado LOBBY.
     * El codigo_sala es generado por la capa de aplicación.
     */
    public static Partida crear(UUID idModerador, String codigoSala,
                                int numRondas, int numInfiltrados, int numJugadores) {
        if (numRondas < 2 || numRondas > 5) {
            throw new NumRondasInvalidasException(numRondas);
        }
        if (numInfiltrados < 1) {
            throw new ReglaInfiltradosException(numInfiltrados, 0, 0);
        }
        if (numJugadores < 3) {
            throw new JugadoresInsuficientesException(numJugadores);
        }
        return new Partida(null, idModerador, codigoSala, numRondas, numInfiltrados, numJugadores,
                EstadoPartida.LOBBY, new ArrayList<>(), 0, 0, null, null,
                Instant.now(), null, null);
    }

    public static Partida reconstituir(UUID id, UUID idModerador, String codigoSala,
                                        int numRondasTotal, int numInfiltrados, int numJugadores,
                                        EstadoPartida estado, List<Jugador> jugadores,
                                        int rondaActual, int indiceTurnoActual,
                                        UUID idCosaActual, String modalidad,
                                        Instant creadoEn, Instant iniciadaEn, Instant finalizadaEn) {
        return new Partida(id, idModerador, codigoSala, numRondasTotal, numInfiltrados, numJugadores,
                estado, new ArrayList<>(jugadores), rondaActual, indiceTurnoActual,
                idCosaActual, modalidad, creadoEn, iniciadaEn, finalizadaEn);
    }

    // ── Lobby ─────────────────────────────────────────────────────────────────

    public void unirJugador(UUID idUsuario, String nombre) {
        exigirEstado(EstadoPartida.LOBBY, "unirJugador");
        if (idUsuario.equals(idModerador)) {
            throw new TransicionInvalidaException("El moderador no puede ser jugador");
        }
        boolean yEsta = jugadores.stream().anyMatch(j -> j.getIdUsuario().equals(idUsuario));
        if (yEsta) {
            throw new JugadorYaEnPartidaException(idUsuario);
        }
        if (jugadores.size() >= numJugadores) {
            throw new SalaLlenaException(numJugadores);
        }
        jugadores.add(Jugador.nuevo(idUsuario, nombre, jugadores.size() + 1));
    }

    // ── Transiciones ──────────────────────────────────────────────────────────

    /**
     * LOBBY → EN_CURSO.
     * Valida mínimo 3 jugadores y regla del 50%: numInfiltrados ≤ floor((inscritos-1)/2).
     * Asigna roles y genera código de 4 dígitos único por jugador.
     */
    public void iniciar(AsignadorRoles asignador, SelectorCosa selector,
                        GeneradorCodigo4Digitos generador) {
        exigirEstado(EstadoPartida.LOBBY, "iniciar");

        int n = jugadores.size();
        if (n < 3) {
            throw new JugadoresInsuficientesException(n);
        }

        int maxInfiltrados = (n - 1) / 2;
        if (numInfiltrados > maxInfiltrados) {
            throw new ReglaInfiltradosException(numInfiltrados, n, maxInfiltrados);
        }

        Set<Integer> posicionesInfiltrado = asignador.seleccionar(n, numInfiltrados);
        Set<String> codigosAsignados = new HashSet<>();
        for (Jugador j : jugadores) {
            j.asignarRol(posicionesInfiltrado.contains(j.getOrdenTurno())
                    ? RolJugador.INFILTRADO : RolJugador.NORMAL);
            String codigo = generador.generar(codigosAsignados);
            j.asignarCodigo(codigo);
            codigosAsignados.add(codigo);
        }

        SeleccionCosa seleccion  = selector.seleccionar();
        this.idCosaActual        = seleccion.idCosa();
        this.modalidad           = seleccion.modalidad();
        this.rondaActual         = 1;
        this.indiceTurnoActual   = 0;
        this.iniciadaEn          = Instant.now();
        this.estado              = EstadoPartida.EN_CURSO;
    }

    /**
     * Avanza al turno del siguiente jugador.
     * Cuando completa la última ronda → SENALAMIENTO.
     */
    public void avanzarTurno() {
        exigirEstado(EstadoPartida.EN_CURSO, "avanzarTurno");

        indiceTurnoActual++;
        if (indiceTurnoActual >= jugadores.size()) {
            indiceTurnoActual = 0;
            rondaActual++;
            if (rondaActual > numRondasTotal) {
                estado = EstadoPartida.SENALAMIENTO;
            }
        }
    }

    /**
     * Registra el señalamiento de idJugadorOrigen hacia 0..n objetivos.
     * Lista vacía = abstención válida. Cuando todos han señalado → ADIVINANZA.
     */
    public void registrarSenalamiento(UUID idJugadorOrigen, List<UUID> idsSenalados) {
        exigirEstado(EstadoPartida.SENALAMIENTO, "registrarSenalamiento");
        Jugador origen = buscarJugadorPorId(idJugadorOrigen);
        if (origen.isHaSenalado()) {
            throw new TransicionInvalidaException("El jugador ya registró su señalamiento");
        }
        for (UUID idSenalado : idsSenalados) {
            if (idJugadorOrigen.equals(idSenalado)) {
                throw new TransicionInvalidaException("Un jugador no puede señalarse a sí mismo");
            }
            buscarJugadorPorId(idSenalado); // valida existencia
        }
        origen.registrarSenalamiento();

        if (jugadores.stream().allMatch(Jugador::isHaSenalado)) {
            estado = EstadoPartida.ADIVINANZA;
        }
    }

    /**
     * Registra que un infiltrado declara su adivinanza sobre la cosa objetivo.
     * Cuando todos los infiltrados han declarado → REVELACION.
     */
    public void registrarDeclaracion(UUID idJugador) {
        exigirEstado(EstadoPartida.ADIVINANZA, "registrarDeclaracion");
        Jugador j = buscarJugadorPorId(idJugador);
        if (j.getRol() != RolJugador.INFILTRADO) {
            throw new TransicionInvalidaException("Solo los infiltrados pueden declarar en ADIVINANZA");
        }
        if (j.isHaDeclarado()) {
            throw new TransicionInvalidaException("El infiltrado ya registró su declaración");
        }
        j.registrarDeclaracion();

        boolean todosDeclararon = jugadores.stream()
                .filter(x -> x.getRol() == RolJugador.INFILTRADO)
                .allMatch(Jugador::isHaDeclarado);
        if (todosDeclararon) {
            estado = EstadoPartida.REVELACION;
        }
    }

    /**
     * REVELACION → EN_CURSO.
     * Reinicia roles y flags de fase. Conserva puntosAcumulados y codigo4Digitos.
     */
    public void continuar(AsignadorRoles asignador, SelectorCosa selector) {
        exigirEstado(EstadoPartida.REVELACION, "continuar");

        jugadores.forEach(Jugador::resetearFase);

        Set<Integer> posicionesInfiltrado = asignador.seleccionar(jugadores.size(), numInfiltrados);
        for (Jugador j : jugadores) {
            j.asignarRol(posicionesInfiltrado.contains(j.getOrdenTurno())
                    ? RolJugador.INFILTRADO : RolJugador.NORMAL);
        }

        SeleccionCosa seleccion  = selector.seleccionar();
        this.idCosaActual        = seleccion.idCosa();
        this.modalidad           = seleccion.modalidad();
        this.rondaActual         = 1;
        this.indiceTurnoActual   = 0;
        this.estado              = EstadoPartida.EN_CURSO;
    }

    /**
     * REVELACION → FINALIZADA.
     */
    public void finalizar() {
        exigirEstado(EstadoPartida.REVELACION, "finalizar");
        this.finalizadaEn = Instant.now();
        this.estado       = EstadoPartida.FINALIZADA;
    }

    // ── Puntuación ────────────────────────────────────────────────────────────

    public void sumarPuntos(UUID idJugador, int delta) {
        buscarJugadorPorId(idJugador).sumarPuntos(delta);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public UUID getId()                  { return id; }
    public UUID getIdModerador()         { return idModerador; }
    public String getCodigoSala()        { return codigoSala; }
    public int getNumRondasTotal()       { return numRondasTotal; }
    public int getNumInfiltrados()       { return numInfiltrados; }
    public int getNumJugadores()         { return numJugadores; }
    public EstadoPartida getEstado()     { return estado; }
    public int getRondaActual()          { return rondaActual; }
    public int getIndiceTurnoActual()    { return indiceTurnoActual; }
    public UUID getIdCosaActual()        { return idCosaActual; }
    public String getModalidad()         { return modalidad; }
    public Instant getCreadoEn()         { return creadoEn; }
    public Instant getIniciadaEn()       { return iniciadaEn; }
    public Instant getFinalizadaEn()     { return finalizadaEn; }

    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(jugadores);
    }

    public Jugador getJugadorEnTurno() {
        if (estado != EstadoPartida.EN_CURSO || jugadores.isEmpty()) return null;
        return jugadores.get(indiceTurnoActual);
    }

    public Optional<Jugador> buscarJugadorPorUsuario(UUID idUsuario) {
        return jugadores.stream().filter(j -> j.getIdUsuario().equals(idUsuario)).findFirst();
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void exigirEstado(EstadoPartida requerido, String operacion) {
        if (estado != requerido) {
            throw new TransicionInvalidaException(
                    "'" + operacion + "' requiere estado " + requerido + " pero la partida está en " + estado);
        }
    }

    private Jugador buscarJugadorPorId(UUID idJugador) {
        return jugadores.stream()
                .filter(j -> j.getId().equals(idJugador))
                .findFirst()
                .orElseThrow(() -> new JugadorNoEncontradoException(idJugador));
    }
}
