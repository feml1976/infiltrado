package com.transer.infiltrado.partida.infrastructure.randomgen;

import com.transer.infiltrado.catalogo.domain.Cosa;
import com.transer.infiltrado.catalogo.domain.CosaRepository;
import com.transer.infiltrado.partida.domain.SeleccionCosa;
import com.transer.infiltrado.partida.domain.SelectorCosa;
import org.springframework.stereotype.Component;

@Component
public class SelectorCosaAleatorio implements SelectorCosa {

    private final CosaRepository cosaRepository;

    public SelectorCosaAleatorio(CosaRepository cosaRepository) {
        this.cosaRepository = cosaRepository;
    }

    @Override
    public SeleccionCosa seleccionar() {
        Cosa cosa = cosaRepository.seleccionarAleatoria()
                .orElseThrow(() -> new IllegalStateException("No hay cosas activas en el catálogo"));
        return new SeleccionCosa(cosa.getId(), cosa.getTipo().name());
    }
}
