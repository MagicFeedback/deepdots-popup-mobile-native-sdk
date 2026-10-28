// npm test --prefix scripts/release
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { cpSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { kotlinConst, repoFacts, spmIosMinimum, validateManifest } from './manifest.mjs';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '../..');

// El manifiesto del repo lo valida el propio gate (check.mjs); aqui solo copias que se rompen
// a proposito, para que estos tests no dependan del estado del manifiesto en cada rama.
test('una copia sin tocar del manifiesto es valida', () => {
  assert.deepEqual(sandbox(() => {}), []);
});

test('lee las versiones del codigo', () => {
  assert.equal(kotlinConst('internal const val MAGICFEEDBACK_VERSION: String = "2.2.31"', 'MAGICFEEDBACK_VERSION'), '2.2.31');
  assert.equal(spmIosMinimum('platforms: [\n  .iOS(.v13)\n]'), '13.0');
  assert.equal(spmIosMinimum('.iOS("15.4")'), '15.4');
  const facts = repoFacts({ root: ROOT });
  for (const key of ['version', 'magicfeedbackVersion', 'popupSdkCssVersion', 'minSdk', 'iosSpmMinimum', 'iosLinkerMinimum']) {
    assert.ok(facts[key], key);
  }
});

/** Copia de lo que lee validateManifest, para romperlo sin tocar el repo. */
function sandbox(mutate) {
  const dir = mkdtempSync(join(tmpdir(), 'native-manifest-'));
  for (const p of ['release', 'docs/VERSIONING.md', 'gradle.properties', 'gradle/libs.versions.toml', 'shared/build.gradle.kts', 'scripts/prepare_spm_release.sh', 'shared/src/commonMain/kotlin/com/deepdots/sdk/ui/MagicFeedbackHtml.kt']) {
    cpSync(join(ROOT, p), join(dir, p), { recursive: true });
  }
  const manifestPath = join(dir, 'release/compatibility.json');
  const manifest = JSON.parse(readFileSync(manifestPath, 'utf8'));
  mutate({ dir, manifest });
  writeFileSync(manifestPath, JSON.stringify(manifest));
  try {
    return validateManifest({ root: dir }).errors;
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

test('falta un campo obligatorio: bloquea', () => {
  const errors = sandbox(({ manifest }) => delete manifest.knownIssues);
  assert.match(errors.join(), /must have required property 'knownIssues'/);
});

test('plataforma o renderer no validos: bloquea', () => {
  const errors = sandbox(({ manifest }) => {
    manifest.platform = 'web';
    manifest.surveyRenderer.delivery.ios = 'bundled';
  });
  assert.equal(errors.length, 2);
});

test('el manifiesto dice un MAGICFEEDBACK_VERSION distinto del codigo: bloquea', () => {
  const errors = sandbox(({ manifest }) => (manifest.surveyRenderer.version = '2.2.4'));
  assert.match(errors.join(), /MAGICFEEDBACK_VERSION/);
});

test('fixture que no cumple el schema de la definicion de popup: bloquea', () => {
  const errors = sandbox(({ dir }) => writeFileSync(join(dir, 'release/fixtures/popups/minimal-event.json'), '[{"id":"x","triggers":[{"type":"hover"}]}]'));
  assert.match(errors.join('\n'), /minimal-event\.json.*surveyId/);
  assert.match(errors.join('\n'), /minimal-event\.json.*allowed values/);
});

test('fixture o schema de configuracion que no existen: bloquea', () => {
  const errors = sandbox(({ dir, manifest }) => {
    manifest.fixtures[0].path = 'release/fixtures/popups/nope.json';
    rmSync(join(dir, 'release/config.schema.json'));
  });
  assert.match(errors.join('\n'), /Falta release\/config\.schema\.json/);
});
