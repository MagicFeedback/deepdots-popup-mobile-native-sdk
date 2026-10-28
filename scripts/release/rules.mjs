/**
 * Reglas de release del SDK nativo (MF-3242). Partes puras, sin git ni sistema de ficheros,
 * para poder testearlas; check.mjs las alimenta. Espejo de scripts/release/rules.mjs del SDK
 * web (MF-3241): lo que cambia es de donde sale la version (gradle.properties), que rutas
 * acaban en el AAR / XCFramework y el formato del CHANGELOG ("## X.Y.Z - fecha").
 *
 * Flujo: cada PR que cambia el SDK anade sus notas a `## Unreleased`; la version solo sube en
 * la PR de release (`node scripts/release.mjs prepare`), que convierte Unreleased en
 * `## X.Y.Z - YYYY-MM-DD`. Una PR a `main` con cambios del SDK es un hotfix y sube la version.
 */

const SEMVER = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-[0-9A-Za-z.-]+)?$/;

export function isSemver(version) {
  return SEMVER.test(String(version ?? ''));
}

/** Negativo si a < b, 0 si iguales, positivo si a > b. Una prerelease va por detras de su final. */
export function compareVersions(a, b) {
  const [pa, pb] = [SEMVER.exec(a), SEMVER.exec(b)];
  if (!pa || !pb) throw new Error(`Version no valida: ${pa ? b : a}`);
  for (let i = 1; i <= 3; i++) {
    if (Number(pa[i]) !== Number(pb[i])) return Number(pa[i]) - Number(pb[i]);
  }
  if (pa[4] === pb[4]) return 0;
  if (!pa[4]) return 1;
  if (!pb[4]) return -1;
  return pa[4] < pb[4] ? -1 : 1;
}

// ---------- gradle.properties / libs.versions.toml ----------

/** `clave=valor` de un .properties (sin comentarios). */
export function parseProperties(text) {
  const props = {};
  for (const line of String(text ?? '').split('\n')) {
    const m = /^\s*([^#!\s][^=]*?)\s*=\s*(.*?)\s*$/.exec(line);
    if (m) props[m[1]] = m[2];
  }
  return props;
}

export function gradleVersion(text) {
  return parseProperties(text).PUBLISHING_VERSION ?? null;
}

/** Seccion `[versions]` de gradle/libs.versions.toml. */
export function tomlVersions(text) {
  const versions = {};
  let inVersions = false;
  for (const line of String(text ?? '').split('\n')) {
    const section = /^\s*\[([^\]]+)\]/.exec(line);
    if (section) {
      inVersions = section[1].trim() === 'versions';
      continue;
    }
    const m = inVersions && /^\s*([A-Za-z0-9_.-]+)\s*=\s*"([^"]*)"/.exec(line);
    if (m) versions[m[1]] = m[2];
  }
  return versions;
}

// ---------- que cuenta como cambio del SDK ----------

/**
 * Rutas que acaban en el AAR o en el XCFramework, o deciden como se construyen. Tests, docs,
 * apps de ejemplo, CI y los propios scripts de release pueden cambiar sin release.
 *
 * gradle.properties no esta: cambia siempre con el bump, asi que se compara sin
 * PUBLISHING_VERSION en propertiesChanged().
 */
const SDK_PATHS = [
  /^shared\/src\/(commonMain|androidMain|iosMain|iosArm64Main|iosSimulatorArm64Main)\//,
  /^shared\/build\.gradle\.kts$/,
  /^shared\/(consumer-rules|proguard-rules)\.pro$/,
  /^consumer-rules\.pro$/,
  /^build\.gradle\.kts$/,
  /^settings\.gradle\.kts$/,
  /^gradle\/libs\.versions\.toml$/,
  /^gradle\/wrapper\//,
];

export function isSdkPath(path) {
  return SDK_PATHS.some((re) => re.test(path));
}

/** Si gradle.properties cambia en algo mas que la version (flags de Kotlin, AGP, grupo...). */
export function propertiesChanged(baseText, headText) {
  const strip = (text) => {
    const { PUBLISHING_VERSION, ...rest } = parseProperties(text);
    return JSON.stringify(Object.entries(rest).sort(([a], [b]) => (a < b ? -1 : 1)));
  };
  return strip(baseText) !== strip(headText);
}

/** Ficheros (y gradle.properties) que hacen de esta PR un cambio del SDK publicado. */
export function sdkChanges({ changedFiles, baseProps, headProps }) {
  const reasons = changedFiles.filter(isSdkPath);
  if (propertiesChanged(baseProps, headProps)) reasons.push('gradle.properties (configuracion de build)');
  return reasons;
}

// ---------- CHANGELOG ----------

// `## 0.7.3 - 2026-10-23` (formato del nativo). Se acepta tambien `[0.7.3]` y `—`.
const HEADING = /^## \[?([^\]\s]+)\]?(?:\s+[—-]\s+(\S+))?\s*$/;

