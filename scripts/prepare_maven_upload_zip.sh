#!/bin/bash
set -e

VERSION="0.1.7"
GROUP="com.deepdots.sdk"
ARTIFACT="shared-android"

GROUP_PATH=${GROUP//./\/}
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GRADLEW="$PROJECT_ROOT/gradlew"

TMP_DIR="$PROJECT_ROOT/maven_upload_temp"
ZIP_NAME="${ARTIFACT}-${VERSION}-maven-ready.zip"
M2_DIR="$HOME/.m2/repository/$GROUP_PATH/$ARTIFACT/$VERSION"

# Build and publish to local Maven if artifacts are missing
if [ ! -d "$M2_DIR" ] || [ -z "$(ls -A "$M2_DIR" 2>/dev/null)" ]; then
  echo "🚀 Publicando en Maven Local (version $VERSION)..."
  (cd "$PROJECT_ROOT" && "$GRADLEW" :shared:publishToMavenLocal)
fi

echo "🧹 Limpiando carpeta temporal..."
rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR/$GROUP_PATH/$ARTIFACT/$VERSION"

echo "📦 Copiando artefactos desde Maven Local..."
cp "$M2_DIR"/* "$TMP_DIR/$GROUP_PATH/$ARTIFACT/$VERSION/"

cd "$TMP_DIR"

echo "🧹 Eliminando firmas y checksums existentes..."
find "$GROUP_PATH/$ARTIFACT/$VERSION" \
  \( -name "*.asc" -o -name "*.md5" -o -name "*.sha1" \) \
  -type f -delete

echo "📝 Generando checksums md5 y sha1..."
for f in "$GROUP_PATH/$ARTIFACT/$VERSION"/*; do
    case "$f" in
        *.aar|*.pom|*.module|*.jar|*.asc)
            md5sum "$f" | awk '{print $1}' > "$f.md5"
            sha1sum "$f" | awk '{print $1}' > "$f.sha1"
            ;;
    esac
done

echo "🔏 Firmando archivos base con GPG..."
for f in "$GROUP_PATH/$ARTIFACT/$VERSION"/*; do
    case "$f" in
        *.aar|*.pom|*.module|*.jar)
            gpg --batch --yes --armor --detach-sign "$f"
            ;;
    esac
done

echo "📦 Empaquetando ZIP final..."
zip -r "../$ZIP_NAME" "$GROUP_PATH"

cd "$PROJECT_ROOT"

echo "✅ LISTO"
echo "➡️ Sube este archivo a Central Portal:"
echo "   $ZIP_NAME"
