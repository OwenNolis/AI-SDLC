// Validation of all Technical Analyses + Flow JSON files against schemas
import { readFile, readdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import Ajv from "ajv/dist/2020.js";
import addFormats from "ajv-formats";

// --- Resolve repo root reliably ---
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// ai/validator → ai → repo root
const repoRoot = path.resolve(__dirname, "..", "..");

const schemasDir = path.join(repoRoot, "ai", "schemas");
const taDir = path.join(repoRoot, "docs", "technical-analysis");
const flowDir = path.join(repoRoot, "docs", "test-scenarios");

// --- AJV setup ---
const ajv = new Ajv({ allErrors: true });
addFormats(ajv);

// --- Find schema files dynamically (no hard-coded names) ---
async function findSchemaFile(dir, predicate) {
  const files = await readdir(dir);
  const hit = files.find(predicate);
  if (!hit) {
    throw new Error(
      `No matching schema found in ${dir}. Files: ${files.join(", ")}`
    );
  }
  return path.join(dir, hit);
}

const taSchemaPath = await findSchemaFile(
  schemasDir,
  (f) =>
    (f.toLowerCase().includes("ta") || f.toLowerCase().includes("technical")) &&
    f.endsWith(".json")
);

const flowSchemaPath = await findSchemaFile(
  schemasDir,
  (f) =>
    (f.toLowerCase().includes("flow") ||
      f.toLowerCase().includes("flowtest") ||
      f.toLowerCase().includes("flowtests") ||
      f.toLowerCase().includes("test")) &&
    f.endsWith(".json")
);

const taSchema = JSON.parse(await readFile(taSchemaPath, "utf8"));
const flowSchema = JSON.parse(await readFile(flowSchemaPath, "utf8"));

console.log("Using TA schema:", path.basename(taSchemaPath));
console.log("Using Flow schema:", path.basename(flowSchemaPath));

const validateTA = ajv.compile(taSchema);
const validateFlow = ajv.compile(flowSchema);

let hasErrors = false;

async function validateDir(dir, validator, label) {
  const files = (await readdir(dir)).filter((f) => f.endsWith(".json"));

  for (const file of files) {
    const fullPath = path.join(dir, file);
    const data = JSON.parse(await readFile(fullPath, "utf8"));

    const valid = validator(data);
    if (!valid) {
      hasErrors = true;
      console.error(`❌ ${label} invalid: ${file}`);
      console.error(validator.errors);
    } else {
      console.log(`✅ ${label} valid: ${file}`);
    }
  }
}

await validateDir(taDir, validateTA, "TA");
await validateDir(flowDir, validateFlow, "Flow");

if (hasErrors) {
  console.error("\nValidation failed. Fix JSON before generating tests.");
  process.exit(1);
}

console.log("\nAll AI artifacts validated successfully.");