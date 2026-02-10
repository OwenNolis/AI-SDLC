import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import { GoogleGenerativeAI } from "@google/generative-ai";

const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/agent/sync-from-fa.mjs <feature-id>");
  process.exit(1);
}

const FA = path.join("docs", "functional-analysis", `${feature}.md`);
const TA = path.join("docs", "technical-analysis", `${feature}.ta.json`);
const FLOW = path.join("docs", "test-scenarios", `${feature}.flow.json`);
const CTX = path.join("docs", "test-context", `${feature}.md`);

async function ensureDirForFile(p) {
  await mkdir(path.dirname(p), { recursive: true });
}
async function readText(p) {
  return await readFile(p, "utf8");
}
async function readJson(p) {
  return JSON.parse(await readFile(p, "utf8"));
}
async function writeText(p, content) {
  await ensureDirForFile(p);
  await writeFile(p, content, "utf8");
}
async function writeJson(p, obj) {
  await ensureDirForFile(p);
  await writeFile(p, JSON.stringify(obj, null, 2) + "\n", "utf8");
}
async function mustExist(p, label) {
  try {
    await readFile(p);
  } catch {
    console.error(`Missing ${label}: ${p}`);
    process.exit(1);
  }
}

await mustExist(FA, "FA");
await mustExist(TA, "TA");
await mustExist(FLOW, "FLOW");
await ensureDirForFile(CTX);

const faMd = await readText(FA);
const taJson = await readJson(TA);
const flowJson = await readJson(FLOW);
const ctxMd = (await readFile(CTX).catch(() => Buffer.from(""))).toString("utf8");

// ---- Gemini setup ----
const geminiKey = process.env.GEMINI_API_KEY;
if (!geminiKey) {
  console.error("Missing GEMINI_API_KEY env var.");
  process.exit(1);
}

const geminiModel = process.env.GEMINI_MODEL || "models/gemini-2.5-flash-lite";

const genAI = new GoogleGenerativeAI(geminiKey);
const model = genAI.getGenerativeModel({
  model: geminiModel,
  generationConfig: {
    responseMimeType: "application/json",
    temperature: 0,
  },
});

// ---- Prompts ----
const system = `
You are an SDLC automation agent.

Task: Sync artifacts from Functional Analysis (FA) into:
- Technical Analysis (TA JSON) at docs/technical-analysis/<feature>.ta.json
- Flow test scenarios (flow JSON) at docs/test-scenarios/<feature>.flow.json
- Test context markdown at docs/test-context/<feature>.md

Hard rules:
- Do NOT remove existing requirements, flows, variants, traceability unless they contradict FA.
- Keep IDs stable. Do not duplicate requirements/scenarios for the same rule.
- TA must remain valid against our ta.schema.json:
  - Do not invent new top-level keys.
  - requirements[] items must have id (REQ-###), text, priority (must/should/could).
- Flow must remain valid against our flowtests.schema.json:
  - Preserve meta and flows.
  - If scenarios[] exists in the current flow.json, you may add to it, but do NOT explode duplicates.
- If a rule is not testable with existing endpoints, add a scenario/variant documenting a TODO; do not invent endpoints.

Output format:
Return ONLY a single JSON object with keys:
- notes (string)
- taJson (string)  -> FULL TA JSON as a string
- flowJson (string) -> FULL Flow JSON as a string
- testContextMd (string)

JSON MUST be strict JSON:
- double quotes only
- no trailing commas
- no single quotes
- no comments
`;

const user = `
FEATURE: ${feature}

INPUTS
1) FA (markdown):
---
${faMd}
---

2) Current TA (json):
---
${JSON.stringify(taJson, null, 2)}
---

3) Current Flow (json):
---
${JSON.stringify(flowJson, null, 2)}
---

4) Current test-context.md:
---
${ctxMd}
---

GOAL
- Update TA requirements and assumptions/notes so FA business rules are reflected.
- Update flow.json scenarios and/or FLOW-001 variants to reflect new rules.
- Update test-context.md with concise sections per new/changed rule.

Remember: do NOT duplicate requirements/scenarios for the same rule. Keep IDs stable and minimal.
Return ONLY JSON with keys: notes, taJson, flowJson, testContextMd.
`;

