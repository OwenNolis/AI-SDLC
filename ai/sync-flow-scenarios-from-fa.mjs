import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/sync-flow-scenarios-from-fa.mjs <feature-id>");
  process.exit(1);
}

const faPath = path.join("docs", "functional-analysis", `${feature}.md`);
const flowPath = path.join("docs", "test-scenarios", `${feature}.flow.json`);

function slug(s) {
  return String(s)
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .slice(0, 60);
}

function extractAdditionalBusinessRules(md) {
  const lines = md.split(/\r?\n/);

  // Find "### Additional business rules"
  const startIdx = lines.findIndex((l) =>
    l.trim().toLowerCase().startsWith("### additional business rules")
  );
  if (startIdx === -1) return [];

  // Collect bullet lines until next heading (## or ###) or end
  const rules = [];
  for (let i = startIdx + 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (line.startsWith("## ") || line.startsWith("### ")) break;
    if (line.startsWith("- ")) {
      const text = line.slice(2).trim();
      if (text) rules.push(text);
    }
  }
  return rules;
}

function scenarioTypeFromRule(ruleText) {
  // Simple heuristic:
  // - contains "must" or "only" or "before" => validation/negative-ish (usually rule enforcement)
  // - otherwise -> validation
  const t = ruleText.toLowerCase();
  if (t.includes("only") || t.includes("must") || t.includes("before") || t.includes("unique") || t.includes("limit")) {
    return "validation";
  }
  return "validation";
}

const md = await readFile(faPath, "utf8");
const rules = extractAdditionalBusinessRules(md);

const flow = JSON.parse(await readFile(flowPath, "utf8"));
if (!Array.isArray(flow.scenarios)) flow.scenarios = [];

const existingIds = new Set(flow.scenarios.map((s) => s.id));
let added = 0;

for (const rule of rules) {
  const id = `br_${slug(rule)}`;
  if (existingIds.has(id)) continue;

  flow.scenarios.push({
    id,
    title: `Business rule: ${rule}`,
    type: scenarioTypeFromRule(rule),
  });

  existingIds.add(id);
  added++;
}

if (added > 0) {
  await writeFile(flowPath, JSON.stringify(flow, null, 2) + "\n", "utf8");
}

console.log(`✅ Synced scenarios from FA. Added: ${added}`);