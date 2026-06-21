package com.transer.infiltrado.tiemporeal.infrastructure.interceptor;

import java.security.Principal;
import java.util.UUID;

record StompPrincipal(UUID idUsuario) implements Principal {
    @Override
    public String getName() { return idUsuario.toString(); }
}
