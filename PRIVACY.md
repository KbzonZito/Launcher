# Privacidad — auditoría de red

El launcher no tiene telemetría, analytics ni SDKs de terceros (sin Firebase, Play Services ni ads).
El crash reporter es local. Esta tabla lista **todo** lo que puede salir del teléfono tras la fase 1.

| Función | Módulo | Destino | Qué se envía | Por defecto | Estado / plan |
|---|---|---|---|---|---|
| Clima (Met.no) | `data/weather` | `api.met.no` | Coordenadas **redondeadas a 2 decimales (~1 km)** + user agent con el paquete | Activo (proveedor por defecto) | ✅ Redondeo aplicado en fase 1 |
| Clima (Bright Sky / OpenWeatherMap) | `data/weather` | `api.brightsky.dev`, `api.openweathermap.org` | Coordenadas (+ API key propia en OWM) | Solo si se elige | Fase 1b: aplicar el mismo redondeo |
| Clima (Breezy Weather / plugins) | `data/weather` | Ninguno (lee de la app local) | — | Solo si se elige | ✅ Opción más privada |
| Tipo de cambio | `data/currencies` | `ecb.europa.eu` | Nada personal (descarga un XML público) | Activo con el conversor de divisas | Fase 1b: desactivar por defecto |
| Previsualización de sitios web | `data/websites` | El sitio que escribes | La URL escrita en la búsqueda | **Activo** (`websiteSearchEnabled`) | Fase 1b: desactivar por defecto |
| Lugares cercanos (OSM) | `data/locations` | `overpass-api.de`, `tile.openstreetmap.org` | Texto buscado + zona alrededor de tu ubicación (radio 1,5 km) | Desactivado (`locationSearchEnabled = false`) | Mantener desactivado |
| Íconos/imágenes remotas | `app/ui` (Coil) | Varios (mapas, favicons de sitios) | URL de la imagen | Solo cuando se muestra el resultado | Se apaga con el modo offline |
| Búsqueda web | `data/search-actions` | El motor elegido | El texto, **solo al pulsar** la acción | Solo bajo demanda | ✅ Sin sugerencias online |

Eliminado en la fase 1: **Nextcloud, ownCloud, WebDAV, cuentas** y **Wikipedia** (enviaba cada búsqueda a `wikipedia.org`).

## Pendiente (fase 1b)
- Interruptor global **modo offline**: `allowNetwork = false` en todas las búsquedas, sin refrescar el clima ni el tipo de cambio y sin imágenes remotas.
- Desactivar por defecto la previsualización de sitios web y el tipo de cambio.
- Redondeo de coordenadas en el resto de proveedores de clima.
- Apps ocultas con biometría, espacio privado, búsqueda incógnita y `FLAG_SECURE` (ver PLAN.md §4b).
