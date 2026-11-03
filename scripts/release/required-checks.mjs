#!/usr/bin/env node
/**
 * Checks de la PR de release que tienen que estar en verde antes de publicar (DD-25):
 *   - "Release dry run": gate + tests + build + smoke en macOS (MF-3242);
 *   - los test apps de Android e iOS con la SDK compilada desde ese commit ("Android · SDK local"
 *     e "iOS · SDK local" de test-apps.yml, llamado por release-check.yml): un flujo de Maestro
 *     roto en cualquiera de las dos plataformas bloquea la release.
 *
 *   required-checks.mjs --sha <commit etiquetado>
 *
 * Busca la PR fusionada que metió ese commit, lee los check-runs de su último commit y sale con 1
 * si alguno no terminó en verde. Lo usan release.yml (antes de publicar) y scripts/release.mjs tag.
 */
import { execFileSync } from 'node:child_process';
import { pathToFileURL } from 'node:url';
import { releasePullFrom } from './metadata.mjs';

export const REQUIRED_CHECKS = [
  { name: 'Release dry run', match: (n) => n === 'Release dry run' },
  // Como job de un workflow reutilizable, GitHub lo llama "<job que llama> / Android · SDK local".
  { name: 'Test apps (release build) · Android', match: (n) => n.endsWith('Android · SDK local') },
  { name: 'Test apps (release build) · iOS', match: (n) => n.endsWith('iOS · SDK local') },
];

/** El check-run más reciente de cada check requerido, o `missing`. */
export function requiredChecksFrom(checkRuns = []) {
  return REQUIRED_CHECKS.map(({ name, match }) => {
    const last = checkRuns
      .filter((c) => match(c.name ?? ''))
      .sort((a, b) => String(b.started_at ?? '').localeCompare(String(a.started_at ?? '')))[0];
    return { name, conclusion: last ? (last.conclusion ?? last.status) : 'missing', url: last?.html_url ?? null };
  });
}

/** Mensajes de error de los checks que no están en verde. */
export function failingChecks(results) {
  return results.filter((r) => r.conclusion !== 'success').map((r) => `"${r.name}" ${r.conclusion}${r.url ? ` ${r.url}` : ''}`);
}

function gh(path) {
  return JSON.parse(execFileSync('gh', ['api', path], { encoding: 'utf8' }));
}

function main() {
  const at = process.argv.indexOf('--sha');
  const sha = at > 0 ? process.argv[at + 1] : null;
  if (!sha) {
    console.error('Uso: required-checks.mjs --sha <commit>');
    process.exit(2);
  }
  const repo = process.env.GITHUB_REPOSITORY ?? 'MagicFeedback/deepdots-popup-mobile-native-sdk';
  const pr = releasePullFrom(gh(`repos/${repo}/commits/${sha}/pulls`), sha);
  if (!pr) {
    console.log(`::warning::No encuentro la PR fusionada de ${sha.slice(0, 7)}: no se comprueban sus checks.`);
    return;
  }
  const head = gh(`repos/${repo}/pulls/${pr.number}`).head.sha;
  const results = requiredChecksFrom(gh(`repos/${repo}/commits/${head}/check-runs?per_page=100`).check_runs);
  for (const r of results) console.log(`[release] PR #${pr.number} · ${r.name}: ${r.conclusion}`);
  const errors = failingChecks(results);
  if (errors.length) {
    for (const e of errors) console.log(`::error::Check de la PR #${pr.number} no en verde: ${e}`);
    process.exit(1);
  }
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) main();
