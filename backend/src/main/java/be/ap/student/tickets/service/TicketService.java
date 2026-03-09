package be.ap.student.tickets.service;

import be.ap.student.tickets.domain.Priority;
import be.ap.student.tickets.domain.SupportTicket;
import be.ap.student.tickets.domain.TicketStatus;
import be.ap.student.tickets.dto.CreateTicketRequest;
import be.ap.student.tickets.repo.SupportTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static be.ap.student.common.web.CorrelationIdFilter.MDC_KEY;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final SupportTicketRepository repository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final be.ap.student.tickets.audit.AuditService auditService;

    public TicketService(SupportTicketRepository repository, TicketNumberGenerator ticketNumberGenerator, be.ap.student.tickets.audit.AuditService auditService) {
        this.repository = repository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.auditService = auditService;
    }

    public java.util.Optional<SupportTicket> create(CreateTicketRequest req) {
        Priority priority;
        try {
            priority = Priority.valueOf(req.getPriority());
        } catch (Exception e) {
            throw new IllegalArgumentException("priority must be one of LOW, MEDIUM, HIGH");
        }

        String ticketNumber = ticketNumberGenerator.nextTicketNumber();
        SupportTicket ticket = new SupportTicket(
                ticketNumber,     // Corrected: String for ticketNumber
                UUID.randomUUID(),// Corrected: UUID for id
                req.getSubject(),
                req.getDescription(),
                priority,
                TicketStatus.OPEN,
                Instant.now()
        );

        SupportTicket saved = repository.save(ticket);
        long openCount = repository.countByStatus(TicketStatus.OPEN);
        log.info("ticket_created ticketNumber={} priority={} correlationId={} openTickets={}",
                saved.getTicketNumber(), saved.getPriority(), MDC.get(MDC_KEY), openCount);
        return java.util.Optional.ofNullable(saved);
    }
}