/** Secciones del CHANGELOG en orden: {version, date, body}. Unreleased incluida. */
export function parseChangelog(text) {
  const sections = [];
  let current = null;
  for (const line of String(text ?? '').split('\n')) {
    const heading = HEADING.exec(line);
    if (heading) {
      current = { version: heading[1].trim(), date: heading[2] ?? null, lines: [] };
      sections.push(current);
      continue;
    }
    if (/^## /.test(line) || /^\[[^\]]+\]:\s/.test(line)) {
      current = null;
      continue;
    }
    current?.lines.push(line);
  }
  return sections.map(({ lines, ...s }) => ({ ...s, body: lines.join('\n').trim() }));
}

/** Notas con contenido real: sin cabeceras `###` ni bullets vacios. */
export function hasNotes(body) {
  return Boolean(
    String(body ?? '')
      .replace(/^#+.*$/gm, '')
      .replace(/^\s*[-*]\s*$/gm, '')
      .trim(),
  );
}

export function unreleasedNotes(changelog) {
  return parseChangelog(changelog).find((s) => s.version.toLowerCase() === 'unreleased')?.body ?? '';
}

function isValidDate(date) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date ?? '')) return false;
  const parsed = new Date(`${date}T00:00:00Z`);
  return !Number.isNaN(parsed.getTime()) && parsed.toISOString().startsWith(date);
}

/**
 * La seccion de `version`: existe, tiene fecha y dice algo. Ademas tiene que ir por encima de
 * las releases menores: un parche de una linea antigua (0.6.3 con 0.7.x publicada) va debajo
 * de las 0.7.x, en su sitio.
 */
export function checkReleaseEntry(changelog, version) {
  const releases = parseChangelog(changelog).filter((s) => s.version.toLowerCase() !== 'unreleased');
  const entry = releases.find((s) => s.version === version) ?? null;
  const errors = [];
  if (!entry) {
    errors.push(`CHANGELOG.md no tiene seccion "## ${version} - YYYY-MM-DD" (release notes).`);
    return { entry, errors };
  }
  const below = releases.slice(releases.indexOf(entry) + 1).find((s) => isSemver(s.version) && compareVersions(s.version, version) > 0);
  const above = releases.slice(0, releases.indexOf(entry)).find((s) => isSemver(s.version) && compareVersions(s.version, version) < 0);
  if (above || below) {
    errors.push(`La seccion ${version} esta fuera de orden en el CHANGELOG (${above ? `encima esta ${above.version}` : `debajo esta ${below.version}`}).`);
  }
  if (!isValidDate(entry.date)) errors.push(`La seccion ${version} necesita fecha: "## ${version} - YYYY-MM-DD".`);
  if (!hasNotes(entry.body)) errors.push(`La seccion ${version} del CHANGELOG esta vacia: faltan las release notes.`);
  return { entry, errors };
}

// ---------- regla de PR ----------

/**
 * Regla de las PRs a dev y main. Devuelve {kind, reasons, errors}:
 *  - kind 'release': la PR sube la version (PR de release o hotfix);
 *  - kind 'change': cambia el SDK sin subir version (solo valido hacia dev);
 *  - kind 'none': no toca el SDK.
 */
export function checkPullRequest({
  baseBranch,
  changedFiles,
  baseProps,
  headProps,
  baseChangelog,
  headChangelog,
  tagExists = false,
  tagMatchesHead = false,
}) {
  const reasons = sdkChanges({ changedFiles, baseProps, headProps });
  const errors = [];
  const [base, head] = [gradleVersion(baseProps), gradleVersion(headProps)];

  if (!isSemver(head)) {
    errors.push(`PUBLISHING_VERSION de gradle.properties ("${head}") no es semver X.Y.Z.`);
    return { kind: 'release', reasons, errors };
  }

  if (base !== head) {
    if (isSemver(base) && compareVersions(head, base) <= 0) {
      errors.push(`La version baja o se repite: ${base} en ${baseBranch}, ${head} en esta rama.`);
    }
    errors.push(...checkReleaseEntry(headChangelog, head).errors);
    if (parseChangelog(baseChangelog).some((s) => s.version === head)) {
      errors.push(`${baseBranch} ya tiene la seccion ${head} en el CHANGELOG: esa version esta cogida.`);
    }
    if (hasNotes(unreleasedNotes(headChangelog))) {
      errors.push('Queda contenido en "## Unreleased": la PR de release tiene que moverlo a la seccion de la version (node scripts/release.mjs prepare).');
    }
    if (tagExists && !tagMatchesHead) errors.push(`v${head} ya esta publicada con otro codigo. Usa una version nueva.`);
    return { kind: 'release', reasons, errors };
  }

  if (!reasons.length) return { kind: 'none', reasons, errors };

  // Mismo numero de version: hacia main es publicar codigo distinto bajo una version que ya
  // existe, salvo que sea exactamente lo etiquetado (promocion de una release de dev a main).
  if (baseBranch === 'main') {
    if (!(tagExists && tagMatchesHead)) {
      errors.push(`Cambia el SDK sin subir la version (${head}). Hacia main eso es un hotfix: sube la version y anade su seccion al CHANGELOG.`);
    }
    return { kind: 'change', reasons, errors };
  }

  const before = unreleasedNotes(baseChangelog);
  const after = unreleasedNotes(headChangelog);
  if (!hasNotes(after) || after === before) {
    errors.push('Cambia el SDK pero no anade release notes en "## Unreleased" de CHANGELOG.md.');
  }
  return { kind: 'change', reasons, errors };
}

