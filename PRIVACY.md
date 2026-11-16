# Privacidad — auditoría de red

El launcher no tiene telemetría, analytics ni SDKs de terceros (sin Firebase, Play Services ni ads).
El crash reporter es local. Esta tabla lista **todo** lo que puede salir del teléfono tras la fase 1.

| Función | Módulo | Destino | Qué se envía | Por defecto | Estado / plan |
|---|---|---|---|---|---|
| Clima (Met.no) | `data/weather` | `api.met.no` | Coordenadas **redondeadas a 2 decimales (~1 km)** + user agent con el paquete | Activo (proveedor por defecto) | ✅ Redondeo aplicado en fase 1 |
| Clima (Bright Sky / OpenWeatherMap) | `data/weather` | `api.brightsky.dev`, `api.openweathermap.org` | Coordenadas a ~1 km (+ API key propia en OWM) | Solo si se elige | ✅ Redondeo aplicado en fase 1b |
| Clima (Breezy Weather / plugins) | `data/weather` | Ninguno (lee de la app local) | — | Solo si se elige | ✅ Opción más privada |
| Tipo de cambio | `data/currencies` | `ecb.europa.eu` | Nada personal (descarga un XML público cada hora) | ✅ Desactivado (fase 1b) | Se activa en ajustes del conversor |
| Previsualización de sitios web | `data/websites` | El sitio que escribes | La URL escrita en la búsqueda | Solo con el filtro "online" de la búsqueda, que viene apagado | Se apaga con el modo offline |
| Lugares cercanos (OSM) | `data/locations` | `overpass-api.de`, `tile.openstreetmap.org` | Texto buscado + zona alrededor de tu ubicación (radio 1,5 km) | Solo con el filtro "online" (apagado por defecto) y permiso de ubicación | Se apaga con el modo offline |
| Íconos/imágenes remotas | `app/ui` (Coil) | Varios (mapas, favicons de sitios) | URL de la imagen | Solo cuando se muestra el resultado | Se apaga con el modo offline |
| Búsqueda web | `data/search-actions` | El motor elegido | El texto, **solo al pulsar** la acción | Solo bajo demanda | ✅ Sin sugerencias online |

Eliminado en la fase 1: **Nextcloud, ownCloud, WebDAV, cuentas** y **Wikipedia** (enviaba cada búsqueda a `wikipedia.org`).

## Modo offline (fase 1b) ✅
Ajustes → Privacidad → **Modo offline**. Con él activo:
- La búsqueda ignora el filtro "online" (sin sitios web, lugares ni plugins online).
- No se actualiza el clima de Met.no, Bright Sky ni OpenWeatherMap (Breezy Weather y los plugins siguen, porque son locales).
- No se descarga el tipo de cambio.
- Las imágenes remotas solo se muestran si ya estaban en caché.

Los mosaicos de mapa también respetan el modo offline (solo caché).

## Protección en el dispositivo (fase 1b) ✅
Ajustes → Privacidad → *En este dispositivo*:
- **Bloquear apps ocultas**: pide huella, rostro o PIN del dispositivo antes de mostrarlas, ya sea desde el filtro de la búsqueda, el botón de ocultas o Ajustes → Apps ocultas. Desactivar la protección también pide autenticación. Se vuelve a bloquear cada vez que el launcher deja de verse (abrir una app, apagar la pantalla).
- **Incógnito**: no registra aperturas; las apps frecuentes y el orden de la búsqueda dejan de aprender.
- **Borrar estadísticas de uso**: pone a cero lo aprendido (favoritos fijados y ocultas se mantienen).
- **Proteger la búsqueda de capturas** (activado por defecto): `FLAG_SECURE` mientras hay texto en la búsqueda, el filtro de ocultas activo o la hoja de ocultas abierta. La home normal sí se puede capturar.

## Backups cifrados ✅
Al crear un backup se puede poner una contraseña (opcional): el archivo se cifra con AES-256-GCM, clave derivada con PBKDF2-SHA256 (210 000 iteraciones) y sal/IV aleatorios. Al restaurar, se pide la contraseña; una incorrecta o un archivo alterado se rechazan (GCM verifica la integridad). Sin contraseña no hay forma de recuperarlo.

## Pendiente
- Espacio privado propio y, más adelante, integración con la Carpeta segura de Samsung / Private Space de Android.
