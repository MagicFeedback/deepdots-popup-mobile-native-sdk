import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { aggregate, buildIndex, buildMarkdown, buildSlackMessage, categoryOf, evaluate, STATUS } from './validation-report.mjs';

const row = (platform, label, status, extra = {}) => ({
  label, platform, os: 'x', device: 'd', build: 'debug', sdk: '0.7.8', resolved: '0.7.8', engine: '',
  outcome: Object.values(status).some((s) => s === 'fail' || s === 'error') ? 'failure' : 'success',
  status, messages: {}, ...extra,
});
const jobs = { android: 'success', ios: 'success', 'negative-control': 'success', 'r8-control': 'success' };
const green = () => [
  row('Android', 'A34', { 'rate-product': 'pass', offline: 'pass', 'missing-navigation': 'expected' }),
  row('iOS', 'iOS 18', { 'rate-product': 'retry', offline: 'skip', 'missing-navigation': 'expected' }),
];

test('aggregate: any failure wins, skipped-only is n/a, nothing is NOT RUN', () => {
  assert.equal(aggregate(['pass', 'retry', 'expected']), 'PASS');
  assert.equal(aggregate(['pass', 'error']), 'FAIL');
  assert.equal(aggregate(['pass', 'known']), 'PASS (known issue)');
  assert.equal(aggregate(['skip', 'skip']), 'n/a');
  assert.equal(aggregate([undefined]), 'NOT RUN');
});

test('all green: PASSED on both platforms', () => {
  const r = evaluate(green(), jobs);
  assert.equal(r.status, STATUS.passed);
  assert.deepEqual(r.platforms, { Android: STATUS.passed, iOS: STATUS.passed });
  assert.equal(r.counts.passed, 5);
  assert.equal(r.counts.skipped, 1);
});

test('a failing Android flow: FAILED, Android failed, iOS still passed', () => {
  const rows = green();
  rows[0] = row('Android', 'A34', { 'rate-product': 'fail' }, { messages: { 'rate-product': 'Assertion is false' } });
  const r = evaluate(rows, { ...jobs, android: 'failure' });
  assert.equal(r.status, STATUS.failed);
  assert.equal(r.platforms.Android, STATUS.failed);
  assert.equal(r.platforms.iOS, STATUS.passed);
  assert.deepEqual(r.failures, [{ platform: 'Android', row: 'A34', flow: 'rate-product', message: 'Assertion is false' }]);
});

test('a flow that did not run (error) is a failure, not a skip', () => {
  const rows = green();
  rows[1].status['rate-product'] = 'error';
  assert.equal(evaluate(rows, jobs).platforms.iOS, STATUS.failed);
});

test('green flows but a red job (device log / prod isolation): FAILED', () => {
  const rows = green();
  rows[0].outcome = 'failure';
  const r = evaluate(rows, jobs);
  assert.equal(r.status, STATUS.failed);
  assert.match(r.failures[0].message, /device log/);
});

test('a platform without rows: FAILED', () => {
  assert.equal(evaluate([green()[0]], jobs).platforms.iOS, STATUS.failed);
});

test('a platform job cancelled with green rows: FAILED', () => {
  assert.equal(evaluate(green(), { ...jobs, ios: 'cancelled' }).platforms.iOS, STATUS.failed);
});

test('a detection control that did not detect: FAILED', () => {
  const r = evaluate(green(), { ...jobs, 'negative-control': 'failure' });
  assert.equal(r.status, STATUS.failed);
  assert.equal(r.failures[0].flow, 'negative-control');
});

test('a known issue that still fails: PASSED WITH KNOWN ISSUES on that platform only', () => {
  const rows = green();
  rows[1].status['close-with-keyboard'] = 'known';
  const r = evaluate(rows, jobs);
  assert.equal(r.status, STATUS.known);
  assert.equal(r.platforms.iOS, STATUS.known);
  assert.equal(r.platforms.Android, STATUS.passed);
  assert.deepEqual(r.knownIssues, { 'close-with-keyboard': ['iOS 18'] });
});

test('markdown: failure table and FAIL in the category row', () => {
  const rows = green();
  rows[0] = row('Android', 'A34 phone debug', { 'rate-product': 'fail' }, { messages: { 'rate-product': 'popup not shown' } });
  const { markdown } = buildMarkdown({ rows, jobs: { ...jobs, android: 'failure' }, version: '0.7.8', date: '2026-11-03' });
  assert.match(markdown, /\*\*Overall status:\*\* \*\*FAILED\*\*/);
  assert.match(markdown, /\| Android \| A34 phone debug \| rate-product \| popup not shown \|/);
  assert.match(markdown, /\| Event and click triggers \| PASS \| \*\*FAIL\*\* \|/);
  assert.match(markdown, /Real Devices \(manual\)[\s\S]*PENDING/);
});

