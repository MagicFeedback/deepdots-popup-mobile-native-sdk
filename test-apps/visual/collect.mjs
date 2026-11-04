#!/usr/bin/env node
/**
 * Visual regression (DD-28): gathers the `visual-*.png` screenshots of a Maestro run
 * (run-maestro.sh output) into `<dest>/<device>/<state>.png` + `<dest>/meta.json`, the layout
 * scripts/visual/check.mjs compares with the approved baselines.
 *
 *   collect.mjs --run <run-maestro out dir> --dest <dir> --sdk <requested sdk> [--device <key>]
 *
 * The device folder is the baselines folder: `<platform>-<model or AVD profile>-<OS major>`
 * (e.g. ios-iphone-16-ios18, android-pixel-6-api34), from the run's device.json. A different
 * device or OS is a different set of baselines, never compared with another one.
 */
import { copyFileSync, existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { basename, join } from 'node:path';
import { pathToFileURL } from 'node:url';

const slug = (s) => String(s).toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

export function deviceKey(dev) {
  if (dev.platform === 'android') return `android-${slug(dev.profile || dev.model)}-api${dev.os}`;
  return `ios-${slug(dev.model)}-ios${String(dev.os).split('.')[0]}`;
}

export function deviceInfo(dev) {
  if (dev.platform === 'android') return `Android API ${dev.os} emulator · ${dev.profile || dev.model} · ${dev.size ?? '?'} @${dev.density ?? '?'}dpi`;
  return `iOS ${dev.os} simulator · ${dev.model}`;
}

/** Every `visual-<state>.png` under a folder (Maestro nests them per flow). */
export function findShots(dir) {
  const out = [];
  if (!existsSync(dir)) return out;
  for (const e of readdirSync(dir, { withFileTypes: true })) {
    const p = join(dir, e.name);
    if (e.isDirectory()) out.push(...findShots(p));
    else if (/^visual-.+\.png$/.test(e.name)) out.push(p);
  }
  return out;
}

/** SDK version the app reported in the run, if any. */
export function sdkVersionFrom(labLog) {
  // `[lab] sdk init 0.7.9 (SPM)` / `[lab] sdk init local (local :shared)`: the app's own line.
  const m = labLog.match(/sdk init (\S+(?: \([^)]*\))?)/) ?? labLog.match(/sdk requested=\S+ resolved=(\S+)/);
  return m ? m[1] : null;
}

function arg(name) {
  const i = process.argv.indexOf(`--${name}`);
  return i > 0 ? process.argv[i + 1] : null;
}

function main() {
  const run = arg('run');
  const dest = arg('dest');
  const sdk = arg('sdk') ?? 'local';
  if (!run || !dest) {
    console.error('Uso: collect.mjs --run <run-maestro out> --dest <dir> --sdk <sdk> [--device <key>]');
    process.exit(2);
  }
  const dev = existsSync(join(run, 'device.json')) ? JSON.parse(readFileSync(join(run, 'device.json'), 'utf8')) : null;
  const key = arg('device') ?? (dev ? deviceKey(dev) : null);
  if (!key) throw new Error(`${run}/device.json missing and no --device given`);
  const shots = findShots(join(run, 'maestro'));
  mkdirSync(join(dest, key), { recursive: true });
  for (const p of shots) copyFileSync(p, join(dest, key, basename(p).replace(/^visual-/, '')));
  const lab = existsSync(join(run, 'lab.log')) ? readFileSync(join(run, 'lab.log'), 'utf8') : '';
  const metaPath = join(dest, 'meta.json');
  const meta = existsSync(metaPath) ? JSON.parse(readFileSync(metaPath, 'utf8')) : { devices: {} };
  Object.assign(meta, {
    platform: dev?.platform ?? meta.platform ?? null,
    sdk,
    sdkVersion: sdkVersionFrom(lab) ?? meta.sdkVersion ?? null,
    createdAt: new Date().toISOString(),
    commit: process.env.GITHUB_SHA ?? meta.commit ?? null,
    runUrl: process.env.GITHUB_RUN_ID ? `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}/actions/runs/${process.env.GITHUB_RUN_ID}` : (meta.runUrl ?? null),
  });
  meta.devices[key] = dev ? deviceInfo(dev) : key;
  writeFileSync(metaPath, JSON.stringify(meta, null, 2));
  console.log(`[visual] ${shots.length} screenshot(s) → ${join(dest, key)}`);
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) main();
