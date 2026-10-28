// npm test --prefix scripts/release
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { approvalsFrom, dryRunFrom, releasePullFrom, sha256, verifyArtifacts } from './metadata.mjs';
import { aarMinSdk, bundleVersion, osAtMost, plistString, xcframeworkSlices } from './smoke.mjs';

const A = sha256('aar');
const Z = sha256('zip');

test('artefactos: checksum de SPM = sha256 del zip y AAR de Maven = AAR probado', () => {
  assert.deepEqual(verifyArtifacts({ aarSha256: A, zipSha256: Z, spmChecksum: Z, mavenAarSha256: A }), []);
  assert.match(verifyArtifacts({ aarSha256: A, zipSha256: Z, spmChecksum: A }).join(), /checksum de SPM/);
  assert.match(verifyArtifacts({ aarSha256: A, zipSha256: Z, mavenAarSha256: Z }).join(), /bundle de Maven/);
  assert.match(verifyArtifacts({ aarSha256: A, zipSha256: Z, mavenAarSha256: null }).join(), /No encuentro el AAR/);
});

test('aprobaciones: cuenta la ultima revision de cada uno', () => {
  const reviews = [
    { user: { login: 'ana' }, state: 'CHANGES_REQUESTED', submitted_at: '1' },
    { user: { login: 'ana' }, state: 'APPROVED', submitted_at: '2' },
    { user: { login: 'bo' }, state: 'APPROVED', submitted_at: '3' },
    { user: { login: 'bo' }, state: 'COMMENTED', submitted_at: '4' },
    { user: { login: 'cy' }, state: 'APPROVED', submitted_at: '5' },
    { user: { login: 'cy' }, state: 'DISMISSED', submitted_at: '6' },
  ];
  assert.deepEqual(approvalsFrom(reviews), [{ by: 'ana', at: '2' }, { by: 'bo', at: '3' }]);
});

test('PR de release: la del merge commit, o la fusionada en dev/main', () => {
  const pulls = [{ number: 1, merged_at: null, merge_commit_sha: 'x' }, { number: 2, merged_at: 't', base: { ref: 'dev' }, merge_commit_sha: 'y' }];
  assert.equal(releasePullFrom(pulls, 'y').number, 2);
  assert.equal(releasePullFrom(pulls, 'z').number, 2);
  assert.equal(releasePullFrom([], 'z'), null);
});

test('dry run: el check mas reciente con ese nombre', () => {
  const runs = [
    { name: 'Version & changelog', conclusion: 'success', started_at: '3' },
    { name: 'Release dry run', conclusion: 'failure', started_at: '1' },
    { name: 'Release dry run', conclusion: 'success', started_at: '2' },
  ];
  assert.equal(dryRunFrom(runs).conclusion, 'success');
  assert.equal(dryRunFrom([]), null);
});

test('smoke: lectura de plist, manifest del AAR y bundle', () => {
  const plist = '<dict><key>MinimumOSVersion</key>\n  <string>12.0</string><key>LibraryIdentifier</key><string>ios-arm64-simulator</string><key>LibraryIdentifier</key><string>ios-arm64</string></dict>';
  assert.equal(plistString(plist, 'MinimumOSVersion'), '12.0');
  assert.deepEqual(xcframeworkSlices(plist), ['ios-arm64', 'ios-arm64-simulator']);
  assert.equal(aarMinSdk('<uses-sdk android:minSdkVersion="24" />'), 24);
  assert.equal(bundleVersion('x={"name":"@magicfeedback/native","version":"2.2.31","main":1}'), '2.2.31');
  assert.ok(osAtMost('12.0', '13.0'));
  assert.ok(osAtMost('13.0', '13'));
  assert.ok(!osAtMost('14.0', '13.0'));
});
