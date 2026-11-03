import { test } from 'node:test';
import assert from 'node:assert/strict';
import { failingChecks, requiredChecksFrom } from './required-checks.mjs';

test('required checks: the latest run of each, all green', () => {
  const runs = [
    { name: 'Release dry run', conclusion: 'failure', started_at: '1' },
    { name: 'Release dry run', conclusion: 'success', started_at: '2', html_url: 'u1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '2', html_url: 'u2' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '2', html_url: 'u3' },
    { name: 'Version & changelog', conclusion: 'failure', started_at: '3' },
  ];
  const r = requiredChecksFrom(runs);
  assert.deepEqual(r.map((x) => x.conclusion), ['success', 'success', 'success']);
  assert.deepEqual(failingChecks(r), []);
});

test('required checks: a red or missing test app blocks the release', () => {
  const red = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'failure', started_at: '1', html_url: 'u' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'success', started_at: '1' },
  ]);
  assert.deepEqual(failingChecks(red), ['"Test apps (release build) · Android" failure u']);
  const missing = requiredChecksFrom([{ name: 'Release dry run', conclusion: 'success', started_at: '1' }]);
  assert.deepEqual(failingChecks(missing), [
    '"Test apps (release build) · Android" missing',
    '"Test apps (release build) · iOS" missing',
  ]);
});

test('required checks: a red iOS test app blocks the release even with Android green', () => {
  const r = requiredChecksFrom([
    { name: 'Release dry run', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / Android · SDK local', conclusion: 'success', started_at: '1' },
    { name: 'Test apps (release build) / iOS · SDK local', conclusion: 'failure', started_at: '1', html_url: 'i' },
  ]);
  assert.deepEqual(failingChecks(r), ['"Test apps (release build) · iOS" failure i']);
});

test('required checks: a run still in progress is not green', () => {
  const r = requiredChecksFrom([{ name: 'Release dry run', status: 'in_progress', conclusion: null, started_at: '1' }]);
  assert.equal(r[0].conclusion, 'in_progress');
});
