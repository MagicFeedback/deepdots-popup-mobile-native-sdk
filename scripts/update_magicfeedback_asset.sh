#!/usr/bin/env bash
# Actualiza el asset local de @magicfeedback/native para Android e iOS.
# Uso: bash scripts/update_magicfeedback_asset.sh 2.1.2-beta.2
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "Uso: $0 <version>" >&2
  exit 1
fi
VERSION="$1"
BASE="https://cdn.jsdelivr.net/npm/@magicfeedback/native@${VERSION}/dist"
OUT_ANDROID="shared/src/androidMain/assets/magicfeedback"
OUT_IOS="iosApp/iosApp/magicfeedback"
mkdir -p "$OUT_ANDROID" "$OUT_IOS"

fetch() { curl -fsSL "$1" -o "$2"; }

FILE_BROWSER="magicfeedback-sdk.browser.js"
URL_BROWSER="$BASE/$FILE_BROWSER"

echo "Descargando $FILE_BROWSER (${VERSION})"
fetch "$URL_BROWSER" "$OUT_ANDROID/$FILE_BROWSER"
fetch "$URL_BROWSER" "$OUT_IOS/$FILE_BROWSER"

SIZE=$(wc -c < "$OUT_ANDROID/$FILE_BROWSER" || echo 0)
if [ "$SIZE" -lt 10000 ]; then
  echo "[WARN] Asset parece demasiado pequeño (size=$SIZE). Verifica versión o CDN." >&2
fi

echo "Asset actualizado en:"
echo "  - $OUT_ANDROID/$FILE_BROWSER"
echo "  - $OUT_IOS/$FILE_BROWSER"

