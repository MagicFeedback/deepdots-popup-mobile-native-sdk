import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const targetFile = path.resolve(
  __dirname,
  "../node_modules/@astrojs/starlight/utils/head.ts"
);

const originalLine = "let head = HeadSchema.parse(defaults);";
const patchedLine = "let head = defaults as HeadConfig;";

try {
  const source = await readFile(targetFile, "utf8");

  if (source.includes(patchedLine)) {
    console.log("Starlight head patch already applied.");
    process.exit(0);
  }

  if (!source.includes(originalLine)) {
    console.warn("Starlight head patch target not found. Skipping.");
    process.exit(0);
  }

  const patchedSource = source.replace(originalLine, patchedLine);
  await writeFile(targetFile, patchedSource, "utf8");
  console.log("Applied Starlight head patch.");
} catch (error) {
  console.warn("Unable to apply Starlight head patch:", error);
}
