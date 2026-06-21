package com.transer.infiltrado.tiemporeal.infrastructure.interceptor;

import com.transer.infiltrado.partida.domain.Partida;
import com.transer.infiltrado.partida.domain.PartidaRepository;
import com.transer.infiltrado.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthChannelInterceptor.class);
    private static final String TOPIC_PARTIDA_PREFIX = "/topic/partida/";

    private final JwtService jwtService;
    private final PartidaRepository partidaRepository;

    public AuthChannelInterceptor(JwtService jwtService, PartidaRepository partidaRepository) {
        this.jwtService        = jwtService;
        this.partidaRepository = partidaRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            autenticar(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            autorizarSuscripcion(accessor);
        }

        return message;
    }

    // ── Auth en CONNECT ───────────────────────────────────────────────────────

    private void autenticar(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WS CONNECT rechazado: sin cabecera Authorization");
            throw new MessagingException("Token requerido para conectarse");
        }

        String token = authHeader.substring(7);
        Optional<Claims> claims = jwtService.validar(token);
        if (claims.isEmpty()) {
            log.warn("WS CONNECT rechazado: token inválido o expirado");
            throw new MessagingException("Token inválido");
        }

        UUID idUsuario = jwtService.extraerUserId(claims.get());
        accessor.setUser(new StompPrincipal(idUsuario));
    }

    // ── Autorización en SUBSCRIBE ─────────────────────────────────────────────

    private void autorizarSuscripcion(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(TOPIC_PARTIDA_PREFIX)) return;

        UUID idUsuario = resolverIdUsuario(accessor.getUser());
        String idStr   = destination.substring(TOPIC_PARTIDA_PREFIX.length());

        UUID idPartida;
        try {
            idPartida = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            throw new MessagingException("ID de partida inválido en la suscripción");
        }

        Optional<Partida> opt = partidaRepository.buscarPorId(idPartida);
        if (opt.isEmpty()) {
            // Anti-enumeración: no revelar si la partida existe
            log.warn("WS SUBSCRIBE rechazado: partida no encontrada o usuario no participa");
            throw new MessagingException("No autorizado para suscribirse a esta partida");
        }

        Partida partida = opt.get();
        boolean esJugador   = partida.buscarJugadorPorUsuario(idUsuario).isPresent();
        boolean esModerador = partida.getIdModerador().equals(idUsuario);

        if (!esJugador && !esModerador) {
            log.warn("WS SUBSCRIBE rechazado: usuario no inscrito en la partida");
            throw new MessagingException("No autorizado para suscribirse a esta partida");
        }
    }

    private UUID resolverIdUsuario(Principal principal) {
        if (principal instanceof StompPrincipal sp) return sp.idUsuario();
        if (principal != null) {
            try { return UUID.fromString(principal.getName()); }
            catch (IllegalArgumentException ignored) {}
        }
        throw new MessagingException("Sesión sin usuario autenticado");
    }
}
