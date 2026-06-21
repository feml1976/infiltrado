package com.transer.infiltrado;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que Flyway aplica V1__init.sql correctamente en el schema test_infiltrado.
 * Requiere PostgreSQL real en ejecución (docker-compose up -d).
 * Excluido del ciclo por defecto de Surefire; ejecutar con:
 *   mvn test -Dspring.profiles.active=test -DexcludedGroups=
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private static final List<String> TABLAS_ESPERADAS = List.of(
            "usuarios",
            "cosas",
            "partidas",
            "jugadores_partida",
            "pistas",
            "revisiones",
            "votos_revision",
            "senalamientos",
            "adivinanzas",
            "puntuaciones_historicas"
    );

    @Test
    void flywayMigrationCreaTodasLasTablas() {
        TABLAS_ESPERADAS.forEach(tabla -> {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'test_infiltrado' AND table_name = ?",
                    Integer.class, tabla);
            assertThat(count)
                    .as("Tabla '%s' debe existir en el schema test_infiltrado", tabla)
                    .isEqualTo(1);
        });
    }

    @Test
    void flywaySchemHistoryEstaEnSchemaTest() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'test_infiltrado' AND table_name = 'flyway_schema_history'",
                Integer.class);
        assertThat(count)
                .as("flyway_schema_history debe estar en test_infiltrado, no en public")
                .isEqualTo(1);
    }
}
