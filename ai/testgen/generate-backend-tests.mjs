import path from "node:path";
import { readJson, writeText, pascal, camel, safeId } from "./utils.mjs";

const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/testgen/generate-backend-tests.mjs <feature-id>");
  process.exit(1);
}

const flowPath = path.join("docs", "test-scenarios", `${feature}.flow.json`);
const taPath = path.join("docs", "technical-analysis", `${feature}.ta.json`);

const flow = await readJson(flowPath);
await readJson(taPath); // keep read for traceability even if unused for now

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

function happyPayloadJavaMapLines(overrides = {}) {
  const base = {
    subject: "Cannot login to portal",
    description: "I cannot login since yesterday. Please investigate.",
    priority: "HIGH",
    ...overrides,
  };

  const entries = Object.entries(base)
    .filter(([, v]) => v !== undefined)
    .map(([k, v]) => `payload.put("${k}", "${String(v).replace(/"/g, '\\"')}");`)
    .join("\n        ");

  return `
        var payload = new java.util.LinkedHashMap<String, Object>();
        ${entries}
`;
}

function invalidPayloadFromScenario(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);

  // start from happy then mutate
  const payload = {
    subject: "Cannot login to portal",
    description: "I cannot login since yesterday. Please investigate.",
    priority: "HIGH",
  };

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
  if (t.includes("short description") || id.includes("short_description") || t.includes("too short")) {
    payload.description = "short";
    return payload;
  }
  if (t.includes("invalid priority") || id.includes("invalid_priority")) {
    payload.priority = "INVALID";
    return payload;
  }

  // generic invalid
  payload.subject = "abc";
  payload.description = "short";
  return payload;
}

function payloadToJavaMapLines(obj) {
  const entries = Object.entries(obj)
    .filter(([, v]) => v !== undefined)
    .map(([k, v]) => `payload.put("${k}", "${String(v).replace(/"/g, '\\"')}");`)
    .join("\n        ");

  return `
        var payload = new java.util.LinkedHashMap<String, Object>();
        ${entries}
`;
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
  return (t.includes("completed before") || (t.includes("priority") && t.includes("completed")) || id.includes("completion_order"));
}

function expectedErrorCodes(sc) {
  const t = normalize(sc.title);
  const id = normalize(sc.id);

  // flexible until backend implements exact semantics
  if (isLimit3Scenario(sc)) return [400, 409, 429];
  if (isUniqueScenario(sc)) return [400, 409];
  if (t.includes("priority") && (t.includes("order") || t.includes("before"))) return [400, 409];
  return [400, 422, 429];
}

const className = `${pascal(feature)}GeneratedIT`;

const testMethods = scenarios
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
     * GENERATED
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Source: docs/test-scenarios/${feature}.flow.json
     */
    @Test
    void ${methodName}() {
${happyPayloadJavaMapLines()}

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
     * GENERATED
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Rule: subject unique per day
     */
    @Test
    void ${methodName}() {
${happyPayloadJavaMapLines({ subject: "Password reset not working" })}

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
     * GENERATED
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
     * GENERATED (TODO)
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
     * GENERATED
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     */
    @Test
    void ${methodName}() {
${payloadToJavaMapLines(invalid)}

        ResponseEntity<String> res = postTicket(payload);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getStatusCode().value()).isIn(${codes.join(", ")});
        assertThat(res.getBody()).isNotNull();
    }
`;
  })
  .join("\n");

const testClass = `package be.ap.student.tickets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ${className} {

    @Autowired
    private TestRestTemplate rest;

    private ResponseEntity<String> postTicket(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        return rest.postForEntity("${endpoint}", req, String.class);
    }

${testMethods}
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
console.log(`✅ Backend tests generated: ${outPath}`);