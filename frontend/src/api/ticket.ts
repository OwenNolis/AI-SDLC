import { postJson } from "./http";

export type CreateTicketRequest = {
  subject: string;
  description: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
};

export type CreateTicketResponse = {
  ticketNumber: string;
  status: string;
};

export function createTicket(req: CreateTicketRequest) {
  return postJson<CreateTicketRequest, CreateTicketResponse>("/api/tickets", req);
}