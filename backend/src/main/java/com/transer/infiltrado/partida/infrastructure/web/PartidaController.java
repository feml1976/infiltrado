package com.transer.infiltrado.partida.infrastructure.web;

import com.transer.infiltrado.partida.application.*;
import com.transer.infiltrado.partida.application.dto.*;
import com.transer.infiltrado.shared.annotation.RateLimited;
import com.transer.infiltrado.shared.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final CrearPartidaUseCase crearPartida;
    private final UnirseAPartidaUseCase unirseAPartida;
    private final IniciarPartidaUseCase iniciarPartida;
    private final ObtenerEstadoPartidaUseCase obtenerEstado;
    private final ConsultarCartaUseCase consultarCarta;
    private final AvanzarTurnoUseCase avanzarTurno;
    private final RegistrarPistaUseCase registrarPista;
    private final ProponerRevisionUseCase proponerRevision;
    private final VotarRevisionUseCase votarRevision;
    private final RegistrarSenalamienatoUseCase registrarSenalamiento;
    private final RegistrarAdivinanzaUseCase registrarAdivinanza;
    private final RevelacionUseCase revelacion;
    private final ContinuarPartidaUseCase continuarPartida;
    private final TerminarPartidaUseCase terminarPartida;

    public PartidaController(CrearPartidaUseCase crearPartida,
                              UnirseAPartidaUseCase unirseAPartida,
                              IniciarPartidaUseCase iniciarPartida,
                              ObtenerEstadoPartidaUseCase obtenerEstado,
                              ConsultarCartaUseCase consultarCarta,
                              AvanzarTurnoUseCase avanzarTurno,
                              RegistrarPistaUseCase registrarPista,
                              ProponerRevisionUseCase proponerRevision,
                              VotarRevisionUseCase votarRevision,
                              RegistrarSenalamienatoUseCase registrarSenalamiento,
                              RegistrarAdivinanzaUseCase registrarAdivinanza,
                              RevelacionUseCase revelacion,
                              ContinuarPartidaUseCase continuarPartida,
                              TerminarPartidaUseCase terminarPartida) {
        this.crearPartida         = crearPartida;
        this.unirseAPartida       = unirseAPartida;
        this.iniciarPartida       = iniciarPartida;
        this.obtenerEstado        = obtenerEstado;
        this.consultarCarta       = consultarCarta;
        this.avanzarTurno         = avanzarTurno;
        this.registrarPista       = registrarPista;
        this.proponerRevision     = proponerRevision;
        this.votarRevision        = votarRevision;
        this.registrarSenalamiento = registrarSenalamiento;
        this.registrarAdivinanza  = registrarAdivinanza;
        this.revelacion           = revelacion;
        this.continuarPartida     = continuarPartida;
        this.terminarPartida      = terminarPartida;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrearPartidaResponse crear(@Valid @RequestBody CrearPartidaRequest request,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        return crearPartida.ejecutar(principal.getId(), request);
    }

    @PostMapping("/{codigoSala}/unirse")
    @RateLimited(key = "unirse", maxAttempts = 5, windowMinutes = 1, lockoutMinutes = 5)
    public EstadoPartidaResponse unirse(@PathVariable String codigoSala,
                                         @AuthenticationPrincipal UsuarioPrincipal principal) {
        return unirseAPartida.ejecutar(codigoSala.toUpperCase(), principal.getId(), principal.getNombre());
    }

    @PostMapping("/{codigoSala}/iniciar")
    public EstadoPartidaResponse iniciar(@PathVariable String codigoSala,
                                          @AuthenticationPrincipal UsuarioPrincipal principal) {
        return iniciarPartida.ejecutar(codigoSala.toUpperCase(), principal.getId());
    }

    @GetMapping("/{codigoSala}")
    public EstadoPartidaResponse estado(@PathVariable String codigoSala) {
        return obtenerEstado.ejecutar(codigoSala.toUpperCase());
    }

    @PostMapping("/{codigoSala}/turno/avanzar")
    public EstadoPartidaResponse avanzarTurno(@PathVariable String codigoSala,
                                               @AuthenticationPrincipal UsuarioPrincipal principal) {
        return avanzarTurno.ejecutar(codigoSala.toUpperCase(), principal.getId());
    }

    @PostMapping("/{codigoSala}/turno/pista")
    public EstadoPartidaResponse registrarPista(@PathVariable String codigoSala,
                                                 @Valid @RequestBody RegistrarPistaRequest request,
                                                 @AuthenticationPrincipal UsuarioPrincipal principal) {
        return registrarPista.ejecutar(codigoSala.toUpperCase(), principal.getId(), request.contenido());
    }

    @PostMapping("/{codigoSala}/revisiones")
    @ResponseStatus(HttpStatus.CREATED)
    public RevisionResponse proponerRevision(@PathVariable String codigoSala,
                                              @Valid @RequestBody ProponerRevisionRequest request,
                                              @AuthenticationPrincipal UsuarioPrincipal principal) {
        return proponerRevision.ejecutar(codigoSala.toUpperCase(), principal.getId(),
                request.tipo(), request.idJugadorAcusado());
    }

    @PostMapping("/{codigoSala}/revisiones/{idRevision}/votos")
    @ResponseStatus(HttpStatus.CREATED)
    public RevisionResponse votar(@PathVariable String codigoSala,
                                   @PathVariable UUID idRevision,
                                   @Valid @RequestBody VotarRequest request,
                                   @AuthenticationPrincipal UsuarioPrincipal principal) {
        return votarRevision.ejecutar(codigoSala.toUpperCase(), idRevision,
                principal.getId(), request.votoSi());
    }

    @PostMapping("/{codigoSala}/senalamiento")
    public EstadoPartidaResponse senalar(@PathVariable String codigoSala,
                                          @Valid @RequestBody RegistrarSenalamienatoRequest request,
                                          @AuthenticationPrincipal UsuarioPrincipal principal) {
        return registrarSenalamiento.ejecutar(codigoSala.toUpperCase(), principal.getId(),
                request.idsSenalados() != null ? request.idsSenalados() : java.util.List.of());
    }

    @PostMapping("/{codigoSala}/adivinanza")
    public EstadoPartidaResponse adivinanza(@PathVariable String codigoSala,
                                             @Valid @RequestBody RegistrarAdivinanzaRequest request,
                                             @AuthenticationPrincipal UsuarioPrincipal principal) {
        return registrarAdivinanza.ejecutar(codigoSala.toUpperCase(), principal.getId(),
                request.textoAdivinanza());
    }

    @GetMapping("/{codigoSala}/revelacion")
    public RevelacionResponse revelacion(@PathVariable String codigoSala) {
        return revelacion.ejecutar(codigoSala.toUpperCase());
    }

    @PostMapping("/{codigoSala}/continuar")
    public EstadoPartidaResponse continuar(@PathVariable String codigoSala,
                                            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return continuarPartida.ejecutar(codigoSala.toUpperCase(), principal.getId());
    }

    @PostMapping("/{codigoSala}/terminar")
    public EstadoPartidaResponse terminar(@PathVariable String codigoSala,
                                           @AuthenticationPrincipal UsuarioPrincipal principal) {
        return terminarPartida.ejecutar(codigoSala.toUpperCase(), principal.getId());
    }

    // Rate limiting activado en Paso 16 — el interceptor AOP ya respeta la anotación
    @GetMapping("/{codigoSala}/mi-carta")
    @RateLimited(key = "mi-carta", maxAttempts = 5, windowMinutes = 1, lockoutMinutes = 5)
    public CartaResponse miCarta(@PathVariable String codigoSala,
                                  @AuthenticationPrincipal UsuarioPrincipal principal) {
        return consultarCarta.ejecutar(codigoSala.toUpperCase(), principal.getId());
    }
}
