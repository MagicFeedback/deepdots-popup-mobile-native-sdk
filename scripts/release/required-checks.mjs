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
 *
 * Sin evidencia no se publica (DD-27): sin PR de release fusionada, o si el árbol del commit
 * etiquetado no es el del commit que probó la PR, sale con 1 y no hay excepción posible. Los checks
 * en rojo solo se saltan con la excepción de emergencia (override.mjs), que queda en las notas.
 *
 *   required-checks.mjs --sha <commit> [--notes <release notes>]
 */
import { appendFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { pathToFileURL } from 'node:url';
import { releasePullFrom } from './metadata.mjs';
import { evaluateOverride, labelledBy, overrideNotes } from './override.mjs';

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

/**
 * Evalúa el gate para el commit etiquetado. `api(path)` = GET a la API de GitHub (JSON).
 * Devuelve {ok, lines, errors, override}: `lines` para el log, `errors` lo que bloquea.
 */
export function evaluateRelease({ repo, sha, api }) {
  const short = sha.slice(0, 7);
  const pr = releasePullFrom(api(`repos/${repo}/commits/${sha}/pulls`) ?? [], sha);
  if (!pr) return { ok: false, lines: [], errors: [`No hay PR fusionada para ${short}: sin PR de release no hay evidencia de tests y no se publica.`] };
  const full = api(`repos/${repo}/pulls/${pr.number}`);
  const head = full.head.sha;
  // Lo publicado tiene que ser lo probado: el árbol del commit del tag = el del último commit de la PR.
  const tagTree = api(`repos/${repo}/commits/${sha}`).commit.tree.sha;
  const headTree = api(`repos/${repo}/commits/${head}`).commit.tree.sha;
  if (tagTree !== headTree)
    return { ok: false, lines: [], errors: [`El commit ${short} no es el que probó la PR #${pr.number} (${head.slice(0, 7)}): árboles distintos. Rehaz la PR de release sobre dev.`] };
  const results = requiredChecksFrom(api(`repos/${repo}/commits/${head}/check-runs?per_page=100`).check_runs);
  const lines = [`PR #${pr.number} · ${head.slice(0, 7)} = ${short} (mismo árbol)`, ...results.map((r) => `PR #${pr.number} · ${r.name}: ${r.conclusion}`)];
  const failing = failingChecks(results);
  if (!failing.length) return { ok: true, lines, errors: [] };
  const errors = failing.map((e) => `Check de la PR #${pr.number} no en verde: ${e}`);
  const events = api(`repos/${repo}/issues/${pr.number}/events?per_page=100`) ?? [];
  const labeller = labelledBy(events, full.labels ?? [])?.login;
  let permission = null;
  if (labeller) permission = api(`repos/${repo}/collaborators/${labeller}/permission`)?.permission ?? null;
  const override = evaluateOverride({ pr: full, events, permission });
  if (override.granted) return { ok: true, lines, errors, override: { ...override, pr: pr.number, failing } };
  return { ok: false, lines, errors: [...errors, ...override.problems.map((p) => `Excepción de emergencia no válida: ${p}`)] };
}

function main() {
  const arg = (name) => {
    const at = process.argv.indexOf(name);
    return at > 0 ? process.argv[at + 1] : null;
  };
  const sha = arg('--sha');
  if (!sha) {
    console.error('Uso: required-checks.mjs --sha <commit> [--notes <release notes>]');
    process.exit(2);
  }
  const repo = process.env.GITHUB_REPOSITORY ?? 'MagicFeedback/deepdots-popup-mobile-native-sdk';
  const { ok, lines, errors, override } = evaluateRelease({ repo, sha, api: gh });
  for (const l of lines) console.log(`[release] ${l}`);
  if (override) {
    for (const e of errors) console.log(`::warning::${e}`);
    console.log(`::warning::Excepción de emergencia aprobada por ${override.by} en la PR #${override.pr}: ${override.reason}`);
    const notes = arg('--notes');
    if (notes) appendFileSync(notes, overrideNotes(override));
  } else {
    for (const e of errors) console.log(`::error::${e}`);
  }
  if (!ok) process.exit(1);
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) main();
