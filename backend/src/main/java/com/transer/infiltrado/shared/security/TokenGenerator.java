package com.transer.infiltrado.shared.security;

import java.util.UUID;

public interface TokenGenerator {
    String generar(UUID userId, String nombre, boolean esAdmin);
}
