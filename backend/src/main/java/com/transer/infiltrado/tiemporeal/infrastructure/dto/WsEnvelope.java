package com.transer.infiltrado.tiemporeal.infrastructure.dto;

/** Contenedor genérico para todos los eventos STOMP difundidos al cliente. */
public record WsEnvelope(String tipo, Object datos) {}
