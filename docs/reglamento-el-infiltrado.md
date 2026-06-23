# El Infiltrado — Reglamento Oficial

> Juego de deducción social y roles ocultos.
> También conocido como "El Embustero" o "El Conspirador".
> Versión 1.1 · Preparado por TranserIT · 2026-06-18

Este documento es la **fuente de verdad funcional del dominio**. El desarrollo debe respetar estas reglas al pie de la letra.

---

## 1. Introducción y objetivo

El Infiltrado es un juego de mesa social de deducción y faroleo (_bluffing_) para grupos. En cada partida, todos los jugadores reciben en secreto la **misma "cosa objetivo"** (una palabra o una imagen elegida al azar), excepto uno o varios jugadores que reciben el rol de **Infiltrado** y no conocen cuál es la cosa objetivo.

Por turnos, cada jugador aporta una **característica** de la cosa objetivo. Los jugadores normales buscan demostrar sutilmente que sí conocen la cosa, sin nombrarla. El Infiltrado, que solo escucha las pistas de los demás, intenta dos cosas a la vez: deducir cuál es la cosa objetivo y mimetizarse aportando características creíbles para no ser descubierto.

Al final de la partida, todos los jugadores señalan a quienes creen que son Infiltrados, y cada Infiltrado intenta adivinar la cosa objetivo. El juego premia tanto la deducción como el engaño: hay múltiples ganadores posibles y los puntos se acumulan entre partidas sucesivas.

> **Tensión central del juego:** los jugadores normales deben dar pistas suficientemente específicas para reconocerse entre sí, pero no tan obvias que le revelen la cosa al Infiltrado.

---

## 2. Roles

### 2.1 Jugador normal (no infiltrado)

Recibe la cosa objetivo (palabra o imagen). Conoce la cosa y debe aportar características reales de ella en su turno, sin nombrarla directamente. Su meta es identificar al o los Infiltrados al cierre de la partida.

### 2.2 Infiltrado

Al destapar su carta solo ve la etiqueta "Infiltrado"; no conoce la cosa objetivo. Debe deducirla a partir de las pistas ajenas y, simultáneamente, aportar características creíbles para no ser descubierto. Su meta es doble: pasar desapercibido y adivinar la cosa objetivo.

> **Regla de independencia:** los Infiltrados juegan siempre de forma individual. No pueden asociarse, comunicarse ni coordinarse entre sí, aunque haya varios en la misma partida.

### 2.3 Moderador

Usuario que crea y parametriza la partida (número de jugadores, número de infiltrados y número de rondas) y genera el **código de sala** para que los demás se unan. El moderador es un **organizador**: no se inscribe como jugador ni recibe carta, solo controla el avance de la partida. En consecuencia, el **mínimo de 3 jugadores** se refiere a los **jugadores inscritos**, sin contar al moderador (es decir, para jugar se necesitan el moderador más 3 jugadores).

---

## 3. Componentes y conceptos

- **Cosa objetivo:** el elemento secreto de la ronda. El sistema decide al azar si se presenta como palabra o como imagen; en ambos casos es idéntica para todos los jugadores normales.
- **Carta:** lo que cada jugador destapa al inicio. Muestra la cosa objetivo (a los normales) o la etiqueta "Infiltrado" (a los infiltrados).
- **Banco de cosas:** catálogo de palabras e imágenes del que el sistema elige al azar la cosa objetivo de cada ronda. Las cosas no se repiten y se registran en singular.
- **Código de sala:** código compartido que genera el moderador al crear la partida; los jugadores lo ingresan para unirse.
- **Código individual de 4 dígitos:** código alfanumérico secreto, propio de cada jugador y por **sesión de juego**, para consultar la carta de forma privada en cualquier dispositivo. Se conserva mientras el grupo siga jugando "continuaciones"; solo cambia el rol/cosa que hay detrás (ver sección 11).
- **Pista:** una característica de la cosa objetivo que el jugador enuncia en su turno. Nunca puede ser el nombre directo de la cosa.

---

## 4. Configuración de la partida

Antes de iniciar, el moderador define los siguientes parámetros:

| Parámetro             | Descripción                                                                                                       | Valor    |
| --------------------- | ----------------------------------------------------------------------------------------------------------------- | -------- |
| Número de jugadores   | Total de participantes de la partida.                                                                             | Mín. 3   |
| Número de infiltrados | Cantidad de roles "Infiltrado". Lo fija el moderador o lo elige el sistema al azar, respetando el límite de §4.1. | Ver §4.1 |
| Número de rondas      | Cuántas rondas (cada una con una pista por jugador) se juegan antes del cierre.                                   | 2 a 5    |

