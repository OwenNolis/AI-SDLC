import path from "node:path";
import { readJson, writeText, pascal, camel, safeId } from "./utils.mjs";

const feature = process.argv[2];
if (!feature) {
  console.error(
    "Usage: node ai/testgen/generate-backend-tests.mjs <feature-id> [--matrix]"
  );
  process.exit(1);
}

const flags = new Set(process.argv.slice(3));
const matrixEnabled = flags.has("--matrix");

const flowPath = path.join("docs", "test-scenarios", `${feature}.flow.json`);
const taPath = path.join("docs", "technical-analysis", `${feature}.ta.json`);

const flow = await readJson(flowPath);
const ta = await readJson(taPath);

const endpoint = "/api/tickets";

const scenarios =
  Array.isArray(flow.scenarios) && flow.scenarios.length > 0
    ? flow.scenarios
    : [{ id: "create_ticket_happy_path", title: "happy path", type: "happy-path" }];

function normalize(str) {
  return String(str ?? "").toLowerCase();
}

function escapeJavadoc(s) {
  return String(s ?? "").replace(/\*\//g, "*\\/");
}

function parseConstraintNum(constraints, prefix) {
  // constraints like ["min:5","max:120", ...]
  if (!Array.isArray(constraints)) return null;
  const hit = constraints.find((c) => String(c).startsWith(prefix + ":"));
  if (!hit) return null;
  const n = Number(String(hit).split(":")[1]);
  return Number.isFinite(n) ? n : null;
}

function repeatChar(ch, n) {
  return Array.from({ length: Math.max(0, n) }, () => ch).join("");
}

function javaEscapeString(s) {
  return String(s).replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

function javaMapLinesFromObject(obj) {
  const entries = Object.entries(obj)
    .filter(([, v]) => v !== undefined)
    .map(([k, v]) => `payload.put("${k}", "${javaEscapeString(String(v))}");`)
    .join("\n        ");

  return `
        var payload = new java.util.LinkedHashMap<String, Object>();
        ${entries}
`;
}

function happyPayloadObject(overrides = {}) {
  return {
    subject: "Cannot login to portal",
    description: "I cannot login since yesterday. Please investigate.",
    priority: "HIGH",
    ...overrides,
  };
}

function invalidPayloadFromScenario(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);

  const payload = happyPayloadObject();

  if (t.includes("missing subject") || id.includes("missing_subject")) {
    delete payload.subject;
    return payload;
  }
  if (t.includes("short subject") || id.includes("short_subject") || t.includes("too short")) {
    payload.subject = "abc";
    return payload;
  }
  if (t.includes("missing description") || id.includes("missing_description")) {
    delete payload.description;
    return payload;
  }
  if (
    t.includes("short description") ||
    id.includes("short_description") ||
    (t.includes("description") && t.includes("too short"))
  ) {
    payload.description = "short";
    return payload;
  }
  if (t.includes("invalid priority") || id.includes("invalid_priority")) {
    payload.priority = "INVALID";
    return payload;
  }

  payload.subject = "abc";
  payload.description = "short";
  return payload;
}

function isUniqueScenario(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);
  return t.includes("unique") || t.includes("duplicate") || id.includes("unique") || id.includes("duplicate");
}

function isLimit3Scenario(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);
  return t.includes("3 tickets") || t.includes("limit 3") || id.includes("limit_3") || id.includes("limit3");
}

function isPriorityCompletionOrderScenario(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);
  return (
    t.includes("completed before") ||
    (t.includes("priority") && t.includes("completed")) ||
    id.includes("completion_order")
  );
}

function expectedErrorCodes(sc) {
  const t = normalize(sc.title);

  if (isLimit3Scenario(sc)) return [400, 409, 429];
  if (isUniqueScenario(sc)) return [400, 409];
  if (t.includes("priority") && (t.includes("order") || t.includes("before"))) return [400, 409];

  return [400, 422, 429];
}

