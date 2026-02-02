import { render, screen, fireEvent } from "@testing-library/react";
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
  expect(screen.getByRole("alert")).toHaveTextContent("at least 5");
});