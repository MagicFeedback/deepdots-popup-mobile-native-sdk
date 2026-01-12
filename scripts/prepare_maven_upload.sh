#!/bin/bash
set -e

# Carpeta de salida temporal
TMP_DIR="./maven_upload_temp"
mkdir -p "$TMP_DIR"

# Ruta de los artefactos dentro de ~/.m2/repository
ARTIFACTS_DIR="$HOME/.m2/repository/com/deepdots/sdk/shared-android/0.1.2"

ARTIFACTS=(
    "$ARTIFACTS_DIR/shared-android-0.1.2.aar"
    "$ARTIFACTS_DIR/shared-android-0.1.2-sources.jar"
    "$ARTIFACTS_DIR/shared-android-0.1.2.module"
    "$ARTIFACTS_DIR/shared-android-0.1.2.pom"
)

echo "Copiando artefactos a $TMP_DIR ..."
for file in "${ARTIFACTS[@]}"; do
    if [[ -f "$file" ]]; then
        cp "$file" "$TMP_DIR/"
    else
        echo "⚠️  Advertencia: no se encontró $file"
    fi
done

cd "$TMP_DIR"

echo "Generando checksums md5 y sha1 ..."
for f in *; do
    md5sum "$f" > "$f.md5"
    sha1sum "$f" > "$f.sha1"
done

echo "Firmando archivos con GPG ..."
# Asegúrate de exportar tu clave y contraseña antes de ejecutar el script:
# export SIGNING_KEY_ID=XXXXXXXX
# export SIGNING_PASSWORD=YYYYYYYY
for f in *; do
    if [[ -f "$f" ]]; then
        gpg --batch --yes --pinentry-mode loopback \
            --passphrase "$SIGNING_PASSWORD" \
            --default-key "$SIGNING_KEY_ID" \
            -ab "$f"
    fi
done

echo "¡Preparación completa! Archivos listos en $TMP_DIR"
