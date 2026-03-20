import { cp, mkdir, rm, stat } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { spawn } from "node:child_process";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const docsRoot = path.resolve(__dirname, "..");
const outputDir = path.join(docsRoot, ".site");
const distDir = path.join(docsRoot, "dist");
const publicDemoDir = path.resolve(docsRoot, "..", "demo");

function run(command, args) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: docsRoot,
      stdio: "inherit",
      env: process.env
    });

    child.on("close", (code) => {
      if (code === 0) {
        resolve();
        return;
      }
      reject(new Error(`${command} ${args.join(" ")} exited with code ${code}`));
    });
  });
}

async function copyIfExists(sourceDir, targetDir) {
  try {
    const sourceStats = await stat(sourceDir);
    if (!sourceStats.isDirectory()) {
      return false;
    }
    await mkdir(targetDir, { recursive: true });
    await cp(sourceDir, targetDir, { recursive: true });
    return true;
  } catch {
    return false;
  }
}

await run("npm", ["run", "build"]);
await rm(outputDir, { recursive: true, force: true });
await cp(distDir, outputDir, { recursive: true });
await copyIfExists(publicDemoDir, path.join(outputDir, "demo"));