test('markdown: release notes of that version from the CHANGELOG', () => {
  const changelog = '## Unreleased\n\n## 0.7.8 - 2026-11-03\n\n### Fixed\n\n- Popup shown once.\n\n## 0.7.7 - 2026-11-02\n\n- Old.\n';
  const { markdown } = buildMarkdown({ rows: green(), jobs, version: '0.7.8', date: '2026-11-03', changelog });
  assert.match(markdown, /### Release notes 0\.7\.8\n\n#### Fixed\n\n- Popup shown once\./);
  assert.doesNotMatch(markdown, /Old\./);
});

test('every Maestro flow counts for a report category', () => {
  const { categories } = JSON.parse(readFileSync(new URL('./validation/report.json', import.meta.url), 'utf8'));
  const mapped = new Set(categories.flatMap((c) => c.flows));
  const dirs = ['../../test-apps/maestro/flows', '../../test-apps/maestro/regression', '../../test-apps/maestro/failures', '../../.github/test-apps-matrix/client-replica'];
  const flows = dirs.flatMap((d) => readdirSync(new URL(d, import.meta.url)).filter((f) => f.endsWith('.yaml')).map((f) => f.replace(/\.yaml$/, '')));
  assert.deepEqual(flows.filter((f) => !mapped.has(f)), []);
});

test('slack: a failed validation says FAILED and lists the failure', () => {
  const rows = green();
  rows[0] = row('Android', 'A34', { 'rate-product': 'fail' }, { messages: { 'rate-product': 'popup <not> shown' } });
  const summary = { version: '0.7.8', ...evaluate(rows, { ...jobs, android: 'failure' }) };
  const { text } = buildSlackMessage(summary, { links: [{ label: 'Report', url: 'https://x/0.7.8/' }], test: true });
  assert.match(text, /^🧪 TEST — ❌ \*Deepdots native popup SDK 0\.7\.8\* · release validation \*FAILED\*/);
  assert.match(text, /iOS: PASSED · Android: FAILED/);
  assert.match(text, /<https:\/\/x\/0\.7\.8\/\|Report>/);
  assert.match(text, /• ❌ Android · A34 · rate-product: popup &lt;not&gt; shown/);
});

test('slack: passed', () => {
  const { text } = buildSlackMessage({ version: '0.7.8', ...evaluate(green(), jobs) });
  assert.match(text, /^✅ .*\*PASSED\*/);
  assert.doesNotMatch(text, /❌/);
});

test('index: newest version first', () => {
  const html = buildIndex([
    { version: '0.7.8', status: 'PASSED', pdf: 'a.pdf' },
    { version: '0.7.10', status: 'FAILED', pdf: 'b.pdf' },
  ]);
  assert.ok(html.indexOf('0.7.10') < html.indexOf('0.7.8'));
});

test('categoryOf: by name, by regex, otherwise Other checks', () => {
  const config = { categories: [{ name: 'A', flows: ['x'] }, { name: 'B', match: ['^smoke › time'] }] };
  assert.equal(categoryOf('x', config), 'A');
  assert.equal(categoryOf('smoke › time_on_page 3 s', config), 'B');
  assert.equal(categoryOf('y', config), 'Other checks');
});

test('two platforms on one red job: only the one with the failing test fails', () => {
  const config = { platforms: [{ name: 'Desktop', job: 'web' }, { name: 'Mobile', job: 'web' }], categories: [], controls: {} };
  const rows = [row('Desktop', 'D', { a: 'fail' }), row('Mobile', 'M', { a: 'pass' })];
  const r = evaluate(rows, { web: 'failure' }, config);
  assert.deepEqual(r.platforms, { Desktop: STATUS.failed, Mobile: STATUS.passed });
});

test('a row where no test ran (emulator did not start) says so', () => {
  const rows = green();
  rows[0] = { ...rows[0], status: {}, outcome: 'failure' };
  const r = evaluate(rows, jobs);
  assert.equal(r.platforms.Android, STATUS.failed);
  assert.match(r.failures[0].message, /No test ran/);
});
