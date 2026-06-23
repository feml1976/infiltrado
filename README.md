# El Infiltrado

Juego de deducción social y faroleo en tiempo real. Los jugadores reciben en secreto una "cosa objetivo" excepto el/los Infiltrados, quienes deben deducirla a partir de las pistas de los demás.

---

## Requisitos

| Herramienta | Versión mínima |
| ----------- | -------------- |
| Java        | 21 LTS         |
| Maven       | 3.9.x          |
| Node.js     | 20 LTS         |
| Docker      | 24+            |
| Docker Compose | 2.x         |

---

## Puertos

| Servicio   | Puerto |
| ---------- | ------ |
| Backend    | 8093   |
| Frontend   | 5183   |
| PostgreSQL | 5443   |

---

## Arranque del entorno de desarrollo

```bash
# 1. Clonar el repositorio y copiar las variables de entorno
cp backend/.env.example backend/.env
# Editar backend/.env y asignar JWT_SECRET (mínimo 32 caracteres, obligatorio en prod)

# 2. Levantar PostgreSQL
docker-compose up -d

# 3. Backend  (nueva terminal)
cd backend
mvn spring-boot:run

# 4. Frontend (nueva terminal)
cd frontend
npm install
npm run dev
```

- Backend: http://localhost:8093
- Frontend: http://localhost:5183
- WebSocket STOMP: ws://localhost:8093/ws

---

## Variables de entorno del backend

| Variable         | Descripción                                      | Obligatoria en prod |
| ---------------- | ------------------------------------------------ | ------------------- |
| `JWT_SECRET`     | Secreto HMAC-SHA256 (≥ 32 caracteres)            | **Sí**              |
| `DB_URL`         | JDBC URL de PostgreSQL                           | Sí                  |
| `DB_USER`        | Usuario de BD                                    | Sí                  |
| `DB_PASS`        | Contraseña de BD                                 | Sí                  |

> **Aviso de seguridad**: el arranque falla si `JWT_SECRET` coincide con el valor por defecto de
> desarrollo y el perfil activo no es `dev` ni `test`. Esto previene que el secreto público del
> repositorio se use en producción.

---

## Tests

### Backend — solo unit tests (sin base de datos)

```bash
cd backend
mvn test
```

### Backend — suite completa de integración (requiere PostgreSQL)

```bash
# Opción A: con el entorno de desarrollo ya levantado
cd backend
mvn test -Pit -Dspring.profiles.active=test

# Opción B: PostgreSQL efímero para CI
docker-compose -f docker-compose.test.yml up -d
cd backend
mvn test -Pit -Dspring.profiles.active=test
docker-compose -f docker-compose.test.yml down
```

### Frontend — smoke tests

```bash
cd frontend
npm test          # ejecución única (CI)
npm run test:watch  # modo watch (desarrollo)
```

---

## Estructura del proyecto

```
infiltrado/
├── backend/                  # Spring Boot 3.5 · Java 21
│   ├── src/main/java/com/transer/infiltrado/
│   │   ├── usuarios/         # Registro, auth, JWT
│   │   ├── catalogo/         # Banco de cosas (admin)
│   │   ├── partida/          # Lobby, fases, scoring
│   │   ├── puntuacion/       # Historial acumulado
│   │   ├── tiemporeal/       # Gateway WebSocket/STOMP
│   │   └── shared/           # Config, seguridad, errores
│   └── src/main/resources/db/migration/   # Flyway V1–V6 + seed
│
└── frontend/                 # React 19 · Vite 5 · TypeScript
    └── src/
        ├── features/
        │   ├── auth/         # Login, registro
        │   ├── lobby/        # Crear/unirse a sala
        │   ├── partida/      # Fases del juego + carta
        │   └── admin/        # Panel de cosas (admin)
        ├── shared/
        │   ├── api/          # Axios client + endpoints
        │   ├── ui/           # Componentes y rutas protegidas
        │   ├── ws/           # STOMP singleton + hook
        │   └── utils/        # Validación de imágenes
        └── store/            # Zustand (auth, partida)
```

---

## Seguridad

- **JWT**: token único de 12 h, sin refresh. Secreto validado al arranque en producción.
- **Rate limiting**: 5 intentos / 1 min / bloqueo 5 min en `/login`, `/mi-carta` y `/unirse`.
  Clave compuesta: IP (truncada para logs) + identidad hasheada.
  > **Limitación**: los contadores viven en memoria (`ConcurrentHashMap` en `RateLimitingAspect`).
  > En un despliegue de instancia única es suficiente; con múltiples instancias cada una mantiene
  > su propio contador y el umbral real efectivo se multiplica por el número de réplicas.
  > Para escalar horizontalmente, reemplazar `RateLimitingAspect` por una implementación
  > respaldada en Redis (p.ej. con `spring-boot-starter-data-redis` + `RedisTemplate`).
- **Roles**: `NORMAL` / `INFILTRADO`. El rol nunca se expone antes de `REVELACION`.
- **Carta**: `gcTime: 0` en React Query + `removeQueries` al desmontar. Sin caché en disco.
- **Admin**: doble guardia — `AdminRoute` en frontend (UX) + `@PreAuthorize("hasRole('ADMIN')")` en backend (gate real).

---

## Máquina de estados de la partida

```
LOBBY → EN_CURSO → SENALAMIENTO → ADIVINANZA → REVELACION
                                                    ↓
                                             [CONTINUAR] → EN_CURSO (nueva ronda)
                                             [TERMINAR]  → FINALIZADA
```