// ---------- Matrix test generation (TA-driven) ----------
function collectTaFieldCases(taJson) {
  const out = [];

  const entities = taJson?.domain?.entities;
  if (!Array.isArray(entities)) return out;

  for (const e of entities) {
    const fields = e?.fields;
    if (!Array.isArray(fields)) continue;

    for (const f of fields) {
      const testCases = f?.testCases;
      if (!Array.isArray(testCases) || testCases.length === 0) continue;

      out.push({
        entity: String(e?.name ?? "Entity"),
        field: String(f?.name ?? "field"),
        type: String(f?.type ?? ""),
        constraints: Array.isArray(f?.constraints) ? f.constraints : [],
        testCases: testCases.map(String),
      });
    }
  }

  return out;
}

function buildInvalidPayloadFromTaCase(fieldInfo, tc) {
  const base = happyPayloadObject();

  const field = fieldInfo.field;
  const constraints = fieldInfo.constraints || [];

  const min = parseConstraintNum(constraints, "min");
  const max = parseConstraintNum(constraints, "max");

  if (tc === "missing") {
    delete base[field];
    return base;
  }

  if (tc === "invalid_value") {
    // for enums
    base[field] = "INVALID";
    return base;
  }

  if (tc === "empty") {
    // for required strings, empty should be invalid
    base[field] = "";
    return base;
  }

  if (tc === "too_short") {
    // use min-1 if we know min, else a safe short
    const n = min && min > 1 ? min - 2 : 3;
    base[field] = repeatChar("a", Math.max(1, n));
    return base;
  }

  if (tc === "too_long") {
    // use max+1 if we know max, else a safe large
    const n = max ? max + 1 : 5000;
    base[field] = repeatChar("a", n);
    return base;
  }

  // fallback generic invalid (should still fail)
  base[field] = "INVALID";
  return base;
}

function matrixExpectedCodes(fieldInfo, tc) {
  // keep flexible until backend stabilizes exact behavior
  // missing/empty/too_short/too_long/invalid_value => typically 400/422
  return [400, 422];
}

function makeMatrixTestMethods(featureId) {
  const fields = collectTaFieldCases(ta);

  if (fields.length === 0) return "";

  const methods = [];

  for (const f of fields) {
    for (const tc of f.testCases) {
      const sid = safeId(`matrix_${f.entity}_${f.field}_${tc}`);
      const methodName = `${camel(sid)}_rejected`;
      const title = escapeJavadoc(`${f.entity}.${f.field} -> ${tc}`);

      const invalidPayload = buildInvalidPayloadFromTaCase(f, tc);
      const codes = matrixExpectedCodes(f, tc);

      methods.push(`
    /**
     * GENERATED (TA MATRIX)
     * Traceability:
     * - Feature: ${featureId}
     * - Source: docs/technical-analysis/${featureId}.ta.json
     * - Matrix: ${title}
     */
    @Test
    void ${methodName}() {
${javaMapLinesFromObject(invalidPayload)}
        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(${codes.join(", ")});
        assertThat(res.getBody()).isNotNull();
    }
`);
    }
  }

  return methods.join("\n");
}

// ---------- Flow-based tests (existing behavior) ----------
const className = `${pascal(feature)}GeneratedIT`;

