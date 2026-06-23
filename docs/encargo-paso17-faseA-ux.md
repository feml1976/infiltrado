# Encargo — Paso 17 (Fase A): App shell + sistema de diseño + responsive

> Pegar a Claude Code. Alcance acotado a **Fase A** del informe de UX
> (`docs/informe-ux-mejoras-visuales.md`) + **color por jugador** + **diseño responsive**.
> Es trabajo 100% frontend (React 19 + Vite + TS). No tocar backend, API ni BD.

## Objetivo

Quitar la sensación "plana / de formulario" de la app dándole una estructura consistente y
una base visual lúdica, **sin sacrificar legibilidad ni filtrar roles**, y garantizando que
se vea bien en **escritorio, tablet y celular**.

## Alcance (hacer SOLO esto en esta iteración)

### 1. App shell (layout persistente)

Crear un layout que envuelva todas las rutas (no repetir header/footer por pantalla):

- **Header (barra superior):**
  - Marca "El Infiltrado" (logotipo/wordmark).
  - Usuario autenticado: nombre + avatar; badge **"Moderador"** o **"Admin"** cuando aplique;
    botón de **cerrar sesión**.
  - En partida: mostrar el **código de sala** y la **fase actual** de forma visible.
  - En `/login` y `/registro`: versión reducida del header (solo la marca) o sin header.
- **Footer (pie de página), en todas las rutas:**
  - `© 2026 El Infiltrado` · **"Desarrollado por Feml"** · **versión vX.Y.Z**.
  - La versión se toma de `package.json` (importar `version`), **no hardcodear**.
  - Nombre del ingeniero y año como **constante de configuración** (un solo lugar).

### 2. Sistema de diseño (design tokens)

Centralizar en variables CSS (un solo archivo de tokens) y aplicarlas globalmente:

- **Color:** base oscura agradable + **acentos vivos/degradados** (morado primario actual +
  secundarios). Colores semánticos (éxito/alerta/peligro) legibles.
- **Tipografía:** fuente **redondeada y amigable** para títulos (p. ej. Poppins/Baloo/Fredoka)
  - sans legible para cuerpo.
- **Profundidad:** tarjetas con **bordes redondeados, sombra suave y elevación** (hoy son
  planas), jerarquía visual clara.
- Aplicar estos tokens a los componentes existentes (botones, tarjetas, tablas) — no crear
  estilos sueltos por pantalla.

### 3. Color por jugador

- Asignar un **color de acento por jugador** desde una **paleta fija indexada por
  `orden_turno`** (consistente en todas las ventanas/dispositivos).
- Aplicarlo al **avatar/borde** del jugador en lobby y partida.
- 🔴 **Derivar el color de `orden_turno`, NUNCA del rol** (no debe revelar quién es infiltrado).

### 4. Diseño responsive (requisito transversal)

- Enfoque **mobile-first**; breakpoints para **móvil (~360–480px), tablet (~768px) y
  escritorio (≥1024px)**.
- Layout fluido: **sin scroll horizontal** en ningún ancho; contenedores que se adapten.
- **Targets táctiles ≥ 44px**; tipografía y espaciados legibles en móvil.
- Header/footer adaptables (colapsar a menú/hamburguesa si no cabe).
- La **tabla del admin** (`/admin/cosas`) debe ser usable en móvil (scroll contenido o
  reflow a tarjetas), nunca desbordar la pantalla.
- Verificar en los 3 tamaños antes de cerrar.

## Restricciones no negociables

- 🔴 **No filtrar roles** mediante estética: cualquier color/realce sale de datos no
  sensibles (`orden_turno`, fase), jamás del rol.
- 🔴 **Accesibilidad:** contraste **WCAG AA**; respetar `prefers-reduced-motion`; nada de
  parpadeos agresivos.
- 🟡 **CSS-first:** usar CSS (variables, transitions) — **sin** librerías de animación en
  esta fase.
- 🟡 **Versión desde `package.json`**; nombre/copyright como constante única.
- 🟢 **Consistencia:** app shell + tokens globales; no estilos ad-hoc por pantalla.
- Sin `console.log` en código productivo.

## Fuera de alcance (Fases B y C, NO ahora)

Micro-interacciones avanzadas, indicador de turno animado, conteo animado de puntos,
pantalla de revelación/podio celebratoria (confeti/medallas) y sonido. Eso va después.

## Criterios de aceptación

1. Header y footer aparecen de forma consistente en lobby, partida y admin (login/registro
   con header reducido o sin header).
2. El footer muestra **"Desarrollado por Francisco Montoya"** y la **versión leída de
   `package.json`** (al subir la versión en `package.json`, el footer cambia solo).
3. Tokens de diseño centralizados; tarjetas con profundidad y tipografía lúdica aplicadas
   globalmente.
4. Color por jugador por `orden_turno`, visible en lobby y partida, **sin** derivar del rol.
5. **Responsive verificado** en móvil, tablet y escritorio: sin scroll horizontal, tabla de
   admin usable en móvil, targets táctiles adecuados.
6. Ningún rol se revela antes de `REVELACION` por efecto visual alguno.
7. `npm run build` limpio y los smoke tests siguen verdes; agregar al menos un test/render
   que verifique que el footer muestra la versión y el nombre.

## Verificación sugerida

Capturas en tres anchos (móvil ~390px, tablet ~768px, escritorio ~1280px) de lobby, partida
y admin, más el footer con nombre + versión.
