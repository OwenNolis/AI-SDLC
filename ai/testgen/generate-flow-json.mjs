/**
 * AI-SDLC — Flow Test Generator
 *
 * Reads FA + TA + flow schema, calls the AI API, and writes:
 *   docs/test-scenarios/<feature-id>.flow.json
 *   docs/test-scenarios/<feature-id>.flow.md
 *
 * Usage:
 *   node ai/testgen/generate-flow-json.mjs <feature-id>
 *
 * Env vars (from .env or shell):
 *   GEMINI_API_KEY   — Google Gemini key (preferred)
 *   GEMINI_MODEL     — model override (default: gemini-2.0-flash)
 *   OPENAI_API_KEY   — fallback if no Gemini key
 *   OPENAI_MODEL     — model override (default: gpt-4o)
 */

import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import { existsSync } from "node:fs";

// ── Load .env if present ──────────────────────────────────────────────────────
const envPath = path.resolve(".env");
if (existsSync(envPath)) {
  const raw = await readFile(envPath, "utf8");
  for (const line of raw.split("\n")) {
    const m = line.match(/^\s*([A-Z_][A-Z0-9_]*)=(.*)$/);
    if (m && !process.env[m[1]]) {
      process.env[m[1]] = m[2].trim().replace(/^["']|["']$/g, "");
    }
  }
}

// ── Args ──────────────────────────────────────────────────────────────────────
const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/testgen/generate-flow-json.mjs <feature-id>");
  process.exit(1);
}

// ── Helpers ───────────────────────────────────────────────────────────────────
async function readText(p) {
  try {
    return await readFile(p, "utf8");
  } catch {
    console.error(`❌ File not found: ${p}`);
    process.exit(1);
  }
}

async function ensureDir(dir) {
  await mkdir(dir, { recursive: true });
}

// ── Load inputs ───────────────────────────────────────────────────────────────
const faPath     = path.join("docs", "functional-analysis", `${feature}.md`);
const taPath     = path.join("docs", "technical-analysis",  `${feature}.md`);
const schemaPath = path.join("ai", "schemas", "flowtests.schema.json");
const promptPath = path.join("ai", "prompts", "generate_flow_tests.md");

console.log("==============================================");
console.log("AI-SDLC — Flow Test JSON Generator");
console.log(`Feature : ${feature}`);
console.log("==============================================\n");

const [faContent, taContent, schemaContent, promptTemplate] = await Promise.all([
  readText(faPath),
  readText(taPath),
  readText(schemaPath),
  readText(promptPath),
]);

// ── Build prompt ──────────────────────────────────────────────────────────────
const basePrompt = promptTemplate
  .replace("{{FA_MARKDOWN}}", faContent)
  .replace("{{TA_MARKDOWN}}", taContent)
  .replace("{{FLOW_SCHEMA_JSON}}", schemaContent);

// Append explicit output instructions to prevent the model from returning
// the schema definition instead of actual flow data
const prompt = `${basePrompt}

---
IMPORTANT OUTPUT RULES:
- Generate REAL flow test DATA for the feature described above — do NOT output the JSON schema definition itself.
- The JSON must be a concrete document instance, not a schema. It must NOT contain "$schema", "$id", "title", "type", "properties", "required" or "additionalProperties" at the top level.
- Required top-level keys: "meta" (object with featureId and version), "feature" (string), "scenarios" (array), "flows" (array with at least one flow).
- Each flow must have: "id" (format FLOW-001), "name", "preconditions" (array), "steps" (array), "variants" (array).
- Each step must have: "actor" ("user" or "system"), "action" (string), "expected" (string).
- The "## Flow Tests (JSON)" code block must contain only the JSON data object — no schema wrapper.

Example of correct top-level structure:
\`\`\`json
{
  "meta": { "featureId": "${feature}", "version": "1.0.0" },
  "feature": "${feature}",
  "scenarios": [
    { "id": "happy_path_1", "title": "User completes main flow", "type": "happy-path" }
  ],
  "flows": [
    {
      "id": "FLOW-001",
      "name": "Happy path — main flow",
      "preconditions": ["User is authenticated"],
      "steps": [
        { "actor": "user", "action": "Navigate to the page", "expected": "Page is shown" }
      ],
      "variants": []
    }
  ]
}
\`\`\`
`;

// ── Call AI API ───────────────────────────────────────────────────────────────
let responseText = "";

if (process.env.GEMINI_API_KEY) {
  const model = process.env.GEMINI_MODEL || "gemini-2.0-flash";
  console.log(`🤖 Using Gemini (${model})...`);

  const res = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${process.env.GEMINI_API_KEY}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ role: "user", parts: [{ text: prompt }] }],
        generationConfig: { temperature: 0, maxOutputTokens: 8192 },
      }),
    }
  );

  if (!res.ok) {
    const err = await res.text();
    console.error(`❌ Gemini API error ${res.status}: ${err}`);
    process.exit(1);
  }

  const data = await res.json();
  responseText = data?.candidates?.[0]?.content?.parts?.[0]?.text ?? "";

} else if (process.env.OPENAI_API_KEY) {
  const model = process.env.OPENAI_MODEL || "gpt-4o";
  console.log(`🤖 Using OpenAI (${model})...`);

  const res = await fetch("https://api.openai.com/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
    },
    body: JSON.stringify({
      model,
      messages: [{ role: "user", content: prompt }],
      temperature: 0,
      max_tokens: 8192,
    }),
  });

  if (!res.ok) {
    const err = await res.text();
    console.error(`❌ OpenAI API error ${res.status}: ${err}`);
    process.exit(1);
  }

  const data = await res.json();
  responseText = data?.choices?.[0]?.message?.content ?? "";

} else {
  console.error("❌ No API key found. Set GEMINI_API_KEY or OPENAI_API_KEY in .env");
  process.exit(1);
}

