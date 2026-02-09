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

// --- robust JSON extraction (handles ```json ... ```) ---
function extractJson(text) {
  if (!text) return null;

  // remove code fences if present
  const fence = text.match(/```(?:json)?\s*([\s\S]*?)\s*```/i);
  if (fence?.[1]) return fence[1].trim();

  // otherwise try to locate first JSON object in text
  const firstBrace = text.indexOf("{");
  const lastBrace = text.lastIndexOf("}");
  if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
    return text.slice(firstBrace, lastBrace + 1).trim();
  }

  return text.trim();
}

// Accept either a JSON string or object
function normalizeJsonField(value, label) {
  if (value == null) {
    throw new Error(`Missing field '${label}'`);
  }
  if (typeof value === "string") {
    try {
      return JSON.parse(value);
    } catch (e) {
      throw new Error(`Field '${label}' is a string but not valid JSON.`);
    }
  }
  if (typeof value === "object") {
    return value; // already parsed JSON object
  }
  throw new Error(`Field '${label}' must be a JSON string or object.`);
}

await mustExist(FA, "FA");
await mustExist(TA, "TA");
await mustExist(FLOW, "FLOW");
await ensureDirForFile(CTX);

const faMd = await readText(FA);
const taJson = await readJson(TA);
const flowJson = await readJson(FLOW);
const ctxMd = (await readFile(CTX).catch(() => Buffer.from(""))).toString("utf8");

// --- Gemini setup ---
const geminiKey = process.env.GEMINI_API_KEY;
if (!geminiKey) {
  console.error("Missing GEMINI_API_KEY environment variable.");
  process.exit(1);
}

const modelName = process.env.GEMINI_MODEL || "gemini-2.5-flash-lite";
const genAI = new GoogleGenerativeAI(geminiKey);

const model = genAI.getGenerativeModel({
  model: modelName,
  generationConfig: {
    // This helps, but we still parse defensively.
    responseMimeType: "application/json",
    temperature: 0.2
  }
});

// --- prompt (same contract as OpenAI version) ---
const system = `
You are an SDLC automation agent.
Task: sync artifacts from Functional Analysis (FA) into Technical Analysis (TA), Flow Test Scenarios (flow.json), and test-context.md.

Hard rules:
- Keep existing content. Only add/modify what's needed to reflect new/changed business rules and requirements.
- TA must remain valid against our ta.schema.json (do not invent random top-level keys).
- Flow must remain valid against our flowtests.schema.json.
- Do NOT remove existing requirements, flows, variants, or traceability unless they contradict FA.
- Newly added requirements must follow "REQ-###" (3 digits) and include "priority" (must/should/could).
- If FA adds a rule not testable with existing endpoints, still reflect it in TA + flow as a scenario/variant that documents the gap/TODO; do not invent endpoints.

Output format:
Return ONLY valid JSON (double quotes) with keys:
- notes (string)
- taJson (either a JSON string OR a JSON object)
- flowJson (either a JSON string OR a JSON object)
- testContextMd (string)
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

Return ONLY JSON.
`;

let text;
try {
  const result = await model.generateContent(`${system}\n\n${user}`);
  text = result?.response?.text?.() ?? "";
} catch (e) {
  console.error("Gemini API call failed:", e?.message ?? e);
  process.exit(1);
}

const jsonText = extractJson(text);

let parsed;
try {
  parsed = JSON.parse(jsonText);
} catch (e) {
  console.error("Gemini returned non-JSON:");
  console.error(text);
  process.exit(1);
}

// Validate required keys exist
for (const k of ["notes", "taJson", "flowJson", "testContextMd"]) {
  if (!(k in parsed)) {
    console.error(`Gemini response missing key '${k}'. Full response:`);
    console.error(JSON.stringify(parsed, null, 2));
    process.exit(1);
  }
}

// Normalize ta/flow which may be string OR object
let newTa, newFlow;
try {
  newTa = normalizeJsonField(parsed.taJson, "taJson");
} catch (e) {
  console.error("Invalid taJson returned by Gemini:");
  console.error(parsed.taJson);
  console.error(String(e?.message ?? e));
  process.exit(1);
}

try {
  newFlow = normalizeJsonField(parsed.flowJson, "flowJson");
} catch (e) {
  console.error("Invalid flowJson returned by Gemini:");
  console.error(parsed.flowJson);
  console.error(String(e?.message ?? e));
  process.exit(1);
}

// Write outputs
await writeJson(TA, newTa);
await writeJson(FLOW, newFlow);
await writeText(CTX, parsed.testContextMd);

console.log(`✅ Synced from FA using Gemini (${modelName}).`);
console.log(parsed.notes);