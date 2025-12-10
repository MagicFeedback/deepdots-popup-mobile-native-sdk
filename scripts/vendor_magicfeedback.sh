#!/usr/bin/env zsh
# Vendoriza @magicfeedback/native en el módulo shared para Android e iOS.
# Descarga el bundle de navegador (magicfeedback-sdk.browser.js) y lo copia a:
#  - shared/src/androidMain/assets/magicfeedback/magicfeedback-sdk.browser.js
#  - shared/src/iosMain/resources/magicfeedback/magicfeedback-sdk.browser.js
# Uso: scripts/vendor_magicfeedback.sh [version]
# Ejemplo: scripts/vendor_magicfeedback.sh 2.1.2-beta.2

set -euo pipefail
VERSION=${1:-2.1.2-beta.7}
MIN_SIZE=70000 # ~70KB mínimo esperado
ROOT_DIR=$(cd "$(dirname "$0")"/.. && pwd)
ANDROID_DEST="$ROOT_DIR/shared/src/androidMain/assets/magicfeedback/magicfeedback-sdk.browser.js"
IOS_DEST="$ROOT_DIR/shared/src/iosMain/resources/magicfeedback/magicfeedback-sdk.browser.js"
TMP_FILE="$ROOT_DIR/.tmp_magicfeedback_browser.js"

mkdir -p "$(dirname "$ANDROID_DEST")"
mkdir -p "$ROOT_DIR/shared/src/iosMain/resources/magicfeedback"

urls=(
  "https://cdn.jsdelivr.net/npm/@magicfeedback/native@${VERSION}/dist/magicfeedback-sdk.browser.js"
  "https://unpkg.com/@magicfeedback/native@${VERSION}/dist/magicfeedback-sdk.browser.js"
)

echo "[Vendor] Descargando navegador @magicfeedback/native version=${VERSION}"
rm -f "$TMP_FILE"
for u in $urls; do
  echo "[Vendor] Intentando $u"
  if curl -L --fail --retry 3 --retry-delay 1 -A "DeepdotsPopupSDK/1.0 (vendor)" "$u" -o "$TMP_FILE"; then
    SIZE=$(wc -c < "$TMP_FILE")
    echo "[Vendor] Tamaño descargado=$SIZE bytes"
    if [[ "$SIZE" -ge "$MIN_SIZE" ]]; then
      break
    else
      echo "[Vendor] Archivo demasiado pequeño (<$MIN_SIZE), probando siguiente URL"
      rm -f "$TMP_FILE"
    fi
  else
    echo "[Vendor] Error descargando desde $u, probando siguiente"
  fi
done

if [[ ! -f "$TMP_FILE" ]]; then
  echo "[Vendor] ERROR: No se pudo descargar el bundle de navegador válido"
  exit 1
fi

cp "$TMP_FILE" "$ANDROID_DEST"
cp "$TMP_FILE" "$IOS_DEST"
rm -f "$TMP_FILE"
echo "[Vendor] Copiado a:"
echo "  Android: $ANDROID_DEST"
echo "  iOS    : $IOS_DEST"

ASIZE=$(wc -c < "$ANDROID_DEST")
ISIZE=$(wc -c < "$IOS_DEST")
echo "[Vendor] Verificación tamaños -> Android=$ASIZE, iOS=$ISIZE"
if [[ "$ASIZE" -lt "$MIN_SIZE" || "$ISIZE" -lt "$MIN_SIZE" ]]; then
  echo "[Vendor] WARNING: tamaños inferiores al mínimo esperado, revisa conectividad/CDN"
fi

echo "[Vendor] Listo. Recuerda commitear estos archivos para que shared lleve el SDK embebido."
