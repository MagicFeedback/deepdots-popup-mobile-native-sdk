#!/usr/bin/env node
/**
 * Escribe release.json: la metadata trazable de una release del SDK nativo (MF-3242). Va
 * adjunto a la GitHub Release junto al AAR y al zip del XCFramework, y dice que artefactos
 * exactos se probaron y se publicaron, desde que commit, con que dry-run en verde y quien lo
 * aprobo.
 *
 *   metadata.mjs --tag vX.Y.Z --aar f.aar --xcframework f.xcframework.zip --out release.json
 *                [--spm-checksum <checksum>] [--maven-dir <dir del staging repo>]
 *                [--smoke smoke.json] [--maven published|skipped|absent] [--spm published|existing]
 *                [--require-ci] [--dry-run]
 *
 * Comprueba que el checksum de SPM es el sha256 del zip probado y que el AAR del bundle de
 * Maven es el mismo fichero (mismo sha256) que el que se adjunta a la Release.
 * Con --require-ci falla si el "Release dry run" de la PR de release no termino en verde
 * (el nativo no tiene CI en dev: ese job es el que construye y prueba en macOS).
 */
import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { basename, join } from 'node:path';
import { pathToFileURL } from 'node:url';

export const DRY_RUN_CHECK = 'Release dry run';

/** Ultima revision de cada revisor; aprobada si la ultima es APPROVED. */
export function approvalsFrom(reviews = []) {
  const last = new Map();
  for (const r of reviews) if (r.user?.login && r.state !== 'COMMENTED') last.set(r.user.login, r);
  return [...last.values()].filter((r) => r.state === 'APPROVED').map((r) => ({ by: r.user.login, at: r.submitted_at }));
}

/**
 * La PR que metio el commit en la rama de release: fusionada en dev (o main en un hotfix).
 * El commit etiquetado es el merge de la PR de release.
 */
export function releasePullFrom(pulls = [], sha) {
  return (
    pulls.find((p) => p.merged_at && p.merge_commit_sha === sha) ??
    pulls.find((p) => p.merged_at && ['dev', 'main'].includes(p.base?.ref)) ??
    null
  );
}

/** El "Release dry run" mas reciente de los check-runs de un commit. */
export function dryRunFrom(checkRuns = []) {
  return (
    checkRuns
      .filter((c) => c.name === DRY_RUN_CHECK)
      .sort((a, b) => String(b.started_at ?? '').localeCompare(String(a.started_at ?? '')))[0] ?? null
  );
}

export const sha256 = (bytes) => createHash('sha256').update(bytes).digest('hex');

/** Errores de trazabilidad: lo publicado tiene que ser lo probado. */
export function verifyArtifacts({ aarSha256, zipSha256, spmChecksum, mavenAarSha256 }) {
  const errors = [];
  for (const [label, value] of [['AAR', aarSha256], ['XCFramework', zipSha256]]) {
    if (!/^[0-9a-f]{64}$/.test(value ?? '')) errors.push(`sha256 del ${label} no valido.`);
  }
  if (spmChecksum != null && spmChecksum !== zipSha256) {
    errors.push(`El checksum de SPM (${spmChecksum}) no es el sha256 del zip probado (${zipSha256}).`);
  }
  if (mavenAarSha256 !== undefined) {
    if (mavenAarSha256 === null) errors.push('No encuentro el AAR en el bundle de Maven (--maven-dir).');
    else if (mavenAarSha256 !== aarSha256) errors.push(`El AAR del bundle de Maven (${mavenAarSha256}) no es el AAR probado (${aarSha256}).`);
  }
  return errors;
}

function gh(path) {
  try {
    return JSON.parse(execFileSync('gh', ['api', path], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }));
  } catch (e) {
    console.warn(`::warning::gh api ${path}: ${String(e.stderr || e.message).trim().split('\n')[0]}`);
    return null;
  }
}

