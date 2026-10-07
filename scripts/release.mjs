#!/usr/bin/env node
// Release del SDK nativo (KMP) en dos pasos, uno a cada lado de la PR. Espejo de
// scripts/release.mjs del SDK web; solo cambia de donde sale la version y que se comprueba.
//
//   node scripts/release.mjs prepare <X.Y.Z|patch|minor|major> [--dry-run]
//     Desde origin/dev: rama release/X.Y.Z, version con scripts/bump_version.sh
//     (gradle.properties + README/docs), "## Unreleased" del CHANGELOG pasa a "## X.Y.Z - fecha",
//     commit, push y PR a dev. Trabaja en un worktree temporal: tu checkout no se toca.
//
//   node scripts/release.mjs tag [--yes] [--no-watch]
//     Cuando la PR esta fusionada: comprueba lo mismo que .github/workflows/release.yml
//     (version, CHANGELOG, que no existan el tag ni la release), etiqueta vX.Y.Z sobre
//     origin/dev, lo sube y sigue el workflow (tests + Maven Central + SPM + GitHub Release).
//
// Necesita `git` y `gh` autenticado. No publica nada por si mismo: publica el workflow.
// Tests: node --test scripts/release.test.mjs

import { execFileSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createInterface } from 'node:readline/promises';
import { pathToFileURL } from 'node:url';

const REPO = 'MagicFeedback/deepdots-popup-mobile-native-sdk';
const SPM_REPO = 'MagicFeedback/DeepdotsSDK-SPM';
const MAVEN = 'https://repo1.maven.org/maven2/com/deepdots/sdk/shared-android';
const BASE = 'dev';
// Solo para probar el script contra otra rama sin tocar dev. La PR va siempre a dev.
const BASE_REF = process.env.RELEASE_BASE_REF || 'origin/' + BASE;
const SEMVER = /^\d+\.\d+\.\d+(-[0-9A-Za-z.-]+)?$/;

// ---------- utilidades ----------

