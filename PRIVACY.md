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

Excepción conocida: los mosaicos de mapa de un lugar guardado en favoritos usan su propio cargador de imágenes y no respetan el modo offline todavía.

## Pendiente
- Apps ocultas con biometría, espacio privado, búsqueda incógnita y `FLAG_SECURE` (ver PLAN.md §4b).
