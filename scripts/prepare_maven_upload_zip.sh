#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GRADLEW="$PROJECT_ROOT/gradlew"
GRADLE_PROPERTIES="$PROJECT_ROOT/gradle.properties"
LOCAL_GRADLE_PROPERTIES="$PROJECT_ROOT/.gradle/gradle.properties"
GROUP="${PUBLISHING_GROUP:-$(grep '^PUBLISHING_GROUP=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)}"
VERSION="${1:-${PUBLISHING_VERSION:-$(grep '^PUBLISHING_VERSION=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)}}"
ARTIFACT="${ARTIFACT_ID:-shared-android}"
GROUP_PATH="$(printf '%s' "$GROUP" | tr '.' '/')"
STAGING_REPO="$PROJECT_ROOT/build/release-staging-repo"
STAGED_ARTIFACT_DIR="$STAGING_REPO/$GROUP_PATH/$ARTIFACT/$VERSION"
TMP_DIR="$PROJECT_ROOT/maven_upload_temp"
ZIP_NAME="${ARTIFACT}-${VERSION}-maven-ready.zip"
ZIP_PATH="$PROJECT_ROOT/$ZIP_NAME"
GPG_TEMP_HOME=""
GPG_PASSPHRASE_VALUE=""
GPG_KEY_ID_VALUE=""
GPG_KEY_FILE_VALUE=""

cleanup() {
  if [[ -n "$GPG_TEMP_HOME" && -d "$GPG_TEMP_HOME" ]]; then
    rm -rf "$GPG_TEMP_HOME"
  fi
}

trap cleanup EXIT

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $1" >&2
    exit 1
  fi
}

hash_md5() {
  if command -v md5sum >/dev/null 2>&1; then
    md5sum "$1" | awk '{print $1}'
  else
    md5 -q "$1"
  fi
}

hash_sha1() {
  if command -v sha1sum >/dev/null 2>&1; then
    sha1sum "$1" | awk '{print $1}'
  else
    shasum -a 1 "$1" | awk '{print $1}'
  fi
}

write_checksums() {
  local file="$1"
  hash_md5 "$file" > "$file.md5"
  hash_sha1 "$file" > "$file.sha1"
}

read_local_property() {
  local key="$1"
  python3 - "$LOCAL_GRADLE_PROPERTIES" "$key" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
key = sys.argv[2]
if not path.is_file():
    raise SystemExit(0)

for line in path.read_text().splitlines():
    s = line.strip()
    if not s or s.startswith('#') or '=' not in s:
        continue
    k, v = s.split('=', 1)
    if k.strip() == key:
        print(v.strip())
        raise SystemExit(0)
PY
}

prepare_gpg_signing() {
  GPG_KEY_FILE_VALUE="${SIGNING_SECRET_KEY_RING_FILE:-$(read_local_property 'signing.secretKeyRingFile')}"
  GPG_PASSPHRASE_VALUE="${GPG_PASSPHRASE:-${SIGNING_PASSWORD:-$(read_local_property 'signing.password')}}"
  GPG_KEY_ID_VALUE="${SIGNING_KEY_ID:-$(read_local_property 'signing.keyId')}"

  if [[ -n "$GPG_KEY_FILE_VALUE" ]]; then
    if [[ ! -f "$GPG_KEY_FILE_VALUE" ]]; then
      echo "[ERROR] Signing key file not found: $GPG_KEY_FILE_VALUE" >&2
      exit 4
    fi
    GPG_TEMP_HOME="$(mktemp -d "${TMPDIR:-/tmp}/deepdots-gpg.XXXXXX")"
    chmod 700 "$GPG_TEMP_HOME"
    gpg --homedir "$GPG_TEMP_HOME" --batch --import "$GPG_KEY_FILE_VALUE" >/dev/null 2>&1
  fi
}

gpg_sign() {
  local file="$1"
  local args=(--batch --yes --armor --pinentry-mode loopback --detach-sign)
  if [[ -n "$GPG_TEMP_HOME" ]]; then
    args+=(--homedir "$GPG_TEMP_HOME")
  fi
  if [[ -n "$GPG_KEY_ID_VALUE" ]]; then
    args+=(--local-user "$GPG_KEY_ID_VALUE")
  fi
  if [[ -n "$GPG_PASSPHRASE_VALUE" ]]; then
    args+=(--passphrase "$GPG_PASSPHRASE_VALUE")
  fi
  gpg "${args[@]}" "$file"
}

require_command zip
require_command gpg

echo "[maven] Preparing Android release bundle for version $VERSION"
echo "[maven] Publishing Android publication to local staging repo"
rm -rf "$STAGING_REPO"
(cd "$PROJECT_ROOT" && "$GRADLEW" :shared:publishAndroidReleasePublicationToReleaseStagingRepository "-PPUBLISHING_VERSION=$VERSION" -PskipGradleSigning=true)

if [[ ! -d "$STAGED_ARTIFACT_DIR" ]]; then
  echo "[ERROR] Staged artifact directory not found: $STAGED_ARTIFACT_DIR" >&2
  exit 2
fi

echo "[maven] Copying staged artifacts into temporary upload folder"
rm -rf "$TMP_DIR" "$ZIP_PATH"
mkdir -p "$TMP_DIR/$GROUP_PATH/$ARTIFACT/$VERSION"
cp "$STAGED_ARTIFACT_DIR"/* "$TMP_DIR/$GROUP_PATH/$ARTIFACT/$VERSION/"

TARGET_DIR="$TMP_DIR/$GROUP_PATH/$ARTIFACT/$VERSION"
find "$TARGET_DIR" \( -name "*.md5" -o -name "*.sha1" \) -type f -delete

shopt -s nullglob
BASE_FILES=("$TARGET_DIR"/*.aar "$TARGET_DIR"/*.pom "$TARGET_DIR"/*.module "$TARGET_DIR"/*.jar)
if [[ ${#BASE_FILES[@]} -eq 0 ]]; then
  echo "[ERROR] No base artifacts were generated in $TARGET_DIR" >&2
  exit 3
fi

SIGNATURE_FILES=("$TARGET_DIR"/*.asc)
if [[ ${#SIGNATURE_FILES[@]} -eq 0 ]]; then
  prepare_gpg_signing
  echo "[maven] No Gradle signatures found, signing artifacts with GPG"
  for file in "${BASE_FILES[@]}"; do
    gpg_sign "$file"
  done
  SIGNATURE_FILES=("$TARGET_DIR"/*.asc)
else
  echo "[maven] Reusing Gradle-generated signatures"
fi

echo "[maven] Writing checksums for artifacts and signatures"
SIGNED_AND_BASE_FILES=("${BASE_FILES[@]}" "${SIGNATURE_FILES[@]}")
for file in "${SIGNED_AND_BASE_FILES[@]}"; do
  write_checksums "$file"
done
shopt -u nullglob

echo "[maven] Packaging upload zip"
(
  cd "$TMP_DIR"
  zip -rq "$ZIP_PATH" "$GROUP_PATH"
)

echo "[maven] Ready: $ZIP_PATH"
echo "[maven] Upload this zip to Sonatype Central Portal"
