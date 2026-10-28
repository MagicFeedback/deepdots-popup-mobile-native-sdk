#!/usr/bin/env node
/**
 * Prueba de humo de los artefactos que se van a publicar (MF-3242): abre el AAR y el zip del
 * XCFramework y comprueba que traen lo que consume una app, y que dicen lo mismo que el
 * manifiesto. Lo que se prueba aqui es exactamente el fichero que release.yml publica despues.
 *
 *   smoke.mjs --aar DeepdotsSDK-X.Y.Z.aar --xcframework DeepdotsSDK-X.Y.Z.xcframework.zip
 *             [--manifest release/compatibility.json] [--json smoke.json]
 *
 * Necesita `unzip`. --json deja lo comprobado (para release.json).
 */
import { execFileSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';

// Lo que una app usa: si desaparece, la release rompe integraciones.
const ANDROID_CLASSES = ['com/deepdots/sdk/DeepdotsPopups.class', 'com/deepdots/sdk/models/InitOptions.class'];
const RENDERER_ASSET = 'assets/magicfeedback/magicfeedback-sdk.browser.js';
const IOS_SLICES = ['ios-arm64', 'ios-arm64-simulator'];
// Nombres que ve Swift (el prefijo ObjC, DSDK, lo pone Kotlin/Native y puede cambiar).
const IOS_HEADER_SYMBOLS = ['swift_name("DeepdotsPopups")', 'swift_name("InitOptions")'];

/** Valor `<key>K</key><string>V</string>` de un plist XML. */
export function plistString(xml, key) {
  return new RegExp(`<key>${key}</key>\\s*<string>([^<]*)</string>`).exec(xml ?? '')?.[1] ?? null;
}

/** `LibraryIdentifier`s de un Info.plist de XCFramework. */
export function xcframeworkSlices(xml) {
  return [...String(xml ?? '').matchAll(/<key>LibraryIdentifier<\/key>\s*<string>([^<]+)<\/string>/g)].map((m) => m[1]).sort();
}

/** `android:minSdkVersion` del AndroidManifest.xml de un AAR (texto, no binario). */
export function aarMinSdk(manifestXml) {
  const m = /android:minSdkVersion="(\d+)"/.exec(manifestXml ?? '');
  return m ? Number(m[1]) : null;
}

/** Version que lleva dentro el bundle de @magicfeedback/native (su package.json embebido). */
export function bundleVersion(js) {
  return /"name":"@magicfeedback\/native","version":"([^"]+)"/.exec(js ?? '')?.[1] ?? /"version":"(\d+\.\d+\.\d+[^"]*)"/.exec(js ?? '')?.[1] ?? null;
}

const versionTuple = (v) => String(v).split('.').map(Number);
export function osAtMost(a, b) {
  const [x, y] = [versionTuple(a), versionTuple(b)];
  for (let i = 0; i < Math.max(x.length, y.length); i++) {
    if ((x[i] ?? 0) !== (y[i] ?? 0)) return (x[i] ?? 0) < (y[i] ?? 0);
  }
  return true;
}

function unzipList(file) {
  return execFileSync('unzip', ['-Z1', file], { encoding: 'utf8', maxBuffer: 64 << 20 }).split('\n').filter(Boolean);
}

function unzipRead(file, entry, encoding = 'utf8') {
  return execFileSync('unzip', ['-p', file, entry], { encoding, maxBuffer: 256 << 20 });
}

function checkAar(aar, manifest, failures) {
  const entries = unzipList(aar);
  const result = { file: aar };
  if (!entries.includes('classes.jar')) failures.push('AAR: falta classes.jar');
  if (!entries.includes('AndroidManifest.xml')) failures.push('AAR: falta AndroidManifest.xml');
  if (!failures.length) {
    const dir = mkdtempSync(join(tmpdir(), 'native-sdk-smoke-'));
    try {
      const jar = join(dir, 'classes.jar');
      writeFileSync(jar, unzipRead(aar, 'classes.jar', 'buffer'));
      const classes = new Set(unzipList(jar));
      for (const c of ANDROID_CLASSES) if (!classes.has(c)) failures.push(`AAR: classes.jar no tiene ${c}`);
      result.classes = classes.size;
    } finally {
      rmSync(dir, { recursive: true, force: true });
    }
    result.minSdk = aarMinSdk(unzipRead(aar, 'AndroidManifest.xml'));
    if (result.minSdk !== manifest.hosts.android.minSdk) {
      failures.push(`AAR: minSdkVersion ${result.minSdk}, el manifiesto dice ${manifest.hosts.android.minSdk}`);
    }
  }
  if (manifest.surveyRenderer.delivery.android === 'vendored') {
    if (!entries.includes(RENDERER_ASSET)) failures.push(`AAR: falta ${RENDERER_ASSET} (renderer vendorizado)`);
    else {
      result.renderer = bundleVersion(unzipRead(aar, RENDERER_ASSET));
      if (result.renderer !== manifest.surveyRenderer.version) {
        failures.push(`AAR: el renderer vendorizado es ${result.renderer}, el manifiesto dice ${manifest.surveyRenderer.version}`);
      }
    }
  }
  return result;
}

function checkXcframework(zip, manifest, failures) {
  const dir = mkdtempSync(join(tmpdir(), 'native-sdk-smoke-'));
  const result = { file: zip };
  try {
    execFileSync('unzip', ['-q', zip, '-d', dir]);
    const root = join(dir, 'DeepdotsSDK.xcframework');
    if (!existsSync(join(root, 'Info.plist'))) {
      failures.push('XCFramework: falta DeepdotsSDK.xcframework/Info.plist');
      return result;
    }
    result.slices = xcframeworkSlices(readFileSync(join(root, 'Info.plist'), 'utf8'));
    const wanted = [...manifest.hosts.ios.architectures].sort();
    if (JSON.stringify(result.slices) !== JSON.stringify(wanted)) {
      failures.push(`XCFramework: slices ${result.slices.join(', ')}, el manifiesto dice ${wanted.join(', ')}`);
    }
    result.minimumOSVersion = {};
    for (const slice of result.slices.filter((s) => IOS_SLICES.includes(s) || s.startsWith('ios-'))) {
      const fw = join(root, slice, 'DeepdotsSDK.framework');
      if (!existsSync(join(fw, 'DeepdotsSDK')) || !statSync(join(fw, 'DeepdotsSDK')).size) failures.push(`XCFramework ${slice}: falta el binario`);
      const header = join(fw, 'Headers', 'DeepdotsSDK.h');
      const text = existsSync(header) ? readFileSync(header, 'utf8') : '';
      if (!text) failures.push(`XCFramework ${slice}: falta Headers/DeepdotsSDK.h`);
      for (const symbol of IOS_HEADER_SYMBOLS) if (text && !text.includes(symbol)) failures.push(`XCFramework ${slice}: el header no declara ${symbol}`);
      const plist = existsSync(join(fw, 'Info.plist')) ? readFileSync(join(fw, 'Info.plist'), 'utf8') : '';
      const min = plistString(plist, 'MinimumOSVersion');
      result.minimumOSVersion[slice] = min;
      // Framework estatico: el minimo efectivo lo pone el enlazado (-mios-version-min) y SPM;
      // el del Info.plist no puede pedir MAS que el manifiesto.
      if (min && !osAtMost(min, manifest.hosts.ios.minimum)) {
        failures.push(`XCFramework ${slice}: MinimumOSVersion ${min} es mayor que el minimo del manifiesto (${manifest.hosts.ios.minimum})`);
      }
    }
    const bundled = readdirSync(dir, { recursive: true }).some((p) => String(p).endsWith('magicfeedback-sdk.browser.js'));
    if (manifest.surveyRenderer.delivery.ios === 'vendored' && !bundled) failures.push('XCFramework: no lleva el renderer vendorizado');
    if (manifest.surveyRenderer.delivery.ios === 'cdn' && bundled) {
      failures.push('XCFramework: lleva el renderer vendorizado pero el manifiesto dice delivery.ios = cdn');
    }
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
  return result;
}

function main() {
  const argv = process.argv.slice(2);
  const option = (name, fallback = null) => {
    const i = argv.indexOf(`--${name}`);
    return i >= 0 ? argv[i + 1] : fallback;
  };
  const [aar, zip] = [option('aar'), option('xcframework')];
  if (!aar || !zip || !existsSync(aar) || !existsSync(zip)) {
    console.error('Uso: smoke.mjs --aar f.aar --xcframework f.xcframework.zip [--manifest release/compatibility.json] [--json smoke.json]');
    process.exit(2);
  }
  const manifest = JSON.parse(readFileSync(option('manifest', 'release/compatibility.json'), 'utf8'));
  const failures = [];
  const android = checkAar(aar, manifest, failures);
  const ios = checkXcframework(zip, manifest, failures);
  if (option('json')) writeFileSync(option('json'), `${JSON.stringify({ android, ios, passed: !failures.length }, null, 2)}\n`);

  if (failures.length) {
    for (const f of failures) console.error(process.env.GITHUB_ACTIONS ? `::error title=Smoke test::${f}` : `x  ${f}`);
    process.exit(1);
  }
  console.log(`✓ AAR: ${android.classes} clases, minSdk ${android.minSdk}, renderer ${android.renderer ?? '-'}`);
  console.log(`✓ XCFramework: ${ios.slices.join(', ')}, MinimumOSVersion ${JSON.stringify(ios.minimumOSVersion)}`);
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) main();