if (!responseText) {
  console.error("❌ Empty response from AI.");
  process.exit(1);
}

// ── Extract JSON from response ────────────────────────────────────────────────
// The prompt asks for a markdown section + a JSON code block
let flowJson = null;
let flowMd   = responseText;

// Try to find a ```json ... ``` block
const jsonBlockMatch = responseText.match(/```json\s*([\s\S]*?)```/);
if (jsonBlockMatch) {
  try {
    flowJson = JSON.parse(jsonBlockMatch[1].trim());
  } catch (e) {
    console.warn("⚠️  Could not parse JSON block:", e.message);
  }
}

// Fallback: try to parse the entire response as JSON
if (!flowJson) {
  try {
    flowJson = JSON.parse(responseText.trim());
  } catch {
    // not pure JSON
  }
}

// Fallback: extract the outermost { ... } block
if (!flowJson) {
  const braceMatch = responseText.match(/(\{[\s\S]*\})/);
  if (braceMatch) {
    try {
      flowJson = JSON.parse(braceMatch[1]);
    } catch (e) {
      console.warn("⚠️  Could not parse extracted JSON block:", e.message);
    }
  }
}

if (!flowJson) {
  console.error("❌ Could not extract valid JSON from AI response.");
  console.error("--- Raw response (first 2000 chars) ---");
  console.error(responseText.slice(0, 2000));
  process.exit(1);
}

// Ensure meta.featureId is set
if (!flowJson.meta) flowJson.meta = {};
if (!flowJson.meta.featureId) flowJson.meta.featureId = feature;
if (!flowJson.meta.version)   flowJson.meta.version   = "1.0.0";
if (!flowJson.feature)        flowJson.feature         = feature;

// ── Write outputs ─────────────────────────────────────────────────────────────
const outDir  = path.join("docs", "test-scenarios");
const jsonOut = path.join(outDir, `${feature}.flow.json`);
const mdOut   = path.join(outDir, `${feature}.flow.md`);

await ensureDir(outDir);
await writeFile(jsonOut, JSON.stringify(flowJson, null, 2), "utf8");
await writeFile(mdOut, flowMd, "utf8");

const scenarioCount = flowJson.scenarios?.length ?? 0;
const flowCount     = flowJson.flows?.length ?? 0;

console.log(`\n✅ Flow JSON saved : ${jsonOut}`);
console.log(`✅ Flow MD saved   : ${mdOut}`);
console.log(`   Scenarios: ${scenarioCount} | Flows: ${flowCount}`);
console.log("\nNext steps:");
console.log(`  node ai/testgen/generate-frontend-tests.mjs ${feature}`);
console.log(`  node ai/testgen/generate-backend-tests.mjs ${feature}`);
