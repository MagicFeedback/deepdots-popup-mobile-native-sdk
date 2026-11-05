// npm test --prefix scripts/release
// DD-70: the "Survey SDK supported" line of the release notes, from the code + the registry.
// Mirror of scripts/release/survey-compat.test.mjs in the web SDK.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { checkSurveyCompat, codeFacts, compatLine, compatLineErrors, HTML_KT, SURVEY_SDK_KT } from './survey-compat.mjs';

const ROOT = fileURLToPath(new URL('../../', import.meta.url));
const registry = {
  latest: '3.2.0',
  items: [
    { version: '3.4.1', status: 'candidate', sdkVersion: '2.2.39' },
    { version: '3.4.0', status: 'stable', sdkVersion: '2.2.38' },
    { version: '3.3.0', status: 'deprecated', sdkVersion: '2.2.38' },
    { version: '3.2.0', status: 'stable', sdkVersion: '2.2.36' },
    { version: '3.0.0', status: 'stable', sdkVersion: null },
    { version: '9.9.9', status: 'unavailable', sdkVersion: '2.2.39' },
  ],
};
const ok = (body) => async () => ({ ok: true, status: 200, json: async () => body });
const sources = {
  surveySdk: 'internal val SUPPORTED_SURVEY_SDK_VERSIONS: List<String> = listOf("2.2.38", "2.2.39")',
  html: 'internal const val MAGICFEEDBACK_VERSION: String = "2.2.39"',
};

test('lee la lista y la vendorizada del codigo Kotlin real', () => {
  const facts = codeFacts({ surveySdk: readFileSync(ROOT + SURVEY_SDK_KT, 'utf8'), html: readFileSync(ROOT + HTML_KT, 'utf8') });
  assert.ok(facts.supported?.length > 0);
  assert.match(facts.bundled, /^\d+\.\d+\.\d+$/);
});

test('la linea: SDKs soportados, versiones de survey stable/candidate con esos SDKs, vendorizada', () => {
  const { line, frontends } = compatLine({ supported: ['2.2.39', '2.2.38'], bundled: '2.2.39', registry });
  assert.deepEqual(frontends, ['3.4.0', '3.4.1']);
  assert.equal(line, 'Survey SDK supported: 2.2.38–2.2.39 (survey frontend 3.4.0–3.4.1); bundled: 2.2.39.');
});

test('la seccion tiene que llevar la linea; lo que siga es libre', () => {
  const expected = 'Survey SDK supported: 2.2.38–2.2.39 (survey frontend 3.4.1); bundled: 2.2.39.';
  assert.deepEqual(compatLineErrors(`${expected} Compatibility table: docs…\n\n### Added`, expected), []);
  assert.match(compatLineErrors('### Added', expected)[0], /Falta la linea/);
  assert.match(compatLineErrors('Survey SDK supported: 2.2.38 (survey frontend 3.3.0); bundled: 2.2.39.', expected)[0], /no coincide/);
});

test('release gate: linea correcta pasa, desactualizada bloquea', async () => {
  const good = 'Survey SDK supported: 2.2.38–2.2.39 (survey frontend 3.4.0–3.4.1); bundled: 2.2.39.';
  assert.deepEqual((await checkSurveyCompat({ sources, entryBody: good, fetchImpl: ok(registry), env: {} })).errors, []);
  const stale = await checkSurveyCompat({ sources, entryBody: 'Survey SDK supported: 2.2.31 (survey frontend none); bundled: 2.2.31.', fetchImpl: ok(registry), env: {} });
  assert.match(stale.errors[0], /deberia empezar por/);
});

test('registro caido: bloquea, salvo SURVEY_COMPAT_OFFLINE=1', async () => {
  const down = async () => { throw new Error('ECONNREFUSED'); };
  assert.match((await checkSurveyCompat({ sources, entryBody: '', fetchImpl: down, env: {} })).errors[0], /No se pudo leer el registro/);
  const offline = await checkSurveyCompat({ sources, entryBody: '', fetchImpl: down, env: { SURVEY_COMPAT_OFFLINE: '1' } });
  assert.deepEqual(offline.errors, []);
  assert.match(offline.warnings[0], /sin comprobar/);
});
