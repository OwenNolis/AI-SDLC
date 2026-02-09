/**
 * GENERATED FILE. DO NOT EDIT MANUALLY.
 * Traceability:
 * - Feature: feature-001-support-ticket
 * - Scenario: create_ticket_happy_path - User creates a valid support ticket
 * - Source: docs/test-scenarios/feature-001-support-ticket.flow.json
 */

import { render, screen, fireEvent } from "@testing-library/react";
import { TicketForm } from "../TicketForm";

describe("feature-001-support-ticket - generated UI tests", () => {
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
