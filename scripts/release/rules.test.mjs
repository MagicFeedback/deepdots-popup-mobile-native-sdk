// npm test --prefix scripts/release
import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  checkManifestConsistency,
  checkPullRequest,
  checkRelease,
  checkReleaseEntry,
  compareVersions,
  gradleVersion,
  isSdkPath,
  parseChangelog,
  propertiesChanged,
  tomlVersions,
} from './rules.mjs';

const props = (version, extra = '') => `#Kotlin\nkotlin.code.style=official\n${extra}\nPUBLISHING_GROUP=com.deepdots.sdk\nPUBLISHING_VERSION=${version}\n`;

const CHANGELOG = (unreleased = '', top = '') => `# Changelog

## Unreleased
${unreleased}
${top}
## 0.7.3 - 2026-10-23

### Fixed

- Algo.

## 0.7.2 - 2026-10-23

- Otra cosa.
`;

const RELEASE_080 = '## 0.8.0 - 2026-10-28\n\n### Added\n\n- Nuevo.\n';

test('compareVersions: semver con prerelease', () => {
  assert.ok(compareVersions('0.8.0', '0.7.10') > 0);
  assert.ok(compareVersions('1.0.0-beta.1', '1.0.0') < 0);
  assert.equal(compareVersions('0.7.3', '0.7.3'), 0);
});

test('gradle.properties: version y cambios que no son la version', () => {
  assert.equal(gradleVersion(props('0.7.3')), '0.7.3');
  assert.equal(propertiesChanged(props('0.7.3'), props('0.8.0')), false);
  assert.equal(propertiesChanged(props('0.7.3'), props('0.7.3', 'kotlin.native.cacheKind=none')), true);
});

test('libs.versions.toml: solo la seccion [versions]', () => {
  const toml = '[versions]\nagp = "9.0.0"\nandroid-minSdk = "24"\n\n[libraries]\nfoo = { module = "x", version = "1" }\n';
  assert.deepEqual(tomlVersions(toml), { agp: '9.0.0', 'android-minSdk': '24' });
});

test('rutas del SDK: main si, tests/docs/ejemplos/scripts no', () => {
  for (const p of ['shared/src/commonMain/kotlin/A.kt', 'shared/src/iosMain/resources/x.js', 'shared/build.gradle.kts', 'gradle/libs.versions.toml']) {
    assert.ok(isSdkPath(p), p);
  }
  for (const p of ['shared/src/commonTest/kotlin/A.kt', 'shared/src/androidUnitTest/kotlin/A.kt', 'docs/x.md', 'example-android/build.gradle.kts', 'scripts/release/check.mjs', 'README.md']) {
    assert.ok(!isSdkPath(p), p);
  }
});

test('CHANGELOG nativo: "## X.Y.Z - fecha" y Unreleased', () => {
  const sections = parseChangelog(CHANGELOG('\n- Pendiente.\n'));
  assert.deepEqual(sections.map((s) => [s.version, s.date]), [['Unreleased', null], ['0.7.3', '2026-10-23'], ['0.7.2', '2026-10-23']]);
});

const pr = (overrides) =>
  checkPullRequest({
    baseBranch: 'dev',
    changedFiles: ['shared/src/commonMain/kotlin/A.kt'],
    baseProps: props('0.7.3'),
    headProps: props('0.7.3'),
    baseChangelog: CHANGELOG(),
    headChangelog: CHANGELOG('\n### Fixed\n\n- Arreglo.\n'),
    ...overrides,
  });

test('PR a dev: cambio del SDK con notas en Unreleased pasa', () => {
  assert.deepEqual(pr({}), { kind: 'change', reasons: ['shared/src/commonMain/kotlin/A.kt'], errors: [] });
});

test('PR a dev: cambio del SDK sin notas se bloquea', () => {
  const r = pr({ headChangelog: CHANGELOG() });
  assert.equal(r.kind, 'change');
  assert.match(r.errors[0], /no anade release notes/);
});

test('PR a dev: tocar solo tests o docs no pide notas', () => {
  assert.deepEqual(pr({ changedFiles: ['shared/src/commonTest/kotlin/A.kt', 'docs/x.md'], headChangelog: CHANGELOG() }).errors, []);
});

test('PR de release: bump + seccion con fecha y notas pasa', () => {
  const r = pr({ headProps: props('0.8.0'), headChangelog: CHANGELOG('', RELEASE_080) });
  assert.equal(r.kind, 'release');
  assert.deepEqual(r.errors, []);
});

