package com.transer.infiltrado.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class UsuarioPrincipal implements UserDetails {

    private final UUID id;
    private final String nombre;
    private final boolean esAdmin;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(UUID id, String nombre, boolean esAdmin) {
        this.id       = id;
        this.nombre   = nombre;
        this.esAdmin  = esAdmin;
        this.authorities = esAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public UUID getId()       { return id; }
    public String getNombre() { return nombre; }
    public boolean isEsAdmin() { return esAdmin; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()  { return ""; }
    @Override public String getUsername()  { return id.toString(); }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()              { return true; }
}
