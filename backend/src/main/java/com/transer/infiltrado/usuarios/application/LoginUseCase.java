package com.transer.infiltrado.usuarios.application;

import com.transer.infiltrado.shared.security.PasswordHasher;
import com.transer.infiltrado.shared.security.TokenGenerator;
import com.transer.infiltrado.usuarios.application.dto.AuthResponse;
import com.transer.infiltrado.usuarios.application.dto.LoginRequest;
import com.transer.infiltrado.usuarios.domain.Usuario;
import com.transer.infiltrado.usuarios.domain.UsuarioRepository;
import com.transer.infiltrado.usuarios.domain.exception.CredencialesInvalidasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public LoginUseCase(UsuarioRepository usuarioRepository,
                        PasswordHasher passwordHasher,
                        TokenGenerator tokenGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher    = passwordHasher;
        this.tokenGenerator    = tokenGenerator;
    }

    @Transactional(readOnly = true)
    public AuthResponse ejecutar(LoginRequest request) {
        log.debug("Login: inicio");

        // Misma excepción para email inexistente y password incorrecta: evita enumeración de usuarios
        Usuario usuario = usuarioRepository.buscarPorEmail(request.email())
                .filter(u -> passwordHasher.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.debug("Login: credenciales no válidas");
                    return new CredencialesInvalidasException();
                });

        String token = tokenGenerator.generar(usuario.getId(), usuario.getNombre(), usuario.isEsAdmin());
        log.info("Login: sesión iniciada id={}", usuario.getId());

        return new AuthResponse(token, usuario.getNombre(), usuario.isEsAdmin());
    }
}
