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
