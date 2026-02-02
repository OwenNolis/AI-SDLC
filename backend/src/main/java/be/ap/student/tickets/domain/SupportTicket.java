package be.ap.student.tickets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "support_ticket", uniqueConstraints = @UniqueConstraint(name = "uk_ticket_number", columnNames = "ticket_number"))
public class SupportTicket {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, updatable = false, length = 32)
    private String ticketNumber;

    @Column(nullable = false, length = 120)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SupportTicket() {}

    public SupportTicket(UUID id, String ticketNumber, String subject, String description, Priority priority, TicketStatus status, Instant createdAt) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
