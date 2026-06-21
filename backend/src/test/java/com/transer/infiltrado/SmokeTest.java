package com.transer.infiltrado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeTest {

    @Test
    void limiteInfiltradosCalculaCorrectamente() {
        assertThat(maxInfiltrados(3)).isEqualTo(1);
        assertThat(maxInfiltrados(4)).isEqualTo(1);
        assertThat(maxInfiltrados(5)).isEqualTo(2);
        assertThat(maxInfiltrados(6)).isEqualTo(2);
        assertThat(maxInfiltrados(9)).isEqualTo(4);
        assertThat(maxInfiltrados(10)).isEqualTo(4);
    }

    private int maxInfiltrados(int n) {
        return (n - 1) / 2;
    }
}
