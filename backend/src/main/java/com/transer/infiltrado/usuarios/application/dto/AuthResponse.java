package com.transer.infiltrado.usuarios.application.dto;

public record AuthResponse(String token, String nombre, boolean esAdmin) {}
