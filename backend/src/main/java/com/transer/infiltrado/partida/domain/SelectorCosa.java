package com.transer.infiltrado.partida.domain;

/**
 * Puerto de dominio para seleccionar una cosa aleatoria del catálogo.
 * Devuelve la cosa elegida y su modalidad (PALABRA/IMAGEN).
 * La implementación concreta se provee en la capa de infraestructura.
 */
public interface SelectorCosa {
    SeleccionCosa seleccionar();
}
