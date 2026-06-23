# Encargo — Paso 17 (Fase C): Revelación/podio celebratorio + sonido opcional

> Pegar a Claude Code. Cierra el informe de UX (`docs/informe-ux-mejoras-visuales.md`, §4.4),
> sobre la base de las Fases A y B ya entregadas. Trabajo **100% frontend** (React 19 + Vite +
> TS). No tocar backend/API/BD.

## Objetivo

Convertir el **final de la partida** (revelación y podio) en un **momento celebratorio** —el
punto emocional del juego— manteniendo accesibilidad, rendimiento en móvil y la regla de no
filtrar roles antes de tiempo.

## Alcance (hacer SOLO esto en esta iteración)

### 1. Revelación animada de roles
- Al entrar en `REVELACION`, revelar los roles de forma **escalonada** (uno tras otro, con
  un pequeño retardo entre cada uno) en vez de mostrarlos todos de golpe — crea tensión.
- Realce del **Infiltrado** al revelarse (sin filtrarlo antes: la animación solo ocurre **en
  estado `REVELACION`**, cuando ya es público).
- Reutilizar el **count-up de Fase B** para los puntos del cierre.

### 2. Podio en `FINALIZADA`
- Pantalla de podio con **medallas** (oro/plata/bronce) para los primeros lugares.
- **Empates permitidos**: si hay empate en una posición, mostrar a todos los empatados en ese
  puesto (no forzar un único ganador) — coherente con el reglamento.
- Entrada animada del podio (las barras/tarjetas suben a su posición).

### 3. Confeti al cierre
- Animación de **confeti** al llegar a `REVELACION`/`FINALIZADA` (un toque festivo, breve).
- Implementación ligera (canvas propio o CSS); si se considera una librería de confeti,
  **justificar el costo** y que sea pequeña y sin dependencias pesadas.

### 4. Sonido opcional
- Efectos de sonido breves para momentos clave (p. ej. revelación, fin de partida).
- 🔴 **Silenciado por defecto**, con un **toggle** visible para activarlo; la preferencia se
  recuerda (p. ej. en `localStorage`/`sessionStorage`). Nunca reproducir audio sin que el
  usuario lo haya activado.

## Restricciones no negociables

- 🔴 **No filtrar roles antes de tiempo:** toda la animación de revelación ocurre **solo** en
  estado `REVELACION`/`FINALIZADA`. Antes de eso, ningún efecto puede insinuar quién es el
  infiltrado.
- 🔴 **Accesibilidad:** respetar **`prefers-reduced-motion`** — con movimiento reducido, el
  confeti y las animaciones escalonadas se **anulan o se reducen al mínimo** (mostrar el
  resultado directo). El **count-up** ya respeta esta preferencia (Fase B); el confeti debe
  hacerlo también. Sonido **off por defecto**. Sin parpadeos agresivos.
- 🟡 **Rendimiento/móvil:** el confeti no debe trabar la UI en celular (limitar partículas y
  duración; usar `transform`/`opacity`; detener el bucle al terminar). Si usas canvas, libera
  el recurso al desmontar.
- 🟡 **Peso:** preferir solución propia ligera; cualquier librería (confeti/sonido) debe
  justificarse y ser pequeña.
- 🟢 **Consistencia:** reutilizar los **tokens** y patrones de A/B (colores por jugador,
  duraciones `--dur-*`/`--tr-*`, easings).
- Sin `console.log` en código productivo.

## Criterios de aceptación
1. En `REVELACION`, los roles se revelan de forma escalonada y los puntos hacen count-up.
2. En `FINALIZADA`, hay podio con medallas que respeta **empates**.
3. Confeti breve al cierre, que **se detiene** solo (no queda corriendo) y no traba en móvil.
4. Toggle de sonido **off por defecto**, con preferencia recordada; no suena sin activarlo.
5. Con **`prefers-reduced-motion` activado**: sin confeti ni escalonado (resultado directo),
   y el count-up instantáneo (ya lo hace).
6. Ningún rol/efecto se revela antes de `REVELACION`.
7. Sin scroll horizontal ni jank en móvil; `npm run build` limpio y smoke tests verdes.

## Verificación sugerida
- GIF o capturas de: revelación escalonada, podio con medallas (incluido un caso con empate)
  y el confeti.
- Repetir con `prefers-reduced-motion` activado para confirmar que se anula el movimiento.
- Probar el toggle de sonido (off por defecto → activar → suena; recargar → recuerda).
