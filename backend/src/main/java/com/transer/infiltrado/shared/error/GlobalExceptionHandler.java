package com.transer.infiltrado.shared.error;

import com.transer.infiltrado.catalogo.domain.exception.CosaNoEncontradaException;
import com.transer.infiltrado.catalogo.domain.exception.ImagenInvalidaException;
import com.transer.infiltrado.catalogo.domain.exception.NombreCosaDuplicadoException;
import com.transer.infiltrado.partida.domain.exception.*;

import com.transer.infiltrado.usuarios.domain.exception.CredencialesInvalidasException;
import com.transer.infiltrado.usuarios.domain.exception.EmailYaRegistradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PartidaNoEncontradaException.class)
    public ResponseEntity<ApiError> handlePartidaNoEncontrada(PartidaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(RevisionNoEncontradaException.class)
    public ResponseEntity<ApiError> handleRevisionNoEncontrada(RevisionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(JugadorYaVotoException.class)
    public ResponseEntity<ApiError> handleJugadorYaVoto(JugadorYaVotoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(RevisionYaCerradaException.class)
    public ResponseEntity<ApiError> handleRevisionYaCerrada(RevisionYaCerradaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(JugadorNoEncontradoException.class)
    public ResponseEntity<ApiError> handleJugadorNoEncontrado(JugadorNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(CartaAccesoDenegadoException.class)
    public ResponseEntity<ApiError> handleCartaAccesoDenegado(CartaAccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, ex.getMessage()));
    }

    @ExceptionHandler(SalaLlenaException.class)
    public ResponseEntity<ApiError> handleSalaLlena(SalaLlenaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(JugadorYaEnPartidaException.class)
    public ResponseEntity<ApiError> handleJugadorDuplicado(JugadorYaEnPartidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "Ya estás en esta partida"));
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<ApiError> handleTransicionInvalida(TransicionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(ReglaInfiltradosException.class)
    public ResponseEntity<ApiError> handleReglaInfiltrados(ReglaInfiltradosException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(422, ex.getMessage()));
    }

    @ExceptionHandler(JugadoresInsuficientesException.class)
    public ResponseEntity<ApiError> handleJugadoresInsuficientes(JugadoresInsuficientesException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(422, ex.getMessage()));
    }

    @ExceptionHandler(NumRondasInvalidasException.class)
    public ResponseEntity<ApiError> handleNumRondasInvalidas(NumRondasInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(422, ex.getMessage()));
    }

    @ExceptionHandler(CosaNoEncontradaException.class)
    public ResponseEntity<ApiError> handleCosaNoEncontrada(CosaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(NombreCosaDuplicadoException.class)
    public ResponseEntity<ApiError> handleNombreDuplicado(NombreCosaDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(ImagenInvalidaException.class)
    public ResponseEntity<ApiError> handleImagenInvalida(ImagenInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.of(422, ex.getMessage()));
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ApiError> handleEmailDuplicado(EmailYaRegistradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> handleCredenciales(CredencialesInvalidasException ex) {
        // Mensaje genérico: no se distingue email inexistente de password incorrecta
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiError.ofValidacion(422, "Error de validación", errores));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformed(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, "Solicitud malformada"));
    }

    // Spring MVC 6 lanza NoResourceFoundException en lugar de NoHandlerFoundException
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Recurso no encontrado"));
    }

    // AccessDeniedException debe propagarse para que Spring Security lo maneje con el
    // accessDeniedHandler configurado; si la captura el handler genérico, devuelve 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "Acceso denegado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Error interno del servidor"));
    }
}