> **Nota de diseño:** esta versión del juego **no usa temporizador** de turno. Los turnos son secuenciales y sin límite de tiempo, lo que permite hacer pausas durante la partida.

### 4.1 Límite de infiltrados (regla del 50%)

Los Infiltrados deben ser siempre **estrictamente menos de la mitad** de los jugadores. El número máximo se calcula como:

```
máx. infiltrados = parte entera de ( (n - 1) / 2 )
```

donde `n` es el número de jugadores. La tabla siguiente resume el límite:

| Jugadores (n) | Máx. infiltrados | Mín. jugadores normales |
| :-----------: | :--------------: | :---------------------: |
|       3       |        1         |            2            |
|       4       |        1         |            3            |
|       5       |        2         |            3            |
|       6       |        2         |            4            |
|       9       |        4         |            5            |
|      10       |        4         |            6            |

---

## 5. Preparación de la partida

1. **Creación por el moderador:** un moderador o administrador crea la partida y define los parámetros (número de jugadores, número de infiltrados dentro del límite de §4.1, y número de rondas de 2 a 5).
2. **Generación del código de sala:** al crear la partida, el sistema genera un código de sala compartido. El moderador lo comparte con el grupo.
3. **Unión de jugadores:** cada participante, desde su propio dispositivo (celular, tablet o computador), ingresa el código de sala para unirse a la partida.
4. **Selección de la cosa objetivo:** el sistema elige al azar una cosa del banco y decide al azar si se mostrará como palabra o imagen.
5. **Reparto de cartas y código individual:** a cada jugador normal se le asigna la cosa objetivo y a los infiltrados la etiqueta "Infiltrado"; además, cada jugador recibe su código individual de 4 dígitos, válido para toda la sesión de juego.
6. **Consulta privada:** cada jugador usa su código individual de 4 dígitos para destapar su carta en privado. Si no tiene dispositivo propio, puede pedir prestado uno y consultar con su código sin que el rol se filtre.

> **Dos códigos distintos:** el **código de sala** es compartido y sirve para unirse a la partida; el **código individual de 4 dígitos** es secreto de cada jugador y solo sirve para ver su propia carta.

---

## 6. Desarrollo del juego

### 6.1 Turnos y rondas

El juego se desarrolla en el número de rondas configurado. En cada ronda, todos los jugadores intervienen una vez, en orden secuencial fijo. En su turno, cada jugador enuncia una característica (pista) de la cosa objetivo. Los turnos no tienen límite de tiempo.

### 6.2 Regla de oro de las pistas

> **Prohibido nombrar la cosa:** ningún jugador puede decir directamente el nombre de la cosa objetivo. Las pistas deben ser características, no el nombre.

La aplicación de esta regla la deciden los **propios jugadores**. Cuando alguien considera que un jugador rompió la regla, puede proponer una revisión: se plantea la pregunta _"¿Considera que el jugador rompió la regla? Sí / No"_ y todos votan. **Gana la opción con más votos; un empate se resuelve a favor del jugador (no se considera falta — beneficio de la duda).** Si la votación confirma que nombró directamente la cosa, el infractor pierde **10 puntos** (sección 9). Este mismo mecanismo de votación —incluida la regla de empate— se usa para las pistas sospechosas (sección 10).

### 6.3 Comportamiento de cada rol durante las rondas

- **Jugador normal:** da características reales, calibrando entre ser reconocible para sus pares y no servirle la respuesta al Infiltrado.
- **Infiltrado:** no conoce la cosa; debe inferirla de las pistas previas y aportar una característica genérica y verosímil para no delatarse.

---

## 7. Fase de cierre: señalamiento de infiltrados

Al finalizar la última ronda, en el **mismo orden de inicio**, se pregunta a cada jugador (tanto normales como infiltrados) a quién o quiénes considera Infiltrado. Cuando todos han señalado, se revela quiénes eran realmente los Infiltrados y se aplica la puntuación de señalamiento (sección 9).

---

## 8. Fase de adivinanza del infiltrado

Tras el señalamiento, en el **mismo orden de inicio**, cada Infiltrado declara cuál cree que era la cosa objetivo. Cuando todos los Infiltrados han declarado, se revela la cosa objetivo y se determina qué Infiltrados acertaron, aplicando la puntuación correspondiente (sección 9).

