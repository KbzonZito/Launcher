#!/usr/bin/env bash
# Sube a los secretos de GitHub la clave de firma que ya usa Bookloft, para que CI firme el launcher
# con ella y cada APK se instale como actualización del anterior.
#
#   scripts/setup-signing.sh [dueño/repo]
#
# A diferencia del script de Bookloft, este NUNCA crea una clave nueva: si no existe, se detiene.
# Respalda ~/.config/biblioteca fuera del Mac (gestor de contraseñas): sin la clave no se puede
# actualizar ninguna app firmada con ella.
set -euo pipefail

REPO="${1:-KbzonZito/Launcher}"
DIR="${SIGNING_DIR:-$HOME/.config/biblioteca}"

if [ ! -f "$DIR/release.p12" ] || [ ! -f "$DIR/signing.env" ]; then
  echo "No encuentro $DIR/release.p12 o $DIR/signing.env. No se crea una clave nueva a propósito." >&2
  exit 1
fi

# shellcheck source=/dev/null
source "$DIR/signing.env"
base64 < "$DIR/release.p12" | gh secret set KEYSTORE_BASE64 -R "$REPO"
gh secret set KEYSTORE_PASSWORD -R "$REPO" --body "$KEYSTORE_PASSWORD"
gh secret set KEY_ALIAS -R "$REPO" --body "$KEY_ALIAS"
echo "Secretos de firma configurados en $REPO."
