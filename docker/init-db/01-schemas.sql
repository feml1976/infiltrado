-- Crea los schemas del proyecto en el primer arranque del contenedor.
-- Este script lo ejecuta PostgreSQL automáticamente (docker-entrypoint-initdb.d)
-- solo cuando el volumen está vacío.

CREATE SCHEMA IF NOT EXISTS infiltrado;
CREATE SCHEMA IF NOT EXISTS test_infiltrado;

-- Asegura que el usuario de la aplicación tiene acceso completo a ambos schemas.
GRANT ALL PRIVILEGES ON SCHEMA infiltrado TO CURRENT_USER;
GRANT ALL PRIVILEGES ON SCHEMA test_infiltrado TO CURRENT_USER;