// ---------- manifiesto de compatibilidad ----------

/**
 * Comprobaciones del manifiesto que no expresa el JSON Schema: que diga lo mismo que el
 * codigo. `facts` sale de los ficheros del repo (manifest.mjs > repoFacts).
 */
export function checkManifestConsistency({ manifest, facts }) {
  const errors = [];
  const expect = (label, actual, wanted, where) => {
    if (wanted == null) {
      errors.push(`No encuentro ${label} en ${where}: no se puede contrastar el manifiesto.`);
    } else if (String(actual) !== String(wanted)) {
      errors.push(`${label} es ${actual} en release/compatibility.json pero ${wanted} en ${where}.`);
    }
  };
  expect('version', manifest.version, facts.version, 'gradle.properties (PUBLISHING_VERSION)');
  expect('surveyRenderer.version', manifest.surveyRenderer?.version, facts.magicfeedbackVersion, 'MagicFeedbackHtml.kt (MAGICFEEDBACK_VERSION)');
  expect('popupSdkCss.version', manifest.popupSdkCss?.version, facts.popupSdkCssVersion, 'MagicFeedbackHtml.kt (POPUP_SDK_CSS_VERSION)');
  expect('hosts.android.minSdk', manifest.hosts?.android?.minSdk, facts.minSdk, 'gradle/libs.versions.toml (android-minSdk)');
  expect('hosts.android.compileSdk', manifest.hosts?.android?.compileSdk, facts.compileSdk, 'gradle/libs.versions.toml (android-compileSdk)');
  expect('hosts.android.targetSdk', manifest.hosts?.android?.targetSdk, facts.targetSdk, 'gradle/libs.versions.toml (android-targetSdk)');
  expect('hosts.ios.minimum', manifest.hosts?.ios?.minimum, facts.iosSpmMinimum, 'scripts/prepare_spm_release.sh (platforms del Package.swift)');
  expect('hosts.ios.minimum', manifest.hosts?.ios?.minimum, facts.iosLinkerMinimum, 'shared/build.gradle.kts (-mios-version-min)');
  for (const [key, toml] of [['kotlin', 'kotlin'], ['composeMultiplatform', 'composeMultiplatform'], ['agp', 'agp']]) {
    expect(`toolchain.${key}`, manifest.toolchain?.[key], facts.toolchain?.[toml], `gradle/libs.versions.toml (${toml})`);
  }
  const ids = manifest.fixtures?.map((f) => f.id) ?? [];
  const dup = ids.find((id, i) => ids.indexOf(id) !== i);
  if (dup) errors.push(`Fixture repetido en release/compatibility.json: ${dup}.`);
  const issues = manifest.knownIssues?.map((k) => k.id) ?? [];
  const dupIssue = issues.find((id, i) => issues.indexOf(id) !== i);
  if (dupIssue) errors.push(`Known issue repetido en release/compatibility.json: ${dupIssue}.`);
  return errors;
}

// ---------- regla de publicacion ----------

/**
 * Antes de publicar: el tag es la version, la version sube respecto a lo ya publicado en su
 * linea X.Y (asi cabe un parche de una minor anterior, ver docs/VERSIONING.md) y el CHANGELOG
 * tiene sus notas. Devuelve {errors, latest}: Maven Central y SPM no tienen dist-tags, asi que
 * un parche de una linea antigua o una prerelease se publican sin marcarse como "latest" en
 * GitHub y sin fusionarse en main.
 */
export function checkRelease({ tag, version, changelog, previousTags = [] }) {
  const errors = [];
  if (!isSemver(version)) {
    errors.push(`PUBLISHING_VERSION de gradle.properties ("${version}") no es semver.`);
    return { errors, latest: false };
  }
  if (tag !== `v${version}`) errors.push(`El tag ${tag} no coincide con PUBLISHING_VERSION (${version}).`);

  const released = previousTags
    .map((t) => t.replace(/^v/, ''))
    .filter((v) => isSemver(v) && v !== version)
    .sort(compareVersions);
  const line = (v) => v.split('.').slice(0, 2).join('.');
  const latestInLine = released.filter((v) => line(v) === line(version)).at(-1);
  if (latestInLine && compareVersions(version, latestInLine) <= 0) {
    errors.push(`Sin bump: ${version} no es mayor que la ultima release de la linea ${line(version)} (v${latestInLine}).`);
  }
  errors.push(...checkReleaseEntry(changelog, version).errors);

  const latest = released.filter((v) => !v.includes('-')).at(-1);
  return { errors, latest: !version.includes('-') && !(latest && compareVersions(version, latest) < 0) };
}
