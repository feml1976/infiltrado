package com.transer.infiltrado.usuarios.application;

import com.transer.infiltrado.shared.security.PasswordHasher;
import com.transer.infiltrado.shared.security.TokenGenerator;
import com.transer.infiltrado.usuarios.application.dto.AuthResponse;
import com.transer.infiltrado.usuarios.application.dto.RegistroRequest;
import com.transer.infiltrado.usuarios.domain.Usuario;
import com.transer.infiltrado.usuarios.domain.UsuarioRepository;
import com.transer.infiltrado.usuarios.domain.exception.EmailYaRegistradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarUsuarioUseCase.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                   PasswordHasher passwordHasher,
                                   TokenGenerator tokenGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher    = passwordHasher;
        this.tokenGenerator    = tokenGenerator;
    }

    @Transactional
    public AuthResponse ejecutar(RegistroRequest request) {
        log.debug("Registro: inicio");

        if (usuarioRepository.existePorEmail(request.email())) {
            log.debug("Registro: email ya registrado");
            throw new EmailYaRegistradoException();
        }

        String hash = passwordHasher.hash(request.password());
        // celular vacío lo normalizamos a null
        String celular = (request.celular() == null || request.celular().isBlank())
                ? null : request.celular().trim();

        Usuario usuario = Usuario.nuevo(request.email().trim().toLowerCase(),
                request.nombre().trim(), celular, hash);

        Usuario guardado = usuarioRepository.guardar(usuario);

        String token = tokenGenerator.generar(guardado.getId(), guardado.getNombre(), guardado.isEsAdmin());
        log.info("Registro: usuario creado id={}", guardado.getId());

        return new AuthResponse(token, guardado.getNombre(), guardado.isEsAdmin());
    }
}
