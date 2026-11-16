#!/usr/bin/env bash
# Compila el launcher firmado con la clave personal y lo instala como actualización en los
# dispositivos conectados por ADB (USB o inalámbrico).
#
#   scripts/deploy.sh            # release (daily driver, dev.kbzon.launcher) en todos los dispositivos
#   scripts/deploy.sh debug      # debug (dev.kbzon.launcher.dev), se instala junto al daily driver
#   ANDROID_SERIAL=xxx scripts/deploy.sh   # solo un dispositivo (ver `adb devices`)
set -euo pipefail

BUILD_TYPE="${1:-release}"
# JDK 21 y SDK de Homebrew (keg-only, no están en el PATH por defecto)
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"
export ANDROID_HOME="${ANDROID_HOME:-/opt/homebrew/share/android-commandlinetools}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
DIR="${SIGNING_DIR:-$HOME/.config/biblioteca}"
cd "$(dirname "$0")/.."

if [ ! -f "$DIR/release.p12" ] || [ ! -f "$DIR/signing.env" ]; then
  echo "Falta la clave de firma en $DIR; sin ella el APK no actualizaría la app instalada." >&2
  exit 1
fi

# signing.env también guarda claves de Bookloft: solo se exporta lo que necesita la firma.
# shellcheck source=/dev/null
source "$DIR/signing.env"
export KEYSTORE_PASSWORD KEY_ALIAS
export KEYSTORE_PATH="$DIR/release.p12"

case "$BUILD_TYPE" in
  release) TASK=assembleDefaultRelease; APK=app/app/build/outputs/apk/default/release/app-default-release.apk ;;
  debug)   TASK=assembleDefaultDebug;   APK=app/app/build/outputs/apk/default/debug/app-default-debug.apk ;;
  *) echo "Uso: $0 [release|debug]" >&2; exit 1 ;;
esac

./gradlew ":app:app:$TASK"

if [ -n "${ANDROID_SERIAL:-}" ]; then
  DEVICES="$ANDROID_SERIAL"
else
  DEVICES=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
fi
if [ -z "$DEVICES" ]; then
  echo "APK listo en $APK, pero no hay dispositivos conectados (adb devices)." >&2
  exit 0
fi

for d in $DEVICES; do
  echo "→ Instalando en $(adb -s "$d" shell getprop ro.product.model | tr -d '\r') ($d)"
  # -r reemplaza conservando datos; nunca desinstalar.
  adb -s "$d" install -r "$APK"
done
