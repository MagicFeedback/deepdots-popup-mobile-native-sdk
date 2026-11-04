// npm test --prefix scripts/visual
// Mismos casos que scripts/visual/visual.test.mjs de deepdots-popup-sdk (allí con vitest).
import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { PNG } from 'pngjs';
import { comparePng, maskPng } from './compare.mjs';
import { evaluate, markdownSummary, passed, sha256 } from './check.mjs';
import { planApproval } from './approve.mjs';
import { deviceKey } from '../../test-apps/visual/collect.mjs';

/** PNG de `w×h` blanco con un rectángulo negro opcional. */
function png(w, h, rect = null) {
  const img = new PNG({ width: w, height: h });
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const i = (y * w + x) * 4;
      const inRect = rect && x >= rect.x && x < rect.x + rect.width && y >= rect.y && y < rect.y + rect.height;
      img.data.fill(inRect ? 0 : 255, i, i + 3);
      img.data[i + 3] = 255;
    }
  }
  return PNG.sync.write(img);
}

const button = { x: 20, y: 20, width: 10, height: 6 };
const moved = { x: 20, y: 24, width: 10, height: 6 };

describe('comparePng', () => {
  it('identical images match with 0 px', () => {
    const r = comparePng(png(60, 40, button), png(60, 40, button));
    assert.equal(r.match, true);
    assert.equal(r.diffPixels, 0);
    assert.equal(r.diff, null);
  });

  it('a button moved 4 px is a difference, with its region and a diff image', () => {
    const r = comparePng(png(60, 40, button), png(60, 40, moved));
    assert.equal(r.match, false);
    assert.deepEqual(r.region, { x: 20, y: 20, width: 10, height: 10 });
    assert.equal(PNG.sync.read(r.diff).width, 60);
  });

  it('a size change never matches, whatever the tolerance', () => {
    const r = comparePng(png(60, 40), png(60, 44), { threshold: 0.01, maxDiffPixels: 1e9 });
    assert.equal(r.match, false);
    assert.equal(r.diffPixels, 60 * 4);
  });

  it('masked regions are ignored', () => {
    const mask = [{ x: 0, y: 0, width: 60, height: 32 }];
    assert.equal(comparePng(maskPng(png(60, 40, button), mask), maskPng(png(60, 40, moved), mask)).match, true);
  });
});

describe('evaluate', () => {
  const A = png(60, 40, button);
  const B = png(60, 40, moved);
  const approvals = { baselines: { 'ios/open.png': { sha256: sha256(A), approvalId: 'x1' } }, approvals: [] };
  const config = { compare: { threshold: 0.01, maxDiffPixels: 0 } };
  const run = (base, actual) => evaluate({ config, approvals, baselines: new Map(base), actuals: new Map(actual) });

  it('pass / diff', () => {
    assert.equal(run([['ios/open.png', A]], [['ios/open.png', A]])[0].status, 'pass');
    assert.equal(run([['ios/open.png', A]], [['ios/open.png', B]])[0].status, 'diff');
  });

  it('missing baseline, missing screenshot and unapproved baseline all fail', () => {
    const rows = run([['ios/open.png', A]], [['ios/open.png', A], ['ios/new.png', A]]);
    assert.equal(rows.find((r) => r.key === 'ios/new.png').status, 'missing_baseline');
    assert.equal(passed(rows), false);
    assert.equal(run([['ios/open.png', A]], [])[0].status, 'missing_actual');
    assert.equal(run([['ios/open.png', B]], [['ios/open.png', B]])[0].status, 'unapproved_baseline');
    assert.equal(passed([]), false);
  });

  it('the summary names the SDK version and the failures', () => {
    const md = markdownSummary(run([['ios/open.png', A]], [['ios/open.png', B]]), { sdk: 'local', sdkVersion: '0.7.9-local', platform: 'ios' });
    assert.match(md, /FAILED/);
    assert.match(md, /0\.7\.9-local/);
    assert.match(md, /`ios\/open\.png`/);
  });
});

describe('planApproval', () => {
  const A = png(60, 40, button);
  const B = png(60, 40, moved);
  const base = { by: 'sarias980', reason: 'new spacing', source: { runId: '9' }, now: new Date('2026-11-04T10:00:00Z') };

  it('approves what changed and records who, when, why and the sha256', () => {
    const plan = planApproval({ ...base, approvals: {}, baselines: new Map([['d/a.png', A]]), candidates: new Map([['d/a.png', A], ['d/b.png', B]]) });
    assert.deepEqual(plan.copy, ['d/b.png']);
    assert.deepEqual(plan.approvals.baselines['d/b.png'], { sha256: sha256(B), approvalId: '20261104T100000Z-9' });
    assert.equal(plan.approvals.approvals[0].approvedBy, 'sarias980');
  });

  it('a reason is mandatory', () => {
    assert.throws(() => planApproval({ ...base, reason: '', approvals: {}, baselines: new Map(), candidates: new Map([['d/a.png', A]]) }), /reason/);
  });
});

describe('deviceKey', () => {
  it('names the folder by platform, model and OS major', () => {
    assert.equal(deviceKey({ platform: 'ios', model: 'iPhone 16', os: '18.5' }), 'ios-iphone-16-ios18');
    assert.equal(deviceKey({ platform: 'android', model: 'sdk_gphone64_x86_64', profile: 'pixel_6', os: '34' }), 'android-pixel-6-api34');
  });
});
