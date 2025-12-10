#!/usr/bin/env zsh
# Automatiza vendorización del SDK de MagicFeedback y build+install de la app Android de ejemplo.
# Uso:
#   scripts/build_and_run_android.sh [version]
# Ejemplo:
#   scripts/build_and_run_android.sh 2.1.2-beta.2

set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")"/.. && pwd)
VERSION=${1:-2.1.2-beta.7}
GRADLEW="$ROOT_DIR/gradlew"

info() { echo "[Android] $1"; }

# 1) Vendorizar SDK navegador en shared
if [[ ! -f "$ROOT_DIR/scripts/vendor_magicfeedback.sh" ]]; then
  echo "[ERROR] No se encuentra scripts/vendor_magicfeedback.sh" && exit 1
fi
info "Vendorizando SDK navegador (@magicfeedback/native@$VERSION)"
zsh "$ROOT_DIR/scripts/vendor_magicfeedback.sh" "$VERSION"

# 2) Build example-android (esto compila shared para Android y empaqueta assets)
info "Compilando example-android (assembleDebug)"
"$GRADLEW" :example-android:assembleDebug

# 3) Install en dispositivo/emulador conectado
info "Instalando example-android (installDebug)"
"$GRADLEW" :example-android:installDebug

info "Listo. Abre la app en tu dispositivo y revisa Logcat (MagicFeedback)."
