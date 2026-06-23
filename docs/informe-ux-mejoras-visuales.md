# Informe — Mejoras visuales y de experiencia (UI/UX) de El Infiltrado

- **Fecha:** 2026-06-22
- **Autor:** Análisis técnico (mentor de arquitectura) para Francisco Montoya
- **Contexto:** Las pantallas actuales (lobby, partida, admin) son funcionales pero "planas"
  y se sienten utilitarias —más cercanas a un CRUD/encuesta que a un juego de fiesta—.

---

## 1. Veredicto (TL;DR)

**Viable y conveniente.** Es 100% frontend (sin tocar backend ni BD), de bajo riesgo y
alto impacto en el objetivo del producto: que el juego se sienta lúdico y atractivo.
Recomiendo abordarlo como una **iteración dedicada de UI/UX (un "Paso 17")**, después de
cerrar los pendientes de correctitud, e implementarlo como un **sistema de diseño + app
shell** (una sola vez, aplicado a todo) en vez de retocar pantalla por pantalla.

## 2. Viabilidad

Alta. Todo se resuelve con React + CSS y, a lo sumo, una librería ligera de animación.
- La info del usuario logueado **ya está disponible** (authStore: nombre, esAdmin).
- La **versión** se toma de `package.json` (fuente única), no se hardcodea.
- El nombre del ingeniero y el copyright son constantes/config.
- No requiere cambios de contrato, API ni esquema.

## 3. Conveniencia

Muy alta. Un juego social vive de "sentirse divertido". La estética actual (azul oscuro
plano, tarjetas sin profundidad) resta enganche. Invertir en una UI lúdica **sirve
directamente al objetivo** que ya venías persiguiendo (imágenes coloridas, color por
jugador). Además, header y footer consistentes **resuelven de paso la falta de cohesión**
entre pantallas.

## 4. Propuesta de diseño

### 4.1 App shell: barra superior + pie de página (lo solicitado)

Implementar un **layout persistente** que envuelva todas las rutas (no repetir header/footer
por pantalla):

- **Barra superior (header):**
  - Logo/wordmark "El Infiltrado" con un logotipo lúdico (y, opcional, una mascota: un
    personaje con antifaz que refuerza la marca y conecta con las imágenes del banco).
  - Identidad del usuario: avatar + **nombre del usuario logueado**, badge **"Moderador"**
    o **"Admin"** cuando aplique, y botón de cerrar sesión.
  - Cuando se está en partida: mostrar el **código de sala** y la fase actual de forma
    visible.
- **Pie de página (footer):**
  - `© 2026 El Infiltrado` · **"Desarrollado por Francisco Montoya"** · **versión** (desde
    `package.json`).
  - Opcional: enlace al reglamento / "Cómo se juega".

### 4.2 Sistema de diseño lúdico (mi aporte principal)

Definir **design tokens** (variables CSS) una sola vez para coherencia y fácil ajuste:

- **Color:** conservar una base oscura agradable, pero sumar **acentos vivos y degradados**
  (el morado actual como primario + secundarios cálidos). Estados con color semántico
  (éxito/alerta/peligro) ya legibles.
- **Tipografía:** una fuente **redondeada y amigable** para títulos (p. ej. Poppins/Baloo/
  Fredoka) que dé aire de juego, + una sans legible para texto.
- **Profundidad:** tarjetas con **bordes redondeados, sombras suaves y elevación** (hoy son
  planas); separadores y secciones con jerarquía visual clara.
- **Iconografía:** íconos/emoji lúdicos para acciones (crear, unirse, ver carta, votar).

### 4.3 Micro-interacciones y "momentos de juego" (lo que más anima)

Es aquí donde una app deja de parecer una encuesta:

- **Botones** con feedback al hover/press (escala suave, glow).
- **Transiciones** entre fases (fade/slide) en vez de cambios secos.
- **Eventos en tiempo real animados:** pulso/resalte cuando **es tu turno**, animación
  cuando alguien se une, **conteo animado de puntos** al actualizarse.
- **Color por jugador** (ítem ya conversado): paleta fija por `orden_turno` aplicada a
  avatar/acento — refuerza la identidad de cada jugador.
