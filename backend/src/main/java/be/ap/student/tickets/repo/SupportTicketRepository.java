package be.ap.student.tickets.repo;


import be.ap.student.tickets.domain.SupportTicket;
import be.ap.student.tickets.domain.TicketStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends CrudRepository<SupportTicket, UUID> {
    long countByStatus(TicketStatus status);
    List<SupportTicket> findAllByPriorityAndStatus(String priority, TicketStatus status);
}
