import { test } from 'node:test';
import assert from 'node:assert/strict';
import { failingChecks, requiredChecksFrom } from './required-checks.mjs';

test('required checks: the latest run of each, all green', () => {
  const runs = [
    { name: 'Release dry run', conclusion: 'failure', started_at: '1' },
    { name: 'Release dry run', conclusion: 'success', started_at: '2', html_url: 'u1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '2', html_url: 'u2' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '2', html_url: 'u3' },
    { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Version & changelog', conclusion: 'failure', started_at: '3' },
  ];
  const r = requiredChecksFrom(runs);
  assert.deepEqual(r.map((x) => x.conclusion), ['success', 'success', 'success', 'success', 'success']);
  assert.deepEqual(failingChecks(r), []);
});

test('required checks: a red or missing test app blocks the release', () => {
  const red = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'failure', started_at: '1', html_url: 'u' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'success', started_at: '1' },
  ]);
  assert.deepEqual(failingChecks(red), ['"Test apps (release build) · Android" failure u']);
  const missing = requiredChecksFrom([{ name: 'Release dry run', conclusion: 'success', started_at: '1' }]);
  assert.deepEqual(failingChecks(missing), [
    '"Test apps (release build) · Android" missing',
    '"Test apps (release build) · iOS" missing',
    '"Visual regression · Android" missing',
    '"Visual regression · iOS" missing',
  ]);
});

test('required checks: a red iOS test app blocks the release even with Android green', () => {
  const r = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'failure', started_at: '1', html_url: 'i' },
    { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'success', started_at: '1' },
  ]);
  assert.deepEqual(failingChecks(r), ['"Test apps (release build) · iOS" failure i']);
});

test('required checks: a run still in progress is not green', () => {
  const r = requiredChecksFrom([{ name: 'Release dry run', status: 'in_progress', conclusion: null, started_at: '1' }]);
  assert.equal(r[0].conclusion, 'in_progress');
});

// DD-27: sin evidencia no se publica; el commit publicado es el probado; excepción explícita.
import { evaluateRelease } from './required-checks.mjs';
import { overrideReasonFrom } from './override.mjs';

const GREEN = [
  { name: 'Release dry run', conclusion: 'success', started_at: '1' },
  { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '1' },
  { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '1' },
  { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'success', started_at: '1' },
  { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'success', started_at: '1' },
];
const RED_ANDROID = GREEN.map((c) => (c.name.endsWith('/ Android · SDK local') ? { ...c, conclusion: 'failure', html_url: 'a' } : c));
const REASON = '## Summary\nx\n\n## Release gate override\nmacOS runners down for 6 h; hotfix for a crash in prod. iOS checked by hand.\n';

function fakeApi({ pulls = [{ number: 81, merged_at: 't', merge_commit_sha: 'tag', base: { ref: 'dev' } }], tagTree = 'T', headTree = 'T', runs = GREEN, labels = [], body = '', events = [], permission = 'admin' } = {}) {
  return (path) => {
    if (path.endsWith('/commits/tag/pulls')) return pulls;
    if (path.endsWith('/pulls/81')) return { number: 81, head: { sha: 'head' }, labels, body };
    if (path.endsWith('/commits/tag')) return { commit: { tree: { sha: tagTree } } };
    if (path.endsWith('/commits/head')) return { commit: { tree: { sha: headTree } } };
    if (path.includes('/commits/head/check-runs')) return { check_runs: runs };
    if (path.includes('/issues/81/events')) return events;
    if (path.includes('/permission')) return { permission };
    throw new Error(`unexpected ${path}`);
  };
}
const gate = (o) => evaluateRelease({ repo: 'o/r', sha: 'tag', api: fakeApi(o) });
const labelled = (login) => [{ event: 'labeled', label: { name: 'release-gate-override' }, actor: { login }, created_at: '2026-11-04T10:00:00Z' }];

test('gate: green checks on the tested commit pass', () => {
  const r = gate();
  assert.equal(r.ok, true);
  assert.equal(r.override, undefined);
});

test('gate: no merged release PR = no evidence, blocked', () => {
  const r = gate({ pulls: [] });
  assert.equal(r.ok, false);
  assert.match(r.errors[0], /No hay PR fusionada/);
});

test('gate: a tagged commit that is not the tested one is blocked, even with an override', () => {
  const r = gate({ headTree: 'OTHER', labels: [{ name: 'release-gate-override' }], body: REASON, events: labelled('sarias980') });
  assert.equal(r.ok, false);
  assert.match(r.errors[0], /no es el que probó la PR #81/);
});

test('gate: a platform-specific failure blocks the release and names the platform', () => {
  const r = gate({ runs: RED_ANDROID });
  assert.equal(r.ok, false);
  assert.deepEqual(r.errors, ['Check de la PR #81 no en verde: "Test apps (release build) · Android" failure a']);
  assert.ok(r.lines.some((l) => l.endsWith('iOS: success')));
});

test('gate: override by an admin with a reason lets it through and says so', () => {
  const r = gate({ runs: RED_ANDROID, labels: [{ name: 'release-gate-override' }], body: REASON, events: labelled('sarias980') });
  assert.equal(r.ok, true);
  assert.equal(r.override.by, 'sarias980');
  assert.match(r.override.reason, /macOS runners down/);
  assert.deepEqual(r.override.failing, ['"Test apps (release build) · Android" failure a']);
});

test('gate: override without admin permission or without a reason is refused', () => {
  const notAdmin = gate({ runs: RED_ANDROID, labels: [{ name: 'release-gate-override' }], body: REASON, events: labelled('dev1'), permission: 'write' });
  assert.equal(notAdmin.ok, false);
  assert.match(notAdmin.errors.at(-1), /solo vale de un admin/);
  const noReason = gate({ runs: RED_ANDROID, labels: [{ name: 'release-gate-override' }], body: '## Summary\nx', events: labelled('sarias980') });
  assert.equal(noReason.ok, false);
  assert.match(noReason.errors.at(-1), /falta el motivo/);
});

test('gate: a label that was removed again does not count', () => {
  const r = gate({ runs: RED_ANDROID, labels: [], body: REASON, events: labelled('sarias980') });
  assert.equal(r.ok, false);
});

test('override reason: section text up to the next heading, comments stripped', () => {
  assert.equal(overrideReasonFrom('## Release gate override\n<!-- why -->\nruns down\n## Next\nno'), 'runs down');
  assert.equal(overrideReasonFrom('## Release gate override\n<!-- why -->\n'), '');
  assert.equal(overrideReasonFrom('nothing'), '');
});

test('required checks: a green visual-regression job does not stand in for a red functional test app (DD-28 names)', () => {
  const r = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'failure', started_at: '1', html_url: 'f' },
    { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'success', started_at: '2' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'failure', started_at: '2' },
  ]);
  assert.deepEqual(failingChecks(r), ['"Test apps (release build) · Android" failure f', '"Visual regression · iOS" failure']);
});

test('required checks: an unapproved visual change on one platform blocks the release and names it (DD-28)', () => {
  const r = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Visual regression · Android · SDK local', conclusion: 'failure', started_at: '1', html_url: 'v' },
    { name: 'Test apps (release build) / Visual regression · iOS · SDK local', conclusion: 'success', started_at: '1' },
  ]);
  assert.deepEqual(failingChecks(r), ['"Visual regression · Android" failure v']);
});
