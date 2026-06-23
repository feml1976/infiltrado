# Encargo — Paso 17 (Fase B): Micro-interacciones y "vida" en tiempo real

> Pegar a Claude Code. Continúa el informe de UX (`docs/informe-ux-mejoras-visuales.md`),
> sobre la base ya construida en la Fase A (app shell + tokens + color por jugador +
> responsive). Es trabajo **100% frontend** (React 19 + Vite + TS). No tocar backend/API/BD.

## Objetivo

Que la app se sienta un **juego vivo** y no un formulario: feedback en los controles,
transiciones suaves entre fases y animaciones en los eventos de tiempo real — sin sacrificar
accesibilidad, rendimiento en móvil ni la regla de no filtrar roles.

## Alcance (hacer SOLO esto en esta iteración)

### 1. Feedback en controles
- Botones e ítems interactivos con estados **hover / active / focus** (escala suave,
  elevación o glow leve usando los tokens de Fase A).
- **Focus visible y accesible** (anillo de foco) en botones e inputs.

### 2. Transiciones entre fases
- Al cambiar el estado de la partida (`LOBBY → EN_CURSO → SENALAMIENTO → ADIVINANZA →
  REVELACION → FINALIZADA`), animar la entrada/salida del contenido principal con una
  transición suave (fade/slide corto), no un cambio seco.

### 3. Indicador de turno animado
- Cuando es **el turno del propio jugador**, resaltar de forma clara (pulso/realce) el área
  de acción ("¡Es tu turno!").
- Destacar al **jugador en turno** en la lista (acento/halo), usando su color de jugador.

### 4. Llegada de jugadores al lobby
- Cuando un jugador se une (evento WS), su fila aparece con una **animación de entrada**
  (fade/slide-in), para que se note el movimiento en tiempo real.

### 5. Conteo animado de puntos
- Cuando los puntos cambian (al revelar o continuar), animar el número con un **count-up**
  y un breve realce de la fila del jugador.
- Para el count-up se permite un **hook propio mínimo** (requestAnimationFrame); **no**
  agregar una librería de animación.

### 6. Confirmación al registrar acción
- Al registrar pista / emitir voto / señalar, una micro-confirmación visual (el ítem entra
  con animación o un check breve) para dar sensación de respuesta.

## Restricciones no negociables

- 🔴 **No filtrar roles** mediante animación: todo realce sale de datos no sensibles
  (`orden_turno`, fase, "es mi turno"), **jamás del rol**. El realce de "tu turno" ocurre en
  la pantalla del propio jugador, no revela roles ajenos.
- 🔴 **Accesibilidad:** respetar **`prefers-reduced-motion`** (si está activo, reducir o
  anular las animaciones); sin parpadeos agresivos; mantener foco visible y contraste WCAG AA.
- 🟡 **CSS-first:** usar **transiciones/keyframes CSS** (animar solo `transform` y `opacity`
  para no provocar reflow). Para el count-up, hook propio mínimo; sin librerías de animación
  (si se considera una, justificar el costo).
- 🟡 **Rendimiento y responsive en móvil:** las animaciones no deben romper el layout ni
  causar jank; deben verse bien en móvil/tablet/escritorio (Fase A no debe regresar).
- 🟢 **Consistencia:** reutilizar los **tokens** de Fase A (duraciones, colores, sombras);
  centralizar duraciones/curvas como tokens (`--tr-*`), no valores sueltos por componente.
- Sin `console.log` en código productivo.

## Fuera de alcance (Fase C, NO ahora)
Pantalla de **revelación/podio celebratoria** (confeti, medallas, revelado animado de roles)
y **sonido**. Eso va en la Fase C.

## Criterios de aceptación
1. Botones e inputs con estados hover/active/**focus visible**.
2. Transición perceptible al cambiar de fase.
3. "Tu turno" claramente animado/resaltado; el jugador en turno se destaca en la lista.
4. Al unirse un jugador en el lobby, su fila entra con animación.
5. Los puntos hacen **count-up** al actualizarse, con realce breve de la fila.
6. Con **`prefers-reduced-motion` activado**, las animaciones se reducen o anulan (verificar
   en DevTools → Rendering → "Emulate prefers-reduced-motion").
7. Sin scroll horizontal ni jank en móvil; `npm run build` limpio y smoke tests verdes.
8. Ningún rol se revela antes de `REVELACION` por efecto visual alguno.

## Verificación sugerida
- Un GIF o capturas de: cambio de fase, "tu turno" resaltado, llegada de jugador al lobby y
  count-up de puntos.
- Repetir la prueba con `prefers-reduced-motion` activado para confirmar que se respeta.
