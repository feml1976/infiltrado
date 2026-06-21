package com.transer.infiltrado.usuarios.infrastructure.web;

import com.transer.infiltrado.shared.annotation.RateLimited;
import com.transer.infiltrado.usuarios.application.LoginUseCase;
import com.transer.infiltrado.usuarios.application.RegistrarUsuarioUseCase;
import com.transer.infiltrado.usuarios.application.dto.AuthResponse;
import com.transer.infiltrado.usuarios.application.dto.LoginRequest;
import com.transer.infiltrado.usuarios.application.dto.RegistroRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrar;
    private final LoginUseCase login;

    public AuthController(RegistrarUsuarioUseCase registrar, LoginUseCase login) {
        this.registrar = registrar;
        this.login     = login;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@Valid @RequestBody RegistroRequest request) {
        return registrar.ejecutar(request);
    }

    // @RateLimited activa el interceptor de fuerza bruta que se implementa en el Paso 16
    @PostMapping("/login")
    @RateLimited(key = "login", maxAttempts = 5, windowMinutes = 1, lockoutMinutes = 5)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return login.ejecutar(request);
    }
}
