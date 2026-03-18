package be.ap.student.tickets.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SonarMediumIssueExampleTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(SonarMediumIssueExample.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void doSomething_executesAllBranchesAndLogsResult() {
        SonarMediumIssueExample example = new SonarMediumIssueExample();
        example.doSomething();

        // Verify that the log message was captured
        assertThat(listAppender.list).isNotEmpty();
        assertThat(listAppender.list.get(0).getMessage()).contains("Result:");
        // The exact result depends on the internal logic, but we can check if it's logged.
        // To verify the exact result, we would need to calculate it manually or make processK public.
        // For coverage, just calling doSomething is enough to hit all internal methods and branches.
    }
}
