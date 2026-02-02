package be.ap.student.tickets.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Year;

@Component
public class TicketNumberGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public TicketNumberGenerator(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, Clock.systemUTC());
    }

    // for tests
    TicketNumberGenerator(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    public String nextTicketNumber() {
        // Demo: uses a DB sequence. In real life: reset per year if required.
        Long seq = jdbcTemplate.queryForObject("select nextval('ticket_seq')", Long.class);
        int year = Year.now(clock).getValue();
        return String.format("TCK-%d-%06d", year, seq);
    }
}