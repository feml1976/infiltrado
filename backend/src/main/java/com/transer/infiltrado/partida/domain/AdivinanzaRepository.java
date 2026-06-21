package com.transer.infiltrado.partida.domain;

import java.util.List;
import java.util.UUID;

public interface AdivinanzaRepository {
    Adivinanza guardar(Adivinanza adivinanza);
    List<Adivinanza> buscarPorPartida(UUID idPartida);
}
