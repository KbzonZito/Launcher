# Launcher

Launcher minimalista para Android, pensado para el Galaxy S24 Ultra y la Galaxy Tab S9.

- **Inicio minimalista**: reloj, línea de un vistazo (próximo evento, clima, alarma, batería, música) y favoritos.
- **Riel del alfabeto** con ola y vibración por letra; soltar despliega las apps de esa letra. Funciona desde ambos bordes; doble toque para apagar la pantalla.
- **Gestos**: bajar dividido (notificaciones / ajustes rápidos), dos dedos, deslizar una app para abrir otra o ver sus atajos.
- **Búsqueda** con prefijos (`a` apps, `c` contactos, `e` eventos, `f` archivos, `s` atajos, `t` herramientas), invocable desde el botón lateral.
- **Privacidad**: sin telemetría, modo offline, apps ocultas con biometría, incógnito, búsqueda protegida de capturas, backups cifrados. Detalle en [PRIVACY.md](PRIVACY.md).
- **Tablet**: diseño a dos columnas en horizontal y teclado físico.

## Instalar

Descarga el APK de la última versión en [Releases](https://github.com/KbzonZito/Launcher/releases) e instálalo. Cada versión se instala encima de la anterior conservando la configuración.

## Compilar

```bash
scripts/deploy.sh          # release firmado, instalado en los dispositivos conectados por ADB
scripts/deploy.sh debug    # variante de pruebas (se instala aparte)
```

La firma usa la clave personal descrita en [PLAN.md](PLAN.md) §2. El plan de trabajo y las próximas mejoras también están ahí.

## Apoyar

Si te sirve, puedes invitarme un café en [Ko-fi](https://ko-fi.com/kbzonzito).

## Licencia y créditos

GPL-3.0 (ver [LICENSE.txt](LICENSE.txt)).

Este proyecto nació como fork de [Kvaesitso](https://github.com/MM2-0/Kvaesitso), de MM2-0 y sus colaboradores, también GPL-3.0. Gracias por su trabajo.
