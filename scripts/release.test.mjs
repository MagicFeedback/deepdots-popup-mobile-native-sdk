// node --test scripts/release.test.mjs
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { gradleVersion, isGreater, nextVersion, promoteUnreleased, unreleasedBody, versionBody } from './release.mjs';

const CHANGELOG = `# Changelog

## Unreleased

### Fixed

- Algo arreglado.

## 0.6.0 - 2026-10-05

### Added

- Algo nuevo.

## 0.1.2 - 2025-01-01

- Ultima.
`;

test('lee el cuerpo de "## Unreleased"', () => {
  assert.equal(unreleasedBody(CHANGELOG), '### Fixed\n\n- Algo arreglado.');
});

test('"## Unreleased" vacia devuelve null', () => {
  assert.equal(unreleasedBody('# C\n\n## Unreleased\n\n## 0.6.0 - x\n\n- a\n'), null);
});

test('promueve Unreleased a la version nueva y la deja vacia', () => {
  const out = promoteUnreleased(CHANGELOG, '0.7.0', '2026-10-07');
  assert.ok(out.includes('## Unreleased\n\n## 0.7.0 - 2026-10-07\n\n### Fixed'));
  assert.equal(unreleasedBody(out), null);
  assert.equal(versionBody(out, '0.7.0'), '### Fixed\n\n- Algo arreglado.');
  assert.equal(versionBody(out, '0.6.0'), '### Added\n\n- Algo nuevo.');
});

test('lee la ultima seccion y no confunde 0.6.0 con 0.6.01', () => {
  assert.equal(versionBody(CHANGELOG, '0.1.2'), '- Ultima.');
  assert.equal(versionBody('## 0.6.01 - x\n\n- otra\n', '0.6.0'), null);
});

test('falla si no hay "## Unreleased"', () => {
  assert.throws(() => promoteUnreleased('# C\n', '1.0.0', 'x'));
});

test('funciona con el CHANGELOG y el gradle.properties reales del repo', () => {
  const real = readFileSync(new URL('../CHANGELOG.md', import.meta.url), 'utf8');
  const out = promoteUnreleased(real, '99.0.0', '2099-01-01');
  assert.equal(versionBody(out, '99.0.0'), unreleasedBody(real));
  assert.ok(versionBody(real, '0.6.0').length > 100);
  const props = readFileSync(new URL('../gradle.properties', import.meta.url), 'utf8');
  assert.match(gradleVersion(props), /^\d+\.\d+\.\d+/);
});

test('calcula la version siguiente', () => {
  assert.equal(nextVersion('0.6.0', 'patch'), '0.6.1');
  assert.equal(nextVersion('0.6.0', 'minor'), '0.7.0');
  assert.equal(nextVersion('0.6.0', 'major'), '1.0.0');
  assert.equal(nextVersion('0.6.0', '1.0.0-beta.1'), '1.0.0-beta.1');
  assert.throws(() => nextVersion('0.6.0', '0.7'));
});

test('compara versiones', () => {
  assert.ok(isGreater('0.6.1', '0.6.0'));
  assert.ok(isGreater('0.10.0', '0.9.0'));
  assert.ok(!isGreater('0.6.0', '0.6.0'));
  assert.ok(isGreater('1.0.0', '1.0.0-beta.1'));
});
