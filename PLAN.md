# Plan — Launcher personal (base: fork de Kvaesitso)

> Objetivo: un launcher estilo Niagara (lista + alfabeto), minimalista, con más gestos, más relojes/widgets,
> buen buscador invocable desde el botón lateral, integrado con las apps de calendario y clima que ya usas,
> ultra optimizado y respetuoso con la privacidad (el código es público; tus datos no salen del teléfono). Dispositivos objetivo: **Galaxy S24 Ultra** y **Galaxy Tab S9**.

---

## 0. Decisión: ¿fork o desde cero?

**Recomendación: híbrido sobre el fork.** Conservar la capa de datos/servicios de Kvaesitso y escribir **una UI de home nueva**.

| Qué | Decisión | Por qué |
|---|---|---|
| `data/*`, `services/*`, `core/*` (apps, perfiles de trabajo, icon packs, búsqueda, calendario, clima, widget host, badges, backup, preferencias) | **Reutilizar** | Son meses de trabajo resuelto y probado (work profile, icon packs, AppWidgetHost, CalendarContract, frecency de búsqueda). Reescribirlo no aporta nada visible. |
| `app/ui` home (scaffold de reloj + widgets + hoja de búsqueda) | **Reemplazar** por un módulo nuevo `:app:home` | El paradigma de Kvaesitso es distinto a Niagara. Aquí está toda la libertad de diseño. |
| Pantallas de ajustes de Kvaesitso | Reutilizar al principio, podar/rediseñar después | Funcionan; no son prioridad. |
| Módulos que no aportan (Nextcloud, ownCloud, WebDAV, Wikipedia, flavor F-Droid, Crowdin, fastlane, docs) | **Eliminar** | Menos superficie de red, APK más chico, build más rápido. |

Empezar de cero solo tendría sentido si quisiéramos otra arquitectura (p. ej. Views en vez de Compose). No es el caso: Compose + baseline profiles da 120 Hz sin problema en el S24U/Tab S9.

**Consecuencia asumida:** con el tiempo esto será un *hard fork*. Se seguirá trayendo upstream mientras sea barato (sobre todo `data/` y `services/`), sin obligarnos a ello.

---

## 1. Repositorio y forma de trabajar con el fork

> **Cambio de rumbo (16-11-2026):** se decidió un **fork total** (§10, Fase 10). Cuando se haga, las reglas de sincronización con upstream de §1.2–1.3 dejan de aplicar: no se traen más cambios de Kvaesitso y el código se renombra a `dev.kbzon.launcher`.

### 1.1 Remotos
El repo sigue siendo el fork público `KbzonZito/Launcher` (cumple la GPL‑3 sin esfuerzo extra). Solo se agrega upstream:

```bash
git remote add upstream https://github.com/MM2-0/Kvaesitso.git
git remote set-url --push upstream DISABLED   # nunca empujar a upstream por error
```
Como el repo es público: **nada sensible se commitea** (claves `.p12`/`.jks`, `signing.env`, claves de API, `local.properties`). Se agregan al `.gitignore` en la fase 0 y un hook de pre-commit bloquea `*.p12`, `*.jks` y `*.keystore`.

### 1.2 Ramas
- `main` → siempre instalable; es lo que corre en tus equipos.
- `feat/<tema>` → cada cambio va en una rama y entra por PR a `main` (squash). Permite trabajar en paralelo con worktrees.
- `upstream-sync/<tag>` → para traer upstream: `git fetch upstream --tags && git merge v1.4x.0` en esa rama, resolver conflictos y PR a `main`. **Solo tags de release**, no `main` de upstream, y como mucho una vez al mes.
- Tags propios `v0.1.0`, `v0.2.0`… en cada versión que instales.

### 1.3 Reglas para que los merges de upstream sigan siendo baratos
1. Código nuevo en **módulos nuevos** (`:app:home`, `:feature:alphabet`, `:feature:gestures`, `:feature:clocks`…), no dentro de archivos de upstream.
2. Si hay que tocar un archivo de upstream: cambio pequeño con un comentario `// [fork]` para encontrarlo con grep.
3. Borrados de módulos en commits aislados (`chore(strip): remove nextcloud`) para poder revertirlos si upstream los vuelve a tocar.
4. No renombrar el paquete Kotlin `de.mm20.launcher2` (rompería todos los merges). Solo cambia el `applicationId`.

---

## 2. Firma estable (actualizar siempre sin desinstalar)

Hoy `app/app/build.gradle.kts` firma con `signingConfigs.getByName("debug")`, o sea con `~/.android/debug.keystore`, que es distinto en cada máquina → si cambias de Mac o reinstalas, tendrías que desinstalar. Además usa sufijos `.debug`/`.release`, que en la práctica son apps distintas.