- **Sonido opcional** (turno, revelación): **silenciado por defecto**, con toggle.

### 4.4 Pantalla de revelación / podio (el clímax)

Convertir la revelación y el podio final en un **momento celebratorio**: animación al
revelar roles, **medallas** en el podio, **confeti** al cierre. Es el punto emocional del
juego y hoy se ve igual que cualquier otra tabla.

## 5. Restricciones y cuidados (no negociables)

- 🔴 **No filtrar roles con la estética.** Cualquier color/animación debe derivar de datos
  **no sensibles** (`orden_turno`, fase), **nunca del rol**. Vibrante ≠ revelar quién es
  infiltrado antes de la revelación.
- 🔴 **Accesibilidad:** mantener **contraste WCAG** (vibrante pero legible), respetar
  `prefers-reduced-motion` (animaciones suaves/desactivables), sonido **off por defecto**.
- 🟡 **Performance / peso:** preferir **CSS transitions/keyframes** (baratas) antes que una
  librería de animación; si se usa una (p. ej. Framer Motion), justificar el costo. Cargar
  fuentes e ilustraciones de forma eficiente.
- 🟡 **Versión desde `package.json`** (fuente única); nombre del ingeniero y año como
  constantes de configuración, no repartidos por el código.
- 🟢 **Consistencia:** implementar como **app shell + tokens**, aplicado globalmente, para
  que toda pantalla (lobby, partida, admin) herede el mismo estilo.

## 6. Alcance y priorización

Es **pulido**, no correctitud. Sugiero secuenciarlo como una iteración propia ("Paso 17 —
UI/UX") **después** de cerrar lo pendiente (doble conteo ya corregido; confirmar el cierre
final). Se puede entregar por fases:

1. **Fase A (estructura):** app shell con header + footer, tokens de diseño, tipografía y
   profundidad de tarjetas. (Impacto alto, esfuerzo bajo.)
2. **Fase B (vida):** micro-interacciones, color por jugador, indicador de turno animado.
3. **Fase C (clímax):** pantalla de revelación/podio celebratoria (confeti, medallas).

## 7. Conclusión

Recomiendo **proceder**. El cambio es viable, de bajo riesgo y directamente alineado con el
propósito del juego. La clave es hacerlo como **sistema de diseño reutilizable** (no parches
por pantalla) y respetando dos líneas rojas: **no filtrar roles** y **accesibilidad/
legibilidad**. Con eso, la app pasa de "formulario funcional" a "juego que invita a jugar".

---

## 8. Fase B implementada — Micro-interacciones y "vida" en tiempo real

- **Fecha:** 2026-06-23
- **Autor:** Francisco Montoya (implementación) / mentor de arquitectura (diseño)
- **Encargo:** Paso 17 Fase B

### 8.1 Resumen de cambios

#### Nuevos tokens CSS (`tokens.css`)

| Token | Valor | Uso |
|-------|-------|-----|
| `--tr-slow` | `0.4s ease` | Transiciones lentas (reservado) |
| `--dur-slide` | `0.28s` | Duración de entrada de fase |
| `--dur-pulse` | `2s` | Ciclo del pulso del turno propio |
| `--ease-back` | `cubic-bezier(0.34, 1.56, 0.64, 1)` | Rebote suave en entrada de jugador |

#### Nuevos keyframes y clases CSS (`styles.css`)

