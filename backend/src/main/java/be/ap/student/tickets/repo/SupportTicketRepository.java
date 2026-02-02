package be.ap.student.tickets.repo;


import be.ap.student.tickets.domain.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
}