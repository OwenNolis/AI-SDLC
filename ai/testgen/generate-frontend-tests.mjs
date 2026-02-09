import path from "node:path";
import { readJson, writeText, safeId } from "./utils.mjs";

const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/testgen/generate-frontend-tests.mjs <feature-id>");
  process.exit(1);
}

const flowPath = path.join("docs", "test-scenarios", `${feature}.flow.json`);
const flow = await readJson(flowPath);

const scenarios =
  Array.isArray(flow.scenarios) && flow.scenarios.length > 0
    ? flow.scenarios
    : [{ id: "scenario_1", title: "happy path", type: "happy-path" }];

const scenarioTests = scenarios
  .map((sc, idx) => {
    const sid = safeId(sc.id ?? `scenario_${idx + 1}`);
    const title = String(sc.title ?? "");
    const type = String(sc.type ?? "");

    // For now: “validation/negative” scenarios will assert submit stays disabled
    if (type === "validation" || type === "negative") {
      return `
  test("${sid} - ${title} (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });
`;
    }

    // Happy path
    return `
  test("${sid} - ${title} (happy path)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/subject/i), {
      target: { value: "Cannot login to portal" },
    });

    fireEvent.change(screen.getByLabelText(/description/i), {
      target: { value: "I cannot login since yesterday. Please investigate." },
    });

    fireEvent.change(screen.getByLabelText(/priority/i), {
      target: { value: "HIGH" },
    });

    const btn = screen.getByRole("button", { name: /create ticket/i });
    expect(btn).toBeEnabled();

    fireEvent.click(btn);
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
`;
  })
  .join("\n");

const content = `/**
 * GENERATED FILE. DO NOT EDIT MANUALLY.
 * Traceability:
 * - Feature: ${feature}
 * - Source: docs/test-scenarios/${feature}.flow.json
 */

import { render, screen, fireEvent } from "@testing-library/react";
import { TicketForm } from "../TicketForm";

describe("${feature} - generated UI tests", () => {
  test("submit disabled when form invalid", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });

${scenarioTests}

  test("shows validation error for short subject", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });
    expect(screen.getAllByRole("alert").map(a => a.textContent).join(" ")).toMatch(/at least 5/i);
  });
});
`;

const outPath = path.join(
  "frontend",
  "src",
  "ui",
  "__generated__",
  `${feature}.TicketForm.test.tsx`
);

await writeText(outPath, content);
console.log(`✅ Frontend tests generated: ${outPath}`);