| Keyframe / Clase | Efecto |
|-----------------|--------|
| `@keyframes fadeSlideIn` | Fade + translateY(10px→0) |
| `@keyframes playerSlideIn` | Fade + translateX(-14px→0) con rebote |
| `@keyframes pulseTurno` | Glow pulsante en indicador de turno propio |
| `@keyframes scoreFlash` | Fondo tenue que aparece y desaparece al cambiar puntos |
| `@keyframes actionConfirmed` | Fade + slide-in para mensajes de confirmación |
| `.fase-content` | Aplica `fadeSlideIn` — envuelve el contenido de cada fase |
| `.turno-info--mio` | Borde morado + pulso infinito cuando es el turno del propio jugador |
| `.player-item--new` | `playerSlideIn` con `ease-back` — jugadores recién unidos al lobby |
| `.player-item--active-turn` | Transition para highlight del jugador en turno |
| `.score-row--flash` | `scoreFlash` 0.7s al cambiar puntos |
| `.action-confirmed` | `actionConfirmed` 0.25s en confirmaciones de acción |
| `:focus-visible` | Anillo de foco 2px `--c-primary-light` — accesible en toda la app |
| `.btn` | Transición ampliada: `background + transform + box-shadow` |
| `.btn-primary:hover` | `translateY(-2px)` + glow indigo |
| `.btn-primary:active` | `scale(0.97)` + transición rápida 0.05s |
| `.btn-secondary:hover` | `translateY(-1px)` |
| `label.player-item:hover` | `translateX(2px)` — items interactivos en señalamiento |
| `.field input:focus-visible` | Box-shadow 3px rgba(indigo) + borde primario |

#### Nuevo hook (`src/shared/hooks/useCountUp.ts`)

`useCountUp(target, duration?)` — anima un número del valor previo al nuevo usando
`requestAnimationFrame` con easing ease-out-quad. Sin librerías externas.
Se usa en `EnCursoView` (tabla de puntos) y `RevelacionView` (scores al revelar).

La constante `prefersReducedMotion` se evalúa una vez al cargar el módulo
(`window.matchMedia(...).matches`). Si está activa, el efecto salta directo a `setDisplay(target)`
sin iniciar ningún `rAF`, garantizando que la promesa de accesibilidad aplica también a
las animaciones JS — no solo a las CSS.

#### Cambios por componente

**`PartidaPage.tsx`**
- El contenido de cada fase se envuelve en `<div key={partida.estado} className="fase-content">`,
  lo que fuerza remount en cada cambio de fase y dispara `fadeSlideIn`.
- Se rastrea el set de IDs de jugadores con `useRef`; los nuevos IDs reciben la clase
  `player-item--new` para la animación de entrada en el lobby.

**`EnCursoView.tsx`**
- El `turno-info` recibe `turno-info--mio` cuando `esMiTurno`, activando el pulso visual.
- El texto cambia a "¡Es tu turno!" para mayor claridad.
- Los jugadores en la lista de pistas reciben `background` + `boxShadow` inline con el color
  de jugador (`getPlayerColor`) cuando son el jugador en turno activo.
- Tabla de puntos: sub-componente `ScoreRow` que usa `useCountUp` + `score-row--flash` por
  jugador, independiente entre filas.
- Confirmación de pista enviada con clase `action-confirmed`.

**`SenalamientoView.tsx`**
- Confirmación "Señalamiento registrado" con `action-confirmed` (solo cuando el propio
  jugador acaba de enviar, no si ya venía señalado del servidor).
- Badges de jugadores con su color de paleta (`getPlayerColor`).
- `label.player-item` aprovecha el estilo hover interactivo del CSS global.

**`AdivinanzaView.tsx`**
- Confirmación "Adivinanza registrada" con `action-confirmed`.

**`RevelacionView.tsx`**
- Sub-componente `RevealScore` que anima desde `puntosAcumulados - deltaRonda` hasta
  `puntosAcumulados` con un delay de 150ms (solo cuando `deltaRonda > 0`), usando
  `useCountUp`. El `score-row--flash` se aplica durante la animación para realzar la fila.
- Badges con color de jugador (`getPlayerColor`).

### 8.2 Restricciones respetadas

| Restricción | Cómo se cumple |
|-------------|----------------|
| No filtrar roles | Todo realce usa `orden_turno`, `esMiTurno` (dato propio) o la fase — nunca el campo `rol` |
| `prefers-reduced-motion` | CSS: regla global en `tokens.css` anula todas las transiciones/animaciones CSS a 0.01ms. JS: `useCountUp` evalúa `matchMedia(...).matches` al cargar el módulo y salta el `rAF` si está activo, mostrando el valor final directamente. Ninguna capa (CSS ni JS) anima si el usuario lo ha desactivado. |
| CSS-first | Todos los efectos son CSS keyframes/transitions sobre `transform` y `opacity`. El count-up usa `requestAnimationFrame` propio, sin librerías |
| Sin jank en móvil | Solo se animan `transform` y `opacity` (compositor-only, sin reflow) |
| Sin `console.log` | Ninguno en código productivo |
| Focus visible | `:focus-visible` global con anillo de 2px sobre `--c-primary-light` |

