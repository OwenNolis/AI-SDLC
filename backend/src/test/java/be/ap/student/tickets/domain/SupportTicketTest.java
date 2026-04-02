package be.ap.student.tickets.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SupportTicketTest {

    @Test
    void testConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        String ticketNumber = "TICKET-123";
        String subject = "Test Subject";
        String description = "Test Description";
        Priority priority = Priority.MEDIUM;
        TicketStatus status = TicketStatus.PENDING;
        Instant createdAt = Instant.now();

        SupportTicket ticket = new SupportTicket(ticketNumber, id, subject, description, priority, status, createdAt);

        assertThat(ticket.getId()).isEqualTo(id);
        assertThat(ticket.getTicketNumber()).isEqualTo(ticketNumber);
        assertThat(ticket.getSubject()).isEqualTo(subject);
        assertThat(ticket.getDescription()).isEqualTo(description);
        assertThat(ticket.getPriority()).isEqualTo(priority);
        assertThat(ticket.getStatus()).isEqualTo(status.name());
        assertThat(ticket.getCreatedAt()).isEqualTo(createdAt);
        assertThat(ticket.getFormattedStatus()).isEqualTo(status.name());
    }

    @Test
    void testProtectedNoArgsConstructor() throws Exception {
        // This constructor is typically used by JPA. We can test its existence and accessibility via reflection.
        // Direct instantiation is not possible as it's protected.
        // If JPA is used, this constructor will be implicitly covered.
        // For explicit coverage, reflection can be used, but it's often considered low value for simple constructors.
        // Here, we just ensure it exists and is accessible for frameworks.
        SupportTicket ticket = SupportTicket.class.getDeclaredConstructor().newInstance();
        assertThat(ticket).isNotNull();
        // No fields are set by this constructor, so no meaningful assertions on state.
    }
}