function run(cmd, args, opts = {}) {
  return execFileSync(cmd, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'], ...opts }).trim();
}

function tryRun(cmd, args, opts = {}) {
  try {
    return { ok: true, out: run(cmd, args, opts) };
  } catch (e) {
    return { ok: false, out: `${e.stdout ?? ''}${e.stderr ?? ''}`.trim() };
  }
}

function fail(msg) {
  console.error(`\n✖ ${msg}`);
  process.exit(1);
}

function step(msg) {
  console.log(`→ ${msg}`);
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Contenido de un fichero en un commit, tal cual (sin recortar: conserva el salto final). */
function showFile(ref, path) {
  return execFileSync('git', ['show', `${ref}:${path}`], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
}

export function gradleVersion(gradleProperties) {
  const m = gradleProperties.match(/^PUBLISHING_VERSION=(.+)$/m);
  return m ? m[1].trim() : null;
}

function remoteTagExists(repoUrl, tag) {
  return tryRun('git', ['ls-remote', '--exit-code', '--tags', repoUrl, `refs/tags/${tag}`]).ok;
}

function remoteBranchExists(branch) {
  return tryRun('git', ['ls-remote', '--exit-code', '--heads', 'origin', branch]).ok;
}

function releaseExists(repo, tag) {
  return tryRun('gh', ['release', 'view', tag, '--repo', repo, '--json', 'tagName']).ok;
}

async function onMavenCentral(version) {
  try {
    const res = await fetch(`${MAVEN}/${version}/shared-android-${version}.pom`, { method: 'HEAD' });
    return res.ok;
  } catch {
    return false;
  }
}

// ---------- CHANGELOG (formato "## Unreleased" / "## X.Y.Z - fecha") ----------

/** Cuerpo de la seccion "## Unreleased" (sin la cabecera), o null si esta vacia. */
export function unreleasedBody(changelog) {
  const m = changelog.match(/^## Unreleased[^\n]*\n([\s\S]*?)(?=^## |(?![\s\S]))/m);
  return (m && m[1].trim()) || null;
}

/** Inserta "## X.Y.Z - fecha" justo debajo de "## Unreleased", que queda vacia. */
export function promoteUnreleased(changelog, version, date) {
  const head = /^## Unreleased[^\n]*\n\n?/m;
  if (!head.test(changelog)) throw new Error('El CHANGELOG no tiene seccion "## Unreleased".');
  return changelog.replace(head, (h) => `${h.trimEnd()}\n\n## ${version} - ${date}\n\n`);
}

/** Cuerpo de la seccion "## X.Y.Z" (lo mismo que lee scripts/extract_changelog_section.sh). */
export function versionBody(changelog, version) {
  const esc = version.replace(/\./g, '\\.');
  const m = changelog.match(new RegExp(`^## ${esc}(?:[ \\t][^\\n]*)?\\n([\\s\\S]*?)(?=^## |(?![\\s\\S]))`, 'm'));
  return (m && m[1].trim()) || null;
}

// ---------- versiones ----------

export function nextVersion(current, bump) {
  if (SEMVER.test(bump)) return bump;
  const [maj, min, pat] = current.split('-')[0].split('.').map(Number);
  if (bump === 'major') return `${maj + 1}.0.0`;
  if (bump === 'minor') return `${maj}.${min + 1}.0`;
  if (bump === 'patch') return `${maj}.${min}.${pat + 1}`;
  throw new Error(`Version no valida: "${bump}". Usa X.Y.Z, patch, minor o major.`);
}

export function isGreater(a, b) {
  const pa = a.split('-')[0].split('.').map(Number);
  const pb = b.split('-')[0].split('.').map(Number);
  for (let i = 0; i < 3; i++) if (pa[i] !== pb[i]) return pa[i] > pb[i];
  // Misma X.Y.Z: una version final va por delante de su prerelease.
  return !a.includes('-') && b.includes('-');
}

// ---------- prepare ----------

async function prepare(bump, { dryRun }) {
  if (!bump) fail('Falta la version: node scripts/release.mjs prepare <X.Y.Z|patch|minor|major>');

  step('git fetch origin');
  run('git', ['fetch', '--quiet', '--prune', '--tags', 'origin']);

  const current = gradleVersion(showFile(BASE_REF, 'gradle.properties'));
  if (!current) fail(`No encuentro PUBLISHING_VERSION en gradle.properties de ${BASE}.`);
  const version = nextVersion(current, bump);
  const tag = `v${version}`;
  const branch = `release/${version}`;
  console.log(`  ${BASE} esta en ${current} → release ${version}`);

  if (!isGreater(version, current)) fail(`${version} no es mayor que la version de ${BASE} (${current}).`);
  if (remoteTagExists('origin', tag)) fail(`El tag ${tag} ya existe en origin.`);
  if (remoteTagExists(`https://github.com/${SPM_REPO}.git`, version)) fail(`${SPM_REPO} ya tiene el tag ${version}.`);
  if (remoteBranchExists(branch)) fail(`La rama ${branch} ya existe en origin (¿hay una PR de release abierta?).`);

  const changelog = showFile(BASE_REF, 'CHANGELOG.md');
  const notes = unreleasedBody(changelog);
  if (!notes) fail('La seccion "## Unreleased" del CHANGELOG esta vacia: no hay nada que publicar.');

  const dir = mkdtempSync(join(tmpdir(), 'native-sdk-release-'));
  step(`worktree temporal en ${dir}`);
  run('git', ['worktree', 'add', '--quiet', '-b', branch, dir, BASE_REF]);
  let error = null;
  try {
    const date = new Date().toISOString().slice(0, 10);
    writeFileSync(join(dir, 'CHANGELOG.md'), promoteUnreleased(changelog, version, date));
    // bump_version.sh es la fuente de la verdad de que ficheros llevan la version.
    run('bash', ['scripts/bump_version.sh', version], { cwd: dir });
    if (gradleVersion(readFileSync(join(dir, 'gradle.properties'), 'utf8')) !== version)
      throw new Error('bump_version.sh no ha dejado PUBLISHING_VERSION en la version nueva.');
    run('git', ['add', '-A'], { cwd: dir });
    run('git', ['commit', '--quiet', '-m', `chore(release): ${version}`], { cwd: dir });
    console.log(`\n${run('git', ['show', '--stat', '--format=%h %s', 'HEAD'], { cwd: dir })}\n`);

    if (dryRun) {
      console.log(run('git', ['show', '--format=', 'HEAD', '--', 'CHANGELOG.md', 'gradle.properties'], { cwd: dir }));
      console.log('\n(dry-run) No se sube la rama ni se abre la PR.');
      return;
    }

    step(`git push origin ${branch}`);
    run('git', ['push', '--quiet', '-u', 'origin', branch], { cwd: dir });
    const body = `Release ${version}.\n\nAl fusionar: \`node scripts/release.mjs tag\`.\n\n${notes}`;
    const url = run('gh', ['pr', 'create', '--base', BASE, '--head', branch, '--title', `Release ${version}`, '--body', body], { cwd: dir });
    console.log(`\n✔ PR abierta: ${url}`);
    console.log('  Cuando este fusionada: node scripts/release.mjs tag');
  } catch (e) {
    error = e;
  } finally {
    tryRun('git', ['worktree', 'remove', '--force', dir]);
    rmSync(dir, { recursive: true, force: true });
    // Con dry-run o con error la rama local sobra (en el segundo caso no llego a subirse o
    // quedo a medias: mejor que el siguiente intento empiece limpio).
    if (dryRun || error) tryRun('git', ['branch', '-D', branch]);
  }
  if (error) fail(`${error.message}${error.stderr ? `\n${error.stderr}` : ''}`);
}

// ---------- tag ----------

async function confirm(question) {
  const rl = createInterface({ input: process.stdin, output: process.stdout });
  const answer = await rl.question(`${question} [s/N] `);
  rl.close();
  return /^(s|si|sí|y|yes)$/i.test(answer.trim());
}

async function tagRelease({ yes, watch }) {
  step('git fetch origin');
  run('git', ['fetch', '--quiet', '--prune', '--tags', 'origin']);

  const sha = run('git', ['rev-parse', BASE_REF]);
  const subject = run('git', ['log', '-1', '--format=%s', sha]);
  const version = gradleVersion(showFile(sha, 'gradle.properties'));
  const tag = `v${version}`;
  console.log(`  ${BASE} = ${sha.slice(0, 7)} "${subject}" · version ${version}`);

  if (!version || !SEMVER.test(version)) fail(`gradle.properties tiene una version no valida: ${version}`);
  if (remoteTagExists('origin', tag)) fail(`El tag ${tag} ya existe en origin: esta version ya se etiqueto.`);
  if (tryRun('git', ['rev-parse', '--verify', '--quiet', `refs/tags/${tag}`]).ok)
    fail(`El tag ${tag} existe en local pero no en origin. Borralo con: git tag -d ${tag}`);
  if (releaseExists(REPO, tag)) fail(`Ya hay una GitHub Release ${tag}.`);
  if (!versionBody(showFile(sha, 'CHANGELOG.md'), version))
    fail(`El CHANGELOG de ${BASE} no tiene la seccion "## ${version}" (o esta vacia).`);

  // Si hay una PR de release para esta version, tiene que estar fusionada.
  const pr = tryRun('gh', ['pr', 'list', '--repo', REPO, '--head', `release/${version}`, '--state', 'all', '--json', 'number,state', '--jq', '.[0] // empty']);
  if (pr.ok && pr.out) {
    const { number, state } = JSON.parse(pr.out);
    if (state !== 'MERGED') fail(`La PR #${number} (release/${version}) esta ${state}: fusionala antes de etiquetar.`);
  }

  // Avisos, no bloqueos: el workflow se salta lo que ya existe o lo que no tiene secrets.
  if (remoteTagExists(`https://github.com/${SPM_REPO}.git`, version))
    console.log(`  ⚠ ${SPM_REPO} ya tiene ${version}: el workflow no publicara iOS.`);
  if (await onMavenCentral(version)) console.log(`  ⚠ ${version} ya esta en Maven Central: el workflow no lo republicara.`);
  console.log('  (el nativo no tiene CI en dev: los tests los corre el propio workflow antes de publicar)');

  if (!yes && !(await confirm(`\nEtiquetar ${tag} sobre ${sha.slice(0, 7)} y publicar?`))) fail('Cancelado.');

  run('git', ['tag', '-a', tag, sha, '-m', tag]);
  step(`git push origin ${tag}`);
  run('git', ['push', '--quiet', 'origin', tag]);
  console.log(`✔ ${tag} subido. El workflow de release se encarga del resto.`);
  if (!watch) return;

  // Por commit y posterior al push: con un tag rehecho con el mismo nombre, buscar por el nombre
  // devolvia la ejecucion ANTERIOR (paso con v0.6.1 en el nativo).
  const pushedAt = new Date(Date.now() - 60_000).toISOString();
  let runId = '';
  for (let i = 0; i < 20 && !runId; i++) {
    await sleep(3000);
    runId = tryRun('gh', ['run', 'list', '--repo', REPO, '--workflow', 'release.yml', '--commit', sha, '--event', 'push', '--json', 'databaseId,createdAt', '--jq', `[.[] | select(.createdAt >= "${pushedAt}")][0].databaseId // empty`]).out;
  }
  if (!runId) fail(`No veo la ejecucion del workflow para ${tag}. Revisa: gh run list --workflow release.yml`);
  step(`siguiendo el workflow (gh run watch ${runId}); en macOS tarda ~30 min`);
  const watched = tryRun('gh', ['run', 'watch', runId, '--repo', REPO, '--exit-status', '--interval', '30']);
  if (!watched.ok) fail(`El workflow ha fallado:\n${tryRun('gh', ['run', 'view', runId, '--repo', REPO, '--log-failed']).out.split('\n').slice(-20).join('\n')}`);

  console.log(`\n✔ Workflow de ${tag} terminado.`);
  console.log(`  GitHub: ${releaseExists(REPO, tag) ? '✔' : '✖'} https://github.com/${REPO}/releases/tag/${tag}`);
  console.log(`  SPM:    ${releaseExists(SPM_REPO, version) ? '✔' : '✖'} https://github.com/${SPM_REPO}/releases/tag/${version}`);
  console.log(`  Maven:  ${(await onMavenCentral(version)) ? '✔' : '… aun no (tarda unos minutos, o se salto por falta de secrets)'} com.deepdots.sdk:shared-android:${version}`);
}

// ---------- main ----------

// Solo como comando: importar el modulo (tests) no debe ejecutar nada.
if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) {
  const [, , cmd, ...rest] = process.argv;
  const flags = new Set(rest.filter((a) => a.startsWith('--')));
  const positional = rest.filter((a) => !a.startsWith('--'));

  if (cmd === 'prepare') await prepare(positional[0], { dryRun: flags.has('--dry-run') });
  else if (cmd === 'tag') await tagRelease({ yes: flags.has('--yes'), watch: !flags.has('--no-watch') });
  else {
    console.log('Uso:\n  node scripts/release.mjs prepare <X.Y.Z|patch|minor|major> [--dry-run]\n  node scripts/release.mjs tag [--yes] [--no-watch]');
    process.exit(cmd ? 1 : 0);
  }
}
