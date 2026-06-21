package com.transer.infiltrado.usuarios.infrastructure.persistence;

import com.transer.infiltrado.usuarios.domain.Usuario;
import com.transer.infiltrado.usuarios.domain.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;

    UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return toDomain(jpa.save(toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmailActivo(email).map(this::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmailActivo(email);
    }

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.setEmail(u.getEmail());
        e.setNombre(u.getNombre());
        e.setCelular(u.getCelular());
        e.setPasswordHash(u.getPasswordHash());
        e.setEsAdmin(u.isEsAdmin());
        return e;
    }

    private Usuario toDomain(UsuarioJpaEntity e) {
        return Usuario.reconstituir(
                e.getId(), e.getEmail(), e.getNombre(),
                e.getCelular(), e.getPasswordHash(), e.isEsAdmin(), e.getCreatedAt());
    }
}
