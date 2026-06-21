package com.transer.infiltrado.partida.domain;

import java.util.UUID;

/** Resultado de SelectorCosa: la cosa elegida y su modalidad de juego. */
public record SeleccionCosa(UUID idCosa, String modalidad) {}
