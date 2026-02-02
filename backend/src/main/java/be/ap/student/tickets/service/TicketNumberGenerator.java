package be.ap.student.tickets.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class TicketNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    public TicketNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextTicketNumber() {
        Long seq = jdbcTemplate.queryForObject("select nextval('ticket_seq')", Long.class);
        int year = Year.now().getValue();
        return String.format("TCK-%d-%06d", year, seq);
    }
}