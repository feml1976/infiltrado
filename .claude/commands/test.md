# test — Ejecutar la suite de pruebas de integración

Las pruebas de integración corren SIEMPRE contra PostgreSQL real en Docker (schema `test_infiltrado`).
**Sin TestContainers. Sin H2.**

Los tests de integración (`@Tag("integration")`) están excluidos del ciclo por defecto
para que `mvn test` pase siempre (sin BD). Se ejecutan con el perfil Maven `it`.

## Ciclo por defecto — solo unit tests (sin BD)

```bash
cd backend
mvn test
```

## Tests de integración — requieren PostgreSQL en ejecución

### Opción A — Entorno de desarrollo activo (recomendada)

Si ya tienes `docker-compose up -d` corriendo, los schemas existen. Ejecuta:

```bash
cd backend
mvn test -Pit "-Dspring.profiles.active=test"
```

### Opción B — Entorno CI/CD (PostgreSQL efímero)

```bash
# 1. Levantar PostgreSQL efímero
docker-compose -f docker-compose.test.yml up -d

# 2. Esperar healthcheck
docker inspect --format='{{.State.Health.Status}}' infiltrado-postgres-test
# Debe mostrar: healthy

# 3. Ejecutar tests de integración
cd backend
mvn test -Pit "-Dspring.profiles.active=test"

# 4. Limpiar
docker-compose -f docker-compose.test.yml down
```

## Ejecutar un módulo específico (integración)

```bash
mvn test -Pit "-Dspring.profiles.active=test" "-Dtest=com.transer.infiltrado.usuarios.**"
```
