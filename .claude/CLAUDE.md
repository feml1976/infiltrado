# El Infiltrado — Guía de Desarrollo

Juego de deducción social y faroleo en tiempo real. Los jugadores reciben en secreto una "cosa objetivo" excepto el/los Infiltrados, quienes deben deducirla a partir de las pistas de los demás.

---

## Stack y versiones

| Capa          | Tecnología                                         |
| ------------- | -------------------------------------------------- |
| Backend       | Java 21 LTS · Spring Boot 3.5.x · Maven 3.9.x     |
| Tiempo real   | Spring WebSocket + STOMP · SockJS                  |
| Frontend      | React 19 · Vite 5 · TypeScript 5.x                |
| Estado global | Zustand · React Query (@tanstack/react-query v5)   |
| Base de datos | PostgreSQL 16.x                                    |
| Migraciones   | Flyway 10.x                                        |
| Seguridad     | Spring Security · JJWT 0.12.x (JWT 12h, sin refresh) |
| Contenedores  | Docker · Docker Compose                            |

---

## Puertos

| Servicio   | Puerto |
| ---------- | ------ |
| Backend    | 8093   |
| Frontend   | 5183   |
| PostgreSQL | 5443   |

**Regla absoluta:** nunca usar los puertos por defecto (8080, 5173, 5432).

---

## Schemas de base de datos

- **Producción:** `infiltrado`
- **Pruebas de integración:** `test_infiltrado`

Las pruebas de integración siempre corren contra PostgreSQL real. **Sin TestContainers. Sin H2.**

---

## Cómo levantar el entorno de desarrollo

```bash
# 1. Levantar infraestructura (PostgreSQL + servicios)
docker-compose up -d

# 2. Backend (en una terminal)
cd backend
mvn spring-boot:run

# 3. Frontend (en otra terminal)
cd frontend
npm run dev
```

El backend queda en http://localhost:8093 y el frontend en http://localhost:5183.
El WebSocket STOMP se conecta a ws://localhost:8093/ws.

---

## Cómo correr las pruebas

```bash
# 1. Levantar PostgreSQL de pruebas
docker-compose -f docker-compose.test.yml up -d

# 2. Esperar healthcheck (el compose tiene uno configurado)
# 3. Ejecutar suite
cd backend
mvn test -Dspring.profiles.active=test
```

---

## Estructura de paquetes y capas

Cada módulo sigue **Arquitectura Hexagonal** (Puertos & Adaptadores):

```
com.transer.infiltrado.[modulo]/
├── domain/          ← Entidades, value objects, puertos (interfaces), reglas de negocio
│                      NO depende de Spring ni JPA
├── application/     ← Casos de uso (orquestan dominio + puertos)
└── infrastructure/  ← Adaptadores: JPA (repositorios), controllers REST, mensajería WS
```

### Módulos

| Módulo        | Responsabilidad                                                              |
| ------------- | ---------------------------------------------------------------------------- |
| `usuarios`    | Registro, autenticación, perfiles                                            |
| `catalogo`    | Banco de cosas (palabras/imágenes), administración                           |
| `partida`     | Lobby, fases, turnos, máquina de estados, reglas del juego                   |
| `puntuacion`  | Scoring por partida, historial acumulado                                     |
| `tiemporeal`  | Gateway WebSocket/STOMP — solo mensajería, sin lógica de negocio             |
| `shared`      | Configuración Spring, seguridad JWT, manejo global de errores, logging       |

---

## Máquina de estados de la partida

```
LOBBY ──[iniciar]──► EN_CURSO ──[última ronda completa]──► SENALAMIENTO
                                                               │
                                                      [todos señalaron]
                                                               │
                                                               ▼
                                                          ADIVINANZA
                                                               │
                                                  [todos los infiltrados declararon]
                                                               │
                                                               ▼
                                                          REVELACION
                                                               │
                                          ┌────────────────────┴──────────────────────┐
                                   [continuar]                                  [terminar]
                                          │                                            │
                                          ▼                                            ▼
                               reinicia roles/cosa,                              FINALIZADA
                               conserva puntos acumulados
                               vuelve a EN_CURSO
```

**Sin temporizador de turno.** Los turnos son secuenciales y avanzan por acción explícita del jugador.

### Roles de jugador

- `NORMAL` — jugador que conoce la cosa objetivo. Nombre canónico en código, BD y reglamento. **No usar INOCENTE.**
- `INFILTRADO` — jugador que desconoce la cosa y debe deducirla.

### Semántica del código de 4 dígitos

El `codigo_4_digitos` es **por sesión de juego**, no por ronda. Se asigna en `iniciar()` y **se conserva** en las transiciones `CONTINUAR` (REVELACION → EN_CURSO). Solo cambia si se crea una nueva partida desde cero. Esto permite que los jugadores compartan su código físico al inicio de la sesión sin tener que revelarlo en cada ronda.

---

## Convenciones de código

### Backend (Java)

- **Inyección por constructor** — todos los beans Spring usan campos `final`.
- **Records** para DTOs inmutables (`record LoginRequest(String email, String password) {}`).
- **Logging estructurado** en todos los límites de operación: entrada (identificadores no sensibles), salida (outcome + duración), errores (contexto). Nunca `System.out.println`.
- **Jakarta Validation** para toda entrada externa. `@RestControllerAdvice` devuelve errores saneados (nunca stack trace al cliente).
- Naming BD: `snake_case`, tablas en plural, columnas en singular, UUID para PKs, soft deletes (`deleted_at`).
- Migraciones: `V1__init.sql`, `V2__...`, seeds repetibles como `R__seed_cosas.sql`.

### Frontend (TypeScript/React)

- Hooks de dominio en `src/features/<feature>/hooks/`
- API calls via React Query; estado persistente con Zustand.
- WS STOMP en `src/shared/ws/`; nunca conectar directo desde componentes.
- Sin `console.log` en código productivo.

---

## Reglas críticas de seguridad del rol

1. El **rol** (`NORMAL`/`INFILTRADO`) y el **código de 4 dígitos** de un jugador **jamás** se envían al cliente de otro jugador.
2. Los eventos WebSocket de difusión **nunca** incluyen el rol de nadie antes de la fase `REVELACION`.
3. La carta solo se sirve al propietario autenticado de ese código, vía endpoint dedicado protegido.
4. El `codigo_4_digitos` y el `codigo_sala` nunca aparecen en logs.
5. Rate limiting: máx. 5 intentos / 1 min / bloqueo 5 min en `/login`, `/mi-carta` y `/unirse`.
   Implementación: `RateLimitingAspect` (AOP, ventana deslizante, `ConcurrentHashMap`).
   **Limitación conocida:** los contadores son in-memory — válido para instancia única.
   En despliegue multi-instancia el umbral real se multiplica por réplicas; migrar a Redis si escala.
6. Regla del 50 %: `num_infiltrados <= floor((n-1)/2)`. Rechazar en el límite del sistema.
7. Mínimo 3 jugadores para iniciar una partida.

---

## Decisiones arquitectónicas confirmadas

Ver `docs/ADR-NN-1.MD` para decisiones formales. Resumen:

- JWT de token único, TTL 12h, sin refresh token.
- Imágenes del banco almacenadas como Base64 en BD.
- `codigo_sala` de 6 caracteres alfanuméricos en mayúsculas.
- Validación de "nombrar la cosa" por votación de jugadores (no automática).
- Se admiten empates en el ranking.
