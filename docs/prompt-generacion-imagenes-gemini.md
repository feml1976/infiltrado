# Prompt para generación de imágenes del banco — El Infiltrado (Gemini)

Plantilla reutilizable para generar las imágenes de las "cosas" del banco con un estilo
coherente, colorido y caricaturesco. Reemplaza `{NOMBRE_COSA}` por la cosa (en **singular
y minúscula**, igual que en el banco).

---

## Recomendación de uso

- **Opción A — nombre dentro de la imagen** (lo solicitado): un solo asset, pero el texto
  generado por IA puede salir mal escrito o deforme. Revisa siempre la ortografía del
  resultado.
- **Opción B — imagen sin texto + nombre por la app** (recomendada): genera solo la
  ilustración y deja que el frontend dibuje la franja con el nombre. Ortografía perfecta,
  tipografía y estilo consistentes, sin re-rolls. Más robusto a largo plazo.

> Sea cual sea la opción, **mantén el mismo estilo para todas las cartas** (mismo grosor de
> contorno, misma familia de paleta luminosa, mismo tipo de fondo) para que el banco se
> vea cohesivo.

---

## Opción A — Prompt con nombre incluido

```
Ilustración estilo caricatura de "{NOMBRE_COSA}" para la carta de un juego de mesa.

Estilo visual:
- Dibujo animado plano y moderno (flat cartoon / vector), con contornos negros marcados.
- Colores MUY vivos y saturados, alegres y llamativos (la imagen debe animar el juego).
- Sombreado simple tipo cel-shading; iluminación clara y luminosa.
- Apariencia amigable tipo sticker, apta para todo público.

Composición:
- El objeto "{NOMBRE_COSA}" centrado, ocupando la mayor parte del encuadre, inmediatamente
  reconocible y siendo el ÚNICO elemento protagonista (sin otros objetos que distraigan o
  confundan).
- Fondo simple: color sólido o degradado suave que contraste y haga resaltar el objeto,
  sin escenas complejas ni desorden.
- Formato cuadrado (1:1).

Texto:
- En una franja horizontal en la parte INFERIOR de la imagen, incluir únicamente la palabra
  "{NOMBRE_COSA}" en minúscula, con tipografía sans-serif gruesa, limpia y perfectamente
  legible, centrada y con alto contraste sobre la franja.
- No agregar ningún otro texto.

Evitar (negative prompt):
- Realismo fotográfico, fotografías.
- Texto adicional, frases o palabras sueltas más allá del nombre indicado.
- Letras distorsionadas, mal escritas o ilegibles.
- Marcas de agua, logos o firmas.
- Varios objetos distintos en la misma imagen.
- Fondos recargados o desordenados.
- Colores apagados, oscuros o tristes.
- Imágenes borrosas o de baja calidad.
- Contenido violento, perturbador o no apto para niños.
```

## Opción B — Prompt sin texto (la app pone el nombre)

Igual que la Opción A pero **eliminando el bloque "Texto:"** y agregando al negative prompt:
"sin ningún texto, letras ni palabras en la imagen". El frontend dibuja la franja inferior
con el nombre exacto tomado del banco.

---

## Parámetros técnicos (importante)

- **Nombre**: siempre en **singular y minúscula** (coincide con la convención del banco y
  con el índice único `lower(nombre)`).
- **Formato y tamaño**: exportar como **PNG, JPG o WEBP** (formatos aceptados por el banco),
  cuadrada (sugerido 768×768 o 1024×1024) y **comprimir a ≤ 200 KB** del binario real antes
  de cargarla — es el límite que valida el backend por magic bytes y tamaño decodificado.
- **Fondo simple**: además de verse mejor en la carta, facilita que la app recorte o
  superponga la franja del nombre (Opción B).
- **Un solo objeto**: nunca incluir objetos secundarios reconocibles; podrían confundir a
  los jugadores sobre cuál es "la cosa".
- **Consistencia**: reutiliza exactamente el mismo bloque de "Estilo visual" para todas las
  cartas; solo cambia `{NOMBRE_COSA}`.

---

## Ejemplo lleno — `{NOMBRE_COSA}` = "luna"

```
Ilustración estilo caricatura de "luna" para la carta de un juego de mesa.

Estilo visual:
- Dibujo animado plano y moderno (flat cartoon / vector), con contornos negros marcados.
- Colores MUY vivos y saturados, alegres y llamativos.
- Sombreado simple tipo cel-shading; iluminación clara y luminosa.
- Apariencia amigable tipo sticker, apta para todo público.

Composición:
- Una luna centrada, ocupando la mayor parte del encuadre, inmediatamente reconocible y
  siendo el ÚNICO elemento protagonista.
- Fondo simple: degradado suave azul noche con algunas estrellas mínimas, que haga resaltar
  la luna, sin escenas complejas.
- Formato cuadrado (1:1).

Texto:
- En una franja horizontal en la parte INFERIOR, incluir únicamente la palabra "luna" en
  minúscula, tipografía sans-serif gruesa, limpia y legible, centrada y con alto contraste.
- No agregar ningún otro texto.

Evitar: realismo fotográfico; texto adicional; letras distorsionadas o mal escritas; marcas
de agua o logos; varios objetos distintos; fondos recargados; colores apagados u oscuros;
imágenes borrosas; contenido no apto para niños.
```
