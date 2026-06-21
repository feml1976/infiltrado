package com.transer.infiltrado.usuarios.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    // Usa lower() para case-insensitive, coherente con el índice parcial ux_usuarios_email_activo
    @Query("SELECT u FROM UsuarioJpaEntity u WHERE lower(u.email) = lower(:email) AND u.deletedAt IS NULL")
    Optional<UsuarioJpaEntity> findByEmailActivo(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM UsuarioJpaEntity u WHERE lower(u.email) = lower(:email) AND u.deletedAt IS NULL")
    boolean existsByEmailActivo(@Param("email") String email);
}
