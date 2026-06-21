package com.transer.infiltrado.usuarios.domain;

import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    /** Busca por email de forma case-insensitive, solo registros activos. */
    Optional<Usuario> buscarPorEmail(String email);

    /** Verifica existencia de email case-insensitive, solo activos. */
    boolean existePorEmail(String email);
}
