import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";

export async function readJson(p) {
  return JSON.parse(await readFile(p, "utf8"));
}

export async function ensureDir(dir) {
  await mkdir(dir, { recursive: true });
}

export async function writeText(filePath, content) {
  await ensureDir(path.dirname(filePath));
  await writeFile(filePath, content, "utf8");
}

export function pascal(s) {
  return s
    .split(/[^a-zA-Z0-9]+/)
    .filter(Boolean)
    .map((p) => p[0].toUpperCase() + p.slice(1))
    .join("");
}

export function camel(s) {
  const p = pascal(s);
  return p ? p[0].toLowerCase() + p.slice(1) : "scenario";
}

export function safeId(s) {
  return String(s ?? "scenario").replace(/[^a-zA-Z0-9_]/g, "_");
}