package be.ap.student.tickets.dto;

import be.ap.student.tickets.domain.TicketStatus;

public record CreateTicketResponse(String ticketNumber, TicketStatus status) {}