// ---- JSON extraction + repair ----
function extractLikelyJson(text) {
  const first = text.indexOf("{");
  const last = text.lastIndexOf("}");
  if (first === -1 || last === -1 || last <= first) return text;
  return text.slice(first, last + 1);
}

function repairCommonJsonIssues(s) {
  let t = String(s ?? "");

  // smart quotes -> normal
  t = t.replace(/[“”]/g, '"').replace(/[‘’]/g, "'");

  // remove trailing commas
  t = t.replace(/,\s*([}\]])/g, "$1");

  // common broken enum string values like "negative'"
  t = t.replace(/"type"\s*:\s*"negative'\s*,/g, '"type": "negative",');
  t = t.replace(/"type"\s*:\s*"alternate'\s*,/g, '"type": "alternate",');
  t = t.replace(/"type"\s*:\s*"data'\s*,/g, '"type": "data",');
  t = t.replace(/"type"\s*:\s*"happy-path'\s*,/g, '"type": "happy-path",');
  t = t.replace(/"type"\s*:\s*"validation'\s*,/g, '"type": "validation",');

  return t;
}

function uniqueById(items) {
  const seen = new Set();
  const out = [];
  for (const it of items || []) {
    const id = String(it?.id ?? "");
    if (!id) continue;
    if (seen.has(id)) continue;
    seen.add(id);
    out.push(it);
  }
  return out;
}

function sanitizeFlow(flow) {
  if (Array.isArray(flow.scenarios)) {
    flow.scenarios = uniqueById(flow.scenarios);
  }
  if (Array.isArray(flow.flows)) {
    for (const f of flow.flows) {
      if (Array.isArray(f.variants)) {
        const seen = new Set();
        f.variants = f.variants.filter((v) => {
          const name = String(v?.name ?? "");
          if (!name) return true;
          if (seen.has(name)) return false;
          seen.add(name);
          return true;
        });
      }
    }
  }
  return flow;
}

function sanitizeTA(ta) {
  if (Array.isArray(ta.requirements)) {
    ta.requirements = uniqueById(ta.requirements);
  }
  return ta;
}

async function geminiCall(prompt) {
  try {
    const result = await model.generateContent(prompt);
    return result.response.text();
  } catch (e) {
    console.error("Gemini API call failed:", e?.message ?? e);
    process.exit(1);
  }
}

// ---- Run ----
const prompt = `${system}\n\n${user}`;
const raw = await geminiCall(prompt);

// Parse outer JSON
let parsed;
{
  const candidate = repairCommonJsonIssues(extractLikelyJson(raw));
  try {
    parsed = JSON.parse(candidate);
  } catch {
    console.error("Gemini returned non-JSON (or unrecoverable JSON):");
    console.error(raw);
    process.exit(1);
  }
}

for (const k of ["notes", "taJson", "flowJson", "testContextMd"]) {
  if (!(k in parsed)) {
    console.error(`Gemini response missing key '${k}'.`);
    console.error(JSON.stringify(parsed, null, 2));
    process.exit(1);
  }
}

// Parse TA + Flow strings
let newTa, newFlow;
try {
  const taStr = repairCommonJsonIssues(extractLikelyJson(String(parsed.taJson)));
  newTa = JSON.parse(taStr);
} catch {
  console.error("Invalid taJson returned by Gemini:");
  console.error(parsed.taJson);
  process.exit(1);
}

try {
  const flowStr = repairCommonJsonIssues(extractLikelyJson(String(parsed.flowJson)));
  newFlow = JSON.parse(flowStr);
} catch {
  console.error("Invalid flowJson returned by Gemini:");
  console.error(parsed.flowJson);
  process.exit(1);
}

// sanitize (avoid explosion)
newTa = sanitizeTA(newTa);
newFlow = sanitizeFlow(newFlow);

// write outputs
await writeJson(TA, newTa);
await writeJson(FLOW, newFlow);
await writeText(CTX, String(parsed.testContextMd ?? ""));

console.log(`✅ Synced from FA using Gemini (${geminiModel}).`);
console.log(String(parsed.notes ?? "").trim());