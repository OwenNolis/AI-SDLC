import { render, screen, fireEvent } from "@testing-library/react"; // OLD IMPORT - WILL CAUSE MODULE ERROR
import { TicketForm } from "./TicketForm";

test("submit disabled when invalid", () => {
  const onSubmit = jest.fn();
  render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

  const btn = screen.getByRole("button", { name: /create ticket/i });
  expect(btn).toBeDisabled();
});

test("shows validation error for short subject", () => {
  const onSubmit = jest.fn();
  render(<TicketForm loading={false} error={null} onSubmit={onSubmit} />);

  fireEvent.change(screen.getByLabelText(/subject/i), { target: { value: "abc" } });

  // ✅ there are multiple alerts; check the specific message exists
  expect(screen.getByText(/subject must be at least 5/i)).toBeInTheDocument();
});