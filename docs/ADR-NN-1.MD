# ADR-NNN — Adopción de Spring WebSocket + STOMP para la sincronización en tiempo real de El Infiltrado

> ⚠️ **PENDIENTE — número de ADR.** No tengo acceso en esta sesión al índice maestro
> `00-index.md §8.1` del Core TranserIT, que es la fuente autoritativa para asignar el
> `ADR-NNN` consecutivo global. **No asigno número a ciegas** (riesgo de colisión con un
> ADR de otro satélite). Reemplaza `NNN` por el consecutivo real tras consultar §8.1.

- **Fecha:** 2026-06-18
- **Autor:** Fmontoya — [rol, TranserIT] ⟵ _confirmar/ajustar rol_
- **Estado:** Propuesta
- **Alcance:** Aplica exclusivamente al sistema **El Infiltrado** (proyecto satélite), específicamente a la capa de sincronización en tiempo real de la partida (turnos, fases, difusión de eventos de juego). **No** establece un estándar transversal de tiempo real para toda el área TranserIT; otros sistemas que requieran tiempo real deberán evaluar su propio caso. No cubre notificaciones push a dispositivos móviles ni streaming de datos masivos.
- **Sistemas afectados:** El Infiltrado (nuevo). [Cruzar con `mapa-sistemas.md` al incorporar al Knowledge.]
- **Riesgos relacionados:** [Cruzar con `riesgos-transversales.md` (RT-NNN) si aplica.]
- **ADRs relacionadas:** ADR-001 (monolito modular) y los ADRs de stack (Java+Spring Boot, React, PostgreSQL) referidos en `stack-tecnologico.md §8`, si están vigentes.

## Contexto

El Infiltrado es un juego de mesa social multijugador donde varios participantes, cada uno desde su propio dispositivo, comparten una partida con **turnos secuenciales y fases coordinadas** (lobby, rondas, señalamiento, adivinanza, revelación). Todos los clientes deben observar de forma **coherente y casi inmediata** los cambios de estado: de quién es el turno, qué pista se registró, cuándo cambia la fase y cuándo se revelan los resultados.

Este patrón no es un CRUD request/response: requiere que el servidor **empuje** (push) eventos a múltiples clientes suscritos a la misma partida cuando el estado cambia. Una solución basada solo en REST obligaría a _polling_ periódico, con latencia perceptible, consumo innecesario y mayor complejidad para mantener la coherencia del orden de turnos.

Se requiere decidir el mecanismo de comunicación en tiempo real, priorizando: tecnología "aburrida" y nativa del stack (Java 21 + Spring Boot 3.5.x), licencias open-source, y mínima superficie de seguridad (los roles de jugador y los códigos privados **no** deben filtrarse por el canal de difusión).

## Decisión

Adoptar **Spring WebSocket con el subprotocolo STOMP** como mecanismo de sincronización en tiempo real de la partida.

En concreto:

- El backend expone un endpoint STOMP (p. ej. `/ws`) con fallback SockJS.
- Los clientes se suscriben a destinos por partida (p. ej. `/topic/partida/{id}`) y reciben eventos de juego: `turno_de`, `pista_registrada`, `cambio_fase`, `revelacion`.
- El cliente React usa `@stomp/stompjs` + `sockjs-client`.
- **Los eventos de difusión nunca incluyen el rol de ningún jugador ni códigos privados** antes de la fase de revelación. La consulta de la carta propia se realiza por un endpoint REST autenticado dedicado, no por el canal WebSocket.
- El avance de turno ocurre por **acción explícita** del jugador (no hay temporizador de turno en esta versión).

## Alternativas consideradas

### Alternativa A — Polling REST periódico

El cliente consulta el estado de la partida cada cierto intervalo.

- **Por qué NO se eligió:** introduce latencia perceptible en los turnos, genera tráfico y carga innecesarios, complica mantener un orden de turnos coherente entre clientes, y no modela bien los eventos de transición de fase. Mayor complejidad efectiva para un peor resultado de experiencia.

### Alternativa B — Server-Sent Events (SSE)

Canal unidireccional servidor→cliente sobre HTTP.

- **Por qué NO se eligió:** SSE es solo unidireccional; las acciones del jugador (registrar pista, señalar, adivinar) seguirían necesitando REST aparte, partiendo el modelo de interacción en dos mecanismos. STOMP sobre WebSocket ofrece un único canal bidireccional con semántica de suscripción por destino, mejor ajustado al dominio. SSE queda como opción válida pero menos cohesiva.

### Alternativa C — Broker de mensajería dedicado (RabbitMQ/Kafka como relay STOMP)

Externalizar el relay de mensajes a un broker.

- **Por qué NO se eligió:** viola el principio de _boring technology_ / mínima dependencia para la escala esperada (partidas de grupos pequeños en un monolito modular). El _simple broker_ en memoria de Spring es suficiente; introducir un broker externo añade operación, costo e infraestructura sin valor proporcional en esta etapa. Reevaluable si la escala lo exige en el futuro.

## Consecuencias

### Positivas

- Tecnología **nativa de Spring**, open-source y bien soportada; sin dependencias de pago.
- Canal **bidireccional único** que modela limpiamente eventos de turno/fase y acciones del jugador.
- Baja latencia y coherencia de orden, con difusión por destino de partida.
- Encaja con el monolito modular: el _simple broker_ en memoria evita infraestructura adicional.

### Negativas o trade-offs

- WebSocket añade **estado de conexión** que gestionar (reconexión, sesiones caídas, _heartbeats_); requiere manejo explícito de errores y reintentos en el cliente.
- Pruebas e integración algo más complejas que REST puro (se necesita verificar difusión y, críticamente, **ausencia de fuga de roles** en los payloads).
- El _simple broker_ en memoria **no escala horizontalmente** sin un relay externo; si en el futuro se requieren múltiples instancias del backend, habrá que migrar a un broker (RabbitMQ/Kafka) — decisión diferida conscientemente.
- Superficie de seguridad adicional: el canal debe autenticarse y autorizarse igual que REST, y filtrar deliberadamente la información sensible.

## Referencias

- `reglamento-el-infiltrado.docx` — reglas funcionales del juego (turnos secuenciales sin temporizador, fases, seguridad del rol).
- `prompt-claude-code-infiltrado.md` — prompt de desarrollo (Paso 8: gateway WebSocket/STOMP).
- Documentos del Core que aplican: `stack-tecnologico.md`, `convenciones-codigo.md`.
- Spring Framework — WebSocket / STOMP messaging (documentación oficial).

---

## Historial

| Versión | Fecha      | Cambio                                                                                                             |
| ------- | ---------- | ------------------------------------------------------------------------------------------------------------------ |
| v1.0    | 2026-06-18 | Versión inicial — decisión en estado **Propuesta**. Número ADR-NNN pendiente de asignar contra `00-index.md §8.1`. |
