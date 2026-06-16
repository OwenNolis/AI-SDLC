import { useState } from "react";
import { createTicket } from "../api/ticket";
import type { CreateTicketResponse } from "../api/ticket";
import { HttpError } from "../api/http";
import type { ApiError } from "../api/http";
import { TicketForm } from "../ui/TicketForm";
import type { TicketFormValues } from "../ui/TicketForm";

export function TicketCreatePage() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<CreateTicketResponse | null>(null);
  const [error, setError] = useState<ApiError | null>(null);

  async function onSubmit(values: TicketFormValues) {
    setLoading(true);
    setError(null);
    try {
      const res = await createTicket(values);
      setSuccess(res);
    } catch (e: unknown) {
      if (e instanceof HttpError) {
        setError(e);
      } else if (e instanceof Error) {
        setError({
          correlationId: "unknown",
          code: "CLIENT_ERROR",
          message: e.message,
        });
      } else {
        setError({
          correlationId: "unknown",
          code: "CLIENT_ERROR",
          message: String(e),
        });
      }
    } finally {
      setLoading(false);
    }
  }

  if (success) {
    return (
      <div>
        <h1>Ticket created</h1>
        <p>
          Ticket: <b>{success.ticketNumber}</b>
        </p>
        <p>
          Status: <b>{success.status}</b>
        </p>
      </div>
    );
  }

  return (
    <div>
      <h1>Create support ticket</h1>
      <TicketForm loading={loading} error={error} onSubmit={onSubmit} />
    </div>
  );
}
