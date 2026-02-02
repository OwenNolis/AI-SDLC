import { useMemo, useState } from "react";
import type { ApiError } from "../api/http";
import type { CreateTicketRequest } from "../api/ticket";

export type TicketFormValues = CreateTicketRequest;

type Props = {
  loading: boolean;
  error: ApiError | null;
  onSubmit: (values: TicketFormValues) => void | Promise<void>;
};

function validate(values: TicketFormValues) {
  const errors: Partial<Record<keyof TicketFormValues, string>> = {};
  const subject = values.subject.trim();
  const description = values.description.trim();

  if (subject.length < 5) errors.subject = "Subject must be at least 5 characters";
  if (subject.length > 120) errors.subject = "Subject must be at most 120 characters";

  if (description.length < 20) errors.description = "Description must be at least 20 characters";
  if (description.length > 2000) errors.description = "Description must be at most 2000 characters";

  if (!values.priority) errors.priority = "Priority is required";

  return errors;
}

export function TicketForm({ loading, error, onSubmit }: Props) {
  const [values, setValues] = useState<TicketFormValues>({
    subject: "",
    description: "",
    priority: "MEDIUM",
  });

  const clientErrors = useMemo(() => validate(values), [values]);
  const canSubmit = Object.keys(clientErrors).length === 0 && !loading;

  const serverFieldErrors = new Map(
    (error?.fieldErrors ?? []).map((fe) => [fe.field, fe.message])
  );

  const fieldError = (name: keyof TicketFormValues) =>
    clientErrors[name] ?? serverFieldErrors.get(name as string) ?? null;

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        if (canSubmit) onSubmit(values);
      }}
      aria-label="ticket-form"
    >
      <div>
        <label htmlFor="subject">Subject</label>
        <br />
        <input
          id="subject"
          value={values.subject}
          onChange={(e) => setValues({ ...values, subject: e.target.value })}
          disabled={loading}
        />
        {fieldError("subject") && <div role="alert">{fieldError("subject")}</div>}
      </div>

      <div>
        <label htmlFor="description">Description</label>
        <br />
        <textarea
          id="description"
          value={values.description}
          onChange={(e) => setValues({ ...values, description: e.target.value })}
          disabled={loading}
        />
        {fieldError("description") && <div role="alert">{fieldError("description")}</div>}
      </div>

      <div>
        <label htmlFor="priority">Priority</label>
        <br />
        <select
          id="priority"
          value={values.priority}
          onChange={(e) =>
            setValues({ ...values, priority: e.target.value as TicketFormValues["priority"] })
          }
          disabled={loading}
        >
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
        {fieldError("priority") && <div role="alert">{fieldError("priority")}</div>}
      </div>

      {error && (!error.fieldErrors || error.fieldErrors.length === 0) && (
        <div role="alert">
          Something went wrong. Correlation id: <b>{error.correlationId ?? "n/a"}</b>
        </div>
      )}

      <button type="submit" disabled={!canSubmit}>
        {loading ? "Creating..." : "Create ticket"}
      </button>
    </form>
  );
}