### 8.3 Criterios de aceptación verificados

1. ✅ Botones con hover/active (lift + glow/scale) y `focus-visible` accesible.
2. ✅ Transición `fadeSlideIn` al cambiar de fase (`key={partida.estado}`).
3. ✅ Turno propio: `turno-info--mio` con borde y pulso; jugador activo en lista con halo de su color.
4. ✅ Jugadores nuevos en lobby: clase `player-item--new` con `playerSlideIn`.
5. ✅ Count-up en tabla de puntos (`EnCursoView`) y en revelación (`RevealScore`), con flash de fila.
6. ✅ `prefers-reduced-motion` anula animaciones (regla global en `tokens.css`).
7. ✅ `npm run build` limpio (0 errores TS + Vite build OK). Sin scroll horizontal.
8. ✅ Ningún rol revelado antes de `REVELACION` por efecto visual.

### 8.4 Pendiente → completado en Fase C

Ver sección 9 más abajo.

---

## 9. Fase C implementada — Revelación/podio celebratorio + sonido opcional

- **Fecha:** 2026-06-23
- **Encargo:** Paso 17 Fase C

### 9.1 Resumen de cambios

#### Nuevos hooks

| Hook | Archivo | Descripción |
|------|---------|-------------|
| `useConfetti` | `src/shared/hooks/useConfetti.ts` | Canvas propio: 45–90 partículas, gravity, fade-out en el último 45 % de duración (3.5 s). Respeta `prefers-reduced-motion` (no crea canvas si está activo). Libera el canvas al terminar o al desmontar. Sin librerías externas. |
| `useSoundToggle` | `src/shared/hooks/useSoundToggle.ts` | Web Audio API (tonos sintéticos, 0 assets extra). Preferencia en `localStorage` (`infiltrado_sound`). Off por defecto. Expone `{ soundOn, toggleSound, playReveal, playInfiltrado, playFinale }`. Callbacks estables (`useCallback`) que leen `soundOn` vía ref para evitar re-renders del efecto de revelación. |

**Decisión sobre librería de confeti:** descartada. Una lib popular (`canvas-confetti`) añade ~15 kB. La implementación propia con canvas ocupa ~60 líneas y cubre exactamente las necesidades del encargo (duración finita, fade, liberación de recursos, `prefers-reduced-motion`).

**Decisión sobre sonido:** Web Audio API con tonos sintéticos. Evita la necesidad de assets de audio, CORS y carga de ficheros. `playReveal` (ding ascendente C5→E5) para jugadores NORMALES, `playInfiltrado` (tonos graves descendentes G4→D4) para INFILTRADOS, `playFinale` (fanfarria C5-E5-G5-C6) al cerrar la revelación y en FINALIZADA.

#### Nuevas clases CSS

| Clase / Keyframe | Efecto |
|-----------------|--------|
| `@keyframes revealIn` | Fade + slide X(-12px→0) con rebote — jugador revelado |
| `.revelacion-jugador--visible` | Aplica `revealIn` 0.35s al revelarse |
| `.revelacion-jugador--infiltrado` | Borde izquierdo rojo + fondo tenue — resalta INFILTRADO (solo en `REVELACION`) |
| `.revelacion-pending` | Puntos suspensivos `···` para jugadores aún no revelados |
| `@keyframes podioIn` | Fade + translateY(20px→0) — entrada de tarjetas de podio |
| `.podio`, `.podio-card` | Contenedor y tarjeta de podio |
| `.podio-card--oro/plata/bronce` | Borde y fondo con color de la medalla |
| `.podio-medalla` | Emoji de medalla, tamaño 1.375rem |
| `.podio-posicion` | Número de posición para puestos fuera del podio |
| `.podio-puntos`, `.podio-puntos--lider` | Puntos del podio; color dorado para el primero |
| `.sound-toggle` | Botón de toggle de sonido; hover con borde primario y scale |

