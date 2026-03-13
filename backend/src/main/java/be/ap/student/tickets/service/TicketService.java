package be.ap.student.tickets.service;

import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.dto.TicketResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketService {

    public TicketResponse createTicket(CreateTicketRequest request) {
        // Simulate ticket creation and return a response.
        // The log message from the error output suggests this part is reached.
        System.out.println("ticket_created ticketNumber=TCK-2026-000001 priority=" + request.getPriority() + " correlationId=" + UUID.randomUUID() + " openTickets=1");

        TicketResponse response = new TicketResponse();
        response.setTicketNumber("TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        response.setSubject(request.getSubject());
        response.setDescription(request.getDescription());
        response.setPriority(request.getPriority());
        response.setUserId(request.getUserId());
        response.setStatus("OPEN"); // Default status for a new ticket
        return response;
    }
}
