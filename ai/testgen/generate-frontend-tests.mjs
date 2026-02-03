import path from "node:path";
import { readJson, writeText } from "./utils.mjs";

const feature = process.argv[2];
if (!feature) {
  console.error("Usage: node ai/testgen/generate-frontend-tests.mjs <feature-id>");
  process.exit(1);
}

const flowPath = path.join("docs", "test-scenarios", `${feature}.flow.json`);
const flow = await readJson(flowPath);

const scenario = flow.scenarios?.[0] ?? { id: "scenario_1", title: "happy path" };

const content = `/**
 * GENERATED FILE. DO NOT EDIT MANUALLY.
 * Traceability:
 * - Feature: ${feature}
 * - Scenario: ${scenario.id ?? "n/a"} - ${String(scenario.title ?? "")}
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

  test("enables submit when valid and calls onSubmit", () => {
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

  test("shows validation error for short subject", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });
    expect(screen.getAllByRole("alert").map(a => a.textContent).join(" ")).toMatch(/at least 5/i);
  });
});
`;

const outPath = path.join("frontend", "src", "ui", "__generated__", `${feature}.TicketForm.test.tsx`);
await writeText(outPath, content);
console.log(`✅ Frontend tests generated: ${outPath}`);