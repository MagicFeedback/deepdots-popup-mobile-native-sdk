/**
 * Validacion del manifiesto de compatibilidad (release/compatibility.json) y de lo que
 * referencia: el schema de configuracion y los fixtures de popups. Lee del disco; las reglas
 * sin E/S estan en rules.mjs.
 *
 *   validateManifest({ root }) -> { manifest, errors }
 *   repoFacts({ root }) -> lo que el manifiesto tiene que repetir, sacado del codigo
 *
 * Que release/config.schema.json corresponde al codigo Kotlin lo comprueba Gradle
 * (ReleaseContractTest), que tambien pasa los fixtures por el parser real del SDK. Aqui se
 * validan los fixtures contra ese schema, para que el gate de la PR no necesite Gradle.
 */
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import Ajv from 'ajv';
import { checkManifestConsistency, gradleVersion, tomlVersions } from './rules.mjs';

export const MANIFEST_PATH = 'release/compatibility.json';
export const MANIFEST_SCHEMA_PATH = 'release/compatibility.schema.json';

const readJson = (path) => JSON.parse(readFileSync(path, 'utf8'));

function readOrNull(path) {
  try {
    return readFileSync(path, 'utf8');
  } catch {
    return null;
  }
}

/** Version que declara una constante `internal const val NAME: String = "X"` de Kotlin. */
export function kotlinConst(source, name) {
  return new RegExp(`const val ${name}\\s*:\\s*String\\s*=\\s*"([^"]+)"`).exec(source ?? '')?.[1] ?? null;
}

/** `.iOS(.v13)` → "13.0"; `.iOS("15.4")` → "15.4". */
export function spmIosMinimum(source) {
  const m = /\.iOS\(\s*(?:\.v(\d+)(?:_(\d+))?|"(\d+(?:\.\d+)?)")\s*\)/.exec(source ?? '');
  if (!m) return null;
  return m[3] ? (m[3].includes('.') ? m[3] : `${m[3]}.0`) : `${m[1]}.${m[2] ?? 0}`;
}

export function repoFacts({ root = '.' } = {}) {
  const at = (p) => readOrNull(join(root, p));
  const html = at('shared/src/commonMain/kotlin/com/deepdots/sdk/ui/MagicFeedbackHtml.kt');
  const toml = tomlVersions(at('gradle/libs.versions.toml'));
  const linker = /-mios-version-min=(\d+(?:\.\d+)?)/.exec(at('shared/build.gradle.kts') ?? '')?.[1] ?? null;
  return {
    version: gradleVersion(at('gradle.properties')),
    magicfeedbackVersion: kotlinConst(html, 'MAGICFEEDBACK_VERSION'),
    popupSdkCssVersion: kotlinConst(html, 'POPUP_SDK_CSS_VERSION'),
    minSdk: toml['android-minSdk'] ?? null,
    compileSdk: toml['android-compileSdk'] ?? null,
    targetSdk: toml['android-targetSdk'] ?? null,
    iosSpmMinimum: spmIosMinimum(at('scripts/prepare_spm_release.sh')),
    iosLinkerMinimum: linker && !linker.includes('.') ? `${linker}.0` : linker,
    toolchain: toml,
  };
}

function ajvErrors(validate, label) {
  return (validate.errors ?? []).map(
    (e) => `${label}${e.instancePath || ''} ${e.message}${e.params?.allowedValues ? ` (${e.params.allowedValues.join(', ')})` : ''}`,
  );
}

export function validateManifest({ root = '.' } = {}) {
  const errors = [];
  const at = (p) => join(root, p);

  if (!existsSync(at(MANIFEST_PATH))) return { manifest: null, errors: [`Falta ${MANIFEST_PATH}.`] };
  let manifest;
  try {
    manifest = readJson(at(MANIFEST_PATH));
  } catch (e) {
    return { manifest: null, errors: [`${MANIFEST_PATH} no es JSON valido: ${e.message}`] };
  }

  const ajv = new Ajv({ allErrors: true, strict: false });
  const validate = ajv.compile(readJson(at(MANIFEST_SCHEMA_PATH)));
  if (!validate(manifest)) {
    errors.push(...ajvErrors(validate, MANIFEST_PATH));
    // Con la forma rota, lo demas daria errores en cascada.
    return { manifest, errors };
  }

  errors.push(...checkManifestConsistency({ manifest, facts: repoFacts({ root }) }));
  if (!existsSync(at(manifest.supportPolicy))) errors.push(`supportPolicy apunta a ${manifest.supportPolicy}, que no existe.`);

  let configSchema = null;
  try {
    configSchema = readJson(at(manifest.configSchema.path));
  } catch (e) {
    errors.push(
      existsSync(at(manifest.configSchema.path))
        ? `${manifest.configSchema.path} no es JSON valido: ${e.message}`
        : `Falta ${manifest.configSchema.path}. Generalo con: UPDATE_RELEASE_SCHEMA=1 ./gradlew :shared:testDebugUnitTest --tests '*ReleaseContractTest*'`,
    );
  }

  // Fixtures: cada uno existe y es una respuesta valida de GET /sdk/popups segun el schema.
  if (configSchema && !configSchema.definitions?.PopupDefinition) {
    errors.push(`${manifest.configSchema.path} no tiene definitions.PopupDefinition.`);
  } else if (configSchema) {
    const validatePopups = ajv.compile({
      definitions: configSchema.definitions,
      type: 'array',
      minItems: 1,
      items: { $ref: '#/definitions/PopupDefinition' },
    });
    for (const fixture of manifest.fixtures) {
      if (!existsSync(at(fixture.path))) {
        errors.push(`El fixture ${fixture.id} apunta a ${fixture.path}, que no existe.`);
        continue;
      }
      let data;
      try {
        data = readJson(at(fixture.path));
      } catch (e) {
        errors.push(`${fixture.path} no es JSON valido: ${e.message}`);
        continue;
      }
      if (!validatePopups(data)) errors.push(...ajvErrors(validatePopups, fixture.path));
    }
  }

  return { manifest, errors };
}
