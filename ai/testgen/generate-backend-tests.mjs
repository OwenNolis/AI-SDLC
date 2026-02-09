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
await readJson(taPath); // kept for future TA-driven generation

// Default for your demo feature:
const endpoint = "/api/tickets";

// Choose scenarios from flow.json (fallback to single scenario)
const scenarios =
  Array.isArray(flow.scenarios) && flow.scenarios.length > 0
    ? flow.scenarios
    : [{ id: "scenario_1", title: "happy path", type: "happy-path" }];

const className = `${pascal(feature)}GeneratedIT`;

const testMethods = scenarios
  .map((sc, idx) => {
    const id = safeId(sc.id ?? `scenario_${idx + 1}`);
    const type = String(sc.type ?? "").toLowerCase();
    const isNegative = type === "validation" || type === "negative";
    const methodName = `${camel(id)}_${isNegative ? "returns400" : "returns201"}_andCorrelationId`;

    const title = String(sc.title ?? "").replace(/\*\//g, "*\\/");

    const payload = isNegative
      ? `{
          "subject": "abc",
          "description": "short",
          "priority": "HIGH"
        }`
      : `{
          "subject": "Cannot login to portal",
          "description": "I cannot login since yesterday. Please investigate.",
          "priority": "HIGH"
        }`;

    const expectedStatus = isNegative ? "HttpStatus.BAD_REQUEST" : "HttpStatus.CREATED";
    const bodyAssert = isNegative
      ? `assertThat(res.getBody()).contains("fieldErrors");`
      : `assertThat(res.getBody()).contains("ticketNumber");`;

    return `
    /**
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: ${sc.id ?? "n/a"} - ${title}
     * - Scenario type: ${type || "n/a"}
     * - Source: docs/test-scenarios/${feature}.flow.json
     */
    @Test
    void ${methodName}() {
        String json = """
        ${payload}
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "${endpoint}",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(${expectedStatus});
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        ${bodyAssert}
    }
`;
  })
  .join("\n");

const testClass = `package be.ap.student.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ${className} {

    @Autowired
    private TestRestTemplate rest;

${testMethods}

    /**
     * Traceability:
     * - Feature: ${feature}
     * - Scenario: validation (derived from TA constraints)
     */
    @Test
    void validation_invalidPayload_returns400_withFieldErrors() {
        String json = """
        {
          "subject": "abc",
          "description": "short",
          "priority": "HIGH"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        ResponseEntity<String> res = rest.postForEntity(
            "${endpoint}",
            new HttpEntity<>(json, headers),
            String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(res.getBody()).contains("fieldErrors");
    }
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