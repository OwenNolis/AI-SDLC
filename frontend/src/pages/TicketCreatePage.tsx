import { useState } from "react";
import { createTicket } from "../api/ticket";
import type { CreateTicketResponse } from "../api/ticket";
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
    } catch (e) {
      setError(e as ApiError);
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