> **Evaluación del acierto:** la adivinanza se compara con el nombre de la cosa de forma **normalizada** (sin distinguir mayúsculas/minúsculas, sin tildes y sin espacios sobrantes). Así "León" cuenta como acierto frente a "leon".

---

## 9. Sistema de puntuación

Pueden existir **múltiples ganadores** en una misma partida. Los puntos se otorgan así:

| Situación                                                  | Quién puntúa | Puntos |
| ---------------------------------------------------------- | :----------: | :----: |
| El Infiltrado adivina correctamente la cosa objetivo       |  Infiltrado  |  +10   |
| El Infiltrado no es descubierto (ningún jugador lo señaló) |  Infiltrado  |  +10   |
| Señala correctamente a un Infiltrado (por cada acierto)    |   Jugador    |  +10   |
| Señala como Infiltrado a quien no lo era (por cada error)  |   Jugador    |  −10   |
| Nombrar directamente la cosa (confirmado por votación, sección 6.2) | Jugador | −10 |
| Pista sospechosa improcedente (sección 10)                 |   Jugador    |   −5   |

> **Nota:** un mismo jugador puede sumar +10 por **cada** Infiltrado correctamente identificado, de modo que en partidas con varios Infiltrados los puntos por señalamiento se acumulan.
>
> **El señalamiento puntúa para todos los jugadores, infiltrados incluidos:** cualquier jugador (sea normal o infiltrado) gana +10 por cada Infiltrado que señale correctamente y pierde −10 por cada jugador no-infiltrado que señale por error. Los infiltrados juegan de forma individual, así que también pueden ganar puntos identificando a otros infiltrados.

---

## 10. Revisión de pistas sospechosas

Al finalizar la partida, si algún jugador considera que se dio una pista sospechosa (una pista que confunde o que parecía no corresponder a la cosa objetivo), la pista se presenta a todos los jugadores y se somete a **votación Sí/No, ganando la mayoría** (un **empate** se resuelve a favor del autor: no se considera falta). Si la mayoría concluye que la pista era improcedente, a su autor se le restan 5 puntos.

---

## 11. Continuación y acumulación de puntos

Al terminar las rondas configuradas, los jugadores deciden si continúan jugando. Si deciden continuar, se **reinician los roles** y se asigna una **nueva cosa objetivo**, pero los **puntos se conservan** y se siguen acumulando. El **código individual de 4 dígitos de cada jugador se conserva** durante toda la sesión: el jugador sigue usando el mismo botón/código para ver su carta, y solo cambia el rol y la cosa que hay detrás. El puntaje de un jugador corresponde a la suma de todas las partidas que ha jugado en la sesión, y se mantiene hasta que el grupo decide no jugar más.

La **puntuación global** de un usuario es la suma de los puntos de todas las partidas que ha jugado en el juego a lo largo del tiempo.

Se **admiten empates** en el ranking: no es necesario que exista un único ganador. Varios jugadores pueden compartir la primera posición.

---

## 12. Resumen de reglas y restricciones

- La partida la crea y parametriza un moderador, que genera un código de sala para que los jugadores se unan.
- Mínimo 3 jugadores por partida.
- Los Infiltrados son siempre estrictamente menos de la mitad de los jugadores (§4.1).
- Los Infiltrados juegan de forma individual: no se asocian ni coordinan.
- Está prohibido decir el nombre directo de la cosa objetivo; su cumplimiento lo deciden los jugadores por votación.
- Los turnos son secuenciales y sin temporizador.
- La cosa objetivo es idéntica para todos los jugadores normales.
- El reparto de cosa/infiltrado y la modalidad palabra/imagen se deciden al azar.
- El rol se consulta de forma privada con el código individual de 4 dígitos.
- Se admiten empates: no se fuerza un único ganador.

---

## 13. Glosario

- **Cosa objetivo:** elemento secreto de la ronda (palabra o imagen).
- **Infiltrado:** jugador que desconoce la cosa objetivo y debe deducirla sin ser descubierto.
- **Moderador:** usuario que crea, parametriza la partida y genera el código de sala.
- **Código de sala:** código compartido para unirse a una partida.
- **Código individual:** código de 4 dígitos secreto, por jugador y por sesión de juego, para ver la propia carta. Se conserva entre continuaciones; solo cambia el rol/cosa detrás.
- **Pista sospechosa:** pista que confunde o no corresponde a la cosa objetivo, sujeta a revisión por votación y posible penalización.
- **Señalamiento:** fase final en la que cada jugador indica a quién considera Infiltrado.
