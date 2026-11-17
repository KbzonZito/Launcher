# DESIGN.md — estilo del launcher

Guía corta para que la home, la búsqueda, los menús, las hojas y los ajustes se vean como una sola app. Estilo: **minimalista, con tarjetas**. Ante la duda, quitar en vez de añadir.

## Principios
1. **El fondo de pantalla es el protagonista.** La home no tiene paneles ni fondos propios: texto e íconos sobre el fondo, con una sombra suave solo cuando el fondo es claro.
2. **Una tarjeta por cosa flotante.** Popups (carpeta, eventos), menú rápido y diálogos son tarjetas `surfaceContainerHigh` con radio 28 dp, sobre un velo negro al 35 % que cubre toda la pantalla.
3. **Texto antes que íconos.** La lista de apps es tipográfica; los íconos son opcionales y pequeños (30 dp).
4. **Sin sombras de elevación.** La jerarquía se marca con color de superficie y tamaño, no con sombras.
5. **Movimiento con resortes**, cortos y sin rebote exagerado.

## Color
- Se usan los roles de Material 3 del tema activo (Ajustes → Home → Tema de color). Nunca colores fijos en componentes, salvo el velo (`#000` al 35 %) y la sombra de texto.
- **Acento** de la home: el más claro o el más oscuro entre `primary` e `inversePrimary`, según si el texto va claro u oscuro sobre el fondo.
- Temas incluidos: *Predeterminado* (colores del fondo de pantalla), *Alto contraste*, *Blanco y negro*, *Grafito*, *Medianoche OLED* (superficies negras puras en modo oscuro), *Papel*, *Bosque*, *Arena*. Cada uno es una paleta de semilla fija; el editor de Kvaesitso permite crear más.

## Tipografía
- Fuente única para toda la app (Ajustes → Home → Fuente): **Manrope** (predeterminada), Inter, la del sistema o la del tema de tipografía. Las incluidas tienen licencia OFL.
- Escala de la home (en sp, multiplicada por "Tamaño del texto de la home": 0,85 · 1 · 1,15 · 1,3):

| Uso | Tamaño | Peso |
|---|---|---|
| Fila de app | 22 | Normal |
| Vista previa de notificación | 13 | Normal |
| Vistazo (evento, clima, alarma) | 14 | Medium |
| Fecha bajo el reloj | 17 | Medium |
| Reloj | 40–104 según estilo | Thin–Light (Black en "Negrita") |

- Fuera de la home se usan los estilos de `MaterialTheme.typography` (títulos de tarjeta: `titleMedium`/`titleLarge`; etiquetas: `labelMedium`).

## Forma y espaciado
- Radios: tarjetas y popups **28 dp**; píldora de toque en filas **14 dp**; íconos con la forma elegida en el tema de formas.
- Márgenes laterales de la home: **56 dp** a cada lado (ancho del alfabeto), simétricos.
- Filas: 9 dp de padding vertical, 2 dp entre filas. Popups: 20 dp de padding interior, 16–24 dp de margen a la pantalla, ancho máximo 400–420 dp.
- Botón de acción a 32 dp del borde; la lista reserva 104 dp debajo cuando el botón queda a la izquierda o al centro.

## Íconos
- Estilos: **Original**, **Monocromo** (glifo de la capa monocroma de Android 13+, sin fondo, del color del texto), **Contorno** (el glifo dentro de un contorno de 1 dp) y **Sin íconos**. Monocromo y Contorno activan los íconos temáticos forzados, así la búsqueda queda igual.
- Carpetas: mosaico 2×2 de las primeras apps dentro de la forma de ícono, siempre visible aunque los íconos estén apagados.

## Movimiento
- Toque en fila: escala 0,96 + píldora tenue, resorte `dampingRatio 0.6, stiffness 900`, vibración corta.
- Popups: aparecen con `scaleIn` desde 0,85–0,9 (`dampingRatio 0.8, stiffness 700`) + fundido; salen con `scaleOut` a 0,92–0,95.
- Aguja de minutos y anillo del reloj: resortes suaves (`stiffness 200–300`).
- Respetar siempre la animación reducida del sistema cuando se añadan animaciones largas.
