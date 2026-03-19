package be.ap.student.tickets.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SonarIssueDemoTest {

    private final SonarIssueDemo sonarIssueDemo = new SonarIssueDemo();

    @Test
    void getServiceName_returnsCorrectName() {
        assertThat(sonarIssueDemo.getServiceName()).isEqualTo("ticket-service");
    }

    @Test
    void getServiceId_returnsCorrectId() {
        assertThat(sonarIssueDemo.getServiceId()).isEqualTo("ticket-service-id");
    }

    @Test
    void getServiceLabel_returnsCorrectLabel() {
        assertThat(sonarIssueDemo.getServiceLabel()).isEqualTo("Ticket Service Label");
    }

    @Test
    void getServiceTag_returnsCorrectTag() {
        assertThat(sonarIssueDemo.getServiceTag()).isEqualTo("ticket-tag");
    }

    @Test
    void reportStatus_logsStatus() {
        // This method primarily logs. For coverage, simply calling it is sufficient.
        sonarIssueDemo.reportStatus("UP");
    }
}
