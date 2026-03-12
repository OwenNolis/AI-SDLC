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
import java.util.NoSuchElementException;
import java.util.Optional;

import static be.ap.student.common.web.CorrelationIdFilter.MDC_KEY;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final SupportTicketRepository repository;
    private final TicketNumberGenerator ticketNumberGenerator;

    public TicketService(SupportTicketRepository repository, TicketNumberGenerator ticketNumberGenerator) {
        this.repository = repository;
        this.ticketNumberGenerator = ticketNumberGenerator;
    }

    public Optional<SupportTicket> create(CreateTicketRequest req) {
        Priority priority;
        try {
            priority = Priority.valueOf(req.getPriority());
        } catch (Exception e) {
            throw new IllegalArgumentException("priority must be one of LOW, MEDIUM, HIGH");
        }

        String ticketNumber = ticketNumberGenerator.nextTicketNumber();
        SupportTicket ticket = new SupportTicket(
                ticketNumber,
                req.getUserId(), // Assuming getUserId() now returns UUID based on compilation error analysis
                req.getSubject(),
                req.getDescription(),
                priority,
                TicketStatus.PENDING,
                Instant.now()
        );

        SupportTicket saved = repository.save(ticket);
        long openCount = repository.countByStatus(TicketStatus.PENDING);
        log.info("ticket_created ticketNumber={} priority={} correlationId={} openTickets={}",
                saved.getTicketNumber(), saved.getPriority(), MDC.get(MDC_KEY), openCount);
        return Optional.ofNullable(saved);
    }

    public SupportTicket findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket with id " + id + " not found"));
    }
}