test('PR de release: sin notas, sin fecha o fuera de orden se bloquea', () => {
  assert.match(pr({ headProps: props('0.8.0'), headChangelog: CHANGELOG() }).errors.join(), /no tiene seccion "## 0.8.0/);
  assert.match(pr({ headProps: props('0.8.0'), headChangelog: CHANGELOG('', '## 0.8.0 - 2026-10-28\n\n### Added\n') }).errors.join(), /vacia/);
  assert.match(pr({ headProps: props('0.8.0'), headChangelog: CHANGELOG('', '## 0.8.0\n\n- Nuevo.\n') }).errors.join(), /necesita fecha/);
});

test('PR de release: version que baja o se repite se bloquea', () => {
  assert.match(pr({ headProps: props('0.7.2'), headChangelog: CHANGELOG('', '## 0.7.2 - 2026-10-28\n\n- x\n') }).errors.join(), /baja o se repite/);
});

test('PR de release: deja contenido en Unreleased', () => {
  assert.match(pr({ headProps: props('0.8.0'), headChangelog: CHANGELOG('\n- Olvidado.\n', RELEASE_080) }).errors.join(), /Queda contenido/);
});

test('PR a main: cambio del SDK sin bump es un hotfix sin version', () => {
  assert.match(pr({ baseBranch: 'main' }).errors.join(), /hotfix/);
  assert.deepEqual(pr({ baseBranch: 'main', tagExists: true, tagMatchesHead: true }).errors, []);
});

test('version no semver se bloquea', () => {
  assert.match(pr({ headProps: props('0.8') }).errors.join(), /no es semver/);
});

test('checkReleaseEntry: un parche de linea antigua va en su sitio', () => {
  const changelog = CHANGELOG('', '') + '\n## 0.6.2 - 2026-10-08\n\n- x\n';
  const withPatch = changelog.replace('## 0.6.2', '## 0.6.3 - 2026-10-28\n\n- parche\n\n## 0.6.2');
  assert.deepEqual(checkReleaseEntry(withPatch, '0.6.3').errors, []);
});

test('checkRelease: tag, bump en su linea y latest', () => {
  const tags = ['0.1.0', 'v0.6.2', 'v0.7.2', 'v0.7.3'];
  const ok = checkRelease({ tag: 'v0.8.0', version: '0.8.0', changelog: CHANGELOG('', RELEASE_080), previousTags: tags });
  assert.deepEqual(ok, { errors: [], latest: true });
  // Re-publicar una version por debajo de lo ya publicado en su linea (el tag repetido lo para
  // check.mjs --new-tag en el dry-run, y git en el push).
  const noBump = checkRelease({ tag: 'v0.7.2', version: '0.7.2', changelog: CHANGELOG(), previousTags: tags });
  assert.match(noBump.errors.join(), /Sin bump/);
  assert.match(checkRelease({ tag: 'v0.8.1', version: '0.8.0', changelog: CHANGELOG('', RELEASE_080), previousTags: tags }).errors.join(), /no coincide/);
  const patch = checkRelease({
    tag: 'v0.6.3',
    version: '0.6.3',
    changelog: `${CHANGELOG()}\n## 0.6.3 - 2026-10-28\n\n- parche\n`,
    previousTags: tags,
  });
  assert.deepEqual(patch, { errors: [], latest: false });
});

const FACTS = {
  version: '0.7.3',
  magicfeedbackVersion: '2.2.31',
  popupSdkCssVersion: '1.10.0',
  minSdk: '24',
  compileSdk: '36',
  targetSdk: '36',
  iosSpmMinimum: '13.0',
  iosLinkerMinimum: '13.0',
  toolchain: { kotlin: '2.2.20', composeMultiplatform: '1.9.1', agp: '9.0.0' },
};

const MANIFEST = {
  version: '0.7.3',
  surveyRenderer: { version: '2.2.31' },
  popupSdkCss: { version: '1.10.0' },
  hosts: { android: { minSdk: 24, compileSdk: 36, targetSdk: 36 }, ios: { minimum: '13.0' } },
  toolchain: { kotlin: '2.2.20', composeMultiplatform: '1.9.1', agp: '9.0.0' },
  fixtures: [{ id: 'a' }, { id: 'b' }],
  knownIssues: [],
};

test('manifiesto coherente con el codigo', () => {
  assert.deepEqual(checkManifestConsistency({ manifest: MANIFEST, facts: FACTS }), []);
});

test('manifiesto que no coincide con el codigo se bloquea', () => {
  const errors = checkManifestConsistency({
    manifest: { ...MANIFEST, version: '0.8.0', surveyRenderer: { version: '2.2.4' }, hosts: { ...MANIFEST.hosts, android: { ...MANIFEST.hosts.android, minSdk: 21 } }, fixtures: [{ id: 'a' }, { id: 'a' }] },
    facts: FACTS,
  });
  assert.equal(errors.length, 4);
  assert.match(errors.join('\n'), /version es 0\.8\.0.*0\.7\.3/);
  assert.match(errors.join('\n'), /MAGICFEEDBACK_VERSION/);
  assert.match(errors.join('\n'), /minSdk es 21/);
  assert.match(errors.join('\n'), /Fixture repetido/);
});

test('si no encuentra el dato en el codigo, tambien bloquea', () => {
  assert.match(checkManifestConsistency({ manifest: MANIFEST, facts: { ...FACTS, magicfeedbackVersion: null } }).join(), /No encuentro surveyRenderer.version/);
});