const flowTestMethods = scenarios
  .map((sc, idx) => {
    const sid = safeId(sc.id ?? `scenario_${idx + 1}`);
    const methodBase = camel(sid);
    const title = escapeJavadoc(sc.title);
    const type = normalize(sc.type);

    // HAPPY PATH
    if (type === "happy-path") {
      const methodName = `${methodBase}_returns201_created`;

      return `
    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Source: docs/test-scenarios/${feature}.flow.json
     */
    @Test
    void ${methodName}() {
${javaMapLinesFromObject(happyPayloadObject())}
        ResponseEntity<String> res = postTicket(payload);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("ticketNumber");
    }
`;
    }

    // Unique per day (2 calls)
    if (isUniqueScenario(sc)) {
      const methodName = `${methodBase}_duplicateSubject_sameDay_rejected`;

      return `
    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Rule: subject unique per day
     */
    @Test
    void ${methodName}() {
${javaMapLinesFromObject(happyPayloadObject({ subject: "Password reset not working" }))}
        ResponseEntity<String> first = postTicket(payload);
        assertThat(first.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(first.getStatusCode().value()).isIn(200, 201);

        ResponseEntity<String> second = postTicket(payload);
        assertThat(second.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(second.getStatusCode().value()).isIn(400, 409);
        assertThat(second.getBody()).isNotNull();
    }
`;
    }

    // Max 3 tickets/day (4 calls)
    if (isLimit3Scenario(sc)) {
      const methodName = `${methodBase}_limit3PerDay_rejectedOn4th`;

      return `
    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Rule: max 3 tickets per day
     */
    @Test
    void ${methodName}() {
        for (int i = 1; i <= 3; i++) {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("subject", "Limit test subject " + i);
            payload.put("description", "This is a valid description with enough characters (" + i + ").");
            payload.put("priority", "LOW");

            ResponseEntity<String> r = postTicket(payload);
            assertThat(r.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
            assertThat(r.getStatusCode().value()).isIn(200, 201);
        }

        var fourth = new java.util.LinkedHashMap<String, Object>();
        fourth.put("subject", "Limit test subject 4");
        fourth.put("description", "This is a valid description with enough characters (4).");
        fourth.put("priority", "LOW");

        ResponseEntity<String> res = postTicket(fourth);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(400, 409, 429);
        assertThat(res.getBody()).isNotNull();
    }
`;
    }

    // Priority completion order: TODO (no endpoint)
    if (isPriorityCompletionOrderScenario(sc)) {
      const methodName = `${methodBase}_priorityCompletionOrder_TODO`;

      return `
    /**
     * GENERATED (FLOW, TODO)
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     *
     * NOTE:
     * This rule requires a "complete ticket" endpoint. Current API only supports POST ${endpoint}.
     */
    @Disabled("TODO: completion endpoint not implemented; keep scenario in flow for traceability")
    @Test
    void ${methodName}() {
        assertThat(true).isTrue();
    }
`;
    }

    // Generic invalid request: 1 call
    const methodName = `${methodBase}_invalidRequest_rejected`;
    const invalid = invalidPayloadFromScenario(sc);
    const codes = expectedErrorCodes(sc);

    return `
    /**
     * GENERATED (FLOW)
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     */
    @Test
    void ${methodName}() {
${javaMapLinesFromObject(invalid)}
        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(${codes.join(", ")});
        assertThat(res.getBody()).isNotNull();
    }
`;
  })
  .join("\n");

// Matrix tests appended (optional)
const matrixTestMethods = matrixEnabled
  ? makeMatrixTestMethods(feature)
  : "";

// Helpful banner comment
const matrixBanner = matrixEnabled
  ? `
    // ------------------------------------------------------------
    // TA MATRIX TESTS ENABLED (--matrix)
    // ------------------------------------------------------------
`
  : `
    // (Tip) Run with --matrix to also generate TA validation-matrix tests:
    //   node ai/testgen/generate-backend-tests.mjs ${feature} --matrix
`;

const testClass = `package be.ap.student.tickets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ${className} {

    @Autowired
    private TestRestTemplate rest;

    private ResponseEntity<String> postTicket(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        return rest.postForEntity("${endpoint}", req, String.class);
    }

${flowTestMethods}
${matrixBanner}
${matrixTestMethods}
}
`;

const outPath = path.join(
  "backend",
  "src",
  "test",
  "java",
  "be",
  "ap",
  "student",
  "tickets",
  `${className}.java`
);

await writeText(outPath, testClass);
console.log(`✅ Backend tests generated: ${outPath}${matrixEnabled ? " (with --matrix)" : ""}`);