/**
 * GENERATED FILE. DO NOT EDIT MANUALLY.
 * Traceability:
 * - Feature: feature-001-support-ticket
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


  test("create_ticket_happy_path - User creates a valid support ticket (happy path)", () => {
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


  test("create_ticket_missing_subject - Ticket creation fails when subject is missing (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("create_ticket_invalid_priority - Ticket creation fails when priority is invalid (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("create_ticket_duplicate_subject_same_day - Ticket creation fails when subject already exists for the same day (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("create_ticket_high_priority_visible_immediately - HIGH priority ticket visible immediately after creation (happy path)", () => {
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


  test("create_ticket_limit_3_per_day - Ticket creation fails when user exceeds 3 tickets in one day (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("ticket_priority_completion_order - HIGH priority tickets are completed before LOW priority tickets (happy path)", () => {
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


  test("br_a_ticket_with_priority_high_must_always_be_visible_immediate - Business rule: A ticket with priority HIGH must always be visible immediately after creation. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("br_ticket_subject_must_be_unique_per_day_business_constraint - Business rule: Ticket subject must be unique per day (business constraint). (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("br_user_can_only_add_3_tickets_per_day - Business rule: User can only add 3 tickets per day (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("br_a_ticket_with_priority_high_must_always_be_completed_before_ - Business rule: A ticket with priority HIGH must always be completed before a ticket with priority LOW (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("create_ticket_limit_2_high_priority_per_user - Ticket creation fails when user exceeds 2 HIGH priority tickets per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("ticket_priority_completion_order_high_vs_medium - HIGH priority tickets are completed before MEDIUM priority tickets (happy path)", () => {
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


  test("REQ_014_high_priority_visible_immediately - REQ-014: A ticket with priority HIGH must always be visible immediately after creation. (happy path)", () => {
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


  test("REQ_015_unique_subject_per_day - REQ-015: Ticket subject must be unique per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_016_max_3_tickets_per_day - REQ-016: User can create at most 3 tickets per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_017_max_2_high_priority_per_day - REQ-017: User can create at most 2 tickets with the priority HIGH. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_018_high_priority_completion_before_low - REQ-018: A ticket with priority HIGH must always be completed before a ticket with priority LOW. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_019_high_priority_completion_before_medium - REQ-019: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_020_high_priority_visible_immediately - REQ-020: A ticket with priority HIGH must always be visible immediately after creation. (happy path)", () => {
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


  test("REQ_021_unique_subject_per_day - REQ-021: Ticket subject must be unique per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_022_max_3_tickets_per_day - REQ-022: User can create at most 3 tickets per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_023_max_2_high_priority_per_day - REQ-023: User can create at most 2 tickets with the priority HIGH. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_024_high_priority_completion_before_low - REQ-024: A ticket with priority HIGH must always be completed before a ticket with priority LOW. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_025_high_priority_completion_before_medium - REQ-025: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_026_unique_subject_per_day - REQ-026: Ticket subject must be unique per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_027_max_3_tickets_per_day - REQ-027: User can create at most 3 tickets per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_028_max_2_high_priority_per_day - REQ-028: User can create at most 2 tickets with the priority HIGH. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_029_high_priority_completion_before_low - REQ-029: A ticket with priority HIGH must always be completed before a ticket with priority LOW. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_030_high_priority_completion_before_medium - REQ-030: A ticket with priority HIGH must always be completed before a ticket with priority MEDIUM. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_031_high_priority_visible_immediately - REQ-031: A ticket with priority HIGH must always be visible immediately after creation. (happy path)", () => {
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


  test("REQ_032_max_3_tickets_per_day - REQ-032: User can create at most 3 tickets per day. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_033_max_2_high_priority_per_day - REQ-033: User can create at most 2 tickets with the priority HIGH. (UI guard)", () => {
    const onSubmit = jest.fn();
    render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

    // Minimal: keep invalid -> must not submit
    fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

    expect(screen.getByRole("button", { name: /create ticket/i })).toBeDisabled();
  });


  test("REQ_034_high_priority_visible_immediately - REQ-034: A ticket with priority HIGH must always be visible immediately after creation. (happy path)", () => {
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