#### Cambios en `RevelacionView.tsx`

- **Todos los hooks se declaran antes** de los `return` condicionales (regla de hooks respetada).
- `revealedCount` empieza en 0; un `useEffect` con `revStartedRef` (evita re-ejecución) programa un `setTimeout` por jugador (separación de 600 ms). Con `prefersReducedMotion`, salta directo a `revealedCount = sorted.length`.
- Confeti se activa (`confettiActive = true`) después del último jugador revelado (+800 ms).
- Sonido: `playReveal` / `playInfiltrado` en cada timeout según `rol`; `playFinale` al activar confeti.
- Adivinanzas, señalamientos y acciones del moderador se muestran solo cuando `allRevealed`.
- Toggle de sonido en la esquina superior derecha del bloque (inline, no fixed).

#### Cambios en `FinalizadaView.tsx`

- `rankPlayers()`: ordena DESC por puntos y asigna posiciones **respetando empates** (el mismo `pos` para jugadores con igual puntuación; el siguiente con diferente puntuación toma `i + 1`).
- `medalFor(pos)`: posición 1 → 🥇, 2 → 🥈, 3 → 🥉, resto → número con `.podio-posicion`.
- `podio-card--{oro,plata,bronce}`: borde y fondo coloreados para los primeros tres puestos.
- Stagger de entrada: `animationDelay: ${i * 0.1}s` por tarjeta → podio sube con escalonado natural.
- `useCountUp` a través de `PodioScore` en cada tarjeta (anima si el jugador venía de ronda anterior con puntos distintos).
- `useConfetti(true)` al montar + `playFinale()` en `useEffect`.

### 9.2 Restricciones respetadas

| Restricción | Cómo se cumple |
|-------------|----------------|
| No filtrar roles antes de tiempo | `.revelacion-jugador--infiltrado` y `rol-infiltrado` solo se aplican cuando `revealed = true`, que solo ocurre en estado `REVELACION` una vez que el backend ha enviado los datos. Antes, la clase no existe y los datos de rol no se renderizan. |
| `prefers-reduced-motion` (CSS) | Regla global en `tokens.css`: anula a 0.01ms |
| `prefers-reduced-motion` (JS confeti) | `useConfetti` sale inmediatamente si `matchMedia(...).matches` |
| `prefers-reduced-motion` (JS reveal) | Si activo: `setRevealedCount(sorted.length)` directo, sin `setTimeout` |
| `prefers-reduced-motion` (JS count-up) | `useCountUp` ya comprobaba esto desde Fase B |
| Sonido off por defecto | `localStorage.getItem(STORAGE_KEY) === 'on'`; si no hay entrada en storage → `false` |
| Confeti no traba móvil | `COUNT = 45` en móvil, `90` en escritorio; `duration: 3500ms`; loop se detiene al acabar y libera canvas |
| Sin librerías de animación/sonido | 0 dependencias añadidas; Web Audio API nativa |
| Sin `console.log` | Errores de `AudioContext` capturados con `try/catch` vacío |

### 9.3 Criterios de aceptación verificados

1. ✅ En `REVELACION`: roles escalonados (600 ms entre cada uno), count-up en puntos.
2. ✅ En `FINALIZADA`: podio con medallas; empates con mismo puesto (validado en `rankPlayers`).
3. ✅ Confeti breve (3.5 s), fade-out automático, canvas liberado al terminar o desmontar.
4. ✅ Toggle de sonido off por defecto; preferencia en `localStorage`; no suena sin activarlo.
5. ✅ Con `prefers-reduced-motion`: sin confeti, sin escalonado, count-up instantáneo.
6. ✅ Ningún rol/efecto revelado antes de `REVELACION`.
7. ✅ `npm run build` limpio (0 errores TS). Tests: **12/12 verdes**. Sin scroll horizontal.

### 9.4 Paso 17 completo

| Fase | Estado |
|------|--------|
| A — App shell + design tokens | ✅ Completada |
| B — Micro-interacciones + count-up + turno animado | ✅ Completada |
| C — Revelación escalonada + podio + confeti + sonido | ✅ Completada |