function tool(cmd, args) {
  try {
    return execFileSync(cmd, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim().split('\n')[0];
  } catch {
    return null;
  }
}

function main() {
  const argv = process.argv.slice(2);
  const option = (name) => {
    const i = argv.indexOf(`--${name}`);
    return i >= 0 ? argv[i + 1] : null;
  };
  const flag = (name) => argv.includes(`--${name}`);
  const [tag, aar, zip, out] = [option('tag'), option('aar'), option('xcframework'), option('out')];
  if (!tag || !aar || !zip || !out) {
    console.error('Uso: metadata.mjs --tag vX.Y.Z --aar f.aar --xcframework f.zip --out release.json [--spm-checksum c] [--maven-dir d] [--smoke s.json] [--maven m] [--spm s] [--require-ci] [--dry-run]');
    process.exit(2);
  }

  const manifest = JSON.parse(readFileSync('release/compatibility.json', 'utf8'));
  const version = manifest.version;
  const aarBytes = readFileSync(aar);
  const zipBytes = readFileSync(zip);
  const [aarSha256, zipSha256] = [sha256(aarBytes), sha256(zipBytes)];

  // Bundle de Maven Central: el AAR y el POM que salen del staging repo (con sus .sha256/.asc).
  let mavenAarSha256;
  let mavenFiles = null;
  const mavenDir = option('maven-dir');
  if (mavenDir) {
    const base = join(mavenDir, 'com/deepdots/sdk/shared-android', version);
    const file = (ext) => join(base, `shared-android-${version}.${ext}`);
    mavenAarSha256 = existsSync(file('aar')) ? sha256(readFileSync(file('aar'))) : null;
    mavenFiles = ['aar', 'pom', 'module']
      .filter((ext) => existsSync(file(ext)))
      .map((ext) => ({ file: basename(file(ext)), sha256: sha256(readFileSync(file(ext))), signed: existsSync(`${file(ext)}.asc`) }));
  }
  const spmChecksum = option('spm-checksum');
  const errors = verifyArtifacts({ aarSha256, zipSha256, spmChecksum, mavenAarSha256 });

  const repo = process.env.GITHUB_REPOSITORY ?? 'MagicFeedback/deepdots-popup-mobile-native-sdk';
  const server = process.env.GITHUB_SERVER_URL ?? 'https://github.com';
  const sha = process.env.RELEASE_SHA ?? execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim();
  const runUrl = process.env.GITHUB_RUN_ID ? `${server}/${repo}/actions/runs/${process.env.GITHUB_RUN_ID}` : null;

  // Aprobacion: la PR de release fusionada, sus aprobaciones, quien la fusiono, quien subio el
  // tag y el "Release dry run" (gate + tests + build + smoke en macOS) sobre su ultimo commit.
  let approval = null;
  let ci = null;
  if (flag('dry-run')) {
    ci = { check: DRY_RUN_CHECK, url: runUrl, conclusion: 'in_progress' };
  } else {
    const pr = releasePullFrom(gh(`repos/${repo}/commits/${sha}/pulls`) ?? [], sha);
    const full = pr ? gh(`repos/${repo}/pulls/${pr.number}`) : null;
    const headSha = full?.head?.sha ?? pr?.head?.sha ?? null;
    const check = headSha ? dryRunFrom(gh(`repos/${repo}/commits/${headSha}/check-runs?per_page=100`)?.check_runs ?? []) : null;
    ci = check ? { check: DRY_RUN_CHECK, headSha, url: check.html_url, conclusion: check.conclusion ?? check.status } : null;
    approval = {
      pullRequest: pr
        ? { number: pr.number, url: pr.html_url, title: pr.title, base: pr.base?.ref, headSha, mergedAt: pr.merged_at, mergedBy: full?.merged_by?.login ?? null }
        : null,
      approvals: pr ? approvalsFrom(gh(`repos/${repo}/pulls/${pr.number}/reviews`) ?? []) : [],
      taggedBy: process.env.GITHUB_ACTOR ?? null,
      dryRunPassed: ci?.conclusion === 'success',
    };
    if (!pr) console.warn(`::warning::No encuentro la PR fusionada de ${sha.slice(0, 7)}; la aprobacion queda solo con el tag.`);
    if (flag('require-ci') && ci?.conclusion !== 'success') {
      errors.push(`El "${DRY_RUN_CHECK}" de la PR de release no esta en verde: ${ci ? `${ci.conclusion} ${ci.url}` : 'sin ejecucion'}.`);
    }
  }

  const spmRepo = manifest.artifacts.ios.spm;
  const metadata = {
    schemaVersion: 1,
    name: manifest.package,
    version,
    tag,
    platform: manifest.platform,
    dryRun: flag('dry-run'),
    commit: sha,
    commitUrl: `${server}/${repo}/commit/${sha}`,
    artifacts: {
      android: {
        file: basename(aar),
        size: aarBytes.length,
        sha256: aarSha256,
        maven: {
          coordinates: `${manifest.artifacts.android.maven}:${version}`,
          state: option('maven') ?? 'not-attempted',
          bundle: mavenFiles,
        },
      },
      ios: {
        file: basename(zip),
        size: zipBytes.length,
        sha256: zipSha256,
        spm: {
          repo: spmRepo,
          version,
          state: option('spm') ?? 'not-attempted',
          checksum: spmChecksum ?? zipSha256,
          url: `${server}/${spmRepo}/releases/download/${version}/${basename(zip)}`,
        },
      },
    },
    smokeTest: option('smoke') ? JSON.parse(readFileSync(option('smoke'), 'utf8')) : null,
    toolchain: {
      java: tool('java', ['--version']),
      xcode: tool('xcodebuild', ['-version']),
      node: process.version,
    },
    ci,
    approval,
    workflowRun: runUrl,
    compatibility: manifest,
  };
  writeFileSync(out, `${JSON.stringify(metadata, null, 2)}\n`);
  console.log(`${out}: ${manifest.package} ${version}\n  AAR ${aarSha256}\n  XCFramework ${zipSha256}`);

  if (errors.length) {
    for (const e of errors) console.error(process.env.GITHUB_ACTIONS ? `::error title=Release metadata::${e}` : `x  ${e}`);
    process.exit(1);
  }
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) main();