### 2.1 Reutilizar la clave de tus otras apps (mismo esquema que Bookloft / printer-hub)
Tus apps usan `scripts/setup-signing.sh`: una clave PKCS12 en `~/.config/<app>/release.p12` y la contraseña y el alias en `~/.config/<app>/signing.env`. El script copia la clave a los secretos del repo (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`) y el CI firma cada APK con ella.

En este Mac hay dos claves: `~/.config/biblioteca` (Bookloft) y `~/.config/printer-hub`. **El launcher usa la de Bookloft** (`CN=Biblioteca`, alias `biblioteca`). Ya está subida a los secretos de `KbzonZito/Launcher`.
- Se copia el mismo `scripts/setup-signing.sh` al fork, apuntando a la carpeta de esa clave y a `KbzonZito/Launcher`. El script lee la clave existente y **no crea una nueva**. Los secretos de Actions están cifrados aunque el repo sea público.
- Las contraseñas nunca pasan por el chat ni por el repo: Gradle las lee de las variables de entorno, y en local `scripts/deploy.sh` hace `source ~/.config/<app>/signing.env`.
- Firmar varias apps con la misma clave no tiene inconvenientes prácticos para uso personal. Las apps siguen siendo independientes (distinto `applicationId`).
- **Respaldo**: hoy esa clave solo existe en este Mac y en los secretos de GitHub, que no se pueden volver a descargar. Si no está ya en tu gestor de contraseñas, hay que guardarla ahí. Sin ella no se puede actualizar ninguna de las apps firmadas con esa clave.

### 2.2 Cambios en `app/app/build.gradle.kts`
- `applicationId` propio (p. ej. `dev.kbzon.launcher`) → convive con Kvaesitso oficial y no choca con él.
- Configuración de firma `personal` (tipo `PKCS12`, misma contraseña para store y key) leída de `KEYSTORE_PATH`, `KEYSTORE_PASSWORD` y `KEY_ALIAS`, igual que en tus otras apps. La usan **debug y release**.
- Workflow `.github/workflows/android.yml` adaptado de Bookloft: compila un APK firmado en cada push a `main` y publica un release por tag `v*`. Reemplaza los workflows de Kvaesitso.
- Variantes:
  - `release` → sin sufijo. Es el **daily driver**, con R8 y shrink.
  - `debug` → sufijo `.dev`, misma firma; se instala junto al daily driver para probar sin romper tu home.
  - Se eliminan `nightly` y el flavor `fdroid`.
- `versionCode` siempre creciente, calculado automáticamente: `git rev-list --count HEAD` + offset. Así nunca falla una actualización por versión menor.
- Si faltan las variables de firma, **el build de release falla**, nunca cae silenciosamente a la keystore de debug (así no se instala jamás un APK con otra firma).

### 2.3 Red de seguridad
- Instalar siempre con `adb install -r` (o un script `./scripts/deploy.sh s24|tab|all` que compile release y lo instale por ADB inalámbrico en los dos equipos).
- Exportar backup de ajustes del launcher (ya existe en `services/backup`) antes de cambios grandes.

---

## 3. Preparar el entorno (hoy no está listo en este Mac)

En este Mac no hay JDK, Android SDK ni `adb`.
1. Instalar **Android Studio** (trae JDK 21) o `brew install --cask temurin@21 android-commandlinetools`.
2. SDK: platform `android-37`, build-tools, platform-tools (`adb`).
3. En el S24U y la Tab S9: Opciones de desarrollador → Depuración inalámbrica → `adb pair` / `adb connect`.
4. Primer build sin tocar nada (`./gradlew :app:app:assembleDefaultDebug`) para confirmar que el fork compila tal cual.
5. Medir la base: tamaño del APK, tiempo de arranque en frío (`adb shell am start -W`), jank (perfetto). Esa es la referencia para el punto 8.

---

## 4. Poda (fase 1) — ✅ hecha

- Eliminados: `libs/nextcloud`, `libs/owncloud`, `libs/webdav`, `services/accounts`, `data/wikipedia`, `docs/`, `fastlane/`, `crowdin.yml`.
  Wikipedia se reemplaza por un repositorio vacío en `de.mm20.launcher2.fork.forkModule`, así `services/search` no se toca.
- Idiomas: `localeFilters = en, es` en el APK. Las traducciones **se quedan en el repo** (borrarlas generaría conflictos en cada sync de Weblate de upstream).
- Se mantienen los build types/flavors `nightly` y `fdroid`: están declarados en 6 módulos de upstream y quitarlos solo añadiría conflictos. No se usan.
- Auditoría de red en [PRIVACY.md](PRIVACY.md). Clima Met.no ahora recibe coordenadas a ~1 km.
- Resultado: APK release de 13 MB → 11 MB. Pendiente medir el arranque en frío en el S24U (requiere el dispositivo conectado).
- Revisar más adelante: `data/websites`, `data/currencies` (por ahora se desactivan por defecto en la fase 1b).

## 4b. Privacidad dentro de la app (principio de diseño, no una fase)

**Sin fugas de datos:**
- Kvaesitso no trae analytics y su crash reporter es local. Se mantiene así: **cero telemetría y cero SDKs de terceros** (sin Firebase, Play Services ni ads). Los crash logs solo se ven en el dispositivo y se comparten solo si tú lo decides.
- Se auditan todos los usos de red. Solo quedan el clima (Open‑Meteo, sin cuenta: se envían coordenadas redondeadas a ~1 km, no la ubicación exacta) y, de forma opcional, el tipo de cambio de divisas.
- Un interruptor de **modo offline** corta toda la red de la app.
- Sin sugerencias de búsqueda online. La búsqueda web solo sale del teléfono cuando la pides explícitamente, con el motor que elijas (DuckDuckGo, Startpage, Google…).
- Permisos pedidos **solo al activar la función** que los necesita (contactos, calendario, ubicación, notificaciones), con una pantalla que muestra qué permisos tiene el launcher y para qué los usa.

**Protección frente a quien tenga tu teléfono en la mano:**
- **Apps ocultas** y un **espacio privado** (aparece como la letra `🔒` del riel o con un gesto secreto), protegidos con huella/rostro (`BiometricPrompt`). Las apps ocultas no aparecen en la búsqueda, los sugeridos ni los recientes.
- Integración con la **Carpeta segura** de Samsung y el Private Space de Android 15+ (vía `LauncherApps` y perfiles; `core/profiles` ya maneja perfiles de trabajo).
- **Búsqueda incógnita**: modo que no guarda historial ni alimenta el ranking. Botón para borrar el historial y las estadísticas de uso.
- Las vistas previas de notificaciones en la lista se pueden desactivar por app y se ocultan con el teléfono bloqueado.
- `FLAG_SECURE` en la búsqueda y el espacio privado, para que no salgan en la miniatura de recientes ni en capturas.
- Backups locales **cifrados** con contraseña (en `services/backup`); nada se sube a la nube automáticamente.

---

## 5. Diseño — la home nueva

> **Estado (fase 2, ✅ MVP):** vive en `app/ui/.../ui/home/` (paquete nuevo dentro de `app/ui`, no un módulo aparte: necesita el tema, los íconos y el scaffold de `app/ui`, y los archivos nuevos no generan conflictos con upstream). Se integra como `MinimalHomeComponent` del scaffold, así los gestos y la búsqueda siguen funcionando. Ajustes → Inicio minimalista (activada por defecto, íconos sí/no, riel a la izquierda).
> Hecho: reloj y fecha (abren reloj/calendario), línea de un vistazo (próximo evento, clima, alarma), favoritos, riel con ola + háptica, soltar despliega las apps de la letra. **Fase 3 ✅:** entradas especiales del riel (★ favoritos, ◷ frecuentes, 🔒 ocultas, que pide autenticación al soltar), punto y vista previa de la última notificación por app, deslizar una app a la derecha despliega sus shortcuts, menú propio al mantener pulsado (fijar, atajos, personalizar, ocultar/mostrar, info, desinstalar).

### 5.1 Concepto visual
Minimalismo tipográfico: fondo = tu wallpaper, sin tarjetas, sin sombras. Una sola tipografía variable (p. ej. *Inter* o *Manrope*, empaquetada; no Google Fonts en runtime), jerarquía solo por tamaño/peso. Colores dinámicos de Material You con acento único. Íconos opcionales (lista solo con texto como en Niagara, o íconos monocromos).

```
┌──────────────────────────────┐
│ 10:42                        │  reloj grande (estilo intercambiable)
│ lun 16 · 18° nublado · ⏰7:00 │  "glance line": evento, clima, alarma
│ ▸ 11:00 Reunión diseño       │  próximo evento (tap → abre tu app de calendario)
│                              │
│ Teléfono               ●  A │
│ Mensajes  “Ana: ya llegué” B │  vista previa de notificación inline
│ Cámara                    C │
│ Spotify   ▶ Canción – Art.  … │  mini control de música inline
│ Maps                       Z │  ← riel del alfabeto (lado configurable)
└──────────────────────────────┘
```

### 5.2 Riel del alfabeto — donde superamos a Niagara
- Deslizar sobre el riel → las letras hacen una "ola" con lupa, háptica por letra (vibración corta del motor del S24U).
- **Comportamiento tipo Niagara: soltar nunca abre una app.** Mientras arrastras, la lista muestra en vivo las apps de la letra bajo el dedo. Al soltar, esas apps quedan **desplegadas dentro de la propia lista**, en el lugar de los favoritos, con una animación de expansión. Tocas la que quieras para abrirla.
- La letra activa queda marcada en el riel. Tocar fuera, hacer back o volver a home pliega la lista y vuelven los favoritos.
- Mejoras sobre Niagara en la lista desplegada: deslizar una app → shortcuts inline (`LauncherApps.getShortcuts`); notificaciones y badge visibles; mantener pulsado → menú (ocultar, renombrar, info, añadir a favoritos).
- Letras especiales: `★` favoritos, `◷` recientes/sugeridas por hora del día, `#` números, `🔒` privadas (con biometría).
- Riel a la izquierda o derecha (para uso con una mano y para la Tab).

### 5.3 Gestos

> **Estado (fase 5):** los gestos nuevos viven en la home minimalista (`ui/home/HomeGestures.kt`), resueltos antes que el scaffold, para no reescribir el motor de Kvaesitso. Hechos: bajar dividido (izquierda = notificaciones, derecha = ajustes rápidos), dos dedos arriba/abajo con el selector de acciones existente, deslizar una app a la izquierda abre la app asignada desde su menú (su ícono aparece en el borde mientras arrastras), a la derecha sus atajos. Los gestos del scaffold (arriba, izquierda, derecha, doble toque, mantener pulsado) siguen configurándose en Ajustes → Gestos. Pendiente: gestos desde los bordes, pellizcar, feedback visual del gesto de dos dedos.

Hoy Kvaesitso tiene 7: swipe ↑↓←→, doble tap, long press y botón home. Agregamos:
- Swipe ↓ en mitad izquierda = notificaciones, mitad derecha = ajustes rápidos (estilo One UI).
- Gestos con dos dedos (↑↓, pellizcar), swipes desde los bordes, doble tap para bloquear (vía accesibilidad, ya existe `ScreenOffComponent`).
- **Gestos por app** en la lista: swipe → sobre una app abre una app/acción secundaria (p. ej. → sobre "Teléfono" abre WhatsApp), swipe ← = info/ocultar.
- Acciones asignables: abrir app, shortcut, buscar, abrir widget/página, linterna, captura de pantalla, bloquear, panel de música, llamar a un contacto, abrir URL/intent arbitrario.
- Feedback visual del gesto mientras arrastras (no solo el resultado).

### 5.4 Búsqueda

> **Estado (fase 4):** se mantiene el motor y la UI de resultados de Kvaesitso. Agregado: prefijos `a` apps · `c` contactos · `e` eventos · `f` archivos · `s` atajos · `t` herramientas (los chips de filtro siguen al prefijo y se restauran al borrarlo); al entrar por `ASSIST` (botón lateral) el teclado se abre siempre; guía en Ajustes → Inicio minimalista → Búsqueda. Medido en emulador (release, proceso vivo): la búsqueda por `ASSIST` abre en ~30 ms. **Restyle (PR #14):** con el inicio minimalista activo, la búsqueda y los paneles usan un velo oscuro, la barra y los resultados toman un tema oscuro con filas casi transparentes (`MinimalSearchTheme`), sin importar el modo claro/oscuro del sistema. Se puede apagar en Ajustes → Inicio minimalista → Búsqueda.

- Reutilizar `services/search` (es lo mejor de Kvaesitso: apps, contactos, calendario, archivos, calculadora, conversor de unidades, shortcuts, ajustes).
- UI nueva: campo arriba, resultados agrupados, el primero se lanza con Enter, ranking por frecencia + hora del día.
- **Botón lateral**: el launcher ya declara `android.intent.action.ASSIST`, así que se puede elegir como "asistente digital" en *Ajustes → Funciones avanzadas → Botón lateral → Mantener pulsado*. Alternativa con Good Lock (RegiStar) apuntando a una actividad dedicada `SearchActivity` (abre directo el teclado, sin pasar por la home, arranque en < 150 ms).
- Atajos de búsqueda con prefijo (`g ` web, `c ` contactos, `>` comandos/acciones, `=` calculadora).

### 5.5 Relojes

> **Estado (fase 6):** `ui/home/HomeClocks.kt` con 6 estilos: fino, negrita, apilado (minutos en color de acento), en palabras (español/inglés, redondeado a 5 min), analógico minimalista (aguja de minutos con resorte) y progreso del día. Se elige en Ajustes → Inicio minimalista → Reloj y vistazo. Pendiente: flip, reloj mundial/dual (para la Tab), fuente/peso configurables, AOD.

Ya hay: Analog, Digital 1/2, Binary, Segment, Orbit, Custom. Agregar: números grandes apilados, reloj de palabras ("son las diez y cuarto"), analógico minimal de un trazo, flip, anillo de progreso del día/semana, reloj dual/mundial (útil en la Tab), y estilo "solo hora en bold + fecha en thin". Todos con fuente, peso y color configurables, y AOD-friendly.

### 5.6 Widgets y apps existentes (calendario y clima)

> **Estado (fase 6):** agenda en la línea de un vistazo (hasta 3 eventos en 36 h, primero los de todo el día; los de mañana llevan el día); tocar un evento lo abre en tu app de calendario; "El clima abre" y "La fecha abre" se eligen entre tus apps (automático por defecto). Los widgets de terceros (Samsung Weather, etc.) siguen disponibles en el panel de widgets de Kvaesitso (deslizar hacia arriba). Pendiente: widgets propios (batería de dispositivos, temporizador, notas) y widgets incrustados en la home minimalista.

- **Calendario**: `data/calendar` ya lee `CalendarContract` → funciona con Samsung Calendar y Google Calendar. Widgets propios: agenda compacta, próximo evento en la glance line, mini mes. Tocar un evento → abre **tu** app de calendario en ese evento (`ACTION_VIEW` sobre el URI del evento); el destino es configurable.
- **Clima**: los datos vienen de Open‑Meteo (sin clave, sin cuenta) o Breezy Weather (ya hay provider). Tocar el clima → abre la app de clima que elijas (Samsung Weather, Google, Breezy).
- **Widgets nativos de terceros**: el `AppWidgetHost` existente permite poner el widget de Samsung Weather/Calendar tal cual, en línea dentro de la lista o en una página de widgets.
- Widgets propios nuevos: batería de dispositivos (Galaxy Buds/Watch vía `BatteryManager` + bluetooth), temporizador/cronómetro, notas rápidas, contador de pasos (Health Connect, opcional), música.

### 5.7 Tablet (Tab S9)

> **Estado (fase 7):** `MinimalHome` decide con `BoxWithConstraints`: ≥ 600 dp de ancho y en horizontal → dos columnas (reloj y vistazo centrados a la izquierda, lista y riel a la derecha, lista hasta el 80 % del alto); ≥ 600 dp en vertical → una columna con reloj ×1.5 y lista hasta el 50 % del alto; teléfono sin cambios. Probado en el emulador Tablet (2560×1600). Pendiente: teclado físico (escribir en la home abre la búsqueda), S Pen hover, reloj dual.

- Layout adaptativo con `WindowSizeClass`: en horizontal, dos columnas (reloj + widgets | lista + riel). En vertical, lista más ancha con el riel más grande.
- Soporte de rotación, teclado físico (búsqueda escribiendo directo en la home, navegación con flechas) y S Pen hover.

---

## 6. Rendimiento y animaciones (transversal, con números)

> **Estado (fase 8):** módulo `:baselineprofile` (Macrobenchmark 1.5.0). El generador recorre la home (riel, lista de una letra, búsqueda por `ASSIST`) y deja `app/app/src/release/generated/baselineProfiles/` (≈34.600 reglas + perfil de arranque); `profileinstaller` lo aplica al instalar. Emulador (Android 16, arranque en frío de la home): 146 ms de mediana con y sin perfil, es decir, no concluyente; el beneficio real se mide en el teléfono. Para medir en el S24U (con otra app como home por defecto mientras corre):
>
> ```
> ./gradlew :app:app:generateReleaseBaselineProfile
> ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
> ```


- **Baseline profiles + módulo `:benchmark`** (Macrobenchmark): arranque en frío, abrir búsqueda, scroll del riel. Se mide en el S24U en cada PR grande.
- R8 en modo completo, `isShrinkResources`, nada de reflexión nueva.
- Compose: clases de estado estables/inmutables, `derivedStateOf`, listas con `key`, animaciones en la fase de dibujo (`graphicsLayer`, sin recomponer).
- Íconos precargados y cacheados en memoria (la lista de apps tiene que pintar el primer frame sin esperar disco).
- Animaciones con *spring* físicas, 120 Hz, interrumpibles (siempre se puede revertir un gesto a mitad de camino). Transición de lanzamiento desde el ícono/texto (ya existe `EnterHomeTransition`).
- Objetivos: arranque en frío < 300 ms, frame time p95 < 8 ms (120 Hz), 0 frames con jank al deslizar el riel.

---

## 7. Hoja de ruta

| Fase | Entregable | Estado |
|---|---|---|
| **0** | Entorno, remoto `upstream`, `.gitignore` de secretos, firma con la clave de Bookloft, `applicationId` `dev.kbzon.launcher`, `versionCode` automático, `scripts/deploy.sh`, CI | ✅ PR #1 |
| **1** | Poda de módulos (Nextcloud, ownCloud, WebDAV, Wikipedia), solo es/en, auditoría de red (`PRIVACY.md`) | ✅ PR #2 |
| **1b** | Modo offline, divisas apagadas por defecto, coordenadas a ~1 km · apps ocultas con biometría, incógnito, `FLAG_SECURE` | ✅ PR #3, #4 |
| **2** | Home minimalista: reloj, línea de un vistazo, favoritos, riel con ola + háptica | ✅ PR #5 |
| **3** | Riel ★ ◷ 🔒, notificaciones en la lista, shortcuts al deslizar, menú propio | ✅ PR #6 |
| **4** | Prefijos de búsqueda, botón lateral con teclado inmediato, guía de configuración, fix de crash de shortcuts | ✅ PR #7 (restyle visual de resultados → fase 8) |
| **5** | Bajar dividido (notificaciones / ajustes rápidos), dos dedos ↑↓ configurables, deslizar una app a la izquierda abre otra | ✅ PR #8 |
| **6** | 6 estilos de reloj (fino, negrita, apilado, en palabras, analógico, progreso del día), agenda de hasta 3 eventos, apps de clima y fecha configurables | ✅ PR #9 |
| **7** | Layout adaptativo: dos columnas en pantallas anchas en horizontal, una columna grande en la Tab en vertical, reloj escalado | ✅ PR #10 |
| **8** | Baseline profile generado (34 k reglas + perfil de arranque), módulo `:baselineprofile` con generador y Macrobenchmark | ✅ PR #11, restyle de la búsqueda en PR #14 · pendiente: medir en el S24U, pulido de animaciones |
| **9** | Correcciones rápidas: reloj abre la app completa, clima junto a la alarma, solo home minimalista, sugerencias desactivables, doble toque en el riel = bloquear, ola más marcada, riel desde ambos bordes | ✅ PR #20 |
| **10** | Fork total: código renombrado a `dev.kbzon.launcher`, sin upstream, plugins eliminados, Ko-fi, README propio, sin flavors ni traducciones extra | ✅ PR #21 |
| **11** | Home configurable: menú rápido al mantener pulsado, gestos unificados en Ajustes → Gestos, botón de acción con 7 gestos, editar diseño (reloj y alfabeto movibles) | ✅ PR #22 |
| **12** | Orden de lista por letra, carpetas (popup), reordenar favoritos | ⏳ v0.5 |
| **13** | Estilo unificado, temas, fuentes, íconos estilo Niagara, más relojes | ⏳ v0.6 |
| **14** | Búsqueda minimalista 2.0 | ⏳ v0.7 |
| **15** | Limpieza de ajustes heredados | ⏳ futuro |
| ~~continuo~~ | ~~`upstream-sync` mensual por tag~~ — cancelado por el fork total | — |

Hechos fuera de fase: mini reproductor (PR #17), backups cifrados (PR #16), apertura animada + Espacio privado en el candado + batería en el vistazo (PR #15), búsqueda minimalista (PR #14), teclado físico + borrar estadísticas + mapas offline (PR #13), identidad propia (PR #12). Pendientes: ver §9.

---

## 8. Releases

| Versión | Contenido principal |
|---|---|
| v0.1.0 | Fases 0–8: home minimalista con riel, privacidad, gestos, relojes, tablet, baseline profile |
| v0.1.1 | Identidad propia: nombre "Launcher", ícono, enlaces |
| v0.2.0 | Búsqueda minimalista, teclado físico, borrar estadísticas, mapas offline, apertura animada, Espacio privado en el candado, batería en el vistazo, backups cifrados, mini reproductor |
| v0.3.0 | Fases 9–11: correcciones del riel/reloj/clima, fork total (código propio, sin plugins, Ko-fi), home configurable (menú rápido, gestos unificados, botón de acción, editar diseño) |
| v0.3.1 | Entrega A: favoritos fijos y siempre visibles, reordenar arrastrando, lista a pantalla completa con una letra, márgenes simétricos, ola suave, burbuja junto al alfabeto, feedback al tocar |
| v0.3.2 | Entrega B: botón de acción con posición elegible y más cómodo, Editar diseño sin interferencias de gestos (reloj movible), barra de edición compacta |

Publicar: subir `versionName` en `app/app/build.gradle.kts`, mergear a `main` y crear el tag `vX.Y.Z`; el CI publica el APK firmado en GitHub Releases.

## 9. Próximas mejoras (backlog priorizado)

> Esta sección se mantiene al día en cada PR: lo hecho sale de aquí y pasa a la hoja de ruta (§7) o a las releases (§8). El detalle de cada fase está en §10.

**Siguiente: entrega C de arreglos y mejoras (§9) y luego fases 12 → 15 (§10)**, en este orden: riel y lista → personalización visual → búsqueda 2.0 → limpieza de ajustes.

**Fuera de las fases (seguir haciendo en paralelo)**
- Probar cada release en el S24 Ultra y la Tab S9 (vibración del riel, 120 Hz, dos dedos, reproductor, atajos reales).
- Medir el arranque en el S24 Ultra con `:baselineprofile` y fijar objetivos con números.
- Nombre definitivo de la app.
- Notificaciones en la lista: deslizar para descartar, tocar la vista previa para abrir la notificación.
- Widgets propios: batería de Galaxy Buds/Watch, temporizador/cronómetro, nota rápida; widgets de terceros incrustados en la home.
- Animación de cierre: la app vuelve a la fila de la que salió.
- Tablet: S Pen hover; widgets o agenda en la columna izquierda.
- Accesibilidad: etiquetas de TalkBack en el riel, el botón de acción y las entradas especiales.

### Arreglos y mejoras tras v0.3.0 — plan de implementación (17-11-2026)

Bugs reportados el 16-11 (B1–B14) y mejoras pedidas el 17-11 (M1–M3), ordenados en tres entregas. Cada entrega termina con prueba en el emulador, PR y release.

**Entrega A — Favoritos, alfabeto y lista (v0.3.1)**
> ✅ Hecha (PR #24). Causas encontradas: **B1** — Kvaesitso solo volvía a la home tras 5 s de pausa, así que al desbloquear o volver rápido de una app seguía abierta la búsqueda o el panel de widgets (sin favoritos); ahora vuelve siempre que el launcher deja de verse. Además, cada lista tiene su propio estado de scroll y el botón de inicio cierra la letra/menús. **B2** — los favoritos pasaron a orden manual (migración automática conservando el orden).
| # | Qué | Cómo |
|---|---|---|
| B2 | Favoritos con orden fijo, sin moverse por el uso | Causa: fijar guarda `pinPosition = 1` (ordenado automático, por peso de uso). Fijar como **ordenado manual** (`pinPosition > 1`) y migrar una vez los favoritos actuales conservando el orden visible. |
| B1 | Favoritos que a veces desaparecen (al desbloquear / volver de una app) | Reproducir primero. Hipótesis: la lista comparte su estado de scroll con la de letras y vuelve desplazada; la transición de `AnimatedContent` queda a medias al pausar la activity; o un fallo de lanzamiento los "resetea". Arreglo según la causa, con prueba que la cubra. |
| — | Reordenar favoritos arrastrando (adelantado de la Fase 12) | Mantener pulsado → "Reordenar"; arrastrar filas; guardar con `updateFavorites`. Complementa B2. |
| M1 | Al usar el alfabeto, las apps usan todo el espacio; el reloj y el vistazo desaparecen | Con una letra seleccionada, el bloque superior se desvanece hacia arriba y la lista ocupa todo el alto; vuelve al cerrar la letra. |
| B3 | Margen estándar a ambos lados | Margen simétrico igual al ancho del alfabeto (~56 dp) a izquierda y derecha, para deslizar cómodo en cualquier borde y poder cambiar de lado sin que nada se mueva. |
| B4 | Ola más suave | Curva más ancha y redondeada (coseno en vez de cuadrática, alcance de ~6 letras), menos pico. |
| B5 | Burbuja de la letra siempre, del lado del alfabeto | Mostrar la burbuja en todo arrastre, junto al alfabeto (a la altura del dedo), venga el gesto del borde que venga. |
| M3 | Más feedback visual al tocar una app | Al presionar: fondo tenue tipo píldora + leve reducción de escala con resorte; vibración corta al abrir. |

**Entrega B — Botón de acción y Editar diseño (v0.3.2)**
> ✅ Hecha (PR #25, apilado sobre #24). Al editar se bloquea el scaffold (`lock()`), los gestos de la home y la zona del borde contrario: así el reloj ya se mueve (B10 era consecuencia de B9). Botón: posición lado del alfabeto / contrario / centro (Ajustes → Gestos o arrastrándolo al editar; también se puede subir), a 32 dp del borde, y la lista le deja espacio cuando queda a la izquierda o al centro. Barra de edición compacta arriba.
| # | Qué | Cómo |
|---|---|---|
| B8 | Botón más adentro | Separación del borde de ~32 dp (zona cómoda del pulgar). |
| B6 | Con el alfabeto a la izquierda el botón choca con la lista | La lista reserva el espacio del botón (padding inferior) y el botón respeta los márgenes de B3. |
| B7 | Posición del botón elegible | Opciones: lado del alfabeto, lado contrario, centro; además arrastrable en Editar diseño. |
| B9 | Los gestos siguen activos al editar | En modo edición se desactivan los gestos de la home y del scaffold (deslizar, dos dedos, mantener pulsado) y el alfabeto no responde. |
| B10 | El reloj no se puede mover | Consecuencia de B9 (el scaffold se queda con el arrastre vertical); se verifica tras B9 con prueba en el emulador. |
| B11 | El aviso inferior estorba | Reemplazarlo por una barra superior compacta con "Restablecer" y "Listo" y una pista breve que se oculta sola. |

**Entrega C — Vistazo: reloj, eventos, clima, alarma y medios (v0.3.3)**
| # | Qué | Cómo |
|---|---|---|
| M2 | Próximo evento bajo el reloj; al tocarlo, popup con los siguientes | Una línea con el próximo evento; al tocarla, tarjeta flotante con los eventos de hoy y mañana (tocar uno lo abre en la app de calendario). |
| B12 | Clima | Causa: la ubicación automática está activa pero nunca se pide el permiso de ubicación, así que no hay datos. Pedir el permiso desde "Configurar clima" (o elegir ubicación manual) y mostrar el clima actual; tocar abre la app elegida en "El clima abre". Samsung Weather no comparte sus datos con otras apps: para ver *su* clima tal cual, la vía es incrustar su widget (se evalúa aquí). Breezy Weather sí comparte datos y ya está soportado. |
| B13 | Alarma de las 22:30 que no existe | Causa probable: el sistema devuelve la próxima alarma de cualquier app (p. ej. la hora de dormir de Samsung). Mostrar solo alarmas creadas por la app de reloj (`AlarmClockInfo.showIntent.creatorPackage`). |
| B14 | Apps de medios al conectar Bluetooth | Al conectarse unos auriculares o un parlante (`AudioDeviceCallback`, sin permisos extra), mostrar en el vistazo una fila con las apps de música elegidas por el usuario. |

**Después:** Fase 12 (orden de la lista por letra y carpetas) → 13 → 14 → 15, como en §10.

## 10. Plan de funciones v0.3 → v1.0

Decisiones tomadas (16-11-2026):
- **Fork total**: el código se renombra a `dev.kbzon.launcher`, se deja de traer cambios de Kvaesitso y se borra lo que no se use. Se conserva el aviso de copyright de Kvaesitso (lo exige la GPL-3.0).
- **Solo existe la home minimalista** (deja de ser una opción).
- **Botón de acción**: abajo, en la esquina del lado del riel (al alcance del pulgar).
- **Carpetas**: se abren como popup.
- **Relojes**: el cambio de estilo funciona; se agregan más tipos.

### Fase 9 — Correcciones y ajustes rápidos (v0.2.1)
> ✅ Hecha (PR #20). Ajustes nuevos en Inicio minimalista → *Alfabeto* (sugerencias por uso, doble toque para bloquear, ambos bordes, intensidad de la ola) y *Reloj y vistazo* (El reloj abre).
1. **El reloj abre la app de reloj completa**, no solo las alarmas. Nuevo ajuste "El reloj abre" (como clima y fecha): por defecto la app de reloj del sistema (Samsung Reloj); se puede elegir otra.
2. **Clima en la misma línea que la alarma**, siempre que haya datos. Si no hay proveedor o ubicación, mostrar un acceso "Configurar clima" en vez de nada (hoy simplemente no aparece).
3. **Solo home minimalista**: quitar el interruptor; los homes de Kvaesitso (reloj + widgets) dejan de ser alcanzables. La búsqueda minimalista queda siempre activa.
4. **Recomendaciones por uso desactivables**: ajuste "Sugerir apps según el uso". Apagado → desaparece ◷ del riel y no se muestran apps frecuentes en ningún lado (distinto de *Incógnito*, que además deja de registrar).
5. **Doble toque en el riel → apagar pantalla / bloquear**. Se distingue de tocar una letra: dos toques sin movimiento en < 300 ms. Usa el servicio de accesibilidad del launcher (`GLOBAL_ACTION_LOCK_SCREEN`); si no está activado, la primera vez se explica y se lleva al ajuste.
6. **Ola del riel más marcada**: la letra bajo el dedo se desplaza más hacia adentro (≈ 56 dp en vez de 36) y crece más; ajuste "Intensidad de la ola" (suave / normal / marcada).
7. **El riel funciona desde ambos bordes**: aunque las letras se vean solo a la derecha, deslizar en el borde izquierdo también recorre el alfabeto (zona táctil invisible de ~24 dp). Mientras se usa el borde contrario, aparece junto al dedo una burbuja con la letra actual y las letras visibles hacen la ola en su lado.

### Fase 10 — Fork total (v0.3.0)
> ✅ Hecha (PR #21). Notas:
> - Se conservan a propósito cuatro nombres de componente viejos para no perder nada al actualizar: alias `de.mm20.launcher2.ui.launcher.LauncherActivity` (home predeterminada) y `…ui.assistant.AssistantActivity` (asistente), y los servicios `de.mm20.launcher2.notifications.NotificationService` y `…globalactions.LauncherAccessibilityService` (permisos concedidos). Probado instalando encima de la versión anterior: home, permisos, ajustes y favoritos se mantienen.
> - Plugins: módulos, SDK y pantallas eliminados. Quedan restos inertes en la capa de datos (proveedores de plugins en calendario/contactos/archivos/lugares/clima) atados a un `NoPluginRepository` vacío; se borran en la Fase 15.
> - Sin flavors: las tareas son `assembleRelease` / `assembleDebug` y el APK sale en `app/app/build/outputs/apk/release/`.
1. **Renombrar paquetes** `de.mm20.launcher2.*` → `dev.kbzon.launcher.*` en los ~90 módulos (código, `namespace`, manifiestos, ProGuard). El `applicationId` y la firma no cambian: actualiza encima y conserva datos (base de datos, ajustes y backups usan los mismos nombres de archivo).
2. **Quitar el remoto `upstream`** y las reglas de sincronización (§1.2–1.3 pasan a historia). El fork pasa a ser un proyecto propio.
3. **Plugins de Kvaesitso**: decidir si se mantiene la compatibilidad (sus acciones y permisos se llaman `de.mm20.launcher2.*`). Recomendación: mantener los nombres del protocolo de plugins para no romper los plugins de clima, o eliminar el soporte de plugins si no los usas.
4. **Quitar todo rastro visible**: enlaces de ayuda a `kvaesitso.mm20.de`, textos, nombres de archivos, `readme.md` propio.
5. **Ko-fi** en Acerca de (y en el README).
6. **Borrar lo que no se usa**: homes y widgets de Kvaesitso que ya no se alcanzan, build types `nightly`/`fdroid`, traducciones distintas de es/en.

### Fase 11 — Pantalla principal configurable (v0.4)
> ✅ Hecha (PR #22). Notas:
> - **Gestos en un solo lugar**: Ajustes → Gestos (pantalla heredada, ahora propia) suma *Bajar dividido* (reemplaza ↓), dos dedos ↑↓ y el botón de acción. Migración única: ↓ = buscar.
> - **Mantener pulsado** un espacio vacío abre siempre el menú rápido (estilo de reloj, lado del alfabeto, ↓ buscar/dividido, íconos, botón de acción, Editar diseño, Gestos, Ajustes).
> - **Botón de acción** (abajo, lado del alfabeto). Por defecto: toque = buscar, doble toque = bloquear, mantener = ajustes rápidos, ↑ = recientes, ↓ = notificaciones, ← → = nada. Buscar y el panel de widgets se abren con la animación del scaffold.
> - **Editar diseño**: el reloj y el alfabeto se arrastran; el asa del alfabeto cambia su alto; se guarda por orientación.
> - Se corrigió el color de acento de la home (usaba un color que el tema no define).
> - Pendiente de revisar en el teléfono: la acción *Recientes* no abrió la vista de recientes en el emulador.
1. **Mantener pulsado un espacio vacío → popup de ajustes rápidos**: gestos, botón de acción, lado y posición del riel, estilo de reloj, estilo de íconos, tema; botón "Editar diseño" y botón "Ajustes". (Hoy ese gesto va al scaffold de Kvaesitso; se intercepta en la home.)
2. **Gestos configurables en la home, todos en un solo lugar**: deslizar ↑ ↓ ← →, doble toque, dos dedos ↑ ↓, pellizcar; "bajar dividido" pasa a ser una opción de deslizar ↓. Acciones: buscar, notificaciones, ajustes rápidos, bloquear, recientes, apagado, abrir app/atajo, panel de widgets, linterna, nada. Por defecto: ↓ = buscar (tu ejemplo), ↑ = panel de widgets.
3. **Botón de acción** (abajo, lado del riel): toque, doble toque, mantener y deslizar ↑ ↓ ← →, cada uno con su acción. Al arrastrar muestra hacia dónde va y vibra al cruzar el umbral. Por defecto: toque = buscar.
4. **Modo "Editar diseño"**: arrastrar verticalmente el bloque superior (reloj + vistazo) y el riel (posición y alto). Se guarda como fracción de la pantalla, por orientación (teléfono y Tab).

### Fase 12 — Riel, lista y carpetas (v0.5)
1. **Orden de la lista por letra**: alfabético, más usadas primero, recientes primero, recién instaladas primero.
2. **Carpetas (popup)**: crear desde el menú de una app ("Nueva carpeta" / "Añadir a carpeta"); se muestran en favoritos con un ícono mosaico; al tocar, popup con cuadrícula de apps, renombrar y reordenar. Se construyen sobre las **etiquetas fijables de Kvaesitso** (ya existen y se guardan en la base de datos).
3. ~~Reordenar favoritos arrastrando~~ → adelantado a la Entrega A (§9).

### Fase 13 — Personalización visual (v0.6)
> **Preferencias del usuario (16-11-2026):** fuente **Manrope** y la **fuente del sistema** como opciones; estilo **minimalista con tarjetas**. El resto queda a criterio de diseño.
1. **Estilo unificado** (`DESIGN.md`): paleta y roles de color, escala tipográfica, espaciados, radios, sombras (ninguna) y curvas de animación. Se aplica a la home, búsqueda, menús, hojas y ajustes, para que todo se vea como una sola app.
2. **Temas**: temas predeterminados (p. ej. *Grafito*, *Medianoche OLED*, *Papel*, *Bosque*, *Arena*) y editor para crear y editar. Un tema agrupa colores, forma, tipografía, transparencias y estilo de íconos. Se apoya en el editor de temas de Kvaesitso, simplificado.
3. **Fuente y tamaño**: fuentes incluidas con licencia libre (Inter, Manrope, Space Grotesk, IBM Plex Sans, la del sistema) y tamaño ajustable para la lista, el reloj y el vistazo.
4. **Íconos como Niagara**, además de lo que ya trae Kvaesitso (packs de íconos, temáticos): *Original*, *Monocromo* (capa monocroma de Android 13+, teñida con el acento), *Contorno* (glifo monocromo dentro de un contorno fino, sin relleno) y *Sin íconos*. Si una app no trae capa monocroma, se genera una a partir del ícono o se usa su inicial.
5. **Más relojes**: portar los de Kvaesitso (binario, segmentos, órbita, digital 2), y nuevos: flip, mundial/dual (útil en la Tab), matriz de puntos, vertical y anillo de agenda. Fuente del reloj configurable.

### Fase 14 — Búsqueda minimalista 2.0 (v0.7)
1. Todos los tipos de resultado como filas de lista (igual que la home), sin tarjetas, con encabezados de sección discretos.
2. Avisos de permisos compactos (una sola línea por categoría).
3. Acciones rápidas en contactos (llamar, mensaje), calculadora y conversor en línea.
4. Atajos de búsqueda propios (p. ej. `yt` → YouTube, `m` → Maps), además de los prefijos de categoría.
5. Opción de barra de búsqueda abajo (alcance del pulgar) y navegación con teclado físico (flechas + Enter).

### Fase 15 — Limpieza de ajustes (futuro)
Incluye borrar los restos de plugins de la capa de datos (`NoPluginRepository` y proveedores `Plugin*`).
Eliminar los ajustes heredados de Kvaesitso que no aplican a esta app (reloj y widgets de la home de Kvaesitso, dock, estilos de barra, etc.) y reorganizar en: Inicio · Riel y lista · Gestos y botón de acción · Búsqueda · Apariencia · Privacidad · Backup · Acerca de.

### Recomendaciones adicionales
- **Primer arranque guiado**: elegir como launcher predeterminado y pedir cada permiso (notificaciones, calendario, contactos, accesibilidad para bloquear) explicando para qué sirve.
- **Pruebas de capturas** (Roborazzi o Paparazzi) de la home y la búsqueda, para que los cambios visuales no rompan nada sin darnos cuenta; pruebas unitarias del detector de gestos y del orden de la lista.
- **Presupuesto de rendimiento**: cada fase se mide con el recorrido de Macrobenchmark; ninguna función nueva puede empeorar el arranque ni el scroll del riel.
- **Intensidad de vibración** configurable (riel, botón de acción, gestos).
- **Mantener pulsado el reloj** → temporizador rápido o la próxima alarma.
- **Ajustes por dispositivo** (teléfono / Tab) para posición del riel y diseño, ya que tienen proporciones muy distintas.

### Preguntas abiertas
- (ninguna por ahora)
