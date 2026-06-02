/**
 * GENERATED FILE. DO NOT EDIT MANUALLY.
 * Traceability:
 * - Feature: feature-011-preworkout-website
 * - Source: docs/test-scenarios/feature-011-preworkout-website.flow.json
 */

import { render, screen } from "@testing-library/react";
import { fireEvent } from "@testing-library/dom";
import { TicketForm } from "../TicketForm";

describe("feature-011-preworkout-website - generated UI tests", () => {
  test("submit disabled when form invalid", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("scenario_1 - happy path (happy path)", () => {
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
    expect(screen.getAllByRole("alert").map((a: HTMLElement) => a.textContent).join(" ")).toMatch(/at least 5/i);
  });
});
